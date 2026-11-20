package dev.dcrk

internal fun createBlockRules(s: dynamic): dynamic {
    fun scan(lists: dynamic, type: String, text: String, gallery: dynamic, report: ((dynamic, String) -> Unit)? = null): Boolean =
        scanBlocks(lists.entries[type], lists.defaults[type] as String, type, text, gallery as String?, report)
    fun dcconCode(element: dynamic): dynamic {
        val media = if (truthy(element.getAttribute("src"))) element else element.querySelector("img, video, source") ?: element
        val source = media.getAttribute("src")
        val src = if (truthy(source)) source else media.getAttribute("data-src")
        if (!truthy(src)) return undefinedValue()
        val url = js("URL.parse(src, location.href)")
        val code = url?.searchParams?.get("no")
        return if (truthy(code)) code else undefinedValue()
    }
    fun mode(settings: dynamic): String = if (settings.blur == true) "blur" else "hide"
    fun hidden(element: dynamic): Boolean {
        val blocked = element.closest("[data-blocked]") ?: return false
        if (element.closest("[data-block-revealed], [data-comments-revealed]") != null) return false
        return !(blocked.getAttribute("data-blocked") == "blur" && element.closest("[data-blur-reveal]") != null)
    }
    fun isBlocked(type: String, text: String, gallery: dynamic): Boolean =
        text.isNotEmpty() && scan(s.getLists(), type, text, gallery)
    fun anyBlocked(values: dynamic, gallery: dynamic): Boolean =
        entriesOf(values).any { truthy(it[1]) && isBlocked(it[0] as String, it[1] as String, gallery) }
    fun duplicate(settings: dynamic): dynamic = if (settings.foldDuplicate == true)
        props("count" to settings.duplicateCount, "minLength" to settings.duplicateMinLength) else null
    fun group(items: Array<dynamic>, textOf: (dynamic) -> String, config: dynamic): dynamic {
        val whitespace: dynamic = js("/\\s+/g")
        val groups = items.groupBy { textOf(it).asDynamic().replace(whitespace, " ").trim() as String }
        val result = js("new Map()")
        for ((text, items) in groups) {
            if (text.length < (config.minLength as Int) || items.size < (config.count as Int)) continue
            for ((index, item) in items.withIndex()) result.set(item, if (index == 0) items.size else 0)
        }
        return result
    }
    fun blocking(values: dynamic, gallery: dynamic, input: dynamic): Array<dynamic> {
        val lists = input ?: s.getLists()
        val result = mutableListOf<dynamic>()
        for (item in entriesOf(values)) {
            val type = item[0] as String
            val value = item[1]
            if (!truthy(value)) continue
            val hits = js("new Set()")
            val allowed = mutableListOf<dynamic>()
            var matchedAllow = false
            val report: (dynamic, String) -> Unit = { entry, kind ->
                if (kind == "hit") hits.add(entry) else allowed.add(entry)
                if (kind == "allow") matchedAllow = true
            }
            scan(lists, type, value as String, gallery, report)
            for (entry in lists.entries[type] as Array<dynamic>) {
                if (hits.has(entry) == true) result.add(props("type" to type, "entry" to entry))
            }
            if (!matchedAllow) for (entry in allowed) result.add(props("type" to type, "entry" to entry))
        }
        return result.toTypedArray()
    }
    return props("dcconCode" to ::dcconCode, "blockMode" to ::mode, "isBlockedHidden" to ::hidden, "isBlocked" to ::isBlocked,
        "isAnyBlocked" to ::anyBlocked, "duplicateOf" to ::duplicate, "groupDuplicates" to ::group, "blockingEntries" to ::blocking)
}
