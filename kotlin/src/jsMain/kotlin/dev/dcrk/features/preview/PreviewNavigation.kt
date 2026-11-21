package dev.dcrk

import kotlin.js.Date
import kotlin.js.undefined

private val previewWindow: dynamic get() = js("window")
private val previewDocument: dynamic get() = js("document")
private val pageHistory: dynamic get() = js("history")
private val pageLocation: dynamic get() = js("location")
private val newAbort: dynamic get() = js("AbortController")

internal fun createPreviewPostService(services: dynamic): dynamic {
    fun textBlock(pre: dynamic, post: dynamic): dynamic {
        val block = services.runningModuleSettings("block")
        return if (block != null && post.writeText !== undefined &&
            services.isBlocked("TEXT", post.writeText, pre.gallery) == true
        ) services.blockMode(block) else undefined
    }

    fun cached(pre: dynamic): dynamic {
        val entry = services.getEntry(pre)
        val age = Date.now() - ((entry?.fetchedAt ?: 0) as Number).toDouble()
        return if (entry?.post != null && age < 10_000)
            props("post" to entry.post, "age" to age) else undefined
    }

    fun create(ctx: dynamic): dynamic {
        var pending: dynamic = null
        val weakMap: dynamic = js("WeakMap")
        val sanitized = js("Reflect").construct(weakMap, emptyArray<dynamic>())
        val stripped = js("Reflect").construct(weakMap, emptyArray<dynamic>())

        fun process(pre: dynamic, post: dynamic, strip: dynamic): dynamic = jsPromise {
            val stripMedia = strip == true
            val cache = if (stripMedia) stripped else sanitized
            var contents = cache.get(post)
            if (contents === undefined) {
                val raw = (post.contents as String?) ?: ""
                contents = services.sanitizeHtml(if (stripMedia) raw else supplementYoutubeEmbeds(raw), props("stripMedia" to stripMedia))
                cache.set(post, contents)
            }
            js("Object").assign(props(), post, props("contents" to contents, "textBlocked" to textBlock(pre, post)))
        }

        fun request(pre: dynamic): dynamic {
            val key = services.postKey(pre)
            if (pending?.key == key && pending != null) return pending.post
            pending?.ctrl?.abort()
            val ctrl = js("Reflect").construct(newAbort, emptyArray<dynamic>())
            val onPost: (dynamic) -> dynamic = { result ->
                services.setEntry(pre, props("post" to result, "fetchedAt" to Date.now()))
                result
            }
            val promise = services.fetchPost(pre, ctrl.signal).then(onPost)
            val slot = props("key" to key, "ctrl" to ctrl, "post" to promise)
            pending = slot
            val settle: () -> Unit = { if (pending === slot) pending = null }
            val ignore: () -> dynamic = { undefined }
            promise.catch(ignore).finally(settle)
            return promise
        }

        fun get(pre: dynamic): dynamic = jsPromise {
            val hit = if (ctx.settings.disableCache == true) undefined else cached(pre)
            if (hit != null) return@jsPromise props("post" to hit.post, "fresh" to ((hit.age as Double) < 2000))
            try {
                props("post" to awaitJs(request(pre)), "fresh" to true)
            } catch (error: Throwable) {
                if (services.isAbortError(error) == true) throw error
                val archived = if (ctx.settings.archiveArticle == true) services.getEntry(pre)?.post else null
                if (archived == null) throw error
                console.error("Preview fetch failed, showing the archived post:", error)
                services.setEntry(pre, props("post" to archived))
                props("post" to archived, "fresh" to false, "archived" to true)
            }
        }

        return props("processContents" to ::process, "getPost" to ::get, "requestPost" to ::request,
            "hasPending" to { pending != null }, "abortPending" to {
                pending?.ctrl?.abort()
                pending = null
            })
    }

    return props("textBlockOf" to ::textBlock, "cachedPost" to ::cached, "createPostLoader" to ::create,
        "supplementYoutubeEmbeds" to ::supplementYoutubeEmbeds)
}

internal fun createPreviewHistoryController(ctx: dynamic, services: dynamic): dynamic {
    var saved: dynamic = null
    fun restore(fromHistory: Boolean) {
        val entry = saved
        if (entry != null) {
            val title = entry.title
            val depth = services.ownPreviewDepth() as Int
            try {
                if (!fromHistory && depth > 0) {
                    pageHistory.go(-depth)
                    val restoreTitle: () -> Unit = { previewDocument.title = title }
                    previewWindow.addEventListener("popstate", restoreTitle, props("once" to true, "signal" to ctx.signal))
                } else {
                    try {
                        if (!fromHistory && pageLocation.href != entry.url) pageHistory.pushState(entry.state, title, entry.url)
                    } catch (error: Throwable) {
                        console.error("미리보기 기록 되돌리기 실패:", error)
                    }
                    previewDocument.title = title
                }
            } catch (error: Throwable) {
                console.error("미리보기 기록 되돌리기 실패:", error)
            }
        }
        saved = null
    }

    fun replace(state: dynamic, url: dynamic): Boolean = try {
        pageHistory.replaceState(state, "", url)
        true
    } catch (error: Throwable) {
        console.error("미리보기 기록 고치기 실패:", error)
        false
    }

    fun reopen(open: (dynamic) -> Unit) {
        val entry = services.previewEntry(pageHistory.state)
        if (entry == null || entry.doc == services.historyDoc || entry.preData == null || entry.back == null) return
        val pre = entry.preData
        val back = entry.back
        val navigation = js("performance").getEntriesByType("navigation")[0]
        val navigationType = if (js("PerformanceNavigationTiming.prototype").isPrototypeOf(navigation) == true) navigation.type else null
        if (entry.reopen == true) {
            replace(js("Object").assign(props(), entry, props("doc" to services.historyDoc, "reopen" to false)), pre.link)
            saved = back
            services.whenDomReady({ open(pre) }, ctx.signal)
        } else if (navigationType == "back_forward" && previewDocument.readyState != "complete") {
            if (replace(js("Object").assign(props(), entry, props("reopen" to true)), back.url)) pageLocation.reload()
        } else if (navigationType == "reload" || navigationType == "back_forward") {
            replace(null, undefined)
        }
    }

    val remember: (dynamic) -> Unit = { saved = it }
    return props("restore" to ::restore, "remember" to remember, "setBack" to remember,
        "replaceEntry" to ::replace, "back" to { saved }, "reopenFromEntry" to ::reopen)
}

