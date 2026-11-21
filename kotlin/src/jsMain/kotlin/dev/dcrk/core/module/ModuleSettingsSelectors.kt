package dev.dcrk

/** 모듈 설정 조회는 전달된 상태의 원본 참조를 그대로 반환한다. */
fun moduleSettingsValue(id: String, state: dynamic): dynamic = state[id]

/** 설정은 비활성 모듈에도 남지만 실행 중인 모듈에만 노출한다. */
fun runningModuleSettingsValue(id: String, running: dynamic, settings: dynamic): dynamic =
    if (running[id] == true) settings[id] else undefinedValue()
