package dev.dcrk

/** 브라우저가 제공하는 URL API를 Kotlin/JS에서 직접 사용한다. */
@JsName("URL")
internal external class BrowserUrl(source: String) {
    val href: String
    val protocol: String
    val hostname: String
    var pathname: String
    var host: String
    val searchParams: BrowserSearchParams
}

internal external class BrowserSearchParams {
    fun delete(name: String)
    fun delete(name: String, value: String)
    fun set(name: String, value: String)
    fun sort()
    fun forEach(callback: (value: String, key: String) -> Unit)
}

private const val BASE = "https://gall.dcinside.com/"

/** 원본과 마찬가지로 잘못된 주소는 예외 대신 null이다. */
private fun parseUrlOrNull(source: String): BrowserUrl? =
    js("URL.parse")(source) as BrowserUrl?

/** 원본 core/http/urls.ts의 https + dcinside 하위 도메인 검사. */
fun dcinsideHref(url: String): String? {
    val parsed = parseUrlOrNull(url) ?: return null
    return if (parsed.protocol == "https:" && parsed.hostname.endsWith(".dcinside.com")) parsed.href else null
}

/** 검색어가 아닌 URL의 첫 번째 경로 구간만 검사한다. */
fun galleryKind(url: String): String = galleryKindFromPath(parseUrlOrNull(url)?.pathname ?: "")

fun galleryPath(url: String): String = when (galleryKind(url)) {
    "minor" -> "mgallery/"
    "mini" -> "mini/"
    "person" -> "person/"
    else -> ""
}

fun galltypeOf(url: String): String = when (galleryKind(url)) {
    "minor" -> "M"
    "mini" -> "MI"
    "person" -> "PR"
    else -> "G"
}

fun managerAjaxUrl(url: String, action: String): String {
    val manager = when (galleryKind(url)) {
        "mini" -> "mini"
        "person" -> "person"
        else -> "minor"
    }
    return BASE + "ajax/" + manager + "_manager_board_ajax/" + action
}

/** 글 보기용 매개변수를 제거하고 나머지 쿼리는 원본과 같이 정렬한다. */
fun listUrl(url: String): String {
    val params = BrowserUrl(url).searchParams
    params.delete("no")
    params.delete("t")
    params.delete("page", "1")
    params.sort()
    return BASE + galleryPath(url) + "board/lists?" + params.toString()
}

fun mergeParamURL(origin: String, from: String): String {
    val params = BrowserUrl(origin).searchParams
    BrowserUrl(from).searchParams.forEach { value, key -> params.set(key, value) }
    return "?" + params.toString()
}

/** UTF-8 바이트를 '.XX' 형식으로 표현한다. */
fun postSearchUrl(query: String): String =
    "https://search.dcinside.com/post/sort/latest/q/" +
        query.encodeToByteArray().joinToString("") { byte ->
            "." + (byte.toInt() and 255).toString(16).padStart(2, '0').uppercase()
        }
