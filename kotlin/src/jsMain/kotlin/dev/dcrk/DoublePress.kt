package dev.dcrk

/**
 * 같은 키를 제한 시간 안에 두 번 누르면 true를 반환한다.
 * 다른 키가 입력되면 이전 키를 무효화하고, 확인 성공 후에도 초기화한다.
 */
fun createDoublePress(windowMs: Double): (String) -> Boolean {
    var armedKey: String? = null
    var armedAt = 0.0

    return { key ->
        val now: Double = js("Date.now()")
        if (armedKey == key && now - armedAt < windowMs) {
            armedKey = null
            true
        } else {
            armedKey = key
            armedAt = now
            false
        }
    }
}
