package dev.dcrk

import kotlin.js.jsTypeOf
import kotlin.math.max
import kotlin.math.min

fun blockLabel(entry: dynamic): String = (if (truthy(entry.extra)) entry.extra else entry.content) as String

fun emptyBlockEntries(): dynamic = props(
    "COMMENT" to emptyArray<dynamic>(), "DCCON" to emptyArray<dynamic>(), "ID" to emptyArray<dynamic>(), "IP" to emptyArray<dynamic>(),
    "NICK" to emptyArray<dynamic>(), "TAB" to emptyArray<dynamic>(), "TEXT" to emptyArray<dynamic>(), "TITLE" to emptyArray<dynamic>(),
)

fun createBlockState(set: dynamic, get: () -> dynamic, s: dynamic): dynamic {
    fun update(type: String, change: (dynamic) -> dynamic): dynamic = s.lists().update(type, change)
    fun key(entry: dynamic): String = blockKey(entry.content as String, entry.gallery as String?)

    fun addEntries(type: String, list: Array<dynamic>): dynamic {
        val added = linkedMapOf<String, dynamic>()
        for (entry in list) added[key(entry)] = setField(entry, "id", js("crypto.randomUUID()"))
        return update(type) { current ->
            val rows = current as Array<dynamic>
            val kept = rows.associate { key(it) to it.id }
            val previous = rows.filterNot { added.containsKey(key(it)) }
            val appended = added.map { (key, entry) -> setField(entry, "id", kept[key] ?: entry.id) }
            (previous + appended).toTypedArray()
        }
    }

    fun updateEntry(type: String, id: String, entry: dynamic): dynamic {
        val current = get().entries[type] as Array<dynamic>
        if (current.none { it.id === id }) return get().addEntries(type, arrayOf<dynamic>(entry))
        val valueKey = key(entry)
        return update(type) { value ->
            val rows = value as Array<dynamic>
            if (rows.none { it.id === id }) rows
            else rows.filter { it.id === id || key(it) != valueKey }.map {
                if (it.id !== id) it else js("Object.assign")(props(), it, entry, props("id" to id))
            }.toTypedArray()
        }
    }

    fun setDefault(type: String, mode: String): dynamic {
        val apply: (dynamic) -> dynamic = { state -> props("defaults" to setField(state.defaults, type, mode)) }
        set(apply)
        return s.saveOrReload(s.defaultsStorage().setValue(get().defaults), s.lists().load, "차단 목록을 저장하지 못했습니다.")
    }

    return props(
        "entries" to emptyBlockEntries(), "defaults" to normalizeDefaults(null), "updateEntries" to ::update,
        "addEntry" to { type: String, entry: dynamic -> get().addEntries(type, arrayOf<dynamic>(entry)) },
        "addEntries" to ::addEntries, "updateEntry" to ::updateEntry,
        "removeEntry" to { type: String, id: String ->
            update(type) { value -> (value as Array<dynamic>).filterNot { it.id === id }.toTypedArray() }
        },
        "setDefault" to ::setDefault,
    )
}

fun randomMemoColor(): String {
    val hue = (js("Math.random()") as Double) * 360
    fun channel(n: Int): String {
        val k = (n + hue / 30) % 12
        val value = 0.5 - 0.3 * max(-1.0, min(min(k - 3, 9 - k), 1.0))
        val color = js("Math.round")(value * 255) as Int
        return color.toString(16).padStart(2, '0')
    }
    return "#${channel(0)}${channel(8)}${channel(4)}"
}

fun normalizeMemoMap(value: dynamic): dynamic {
    if (!record(value)) return props()
    return fromEntries(entriesOf(value).filter { row ->
        val item = row[1]
        record(item) && jsTypeOf(item.text) == "string" && jsTypeOf(item.color) == "string" &&
            (jsTypeOf(item.gallery) in setOf("undefined", "string")) &&
            (jsTypeOf(item.added) in setOf("undefined", "number"))
    }.toTypedArray())
}

fun ownMemo(map: dynamic, key: String): dynamic =
    if (js("Object.hasOwn")(map, key) == true) map[key] else undefinedValue()

fun lookupMemo(memos: dynamic, user: dynamic, gallery: String?): dynamic {
    val candidates = listOf("UID" to user.uid, "IP" to user.ip, "NICK" to user.nick)
    for ((type, userKey) in candidates) {
        if (!truthy(userKey)) continue
        val entry = ownMemo(memos[type], userKey as String)
        if (entry != null && (!truthy(entry.gallery) || entry.gallery === gallery)) return entry
    }
    return undefinedValue()
}

fun createMemoState(lists: () -> dynamic): dynamic {
    fun update(type: String, change: (dynamic) -> dynamic): dynamic = lists().update(type, change)

    fun setMemo(type: String, user: String, entry: dynamic): dynamic {
        val now: Double = js("Date.now()") as Double
        return update(type) { current ->
            val existing = ownMemo(current, user)
            val added = if (existing == null) now else existing.added
            setField(current, user, if (jsTypeOf(added) == "undefined") entry else setField(entry, "added", added))
        }
    }

    fun removeMemo(type: String, user: String): dynamic = update(type) { current ->
        fromEntries(entriesOf(current).filterNot { it[0] === user }.toTypedArray())
    }

    return props("memos" to props("UID" to props(), "NICK" to props(), "IP" to props()),
        "updateMemos" to ::update, "setMemo" to ::setMemo, "removeMemo" to ::removeMemo)
}
