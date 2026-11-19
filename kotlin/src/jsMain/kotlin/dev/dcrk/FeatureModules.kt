package dev.dcrk

private const val FONTS_STYLE_ID = "refresher-fonts"

private fun applyFonts(ctx: dynamic): () -> Unit =
    writeStyle(
        FONTS_STYLE_ID,
        buildFontCss(
            ctx.settings.customFonts as String,
            ctx.settings.bodyFontSize as Double,
            ctx.settings.changeDCFont as Boolean
        )
    )

/** 폰트 모듈의 설정 변경 및 종료 처리를 모두 Kotlin이 소유한다. */
fun fontsModuleImplementation(): dynamic {
    val impl: dynamic = js("({})")
    val setup: (dynamic) -> Unit = { ctx ->
        ctx.onRevoke(applyFonts(ctx))
        val onChanged: (dynamic) -> Unit = { _ -> applyFonts(ctx) }
        ctx.onSettingsChanged(onChanged)
    }
    impl.setup = setup
    return impl
}

/** 스텔스 모듈의 활성화/종료·버튼·단축키·토글을 Kotlin으로 통합한다. */
fun stealthModuleImplementation(showToast: (String) -> Unit): dynamic {
    val impl: dynamic = js("({})")

    val setup: (dynamic) -> dynamic = { ctx ->
        val cleanup: () -> Unit = { stealthDispose() }
        ctx.onRevoke(cleanup)
        stealthEnable()

        val doc: dynamic = js("document")
        if (doc.body != null) {
            stealthCreateButton()
        } else {
            val options: dynamic = js("({once:true})")
            options.signal = ctx.signal
            val onReady: (dynamic) -> Unit = { _ -> stealthCreateButton() }
            doc.addEventListener("DOMContentLoaded", onReady, options)
        }

        val api: dynamic = js("({})")
        api.isRevealed = { stealthIsRevealed() }
        api.toggle = { stealthToggle() }
        api
    }
    impl.setup = setup

    val shortcuts: dynamic = js("({})")
    val pause: (dynamic) -> Unit = { api ->
        api.toggle()
        showToast(if (api.isRevealed() as Boolean) "이미지를 보이게 했습니다." else "이미지를 숨겼습니다.")
    }
    shortcuts.stealthPause = pause
    impl.shortcuts = shortcuts

    val reveal: dynamic = js("({})")
    reveal.desc = "스텔스로 숨긴 이미지를 보입니다"
    reveal.isOn = { api: dynamic -> api.isRevealed() }
    reveal.toggle = { api: dynamic -> api.toggle() }
    val pageToggles: dynamic = js("({})")
    pageToggles.reveal = reveal
    impl.pageToggles = pageToggles
    return impl
}
