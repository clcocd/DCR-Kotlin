package dev.dcrk.block

import dev.dcrk.props

/** 원본 6.2.2의 차단 모듈 메타데이터. */
fun blockMeta(urls: Array<dynamic>, settings: dynamic): dynamic = props(
    "id" to "block",
    "urls" to urls,
    "settings" to settings,
    "toggles" to arrayOf("reveal"),
    "commands" to props("blockReveal" to props())
)

/** 단축키·페이지 토글도 Kotlin 제어 API를 직접 사용한다. */
fun createBlockControls(): dynamic {
    val reveal: (dynamic) -> Unit = { api -> api.toggleReveal() }
    val desc: (dynamic) -> String = { api ->
        val text: dynamic = js("String")
        "가린 ${text(api.hiddenCount())}개를 흐리게 보입니다"
    }
    val isOn: (dynamic) -> Boolean = { api -> api.isRevealed() as Boolean }
    return props(
        "shortcuts" to props("blockReveal" to reveal),
        "pageToggles" to props("reveal" to props(
            "desc" to desc,
            "isOn" to isOn,
            "toggle" to reveal
        ))
    )
}
