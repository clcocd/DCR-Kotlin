package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun listenPreviewBackground(s: dynamic) {
    val timer: dynamic = js("setTimeout")
    val promises: dynamic = js("Promise")
    val noop: () -> Unit = {}
    val cleanup: () -> Unit = { s.storage.removeItem(s.previewDataKey) }
    s.browser.runtime.onInstalled.addListener(cleanup)
    fun race(request: dynamic, duration: Int?): dynamic {
        request.catch(noop)
        val timeout = Promise<dynamic> { resolve, _ ->
            if (duration != null) {
                val expire: () -> Unit = { resolve(undefinedValue()) }
                timer(expire, duration)
            }
        }
        return promises.race(arrayOf<dynamic>(request, timeout))
    }
    val grecaptcha: (dynamic) -> dynamic = { msg ->
        jsPromise {
            if (s.hasTab(msg.sender) != true) return@jsPromise undefinedValue()
            val input = msg.data
            val site = if (input.action == "insert_icon") s.iconSiteKey else s.commentSiteKeys[input.rcKey]
            try {
                awaitJs(s.preparePageRuntime(msg.sender))
                val request = s.runInPage(msg.sender, s.executeGrecaptcha, arrayOf<dynamic>(site, input.action))
                val results = awaitJs(race(request, 15_000))
                val value = if (results == null) null else results[0]?.result
                if (jsTypeOf(value) == "string") value else undefinedValue()
            } catch (_: Throwable) { undefinedValue() }
        }
    }
    val turnstile: (dynamic) -> dynamic = { msg ->
        jsPromise {
            if (s.hasTab(msg.sender) != true) return@jsPromise undefinedValue()
            val input = msg.data
            val visible = input.visible == true
            try {
                awaitJs(s.preparePageRuntime(msg.sender))
                val request = s.runInPage(msg.sender, s.executeTurnstile,
                    arrayOf<dynamic>(input.config, if (visible) s.turnstileHost() else null, s.turnstileCancel()))
                val results = awaitJs(race(request, if (visible) null else 30_000))
                if (results == null) undefinedValue() else results[0]?.result ?: undefinedValue()
            } catch (_: Throwable) { undefinedValue() }
        }
    }
    s.onMessage("refresher:grecaptchaToken", grecaptcha)
    s.onMessage("refresher:turnstileToken", turnstile)
}

internal fun listenListBridge(s: dynamic) {
    val listener: (dynamic) -> dynamic = { message ->
        jsPromise {
            if (s.hasTab(message.sender) == true) {
                awaitJs(s.preparePageRuntime(message.sender))
                awaitJs(s.runInPage(message.sender, s.installListBridge, arrayOf<dynamic>(s.eventName())))
            }
            undefinedValue()
        }
    }
    s.onMessage("refresher:installListBridge", listener)
}

internal fun tabTarget(sender: dynamic): dynamic =
    if (truthy(sender.documentId)) props("tabId" to sender.tab.id, "documentIds" to arrayOf<dynamic>(sender.documentId))
    else props("tabId" to sender.tab.id, "frameIds" to arrayOf<dynamic>(sender.frameId ?: 0))

internal fun hasSenderTab(sender: dynamic): Boolean =
    sender.tab != null && jsTypeOf(sender.tab.id) != "undefined"
