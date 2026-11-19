package dev.dcrk

private const val LAYOUT_PUSH_CLASS = "refresherPushToRight"
private const val LAYOUT_STYLE_ID = "refresher-layout-hide"

/** 레이아웃 모듈의 폭 감시, 설정 재적용, 종료 정리를 Kotlin이 관리한다. */
fun layoutModuleImplementation(
    hideOptions: dynamic,
    isViewPage: Boolean,
    isDcconShop: Boolean,
    isNoticePage: () -> Boolean
): dynamic {
    val impl: dynamic = js("({})")
    val setup: (dynamic) -> Unit = { ctx ->
        val doc: dynamic = js("document")
        val win: dynamic = js("window")
        var removeHideStyle: (() -> Unit)? = null
        var widthQuery: dynamic = null
        var widthWatch: dynamic = null

        val applyHide: () -> (() -> Unit) = {
            val cleanup = writeStyle(
                LAYOUT_STYLE_ID,
                layoutHideCss(hideOptions, ctx.settings, isNoticePage())
            )
            val push = (ctx.settings.pushToRight as Boolean) &&
                (ctx.settings.hideUselessView as Boolean)
            doc.documentElement.classList.toggle(LAYOUT_PUSH_CLASS, push)
            cleanup
        }

        val compact: () -> Unit = {
            val narrow = widthQuery != null && (widthQuery.matches as Boolean)
            val active = layoutCompact(
                narrow,
                ctx.settings.forceCompact as Boolean,
                isDcconShop,
                isViewPage,
                ctx.settings.useCompactModeOnView as Boolean
            )
            doc.documentElement.classList.toggle("refresherCompact", active)
        }

        val watchWidth: () -> Unit = {
            if (widthWatch != null) widthWatch.abort()
            widthQuery = win.matchMedia("(max-width: ${ctx.settings.activePixel}px)")
            widthWatch = js("new AbortController()")
            val signals: dynamic = js("[]")
            signals.push(ctx.signal)
            signals.push(widthWatch.signal)
            val options: dynamic = js("({})")
            options.signal = js("AbortSignal.any")(signals)
            val changed: (dynamic) -> Unit = { _ -> compact() }
            widthQuery.addEventListener("change", changed, options)
            compact()
        }

        val cleanup: () -> Unit = {
            removeHideStyle?.invoke()
            doc.documentElement.classList.remove("refresherCompact", LAYOUT_PUSH_CLASS)
        }
        ctx.onRevoke(cleanup)

        watchWidth()
        removeHideStyle = applyHide()

        val onSettingsChanged: (dynamic) -> Unit = { keys ->
            if (keys.has("activePixel") as Boolean) watchWidth()
            if ((keys.has("forceCompact") as Boolean) ||
                (keys.has("useCompactModeOnView") as Boolean)) compact()

            val entries: dynamic = js("Array.from")(keys)
            var otherChanged = false
            for (index in 0 until (entries.length as Int)) {
                val key = entries[index] as String
                if (key != "activePixel" && key != "forceCompact" && key != "useCompactModeOnView") {
                    otherChanged = true
                    break
                }
            }
            if (otherChanged) removeHideStyle = applyHide()
        }
        ctx.onSettingsChanged(onSettingsChanged)
    }
    impl.setup = setup
    return impl
}
