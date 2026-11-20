package dev.dcrk

private const val TITLE_COUNT_ATTRIBUTE = "data-refresher-title-count"
private val titleCountPrefix = Regex("^\\(\\d+\\) ")

/** 문서가 직접 붙인 카운트 접두사만 바꾼다. 원래 제목의 '(1)'은 보존한다. */
fun titleCountText(title: String, titleCounted: Boolean, count: Int): String {
    val original = if (titleCounted) title.replaceFirst(titleCountPrefix, "") else title
    return if (count > 0) "($count) $original" else original
}

fun setTitleCount(count: Int) {
    val doc: dynamic = js("document")
    val root = doc.documentElement
    val counted = root.hasAttribute(TITLE_COUNT_ATTRIBUTE) as Boolean
    if (count == 0 && !counted) return
    val next = titleCountText(doc.title as String, counted, count)
    root.toggleAttribute(TITLE_COUNT_ATTRIBUTE, count > 0)
    if (doc.title != next) doc.title = next
}

/** 탭을 떠난 뒤 추가된 행을 세고, 돌아오면 읽음 위치를 표시한다. */
fun createUnseenCounter(ctx: dynamic, listSelector: String, noticeIcon: String, blurredRows: String, rowPostNo: (dynamic) -> String?): dynamic {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val array: dynamic = js("Array")
    var unseen = 0
    var away = false
    var leftAt: String? = null
    var hiddenAway = false
    var returnedFrom: String? = null
    var readMark: String? = null

    fun listRows(): Array<dynamic> = array.from(doc.querySelectorAll("$listSelector > tr")) as Array<dynamic>
    fun isWatching(): Boolean = doc.hidden == false && (doc.hasFocus() as Boolean)
    fun topPostNo(): String? {
        for (row in listRows()) {
            val no = rowPostNo(row)
            if (!no.isNullOrEmpty() && row.querySelector(noticeIcon) == null && (row.checkVisibility() as Boolean)) return no
        }
        return null
    }

    fun showMark() {
        for (row in listRows()) {
            row.classList.toggle("refresherReadMark", readMark != null && rowPostNo(row) == readMark)
        }
    }

    fun count(rows: Array<dynamic>) {
        val mark = returnedFrom
        returnedFrom = null
        if (rows.isEmpty()) return
        window.setTimeout({
            if (ctx.signal.aborted == false && (ctx.settings.titleCount == true || ctx.settings.readMark == true)) {
                val added = rows.count { row ->
                    row.isConnected == true && row.checkVisibility() == true && row.closest(blurredRows) == null
                }
                if (!isWatching()) {
                    unseen += added
                    if (ctx.settings.titleCount == true) setTitleCount(unseen)
                } else if (ctx.settings.readMark == true && mark != null && added > 0) {
                    readMark = mark
                    showMark()
                }
            }
        })
    }

    fun clearTitle() = setTitleCount(0)
    fun clearMark() {
        readMark = null
        returnedFrom = null
        showMark()
    }

    fun onFocusChange() {
        if (!isWatching()) {
            if (!away) leftAt = topPostNo()
            away = true
            hiddenAway = hiddenAway || doc.hidden == true
            return
        }
        if (away) readMark = if (ctx.settings.readMark == true && unseen > 0) leftAt else null
        if (hiddenAway) returnedFrom = leftAt
        away = false
        hiddenAway = false
        unseen = 0
        clearTitle()
        showMark()
    }

    val options: dynamic = js("({})")
    options.signal = ctx.signal
    val listener: (dynamic) -> Unit = { onFocusChange() }
    window.addEventListener("focus", listener, options)
    window.addEventListener("blur", listener, options)
    doc.addEventListener("visibilitychange", listener, options)
    ctx.onRevoke {
        clearTitle()
        clearMark()
    }

    val counter: dynamic = js("({})")
    counter.count = ::count
    counter.clearTitle = ::clearTitle
    counter.clearMark = ::clearMark
    counter.showMark = ::showMark
    return counter
}
