package dev.dcrk

import kotlin.js.jsTypeOf

private fun isHistoryRecord(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && !(js("Array.isArray")(value) as Boolean)

private fun isGalleryPreData(value: dynamic): Boolean {
    if (!isHistoryRecord(value)) return false
    val number: dynamic = js("Number")
    return jsTypeOf(value.gallery) == "string" && jsTypeOf(value.id) == "string" &&
        (jsTypeOf(value.title) == "undefined" || jsTypeOf(value.title) == "string") &&
        jsTypeOf(value.link) == "string" && dcinsideHref(value.link as String) != null &&
        jsTypeOf(value.notice) == "boolean" && jsTypeOf(value.recommend) == "boolean" &&
        jsTypeOf(value.type) == "string" && (number.isInteger(value.commentCount) as Boolean)
}

/** 브라우저 기록에 저장된 버전과 필드를 확인한다. */
fun previewEntry(state: dynamic): dynamic {
    if (!isHistoryRecord(state) || state.refresher !== 1 || jsTypeOf(state.doc) != "number") return null

    val entry: dynamic = js("({})")
    val back = state.back
    entry.refresher = 1
    entry.doc = state.doc
    entry.preData = if (isGalleryPreData(state.preData)) state.preData else js("undefined")
    entry.back = if (isHistoryRecord(back) && jsTypeOf(back.title) == "string" && jsTypeOf(back.url) == "string") back else js("undefined")
    entry.depth = if (jsTypeOf(state.depth) == "number") state.depth else js("undefined")
    entry.reopen = state.reopen === true
    return entry
}
