package dev.dcrk.input

/** 원본 utils/event.ts의 조합키 판정. */
fun hasModifier(ctrl: Boolean, alt: Boolean, meta: Boolean, shift: Boolean): Boolean =
    ctrl || alt || meta || shift

/**
 * 한글 입력 상태에서 key 값이 달라질 수 있으므로, Key/Digit 코드의
 * 영문 대문자·숫자 한 글자를 우선 사용한다.
 */
fun pressedKey(code: String, key: String): String {
    val value = when {
        code.startsWith("Key") && code.length == 4 -> code[3]
        code.startsWith("Digit") && code.length == 6 -> code[5]
        else -> null
    }
    return if (value != null && (value in 'A'..'Z' || value in '0'..'9')) {
        value.lowercaseChar().toString()
    } else {
        key.lowercase()
    }
}
