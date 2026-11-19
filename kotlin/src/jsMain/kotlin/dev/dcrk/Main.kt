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
    api.nickType = { iconUrl: String -> nickType(iconUrl) }
    api.submitResult = { response: String -> submitResult(response) }
    api.resultMessage = { message: String?, detail: String? -> resultMessage(message, detail) }
    api.htmlToText = { html: String -> htmlToText(html) }
    api.decodeD = { value: String -> decodeD(value) }
    api.composeExtra = { isRegex: Boolean, gallery: String?, mode: String? -> composeExtra(isRegex, gallery, mode) }
    api.blockKey = { content: String, gallery: String? -> blockKey(content, gallery) }
    api.normalizeBlockList = { value: dynamic, known: dynamic -> normalizeBlockList(value, known) }
    api.normalizeDefaults = { value: dynamic -> normalizeDefaults(value) }
    api.sameComments = { a: dynamic, b: dynamic -> sameComments(a, b) }
    api.preprocessCommentMemo = { memo: String -> preprocessCommentMemo(memo) }
    api.extractVoice = { memo: String -> extractVoice(memo) }
    api.hasModifier = { ctrl: Boolean, alt: Boolean, meta: Boolean, shift: Boolean -> hasModifier(ctrl, alt, meta, shift) }
    api.pressedKey = { code: String, key: String -> pressedKey(code, key) }
    val scope: dynamic = js("globalThis")
    scope.__DCR_KOTLIN__ = api
}
