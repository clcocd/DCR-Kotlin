package dev.dcrk

/**
 * 창 너비, 강제 컴팩트 및 페이지 종류로 레이아웃의 상태를 판정한다.
 * 디시콘 샵에는 .right_content가 있어 컴팩트 스타일을 적용하면 안 된다.
 */
fun layoutCompact(
    narrow: Boolean,
    forceCompact: Boolean,
    isDcconShop: Boolean,
    isViewPage: Boolean,
    useCompactModeOnView: Boolean
): Boolean = (narrow || forceCompact) && !isDcconShop && (!isViewPage || useCompactModeOnView)

/**
 * 메타의 선택자를 그대로 사용한다. 신규 hide 옵션을 추가해도 Kotlin에
 * 선택자 복사본을 만들지 않아 두 언어 간 설정 불일치를 방지한다.
 */
fun layoutHideCss(options: dynamic, settings: dynamic, noticePage: Boolean): String {
    val names: dynamic = js("Object.keys")(options)
    val rules = mutableListOf<String>()
    var index = 0
    while (index < (names.length as Int)) {
        val key = names[index] as String
        val ignoreNotice = noticePage && (key == "removeNotice" || key == "removeDCNotice")
        if (settings[key] == true && !ignoreNotice) {
            val selector = options[key].selector as String
            rules.add("$selector { display: none !important; }")
        }
        index++
    }
    return rules.joinToString("\n")
}

fun setupLayout(ctx: dynamic, options: dynamic, viewPage: Boolean, queryString: (String) -> String?) {
    val root: dynamic = js("document.documentElement")
    val window: dynamic = js("window")
    val abortSignal: dynamic = js("AbortSignal")
    val isDcconShop = window.location.hostname == "dccon.dcinside.com"
    val compactKeys = setOf("forceCompact", "useCompactModeOnView")
    val pushClass = "refresherPushToRight"
    var removeHideStyle: (() -> Unit)? = null
    var widthQuery: dynamic = null
    var widthWatch: dynamic = null

    fun compact() {
        val settings = ctx.settings
        val enabled = layoutCompact(
            widthQuery?.matches == true,
            settings.forceCompact as Boolean,
            isDcconShop,
            viewPage,
            settings.useCompactModeOnView as Boolean,
        )
        root.classList.toggle("refresherCompact", enabled)
    }

    fun watchWidth() {
        widthWatch?.abort()
        widthQuery = window.matchMedia("(max-width: ${ctx.settings.activePixel}px)")
        widthWatch = js("new AbortController()")
        val listenerOptions: dynamic = js("({})")
        listenerOptions.signal = abortSignal.any(arrayOf<dynamic>(ctx.signal, widthWatch.signal))
        widthQuery.addEventListener("change", { _: dynamic -> compact() }, listenerOptions)
        compact()
    }

    fun applyHide(): () -> Unit {
        val settings = ctx.settings
        val removeStyle = writeStyle(
            "refresher-layout-hide",
            layoutHideCss(options, settings, queryString("exception_mode") == "notice"),
        )
        root.classList.toggle(pushClass, settings.pushToRight == true && settings.hideUselessView == true)
        return removeStyle
    }

    ctx.onRevoke {
        removeHideStyle?.invoke()
        root.classList.remove("refresherCompact", pushClass)
    }

    watchWidth()
    removeHideStyle = applyHide()
    ctx.onSettingsChanged { keys: dynamic ->
        if (keys.has("activePixel") as Boolean) watchWidth()
        if (compactKeys.any { keys.has(it) as Boolean }) compact()
        val changed = js("Array.from")(keys) as Array<String>
        if (changed.any { it != "activePixel" && it !in compactKeys }) removeHideStyle = applyHide()
    }
}
