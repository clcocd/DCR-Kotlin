package dev.dcrk

/**
 * WXT가 불러온 JavaScript 번들의 실행 컨텍스트에 함수들을 공개한다.
 * Kotlin 코드를 각 브라우저 영역(content/background/popup)에서 각각 import한다.
 */
fun main() {
    val api: dynamic = js("({})")
    api.dcinsideHref = { url: String -> dcinsideHref(url) }
    api.galleryKind = { url: String -> galleryKind(url) }
    api.galleryPath = { url: String -> galleryPath(url) }
    api.galltypeOf = { url: String -> galltypeOf(url) }
    api.managerAjaxUrl = { url: String, action: String -> managerAjaxUrl(url, action) }
    api.listUrl = { url: String -> listUrl(url) }
    api.mergeParamURL = { origin: String, from: String -> mergeParamURL(origin, from) }
    api.postSearchUrl = { query: String -> postSearchUrl(query) }
    api.hash53 = { value: String -> hash53(value) }
    val scope: dynamic = js("globalThis")
    scope.__DCR_KOTLIN__ = api
}
