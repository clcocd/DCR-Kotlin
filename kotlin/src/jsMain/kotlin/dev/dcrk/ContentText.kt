package dev.dcrk

private val htmlMarkup = Regex("""<!--[\s\S]*?-->|<(?:[^>"]|"[^"]*")*>""")

/**
 * 원본 sanitize.ts의 htmlToText. 차단어 검색용 텍스트 처리이므로
 * 이스케이프된 &amp;lt;를 두 번 풀지 않는 순서를 보존한다.
 */
fun htmlToText(html: String): String {
    val text = if ('<' in html) html.replace(htmlMarkup, " ") else html
    if ('&' !in text) return text
    return text.replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
}

/** 디시 common.js의 _d() 변환 테이블. */
private const val dAlphabet = "yL/M=zNa0bcPQdReSfTgUhViWjXkYIZmnpo+qArOBslCt2D3uE4Fv5G6wH178xJ9K"
private const val base64Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

/**
 * 디시 전용 Base64 알파벳을 표준 Base64로 변환한 뒤 atob로 해석한다.
 * 표에 없는 문자(개행 포함)는 원본과 동일하게 건너뛴다.
 */
fun decodeD(value: String): String {
    val encoded = buildString {
        for (character in value) {
            val index = dAlphabet.indexOf(character)
            if (index >= 0) append(base64Alphabet[index])
        }
    }
    return js("atob")(encoded) as String
}
