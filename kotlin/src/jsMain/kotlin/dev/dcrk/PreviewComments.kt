package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise

internal fun createCommentProcessing(s: dynamic): dynamic {
    val cleaned = s.createCache()
    val plain = s.createCache()
    val dccon = Regex("""dcimg5\.dcinside\.com/dccon\.php\?no=(\w*)""")

    fun remember(cache: dynamic, key: String, compute: () -> String): String {
        val stored = cache.get(key)
        if (stored != null) return stored as String
        return compute().also { cache.set(key, it) }
    }

    fun prepare(raw: Array<dynamic>, pre: dynamic, archive: Boolean, truncated: dynamic): dynamic {
        val filtered = raw.filter { it.nicktype != "COMMENT_BOY" }.map { comment ->
            val deleted = comment.is_delete != "0" || comment.del_yn == "Y"
            js("Object.assign")(props(), comment, props("is_delete" to if (deleted) "1" else "0"))
        }.toTypedArray()
        return if (archive) s.restoreArchive(pre, filtered, truncated == true) else filtered
    }

    fun process(source: Array<dynamic>, pre: dynamic, block: dynamic, ratios: dynamic): Promise<dynamic> = jsPromise {
        val assign: dynamic = js("Object.assign")
        val list = source.map { comment -> assign(props(), comment, props("memo" to (comment.memo ?: ""))) }.toTypedArray()
        val chunked = list.size > 200
        val scheduler: dynamic = js("globalThis.scheduler")
        for ((index, comment) in list.withIndex()) {
            val voiced = extractVoice(comment.memo as String)
            comment.voice = voiced?.voice ?: undefinedValue()
            val memo = (voiced?.memo ?: comment.memo) as String
            comment.memo = remember(cleaned, memo) { s.sanitizeHtml(preprocessCommentMemo(memo)) as String }
            if (chunked && index % 50 == 49) awaitJs(scheduler.yield())
        }

        val texts: dynamic = js("new Map()")
        if (block != null) {
            for ((index, comment) in list.withIndex()) {
                val memo = comment.memo as String
                val text = remember(plain, memo) { (s.htmlToText(memo) as String).trim() }
                texts.set(comment, text)
                val values = props("NICK" to (comment.name ?: null), "ID" to (comment.user_id ?: null),
                    "IP" to (comment.ip ?: null), "COMMENT" to text)
                val blocked = s.isAnyBlocked(values, pre.gallery) == true ||
                    dccon.findAll(memo).any { s.isBlocked("DCCON", it.groupValues[1], pre.gallery) == true }
                if (blocked) comment.blocked = s.blockMode(block)
                if (chunked && index % 50 == 49) awaitJs(scheduler.yield())
            }
            if (block.replyRemove == true) {
                val threads = list.filter { it.depth == 0 && truthy(it.blocked) }.map { it.no as String }.toSet()
                for (comment in list) {
                    if (comment.depth == 1 && comment.c_no in threads && comment.blocked == null) comment.blocked = s.blockMode(block)
                }
            }
        }

        for (comment in list) {
            val action = if (truthy(comment.blocked)) null else s.lowActivityOf(ratios ?: null, comment.user_id)
            if (truthy(action)) {
                comment.blocked = action
                comment.lowActivity = true
            }
        }

        val duplicate = if (block != null) s.duplicateOf(block) else null
        if (duplicate != null) {
            val candidates = list.filter { !truthy(it.blocked) && it.is_delete != "1" }.toTypedArray()
            val textOf: (dynamic) -> String = { (texts.get(it) ?: "") as String }
            for (entry in jsArray(s.groupDuplicates(candidates, textOf, duplicate))) entry[0].duplicates = entry[1]
        }
        list
    }

    return props("prepareComments" to ::prepare, "processComments" to ::process)
}

private class CommentsPull(private val ctx: dynamic, private val signal: () -> dynamic, private val s: dynamic) {
    private val store = s.store
    private val ui = s.useUiStore.getState()
    private val window: dynamic = js("window")
    private val console: dynamic = js("console")
    private val allowReplies = mutableMapOf<String, Boolean>()
    private var commentSeq = 0
    private var shownSeq = 0
    private var pulling = 0
    private var shown: dynamic = null
    private var freshTimer: dynamic = 0
    private var reapplyTimer: dynamic = 0
    private var blockKey = blockKey()

    private fun blockKey(): String {
        val block = s.runningModuleSettings("block") ?: return ""
        return js("JSON.stringify")(arrayOf<dynamic>(block.blur, block.replyRemove, s.duplicateOf(block))) as String
    }

    private fun stale(id: dynamic, seq: Int): Boolean = store.getState().signalId !== id || seq < shownSeq

    private suspend fun process(source: dynamic, pre: dynamic): Array<dynamic> {
        val old = mutableMapOf<String, dynamic>()
        val current = store.getState().comments
        if (current != null) for (comment in current as Array<dynamic>) old[comment.no as String] = comment
        val processed = awaitJs(s.processComments(source, pre, s.runningModuleSettings("block"), s.useUiStore.getState().ratios)) as Array<dynamic>
        return processed.map { comment ->
            val previous = old[comment.no as String]
            if (previous != null && s.shallow(previous, comment) == true) previous else comment
        }.toTypedArray()
    }

    private fun dropStaleReply() {
        val state = store.getState()
        val reply = state.reply.replyNo
        if (truthy(reply) && (state.comments as Array<dynamic>?)?.any { it.no == reply && it.is_delete != "1" } != true) {
            store.setState(props("reply" to s.noReply))
        }
    }

    private fun pull(pre: dynamic, post: dynamic, id: dynamic, skipArg: dynamic, given: dynamic, cached: dynamic): Promise<dynamic> = jsPromise {
        val skip = skipArg == true
        val requestSignal = signal() ?: js("AbortSignal.abort()")
        val seq = ++commentSeq
        pulling++
        try {
            val response = if (skip) props("list" to emptyArray<dynamic>(),
                "allowReply" to (allowReplies[pre.gallery as String] ?: store.getState().allowReply), "truncated" to false)
            else awaitJs(given) ?: awaitJs(s.fetchComments(pre, post, requestSignal))
            if (stale(id, seq)) return@jsPromise undefinedValue()
            shownSeq = seq
            val raw = response.list
            val allow = response.allowReply
            if (!skip) {
                val patch = props("comments" to props("list" to raw, "allowReply" to allow, "truncated" to response.truncated))
                if (cached != true) patch.commentsAt = Date.now()
                s.setEntry(pre, patch)
                allowReplies[pre.gallery as String] = allow as Boolean
            }

            val source = s.prepareComments(raw, pre, ctx.settings.archiveArticle, response.truncated) as Array<dynamic>
            val samePost = shown != null && shown.signal === id
            if (!skip && samePost && s.sameComments(raw, shown.raw) == true) {
                if (store.getState().allowReply != allow) store.setState(props("allowReply" to allow))
                return@jsPromise undefinedValue()
            }
            val added = if (samePost && ctx.settings.highlightNewComments == true) {
                val before = (shown.source as Array<dynamic>).map { it.no as String }.toHashSet()
                source.filter { it.no !in before && it.is_delete != "1" }.map { it.no as String }.toTypedArray()
            } else emptyArray<String>()
            shown = props("signal" to id, "source" to source, "raw" to raw)
            val comments = process(source, pre)
            if (stale(id, seq)) return@jsPromise undefinedValue()
            store.setState(props("comments" to comments, "allowReply" to allow,
                "freshComments" to if (added.isEmpty()) s.noFresh else js("new Set(added)")))
            window.clearTimeout(freshTimer)
            if (added.isNotEmpty()) {
                val clear: () -> Unit = { if (store.getState().signalId === id) store.setState(props("freshComments" to s.noFresh)) }
                freshTimer = window.setTimeout(clear, 3000)
            }
            dropStaleReply()
        } finally {
            pulling--
        }
        undefinedValue()
    }

    private fun reapply(): Promise<dynamic> = jsPromise {
        val state = store.getState()
        val pre = state.preData
        val id = state.signalId
        if (state.visible != true || pre == null) return@jsPromise undefinedValue()
        val source = if (shown != null && shown.signal === id) shown.source else null
        val seq = commentSeq
        val comments = if (source != null) process(source, pre) else null
        if (comments != null && (seq != commentSeq || store.getState().signalId !== id)) return@jsPromise undefinedValue()
        val assign: dynamic = js("Object.assign")
        val update: (dynamic) -> dynamic = { now ->
            if (now.signalId !== id) now
            else props("post" to if (now.post == null) now.post
                else assign(props(), now.post, props("textBlocked" to s.textBlockOf(pre, now.post))),
                "comments" to (comments ?: now.comments))
        }
        store.setState(update)
        dropStaleReply()
        undefinedValue()
    }

    private fun schedule() {
        if (truthy(reapplyTimer)) return
        val tick: () -> Unit = {
            reapplyTimer = 0
            reapply()
        }
        reapplyTimer = window.setTimeout(tick, 100)
    }

    private fun refresh(reportArg: dynamic): Promise<Boolean> = jsPromise {
        val report = reportArg == true
        val st = store.getState()
        val currentSignal = signal()
        if (st.visible != true || st.preData == null || st.post == null || currentSignal == null) return@jsPromise true
        if (st.commentsFailed == true) store.setState(props("commentsFailed" to false))
        try {
            if (!report && shown != null) {
                val number: dynamic = js("Number")
                val newest = (shown.source as Array<dynamic>).fold(0.0) { max, comment ->
                    val no = number(comment.no) as Double
                    kotlin.math.max(max, if (no.isNaN()) 0.0 else no)
                }
                val fresh = awaitJs(s.fetchComments(st.preData, st.post, currentSignal, newest.asDynamic().toString()))
                if (fresh != null) awaitJs(pull(st.preData, st.post, st.signalId, false, fresh, false))
            } else {
                awaitJs(pull(st.preData, st.post, st.signalId, false, undefinedValue(), false))
            }
            true
        } catch (error: Throwable) {
            if (s.isAbortError(error) == true) return@jsPromise true
            if (store.getState().signalId === st.signalId) store.setState(props("commentsFailed" to true))
            console.error("Comment refresh failed:", error)
            if (report) ui.showToast("댓글을 불러오지 못했습니다.", "error")
            false
        }
    }

    fun bind(): dynamic {
        val clear: () -> Unit = { window.clearTimeout(freshTimer); window.clearTimeout(reapplyTimer) }
        ctx.addCleanup(clear)
        val onLists: (dynamic, dynamic) -> Unit = { state, previous ->
            if (state.entries !== previous.entries || state.defaults !== previous.defaults) schedule()
        }
        ctx.addCleanup(s.useBlocksStore.subscribe(onLists))
        val onModule: () -> Unit = {
            val key = blockKey()
            if (key != blockKey) {
                blockKey = key
                schedule()
            }
        }
        ctx.addCleanup(s.moduleSettingsStore.subscribe(onModule))
        ctx.addCleanup(s.runningModulesStore.subscribe(onModule))
        val onUi: (dynamic, dynamic) -> Unit = { state, previous ->
            if (state.ratios !== previous.ratios && shown != null &&
                (shown.source as Array<dynamic>).any {
                    s.lowActivityOf(state.ratios, it.user_id) !== s.lowActivityOf(previous.ratios, it.user_id)
                }) schedule()
        }
        ctx.addCleanup(s.useUiStore.subscribe(onUi))
        val allow: (String) -> Boolean = { it in allowReplies }
        return props("pullComments" to ::pull, "refreshComments" to ::refresh, "scheduleReapplyBlocks" to ::schedule,
            "isPulling" to { pulling > 0 }, "forgetShown" to { shown = null }, "hasAllowReply" to allow)
    }
}

internal fun createCommentsPull(ctx: dynamic, signal: () -> dynamic, services: dynamic): dynamic =
    CommentsPull(ctx, signal, services).bind()
