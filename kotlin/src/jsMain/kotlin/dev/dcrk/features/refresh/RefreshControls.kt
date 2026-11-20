package dev.dcrk

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
