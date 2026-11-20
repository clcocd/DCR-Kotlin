package dev.dcrk

import kotlin.math.max
import kotlin.math.min

private const val CACHE_AGE = 60_000
private const val SEEN_LIMIT = 2000

fun previewPostKey(pre: dynamic): String {
    val kind = galleryKind(pre.link as String)
    val prefix = if (kind == "normal") "" else "$kind/"
    return "$prefix${pre.gallery}:${pre.id}"
}

/** QuickLRU가 제공하는 만료와 교체를 사용하고, 글별로 본 댓글을 제한된 크기로 보존한다. */
fun createPreviewCache(createEntries: (Int, Int) -> dynamic): dynamic {
    val entries = createEntries(25, CACHE_AGE)
    var sweep: dynamic = undefinedValue()
    fun get(pre: dynamic): dynamic = entries.get(previewPostKey(pre))
    fun set(pre: dynamic, patch: dynamic) {
        val key = previewPostKey(pre)
        entries.set(key, js("Object.assign")(props(), entries.get(key), patch))
        val clear: dynamic = js("clearTimeout")
        val timeout: dynamic = js("setTimeout")
        clear(sweep)
        sweep = timeout({ jsArray(entries) }, CACHE_AGE + 1_000)
    }

    fun restore(pre: dynamic, list: Array<dynamic>, truncated: Boolean?): Array<dynamic> {
        val seen = get(pre)?.seen ?: props()
        val current: dynamic = js("new Set()")
        val number: dynamic = js("Number")
        fun numberOf(value: dynamic): Double = number(value) as Double
        fun thread(comment: dynamic): Double {
            val parent = numberOf(comment.c_no)
            return if (parent == 0.0 || parent.isNaN()) numberOf(comment.no) else parent
        }
        fun deleted(comment: dynamic): dynamic = js("Object.assign")(props(), comment, props("is_delete" to "1"))
        fun numericOrder(a: dynamic, b: dynamic): Int {
            val difference = numberOf(a) - numberOf(b)
            return if (difference < 0) -1 else if (difference > 0) 1 else 0
        }

        var cutoff = if (truncated == true && list.isNotEmpty()) Double.POSITIVE_INFINITY else 0.0
        for (comment in list) {
            current.add(comment.no)
            if (truncated == true) cutoff = min(cutoff, thread(comment))
        }
        val removed = jsValues(seen).filter { current.has(it.no) == false && thread(it) > cutoff }.map(::deleted)
        val nextSeen = js("Object.assign")(props(), seen)
        val output = list.map { comment ->
            val before = seen[comment.no]
            if (comment.is_delete != "0" && before?.is_delete == "0") deleted(before)
            else {
                nextSeen[comment.no] = comment
                comment
            }
        }

        val nos = js("Object.keys")(nextSeen) as Array<String>
        val ordered = nos.sortedWith { left, right ->
            val a = if (current.has(left) == true) 1 else 0
            val b = if (current.has(right) == true) 1 else 0
            if (a != b) a - b else numericOrder(left, right)
        }
        for (key in ordered.take(max(0, nos.size - SEEN_LIMIT))) js("Reflect.deleteProperty")(nextSeen, key)
        set(pre, props("seen" to nextSeen))

        return if (removed.isEmpty()) output.toTypedArray() else
            (output + removed).sortedWith { a, b -> numericOrder(a.no, b.no) }.toTypedArray()
    }

    return props("get" to ::get, "set" to ::set, "restoreArchive" to ::restore)
}
