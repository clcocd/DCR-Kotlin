package dev.dcrk

import kotlin.math.max
import kotlin.math.min

internal const val COUNT_CELLS = ".gall_count, .gall_recommend, .reply_num"
internal val refreshCountText: dynamic = js("""/(class="(?:gall_count|gall_recommend|reply_num)"[^>]*>)[^<]*/g""")
private val rowFrames: dynamic = js("new WeakMap()")

fun clearNewPost(row: dynamic) {
    row.classList.remove("refresherNewPost")
    row.style.removeProperty("animation-delay")
}

private fun elements(root: dynamic, selector: String): Array<dynamic> =
    js("Array.from")(root.querySelectorAll(selector)) as Array<dynamic>

/** 번호 없는 설문·광고 등은 번호 칸의 텍스트로 구분한다. */
private fun rowKey(row: dynamic, rowPostNo: (dynamic) -> String?): String =
    rowPostNo(row) ?: row.querySelector(".gall_num")?.textContent as String? ?: ""

private fun syncCounts(target: dynamic, source: dynamic) {
    val sources = source.querySelectorAll(COUNT_CELLS)
    for ((index, cell) in elements(target, COUNT_CELLS).withIndex()) {
        val text = sources[index]?.textContent as String? ?: ""
        if (cell.textContent != text) cell.textContent = text
    }
}

private fun writerKey(row: dynamic): String? {
    val writer = row.querySelector(".ub-writer") ?: return null
    val data = writer.dataset
    return "${data.nick}|${data.uid}|${data.ip}"
}

/** 제목, 작성자, 수 칸 개수가 같은 처음 그린 행은 받아온 행의 틀을 물려받는다. */
private fun sameRow(a: dynamic, b: dynamic): Boolean =
    a.querySelector(".gall_tit > a")?.textContent === b.querySelector(".gall_tit > a")?.textContent &&
        writerKey(a) == writerKey(b) &&
        a.querySelectorAll(COUNT_CELLS).length == b.querySelectorAll(COUNT_CELLS).length

/** 새 행에 밀려난 맨 아래 글을 제외하고 빠진 글을 원래 위치에 보존한다. */
private fun keepDeletedRows(
    oldRows: Array<dynamic>,
    newRows: Array<dynamic>,
    newKeys: List<String>,
    newList: dynamic,
    newPostCount: Int,
    first: dynamic,
    helpers: dynamic,
) {
    val received = newList.children.length as Int
    val rowPostNo: (dynamic) -> String? = helpers.rowPostNo
    val newByKey = newRows.associateBy { rowKey(it, rowPostNo) }
    val present = newKeys.toSet()
    var next = first

    for (index in oldRows.indices.reversed()) {
        val row = oldRows[index]
        val no = rowPostNo(row)
        if (no.isNullOrEmpty() || row.querySelector(helpers.noticeIcon) != null ||
            no in present || index >= oldRows.size - newPostCount
        ) {
            next = newByKey[rowKey(row, rowPostNo)] ?: next
            continue
        }

        clearNewPost(row)
        row.classList.add("refresherDeleted")
        val copy = row.cloneNode(true)
        if (rowFrames.has(row) as Boolean) rowFrames.set(copy, rowFrames.get(row))
        newList.insertBefore(copy, next)
        next = copy
    }

    while ((newList.children.length as Int) > max(oldRows.size, received)) {
        newList.lastElementChild.remove()
    }
}

private fun listInsertion(oldKeys: List<String>, newKeys: List<String>): Pair<Int, Int>? {
    var at = 0
    while (at < oldKeys.size && at < newKeys.size && oldKeys[at] == newKeys[at]) at++
    val count = if (at == oldKeys.size || at == newKeys.size) {
        max(newKeys.size - oldKeys.size, 0)
    } else {
        val index = (at + 1 until newKeys.size).firstOrNull { newKeys[it] == oldKeys[at] } ?: return null
        index - at
    }
    for (index in at + count until newKeys.size) {
        if (newKeys[index] != oldKeys.getOrNull(index - count)) return null
    }
    return at to count
}

private fun updateRows(
    oldList: dynamic,
    oldRows: Array<dynamic>,
    rows: Array<dynamic>,
    at: Int,
    count: Int,
): Boolean {
    val kept = rows.size - count
    for (index in kept until oldRows.size) oldRows[index].remove()
    val anchor = if (at < kept) oldRows[at] else null
    for (index in at until at + count) oldList.insertBefore(rows[index], anchor)

    var changed = count > 0 || kept < oldRows.size
    for ((index, row) in rows.withIndex()) {
        if (index in at until at + count) continue
        val old = oldRows[if (index < at) index else index - count]
        if (!(rowFrames.has(old) as Boolean) && sameRow(old, row)) {
            rowFrames.set(old, rowFrames.get(row) ?: -1)
        }
        if (rowFrames.get(old) === rowFrames.get(row)) {
            old.classList.toggle("refresherDeleted", row.classList.contains("refresherDeleted"))
            syncCounts(old, row)
        } else {
            old.replaceWith(row)
            changed = true
        }
    }
    return changed
}

/** 새 목록에서 바뀐 행을 갱신한다. 틀이 같은 행의 hover와 리스너는 유지한다. */
fun replaceRefreshList(oldList: dynamic, newList: dynamic, options: dynamic, helpers: dynamic): dynamic {
    val rowPostNo: (dynamic) -> String? = helpers.rowPostNo
    val oldRows = elements(oldList, ":scope > tr")
    val newRows = elements(newList, ":scope > tr")
    val oldKeys = oldRows.map { rowKey(it, rowPostNo) }
    val newKeys = newRows.map { rowKey(it, rowPostNo) }
    val oldSet = oldKeys.toSet()
    val lastKept = newKeys.indexOfLast { it in oldSet }
    val newPosts = mutableListOf<dynamic>()

    val commentSearch = options.searchType == "search_comment"
    val fillCheckbox = helpers.checkboxFiller(oldList, commentSearch)
    for ((index, row) in newRows.withIndex()) {
        val no = newKeys[index]
        val html = row.outerHTML.replace(refreshCountText, "$1") as String
        rowFrames.set(row, hash53(html))
        fillCheckbox(row)
        for (img in elements(row, "img")) img.loading = "lazy"

        if (helpers.isViewPage == true && no == helpers.pagePostNo) {
            row.classList.add("crt")
            val number = row.querySelector(".gall_num")
            if (number != null) number.innerHTML = """<span class="sp_img crt_icon"> </span>"""
        } else if (no !in oldSet && (lastKept == -1 || index < lastKept)) {
            newPosts.add(row)
        }
    }

    val search = options.search as String?
    if (search != null) helpers.highlightSearchResults(newList, search, commentSearch)

    val navigated = options.navigated as Boolean
    if (!navigated && options.fadeIn == true) {
        for ((index, row) in newPosts.withIndex()) {
            row.classList.add("refresherNewPost")
            row.style.animationDelay = "${min(newPosts.size - index, 10) * 50}ms"
        }
    }

    val keepDeleted = options.keepDeleted as Boolean
    if (keepDeleted && !navigated && search == null) {
        keepDeletedRows(oldRows, newRows, newKeys, newList, newPosts.size, newRows.getOrNull(lastKept + 1), helpers)
    }

    val rows = if (keepDeleted) elements(newList, ":scope > tr") else newRows
    val shift = if (!navigated && search == null) listInsertion(oldKeys, rows.map { rowKey(it, rowPostNo) }) else null
    val result: dynamic = js("({})")
    result.added = newPosts.toTypedArray()
    result.changed = if (shift == null) {
        oldList.replaceWith(newList)
        true
    } else {
        updateRows(oldList, oldRows, rows, shift.first, shift.second)
    }
    return result
}

/** 같은 페이징 링크는 교체하지 않아 클릭 중인 요소를 유지한다. */
fun syncRefreshPaging(incoming: dynamic, selector: String) {
    val doc: dynamic = js("document")
    val paging = incoming.querySelector(selector)
    val current = doc.querySelector(selector)
    if (paging != null && current != null && paging.innerHTML != current.innerHTML) {
        current.innerHTML = paging.innerHTML
    }
}

fun isWholeFirstPage(url: String, writerFilter: (String) -> Any?): Boolean {
    val params: dynamic = BrowserUrl(url).asDynamic().searchParams
    return (params.get("page") ?: "1") == "1" && params.has("exception_mode") == false &&
        params.has("search_head") == false && writerFilter(url) == null
}
