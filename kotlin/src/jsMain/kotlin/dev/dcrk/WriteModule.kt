package dev.dcrk

import kotlin.js.JsName

@JsName("MutationObserver")
private external class WriteMutationObserver(callback: (dynamic, dynamic) -> Unit) {
    fun observe(target: dynamic, options: dynamic)
    fun disconnect()
}

private const val EDITED_KEY = "refresherEdited"

/** MAIN world의 업로드 훅은 한 문서에서 한 번만 요청한다. */
private var uploadHookRequested = false

private fun publishWriteImageOptions(settings: dynamic, key: String, hookUploads: () -> dynamic) {
    val doc: dynamic = js("document")
    val dataset: dynamic = doc.documentElement.dataset
    val webp = settings.webpConvert as Boolean
    val rename = settings.obfuscateName as Boolean
    if (!webp && !rename) {
        js("Reflect.deleteProperty")(dataset, key)
        return
    }

    val options: dynamic = js("({})")
    options.webp = webp
    options.quality = (settings.webpQuality as Double) / 100.0
    options.rename = rename
    options.concurrency = settings.webpConcurrency
    dataset[key] = js("JSON.stringify")(options)

    if (uploadHookRequested) return
    uploadHookRequested = true
    val request: dynamic = hookUploads()
    request.catch({ error: dynamic ->
        uploadHookRequested = false
        js("console").error(error)
    })
}

/** 페이지 변경 표시와 입력 데이터 보호를 TS 핸들러가 아닌 Kotlin에서 수행한다. */
fun writeModuleImplementation(
    selectors: dynamic,
    isWritePage: () -> Boolean,
    hookUploads: () -> dynamic
): dynamic {
    val impl: dynamic = js("({})")
    val setup: (dynamic) -> Unit = { ctx ->
        val doc: dynamic = js("document")
        val win: dynamic = js("window")
        val dataset: dynamic = doc.documentElement.dataset
        val clearOptions: () -> Unit = { js("Reflect.deleteProperty")(dataset, selectors.uploadOptionsKey) }
        ctx.onRevoke(clearOptions)

        val publish: () -> Unit = {
            publishWriteImageOptions(ctx.settings, selectors.uploadOptionsKey as String, hookUploads)
        }
        publish()
        val onSettingsChanged: (dynamic) -> Unit = { _ -> publish() }
        ctx.onSettingsChanged(onSettingsChanged)

        if (isWritePage()) {
            var submitting = false
            var leaving = false
            var edited = !(win.location.pathname as String).contains("/board/modify") ||
                (js("Reflect.has")(dataset, EDITED_KEY) as Boolean)
            val markEdited: () -> Unit = {
                edited = true
                dataset[EDITED_KEY] = ""
            }

            val onClick: (dynamic) -> Unit = { event ->
                val target: dynamic = event.target
                if (target != null && target.nodeType == 1) {
                    if (target.closest(selectors.submit) != null) submitting = true
                    if (target.closest(selectors.leave) != null) leaving = true
                }
            }

            val onInput: (dynamic) -> Unit = { _ ->
                submitting = false
                leaving = false
                if (!edited) markEdited()
            }

            val onBeforeUnload: (dynamic) -> Unit = { event ->
                val flag: dynamic = doc.querySelector(selectors.posted)
                val posted = if (flag != null) flag.value == "Y" else submitting
                if ((ctx.settings.preventExit as Boolean) && !posted && !leaving && edited) {
                    val editor: dynamic = doc.querySelector(selectors.editor)
                    val text = StringBuilder()
                    if (editor != null) {
                        val nodes: dynamic = editor.childNodes
                        for (index in 0 until (nodes.length as Int)) {
                            val node: dynamic = nodes[index]
                            if (node.nodeType == 1 && node.matches(selectors.auto)) continue
                            val content: dynamic = node.textContent
                            if (content != null) text.append(content as String)
                        }
                    }

                    var hasMedia = false
                    if (editor != null) {
                        val media: dynamic = editor.querySelectorAll("img, video, iframe, embed")
                        for (index in 0 until (media.length as Int)) {
                            if (media[index].closest(selectors.auto) == null) {
                                hasMedia = true
                                break
                            }
                        }
                    }

                    val subject: dynamic = doc.querySelector(selectors.subject)
                    val title = if (subject == null) "" else (subject.value as String).trim()
                    if (title.isNotEmpty() || text.toString().trim().isNotEmpty() || hasMedia) {
                        event.preventDefault()
                    }
                }
            }

            // stopAll은 유지, 명시적 모듈 끄기에서만 리스너 종료.
            val listeners: dynamic = js("new AbortController()")
            val removeListeners: () -> Unit = { listeners.abort() }
            ctx.onRevoke(removeListeners)
            val capturing: dynamic = js("({capture:true})")
            capturing.signal = listeners.signal
            val normal: dynamic = js("({})")
            normal.signal = listeners.signal

            doc.addEventListener("click", onClick, capturing)
            doc.addEventListener("input", onInput, capturing)
            win.addEventListener("beforeunload", onBeforeUnload, normal)

            // 수정 페이지는 사용자가 건드린 후의 DOM 변화만 수정으로 간주한다.
            if (!edited) {
                lateinit var observer: WriteMutationObserver
                observer = WriteMutationObserver { _, _ ->
                    markEdited()
                    observer.disconnect()
                }
                val observe: (dynamic) -> Unit = { _ ->
                    if (!edited) {
                        val editor: dynamic = doc.querySelector(selectors.editor)
                        if (editor != null) {
                            val options: dynamic = js("({childList:true,subtree:true,characterData:true})")
                            observer.observe(editor, options)
                        }
                    }
                }
                for (type in listOf("pointerdown", "keydown", "dragenter")) {
                    doc.addEventListener(type, observe, capturing)
                }
                val disconnect: (dynamic) -> Unit = { _ -> observer.disconnect() }
                listeners.signal.addEventListener("abort", disconnect)
            }
        }
    }
    impl.setup = setup
    return impl
}
