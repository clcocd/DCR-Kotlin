package dev.dcrk

/** 원본과 같이 객체를 JSON 직렬화하지 않고 각 댓글 필드를 비교한다. */
fun sameComments(a: dynamic, b: dynamic): Boolean {
    val count = a.length as Int
    if (count != (b.length as Int)) return false

    val objectKeys: dynamic = js("Object.keys")
    for (index in 0 until count) {
        val item: dynamic = a[index]
        val other: dynamic = b[index]
        if (other == null) return false
        val keys: dynamic = objectKeys(item)
        for (field in 0 until (keys.length as Int)) {
            val key = keys[field] as String
            if (item[key] !== other[key]) return false
        }
        if ((keys.length as Int) != (objectKeys(other).length as Int)) return false
    }
    return true
}

private val mergedDccon = Regex("\"\\s*(img|video) class=\"written_dccon")
private val dcconStatus = Regex("data-dcconoverstatus=\"?\\w+\"?")

/** 본문 HTML 정화 전에 이어 붙은 디시콘과 디시콘 상태를 원본 규칙대로 정리한다. */
fun preprocessCommentMemo(memo: String): String {
    val separated = mergedDccon.replace(memo) {
        "\"><" + it.groupValues[1] + " class=\"written_dccon"
    }
    return dcconStatus.replace(separated, "data-dcconoverstatus=\"true\"")
}

/**
 * @^dc^@가 있는 음성 댓글은 표시 본문과 음성 URL을 분리한다.
 * 디시 음성 도메인이 아닌 iframe 주소는 voice 정보를 반환하지 않는다.
 */
fun extractVoice(memo: String): dynamic {
    val separator = "@^dc^@"
    if (!memo.contains(separator)) return null

    val raw = memo.substringBefore(separator)
    val display = memo.substringAfter(separator).substringBefore(separator)
    val iframe = raw.contains("<iframe")
    val source = if (iframe) {
        Regex("src=\"([^\"]+)\"").find(raw)?.groupValues?.get(1) ?: ""
    } else {
        "https://vr.dcinside.com/$raw"
    }

    val result: dynamic = js("({})")
    result.memo = display
    if (!source.startsWith("https://vr.dcinside.com/")) return result

    val voice: dynamic = js("({})")
    voice.src = source
    voice.iframe = iframe
    result.voice = voice
    return result
}
