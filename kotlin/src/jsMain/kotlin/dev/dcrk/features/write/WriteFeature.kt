package dev.dcrk

/** 원본 글쓰기/수정·이미지 업로드 페이지와 기본 비활성화 설정을 보존한다. */
internal fun writeMeta(urls: Array<dynamic>): dynamic = props(
    "id" to "write",
    "urls" to urls,
    "defaultEnable" to false,
    "settings" to staticFeatureSettings("write")
)

internal const val UPLOAD_OPTIONS_KEY = "refresherUpload"
private const val EDITED_KEY = "refresherEdited"
private var hookRequested = false

fun setupWrite(ctx: dynamic, s: dynamic) {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val console: dynamic = js("console")
    val dataset = doc.documentElement.dataset
    fun removeUploadOptions() { js("Reflect.deleteProperty")(dataset, UPLOAD_OPTIONS_KEY) }

    fun publish() {
        val settings = ctx.settings
        val webp = settings.webpConvert == true
        val rename = settings.obfuscateName == true
        if (!webp && !rename) {
            removeUploadOptions()
            return
        }
        dataset[UPLOAD_OPTIONS_KEY] = js("JSON.stringify")(props(
            "webp" to webp, "quality" to (settings.webpQuality as Double) / 100, "rename" to rename, "concurrency" to settings.webpConcurrency,
        ))
        if (hookRequested) return
        hookRequested = true
        s.requestHook().catch { error: dynamic -> hookRequested = false; console.error(error) }
    }

    ctx.onRevoke(::removeUploadOptions)
    publish()
    ctx.onSettingsChanged { _: dynamic -> publish() }
    val pathname = window.location.pathname as String
    if (s.writePage.test(pathname) != true) return

    var submitting = false
    var leaving = false
    val marked = js("Reflect.has")(dataset, EDITED_KEY) as Boolean
    var edited = !pathname.contains("/board/modify") || marked
    fun markEdited() { edited = true; dataset[EDITED_KEY] = "" }

    val click: (dynamic) -> Unit = click@{ event ->
        val target = event.target
        if (!(js("target instanceof Element") as Boolean)) return@click
        if (target.closest(s.submit) != null) submitting = true
        if (target.closest(s.leave) != null) leaving = true
    }
    val input: () -> Unit = {
        submitting = false
        leaving = false
        if (!edited) markEdited()
    }
    val beforeUnload: (dynamic) -> Unit = unload@{ event ->
        val flag = doc.querySelector(s.posted)
        val posted = if (flag == null) submitting else flag.value == "Y"
        if (ctx.settings.preventExit != true || posted || leaving || !edited) return@unload
        val editor = doc.querySelector(s.editor)
        val text = jsArray(editor?.childNodes ?: emptyArray<dynamic>()).joinToString("") { node ->
            if (js("node instanceof Element") as Boolean && node.matches(s.auto) == true) ""
            else node.textContent as String? ?: ""
        }.trim()
        val media = jsArray(editor?.querySelectorAll("img, video, iframe, embed") ?: emptyArray<dynamic>())
            .any { it.closest(s.auto) == null }
        val subject = (doc.querySelector(s.subject)?.value as String?)?.trim()
        if (!subject.isNullOrEmpty() || text.isNotEmpty() || media) event.preventDefault()
    }

    val controller: dynamic = js("new AbortController()")
    ctx.onRevoke { controller.abort() }
    val signal = controller.signal
    val capture = props("capture" to true, "signal" to signal)
    doc.addEventListener("click", click, capture)
    doc.addEventListener("input", input, capture)
    window.addEventListener("beforeunload", beforeUnload, props("signal" to signal))
    if (edited) return

    var observer: dynamic = null
    val changed: () -> Unit = { markEdited(); observer.disconnect() }
    observer = js("new MutationObserver(changed)")
    val watch: () -> Unit = {
        if (!edited) {
            val editor = doc.querySelector(s.editor)
            if (editor != null) observer.observe(editor, props("childList" to true, "subtree" to true, "characterData" to true))
        }
    }
    for (type in listOf("pointerdown", "keydown", "dragenter")) doc.addEventListener(type, watch, capture)
    signal.addEventListener("abort", { observer.disconnect() })
}

fun listenWriteBackground(s: dynamic) {
    val receive: (dynamic) -> dynamic = { event -> jsPromise {
        if (s.hasTab(event.sender) == true) awaitJs(s.runFileInPage(event.sender, "/upload-hook.js"))
        undefinedValue()
    } }
    s.onMessage("refresher:hookUploads", receive)
}
