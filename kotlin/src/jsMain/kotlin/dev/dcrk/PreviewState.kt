package dev.dcrk

import kotlin.js.jsTypeOf

private fun plainObject(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && !(js("Array.isArray")(value) as Boolean)

/** 디시 글 주소·댓글 수 등 미리보기 기록의 핵심 필드를 검증한다. */
private fun validPreData(value: dynamic): Boolean {
    if (!plainObject(value)) return false
    if (jsTypeOf(value.gallery) != "string" || jsTypeOf(value.id) != "string") return false
    val title = jsTypeOf(value.title)
    if (title != "undefined" && title != "string") return false
    if (jsTypeOf(value.link) != "string" || dcinsideHref(value.link as String) == null) return false
    if (jsTypeOf(value.notice) != "boolean" || jsTypeOf(value.recommend) != "boolean") return false
    if (jsTypeOf(value.type) != "string") return false
    return js("Number.isInteger")(value.commentCount) as Boolean
}

private fun validSavedHistory(value: dynamic): Boolean =
    plainObject(value) && jsTypeOf(value.title) == "string" && jsTypeOf(value.url) == "string"

/**
 * 페이지 스크립트가 쓰는 history.state 중 확장이 작성한 항목만 받아들인다.
 * 잘못된 preData/back 값은 버리되 나머지 기록과 reopen 플래그는 유지한다.
 */
fun previewEntry(state: dynamic): dynamic {
    if (!plainObject(state) || state.refresher !== 1 || jsTypeOf(state.doc) != "number") return null

    val item: dynamic = js("({})")
    item.refresher = 1
    item.doc = state.doc
    item.preData = if (validPreData(state.preData)) state.preData else js("undefined")
    item.back = if (validSavedHistory(state.back)) state.back else js("undefined")
    item.depth = if (jsTypeOf(state.depth) == "number") state.depth else js("undefined")
    item.reopen = jsTypeOf(state.reopen) == "boolean" && (state.reopen as Boolean)
    return item
}

/**
 * 댓글돌이를 제외하고 삭제 표시를 0/1로 정규화한다.
 * 삭제된 댓글 보존/복원 로직은 호출부에서 원본 동작을 그대로 유지한다.
 */
fun normalizeIncomingComments(raw: dynamic): dynamic {
    val result: dynamic = js("[]")
    val copyFields: dynamic = js("Object.assign")
    for (index in 0 until (raw.length as Int)) {
        val item: dynamic = raw[index]
        if (item.nicktype === "COMMENT_BOY") continue
        val copy: dynamic = copyFields(js("({})"), item)
        copy.is_delete = if (item.is_delete !== "0" || item.del_yn === "Y") "1" else "0"
        result.push(copy)
    }
    return result
}
