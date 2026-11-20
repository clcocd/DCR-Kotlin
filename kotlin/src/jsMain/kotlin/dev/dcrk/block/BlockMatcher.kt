package dev.dcrk.block

import dev.dcrk.*

import kotlin.js.jsTypeOf

private class BlockRegex(val partial: dynamic, val whole: dynamic)
private class BlockIndex(val defaultMode: String, val exact: Map<String, List<dynamic>>, val rest: List<dynamic>)

// 저장소는 수정된 항목 및 목록에 새 객체를 주므로 기존 객체의 캐시는 WeakMap이 정리한다.
private val blockPatterns: dynamic = js("new WeakMap()")
private val ipPatterns: dynamic = js("new WeakMap()")
private val blockIndexes: dynamic = js("new WeakMap()")

@JsName("RegExp")
private external class BrowserRegex(pattern: String) {
    fun test(input: String): Boolean
}

private fun compileBlock(entry: dynamic): BlockRegex? {
    val cached = blockPatterns.get(entry)
    if (jsTypeOf(cached) != "undefined") return cached as BlockRegex?

    val content = entry.content as String
    val compiled = try {
        BlockRegex(BrowserRegex(content), BrowserRegex("^(?:$content)$"))
    } catch (_: Throwable) {
        null
    }
    blockPatterns.set(entry, compiled)
    return compiled
}

private fun ipPattern(entry: dynamic): BrowserRegex {
    val cached = ipPatterns.get(entry)
    if (cached != null) return cached as BrowserRegex

    val content = entry.content as String
    val escaped = js("RegExp.escape")(content) as String
    val head = if (content.firstOrNull() in '0'..'9') "(?<![0-9])" else ""
    val tail = if (content.lastOrNull() in '0'..'9') "(?![0-9])" else ""
    return BrowserRegex(head + escaped + tail).also { ipPatterns.set(entry, it) }
}

private fun matchesBlock(entry: dynamic, mode: String, content: String, type: String): Boolean {
    val whole = mode.endsWith("SAME")
    if (entry.isRegex as Boolean) {
        val regex = compileBlock(entry) ?: return false
        return (if (whole) regex.whole else regex.partial).test(content) as Boolean
    }
    return when {
        whole -> content == entry.content as String
        type == "IP" -> ipPattern(entry).test(content)
        else -> content.contains(entry.content as String)
    }
}

private fun blockIndex(entries: Array<dynamic>, defaultMode: String): BlockIndex {
    val cached = blockIndexes.get(entries) as BlockIndex?
    if (cached?.defaultMode == defaultMode) return cached

    val exact = mutableMapOf<String, MutableList<dynamic>>()
    val rest = mutableListOf<dynamic>()
    for (entry in entries) {
        if (!(entry.isRegex as Boolean) && (entry.mode ?: defaultMode) == "SAME") {
            exact.getOrPut(entry.content as String) { mutableListOf() }.add(entry)
        } else {
            rest.add(entry)
        }
    }
    return BlockIndex(defaultMode, exact, rest).also { blockIndexes.set(entries, it) }
}

private fun inGallery(entry: dynamic, gallery: String?): Boolean {
    val scope = entry.gallery as String?
    return scope.isNullOrEmpty() || scope == gallery
}

/** 항목과 허용 목록을 스캔하고 옵션 화면에 필요한 판정 근거를 콜백으로 전달한다. */
fun scanBlocks(
    entries: Array<dynamic>,
    defaultMode: String,
    type: String,
    content: String,
    gallery: String?,
    onEntry: ((dynamic, String) -> Unit)?,
): Boolean {
    val index = blockIndex(entries, defaultMode)
    var blocked = false
    var hasAllowList = false
    var allowed = false

    for (entry in index.exact[content].orEmpty()) {
        if (!inGallery(entry, gallery)) continue
        blocked = true
        onEntry?.invoke(entry, "hit")
    }

    for (entry in index.rest) {
        if (!inGallery(entry, gallery)) continue
        val mode = (entry.mode as String?) ?: defaultMode
        if (!mode.startsWith("NOT_")) {
            if (!matchesBlock(entry, mode, content, type)) continue
            blocked = true
            onEntry?.invoke(entry, "hit")
        } else if (!(entry.isRegex as Boolean) || compileBlock(entry) != null) {
            hasAllowList = true
            val hit = matchesBlock(entry, mode, content, type)
            allowed = allowed || hit
            onEntry?.invoke(entry, if (hit) "allow" else "miss")
        }
    }

    return blocked || (hasAllowList && !allowed)
}
