package dev.dcrk

import kotlin.js.Promise
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private const val COMMENTS_REUSE = 10_000

private class PreviewController(private val ctx: dynamic, private val s: dynamic) {
    private val window: dynamic = js("window")
    private val doc: dynamic = js("document")
    private val console: dynamic = js("console")
    private val history = window.history
    private val store = s.store
    private val ui = s.uiStore.getState()
    private var abort: dynamic = null
    private var refreshTimer = 0
    private var blocking = false
    private val posts = s.createPostLoader(ctx)
    private val pull = s.createCommentsPull(ctx, { abort?.signal })
    private val nav = s.createPreviewHistory(ctx)

    private fun errorOf(error: Throwable): dynamic {
        val original = error.asDynamic()
        return props("detail" to s.messageOf(original),
            "status" to if (s.isHttpError(original) == true) original.response.status else undefinedValue(),
            "adult" to (original.message == s.adultError), "secret" to (original.message == s.secretError))
    }

    private fun recentComments(pre: dynamic): dynamic {
        val entry = s.getEntry(pre)
        val comments = entry?.comments
        if (comments == null || (js("Date.now()") as Double) - (entry.commentsAt as Double? ?: 0.0) >= COMMENTS_REUSE) return null
        return if (!truthy(comments.truncated) && (pre.commentCount as Int) > (comments.list.length as Int)) null else comments
    }

    private fun sameSignal(id: dynamic): Boolean = store.getState().signalId === id

    private fun reload(): Promise<dynamic> = jsPromise reload@{
        val state = store.getState()
        val pre = state.preData
        if (!truthy(state.visible) || pre == null || abort == null) return@reload undefinedValue()
        try {
            val incoming = awaitJs(posts.requestPost(pre))
            val post = awaitJs(posts.processContents(pre, incoming))
            if (!sameSignal(state.signalId)) return@reload undefinedValue()
            store.setState(props("post" to post, "error" to undefinedValue(), "archived" to false))
        } catch (failure: Throwable) {
            if (s.isAbortError(failure) != true && sameSignal(state.signalId)) {
                console.error("Preview reload failed:", failure)
                ui.showToast("게시글을 다시 불러오지 못했습니다.", "error")
            }
            return@reload undefinedValue()
        }
        awaitJs(pull.refreshComments(true))
        undefinedValue()
    }

    private suspend fun load(pre: dynamic, signal: dynamic, dir: Int) {
        val esno = doc.querySelector("#e_s_n_o")?.value
        val recent = if (ctx.settings.disableCache == true) null else recentComments(pre)
        val early = if (dir == 0 && recent == null && (pre.commentCount as Int) > 0 && truthy(esno) &&
            (ctx.settings.disableCache == true || !truthy(s.cachedPost(pre)))
        ) s.fetchComments(pre, props("esno" to esno), abort.signal).catch { _: dynamic -> undefinedValue() } else null

        val page: dynamic
        val post: dynamic
        try {
            page = awaitJs(posts.getPost(pre))
            post = awaitJs(posts.processContents(pre, page.post))
        } catch (failure: Throwable) {
            if (sameSignal(signal)) {
                console.error("Preview load failed:", failure)
                store.setState(props("error" to errorOf(failure)))
            }
            return
        }
        if (!sameSignal(signal)) return
        store.setState(props("post" to post, "archived" to (page.archived ?: false)))

        try {
            val last = recent ?: if (page.fresh == true) null else s.getEntry(pre)?.comments
            if (last != null) {
                Promise<dynamic> { resolve, _ ->
                    window.requestAnimationFrame { _: dynamic -> window.setTimeout({ resolve(undefinedValue()) }) }
                }.awaitResult()
                awaitJs(pull.pullComments(pre, post, signal, false, last, true))
            }
            if (recent == null) {
                val matches = post.esno === esno && (post.commentId ?: pre.gallery) === pre.gallery && (post.commentNo ?: pre.id) === pre.id
                val given = early?.then { response: dynamic ->
                    if (matches && truthy(response?.list?.length)) response else undefinedValue()
                }
                val seen = s.getEntry(pre)?.seen ?: props()
                val skip = page.fresh == true && post.commentCount == 0 && ownKeys(seen).isEmpty() && pull.hasAllowReply(pre.gallery) == true
                awaitJs(pull.pullComments(pre, post, signal, skip, given ?: undefinedValue()))
            }
        } catch (failure: Throwable) {
            if (sameSignal(signal) && s.isAbortError(failure) != true) {
                store.setState(props("commentsFailed" to true))
                console.error("Comment load failed:", failure)
                if (kotlin.js.jsTypeOf(store.getState().comments) != "undefined") ui.showToast("댓글을 불러오지 못했습니다.", "error")
            }
        }

        if (dir != 0 && ctx.settings.disableCache != true && sameSignal(signal)) {
            val next = s.adjacentPreData(pre, dir)
            if (next != null && posts.hasPending() != true && !truthy(s.cachedPost(next))) posts.requestPost(next)
        }
    }

    private fun close(fromHistory: Boolean = false) {
        abort?.abort()
        abort = null
        posts.abortPending()
        window.clearTimeout(refreshTimer)
        pull.forgetShown()
        nav.restore(fromHistory)
        store.getState().close()
    }

    private fun dismiss(fromHistory: Boolean = false) {
        close(fromHistory)
        s.refresh()?.catchUp()
    }

    private fun open(pre: dynamic, commentsOnly: Boolean = false, skipHistory: Boolean = false, dir: Int = 0) {
        val old = store.getState()
        var historySkip = skipHistory
        if (old.visible == true && old.preData != null && previewPostKey(old.preData) == previewPostKey(pre)) {
            if (!truthy(old.error)) {
                store.setState(props("commentsOnly" to commentsOnly))
                return
            }
            historySkip = true
        }
        abort?.abort()
        abort = js("new AbortController()")
        window.clearTimeout(refreshTimer)

        store.getState().open(pre, props("commentsOnly" to commentsOnly,
            "imageBlocked" to (ctx.settings.blockImage == true && s.isTextPost(pre) == true)))
        val signal = store.getState().signalId
        if (!historySkip && old.visible != true) nav.remember(props("title" to doc.title, "url" to window.location.href, "state" to history.state))
        if (ctx.settings.colorPreviewLink == true) {
            val gallery = (doc.querySelector(".page_head h2 a")?.firstChild?.textContent as String?)?.trim().orEmpty().ifEmpty { "디시인사이드" }
            val title = "${pre.title ?: doc.title} - $gallery"
            if (!historySkip) {
                val depth = (if (old.visible == true) s.ownPreviewDepth() as Int else 0) + 1
                val state = props("refresher" to 1, "doc" to s.historyDoc, "preData" to pre, "back" to (nav.back() ?: undefinedValue()), "depth" to min(depth, 40))
                try {
                    if (depth > 40) history.replaceState(state, title, pre.link)
                    else history.pushState(state, title, pre.link)
                } catch (failure: Throwable) {
                    console.error("미리보기 기록 쌓기 실패:", failure)
                }
            }
            doc.title = title
        }

        var failures = 0
        fun scheduleRefresh() {
            val interval = ctx.settings.commentRefreshInterval as Double
            val refresh: () -> dynamic = { jsPromise {
                if (ctx.settings.autoRefreshComment == true && doc.hidden != true && pull.isPulling() != true) {
                    failures = if (truthy(awaitJs(pull.refreshComments()))) 0 else failures + 1
                }
                if (sameSignal(signal)) scheduleRefresh()
                undefinedValue()
            } }
            refreshTimer = window.setTimeout(refresh, min(interval * 2.0.pow(failures), max(interval, 60_000.0))) as Int
        }
        scheduleRefresh()
        jsPromise { load(pre, signal, dir) }
    }

    private fun block(target: dynamic, options: dynamic, comments: dynamic): Promise<dynamic> = jsPromise block@{
        if (blocking) return@block false
        blocking = true
        val signal = store.getState().signalId
        try {
            val request = if (comments == null) s.blockUser(target, options) else s.blockCommenters(target, comments, options)
            val blocked = awaitJs(s.notifyManage(request, "차단했습니다.", "차단하지 못했습니다. 잠시 후 다시 시도해 주세요."))
            if (truthy(blocked) && sameSignal(signal)) {
                if (comments != null) {
                    store.setState(props("selectedComments" to s.noFresh))
                    pull.refreshComments()
                } else if (options.delChk == true) close()
            }
            s.refresh()?.reload()
            blocked
        } finally {
            blocking = false
        }
    }

    fun install(): dynamic {
        val onPop: (dynamic) -> Unit = { event ->
            val state = s.ownPreviewEntry(event.state)
            if (state?.preData != null) {
                nav.setBack(state.back ?: null)
                open(state.preData, skipHistory = true)
            } else if (store.getState().visible == true) dismiss(true)
        }
        s.bindListKeys(ctx, { pre: dynamic -> open(pre) })
        val prefetch: (dynamic) -> Unit = { pre ->
            if (ctx.settings.disableCache != true && !truthy(s.cachedPost(pre))) posts.requestPost(pre)
        }
        val openRow: (dynamic, Boolean?) -> Unit = { pre, comments -> open(pre, comments == true) }
        s.bindRows(ctx, props("open" to openRow, "prefetch" to prefetch))
        val events = props("signal" to ctx.signal)
        window.addEventListener("popstate", onPop, events)
        nav.reopenFromEntry { pre: dynamic -> open(pre, skipHistory = true) }
        ctx.addCleanup {
            close()
            store.setState(s.noHooks)
        }
        val start: (dynamic, Boolean?, Int?) -> Unit = { pre, comments, direction -> open(pre, comments == true, dir = direction ?: 0) }
        val refresh: (Boolean?) -> dynamic = { report -> jsPromise {
            awaitJs(pull.refreshComments(report ?: undefinedValue()))
            undefinedValue()
        } }
        val closeNow: () -> Unit = { dismiss() }
        store.setState(props("requestOpen" to start, "requestClose" to closeNow, "requestRefresh" to refresh,
            "requestReload" to ::reload, "requestBlock" to ::block))
        return props("archiveArticle" to { ctx.settings.archiveArticle }, "isOpen" to { store.getState().visible })
    }
}

fun setupPreview(ctx: dynamic, services: dynamic): dynamic = PreviewController(ctx, services).install()
