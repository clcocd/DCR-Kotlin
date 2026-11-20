package dev.dcrk

import kotlin.js.jsTypeOf

internal fun ipCandidateKey(candidate: dynamic): String =
    "${candidate.org ?: ""}\u0000${candidate.country ?: ""}\u0000${if (candidate.vpn == true) 1 else 0}"

internal fun encodeIpData(prefixes: dynamic): dynamic {
    data class Frequency(val candidate: dynamic, var count: Int = 1)
    val observed = mutableMapOf<String, Frequency>()
    val entries = jsArray(prefixes)
    for (entry in entries) for (candidate in entry[1] as Array<dynamic>) {
        val key = ipCandidateKey(candidate)
        observed[key]?.let { it.count++ } ?: run { observed[key] = Frequency(candidate) }
    }
    val ordered = observed.values.sortedByDescending { it.count }.map { it.candidate }
    val metaIndex = ordered.withIndex().associate { ipCandidateKey(it.value) to it.index }
    val organizations = mutableMapOf<String, Int>()
    val nations = mutableMapOf<String, Int>()
    val meta = ordered.flatMap { candidate ->
        val org = (candidate.org ?: "") as String
        val country = (candidate.country ?: "") as String
        listOf(organizations.getOrPut(org) { organizations.size }, nations.getOrPut(country) { nations.size }, if (candidate.vpn == true) 1 else 0)
    }
    val lists = mutableListOf<Array<Int>>()
    val idByList = mutableMapOf<String, Int>()
    val table = js("new Uint16Array(65536)")
    for (entry in entries) {
        val candidates = entry[1] as Array<dynamic>
        if (candidates.isEmpty()) continue
        val indexes = candidates.map { metaIndex.getValue(ipCandidateKey(it)) }
        val value = if (indexes.size == 1) indexes[0] else {
            val id = idByList.getOrPut(indexes.joinToString(",")) {
                lists.add(indexes.toTypedArray())
                lists.lastIndex
            }
            ordered.size + id
        }
        if (value + 1 > 0xffff) error("IP 데이터가 Uint16 표 범위를 넘습니다.")
        table[entry[0]] = value + 1
    }
    val starts = mutableListOf<Int>()
    val values = mutableListOf<Int>()
    for (slot in 0 until 65536) {
        if (slot == 0 || table[slot] != table[slot - 1]) {
            starts.add(slot)
            values.add(table[slot] as Int)
        }
    }
    val Uint16: dynamic = js("Uint16Array")
    val encoded = Uint16.from((starts + values).toTypedArray())
    val bytes = js("new Uint8Array(encoded.buffer)")
    return props("runs" to bytes.toBase64(), "orgs" to organizations.keys.toTypedArray(),
        "countries" to nations.keys.toTypedArray(), "meta" to meta.toTypedArray(), "lists" to lists.toTypedArray())
}

internal fun parseStoredIpData(text: String): dynamic = js("JSON.parse")(text)

internal fun parseIpData(text: String): dynamic {
    if (text.isEmpty()) return null
    val data = parseStoredIpData(text)
    if (!record(data) || jsTypeOf(data.runs) != "string" ||
        (data.version !== undefinedValue() && jsTypeOf(data.version) != "string")) return null
    fun arrayOfType(value: dynamic, type: String) = js("Array.isArray")(value) == true &&
        (value as Array<dynamic>).all { jsTypeOf(it) == type }
    if (!arrayOfType(data.orgs, "string") || !arrayOfType(data.countries, "string") ||
        !arrayOfType(data.meta, "number") || (data.meta.length as Int) % 3 != 0 || js("Array.isArray")(data.lists) != true) return null
    val count = (data.meta.length as Int) / 3
    val isInteger: dynamic = js("Number.isInteger")
    fun inRange(value: dynamic, size: Int): Boolean = isInteger(value) == true && (value as Int) in 0 until size
    for (index in 0 until count) {
        if (!inRange(data.meta[index * 3], data.orgs.length as Int) ||
            !inRange(data.meta[index * 3 + 1], data.countries.length as Int)) return null
    }
    for (list in data.lists as Array<dynamic>) {
        if (!arrayOfType(list, "number") || (list as Array<dynamic>).any { !inRange(it, count) }) return null
    }
    return data
}

internal fun createIpLookup(data: dynamic): (String) -> dynamic {
    val bytes = js("Uint8Array.fromBase64(data.runs)")
    val runs = js("new Uint16Array(bytes.buffer)")
    val length = runs.length as Int
    if (length % 2 != 0) error("IP 데이터 형식이 올바르지 않습니다.")
    val count = length / 2
    val total = (data.meta.length as Int) / 3 + (data.lists.length as Int)
    val table = js("new Uint16Array(65536)")
    for (run in 0 until count) {
        val start = runs[run] as Int
        val value = runs[count + run] as Int
        if ((run > 0 && start <= (runs[run - 1] as Int)) || value - 1 >= total) error("IP 데이터 형식이 올바르지 않습니다.")
        table.fill(value, start, if (run + 1 < count) runs[run + 1] else 65536)
    }

    val orgs = data.orgs
    val countries = data.countries
    val packed = js("Uint16Array.from(data.meta)")
    val metaCount = (packed.length as Int) / 3
    val lists = data.lists as Array<dynamic>
    val offsets = IntArray(lists.size + 1)
    for ((index, list) in lists.withIndex()) offsets[index + 1] = offsets[index] + (list.length as Int)
    val totalLength = offsets.last()
    val flattened = js("new Uint16Array(totalLength)")
    for ((index, list) in lists.withIndex()) flattened.set(list, offsets[index])
    val number: dynamic = js("Number")

    fun candidate(index: Int): dynamic {
        val org = orgs[packed[index * 3]]
        val country = countries[packed[index * 3 + 1]]
        return props("org" to if (truthy(org)) org else undefinedValue(), "country" to if (truthy(country)) country else undefinedValue(),
            "vpn" to (packed[index * 3 + 2] == 1))
    }
    return find@{ ip ->
        val parts = ip.split('.', limit = 3)
        if (parts.size < 2) return@find undefinedValue()
        val a = number(parts[0]) as Double
        val b = number(parts[1]) as Double
        if (!(a >= 0 && a < 256 && b >= 0 && b < 256)) return@find undefinedValue()
        val value = table[a * 256 + b]
        if (!truthy(value)) return@find undefinedValue()
        val index = (value as Int) - 1
        if (index < metaCount) arrayOf<dynamic>(candidate(index))
        else {
            val list = index - metaCount
            (offsets[list] until offsets[list + 1]).map { candidate(flattened[it] as Int) }.toTypedArray()
        }
    }
}
