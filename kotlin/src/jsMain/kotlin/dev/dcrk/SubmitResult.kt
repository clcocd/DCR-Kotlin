package dev.dcrk

/** 디시 응답 본문에서 첫 세 구간만 읽어 원본 객체 모양으로 반환한다. */
fun submitResult(response: String): dynamic {
    val result: dynamic = js("({})")
    val text = response.trim()
    if (text.startsWith("<")) {
        result.result = text
        return result
    }

    val first = text.indexOf("||")
    if (first < 0) {
        return props("result" to text, "message" to undefinedValue(), "detail" to undefinedValue())
    }

    result.result = text.substring(0, first)
    val remainder = text.substring(first + 2)
    val second = remainder.indexOf("||")
    if (second < 0) {
        result.message = remainder
        result.detail = undefinedValue()
    } else {
        result.message = remainder.substring(0, second)
        result.detail = remainder.substring(second + 2).substringBefore("||")
    }
    return result
}

/** nomember 응답은 세 번째 칸의 문구를 표시한다. */
fun resultMessage(message: String?, detail: String?): String? {
    val value = if (message == "nomember") detail else message
    return value?.takeIf { it.isNotEmpty() }
}
