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
