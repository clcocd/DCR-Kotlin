package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf
import kotlin.math.max

private val currentPage: dynamic get() = js("window")
private val pageDoc: dynamic get() = js("document")

private fun loadPublicScript(url: String): Promise<dynamic> = Promise { resolve, reject ->
    val script = pageDoc.createElement("script")
    script.src = url
    val loaded: () -> Unit = { resolve(undefinedValue()) }
    val failed: () -> Unit = { reject(Error("api.js")) }
    script.onload = loaded
    script.onerror = failed
    pageDoc.head.append(script)
}

private fun executeGrecaptcha(siteKey: String, action: String): Promise<dynamic> = jsPromise {
    val scope = currentPage
    val doc = pageDoc
    if (!truthy(scope.grecaptcha)) awaitJs(loadPublicScript("https://www.google.com/recaptcha/api.js?render=$siteKey"))
    val challenge = scope.grecaptcha
    val ready = Promise<dynamic> { resolve, _ ->
        val run: () -> Unit = { resolve(undefinedValue()) }
        challenge.ready(run)
    }
    awaitJs(ready)
    val options = props("action" to action)
    try {
        awaitJs(challenge.execute(siteKey, options))
    } catch (_: Throwable) {
        val widgets = scope.refresherRecaptchaWidgets ?: props()
        scope.refresherRecaptchaWidgets = widgets
        if (widgets[siteKey] == null) {
            val box = doc.body.appendChild(doc.createElement("div"))
            widgets[siteKey] = challenge.render(box, props("sitekey" to siteKey, "size" to "invisible"))
        }
        awaitJs(challenge.execute(widgets[siteKey], options))
    }
}

private fun executeTurnstile(config: dynamic, hostAttribute: String?, cancelEvent: String): Promise<dynamic> = jsPromise {
    val scope = currentPage
    val doc = pageDoc
    if (!truthy(scope.turnstile)) awaitJs(loadPublicScript("https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit"))
    val widget = scope.turnstile
    val perf: dynamic = js("performance")
    val started = perf.now() as Double
    val host = if (hostAttribute.isNullOrEmpty()) null
        else doc.querySelector("refresher-root")?.shadowRoot?.querySelector("[$hostAttribute]")
    val box = doc.createElement("div")
    if (host != null) host.append(box)
    else {
        box.dataset.refresherUi = ""
        box.style.cssText = "position:fixed;left:0;bottom:0;opacity:0;pointer-events:none"
        doc.body.append(box)
    }

    var state = "pending"
    var tokenMs = 0.0
    var id: dynamic = undefinedValue()
    try {
        val result = Promise<String> { resolve, _ ->
            val complete: (String) -> Unit = {
                state = "ok"
                val elapsed = (perf.now() as Double) - started
                tokenMs = max(1.0, js("Math.round(elapsed)") as Double)
                resolve(it)
            }
            val interact: () -> Unit = {
                state = "interactive"
                if (host == null) resolve("")
            }
            val failed: () -> Unit = { state = "error"; resolve("") }
            val stop: () -> Unit = { resolve("") }
            val options = props("sitekey" to config.sitekey, "action" to "comment",
                "appearance" to if (host != null) "always" else if (truthy(config.appearance)) config.appearance else "interaction-only",
                "language" to "ko", "callback" to complete, "before-interactive-callback" to interact, "error-callback" to failed)
            if (host != null) options.theme = if (host.closest(".dark") != null) "dark" else "light"
            id = widget.render(box, options)
            if (host != null) host.addEventListener(cancelEvent, stop, props("once" to true))
            else if (truthy(config.wait)) scope.setTimeout(stop, config.wait)
        }
        val token = result.awaitResult()
        props("token" to token, "state" to state, "tokenMs" to tokenMs)
    } finally {
        if (id !== undefinedValue()) widget.remove(id)
        box.remove()
    }
}

private fun installListBridge(eventName: String) {
    val scope = currentPage
    val doc = pageDoc
    if (truthy(scope.refresherListBridge)) return
    scope.refresherListBridge = true
    val registered = Regex("""chk_user_block\('([^']*)'\)""")
    val changed: (dynamic) -> Unit = change@{ event ->
        if (js("CustomEvent.prototype").isPrototypeOf(event) != true || jsTypeOf(event.detail) != "string") return@change
        if (jsTypeOf(scope.chk_user_block) == "function") {
            val gallery = jsArray(doc.scripts).firstNotNullOfOrNull {
                registered.find((it.textContent ?: "") as String)?.groupValues?.get(1)
            }
            scope.chk_user_block(gallery ?: event.detail)
        }
        val memo = scope.UserMemo
        if (jsTypeOf(memo?.renderWriterMemoBadges) == "function") memo.renderWriterMemoBadges(null)
    }
    doc.addEventListener(eventName, changed)
}

internal fun installPageRuntime() {
    val page = currentPage
    if (page.__DCR_PAGE_RUNTIME__ != null) return
    page.__DCR_PAGE_RUNTIME__ = props("executeGrecaptcha" to ::executeGrecaptcha,
        "executeTurnstile" to ::executeTurnstile, "installListBridge" to ::installListBridge)
}
