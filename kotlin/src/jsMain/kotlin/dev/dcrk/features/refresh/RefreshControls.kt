package dev.dcrk

/** 원본 6.2.2의 새로고침 모듈 메타. 브라우저용 RegExp는 WXT 경계에서 주입한다. */
internal fun refreshMeta(urls: Array<dynamic>, settings: dynamic): dynamic = props(
    "id" to "refresh",
    "urls" to urls,
    "settings" to settings,
    "toggles" to arrayOf("pause"),
    "commands" to props(
        "refreshLists" to props("key" to "Alt+R"),
        "refreshPause" to props("key" to "Alt+S")
    )
)

/** 원본 refresh/index.ts의 단축키와 페이지 토글 구현. */
internal fun createRefreshControls(): dynamic {
    val refresh: (dynamic) -> Unit = { api -> api.refreshLists(); Unit }
    val pause: (dynamic) -> Unit = { api -> api.togglePause() }
    val isPaused: (dynamic) -> Boolean = { api -> api.isPaused() as Boolean }
    return props(
        "shortcuts" to props("refreshLists" to refresh, "refreshPause" to pause),
        "pageToggles" to props("pause" to props(
            "desc" to "이 페이지의 자동 새로고침을 멈춥니다",
            "isOn" to isPaused,
            "toggle" to pause
        ))
    )
}
