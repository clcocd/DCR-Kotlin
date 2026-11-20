package dev.dcrk

import kotlin.js.jsTypeOf
import kotlin.math.floor
import kotlin.math.min

private const val PREFIX_SIZE = 65536.0
private data class Match(val value: dynamic, val next: Double)
private data class Count(val candidate: dynamic, var addresses: Double)

private fun cursor(ranges: Array<dynamic>): (Double) -> Match {
    var index = 0
    return { at ->
        while (index < ranges.size && (ranges[index].end as Double) < at) index++
        val range = ranges.getOrNull(index)
        when {
            range == null -> Match(null, Double.POSITIVE_INFINITY)
            (range.start as Double) <= at -> Match(range.value, (range.end as Double) + 1)
            else -> Match(null, range.start as Double)
        }
    }
}

internal fun buildIpCandidates(asns: Array<dynamic>, countries: Array<dynamic>, vpns: Array<dynamic>, kisa: dynamic): dynamic {
    val asnAt = cursor(asns)
    val countryAt = cursor(countries)
    val vpnAt = cursor(vpns)
    val region = js("new Intl.DisplayNames(['ko'], {type: 'region'})")
    val overrides = mapOf("HK" to "홍콩", "MO" to "마카오", "AU" to "호주")
    fun candidate(asn: dynamic, iso: String?, vpn: Boolean): dynamic {
        if (iso == "KR") {
            val org = if (asn != null) kisa.get(asn.asn) ?: asn.org else null
            return if (truthy(org)) props("org" to org, "vpn" to vpn) else null
        }
        if (iso.isNullOrEmpty()) {
            val org = asn?.org
            return if (vpn && truthy(org)) props("org" to org, "vpn" to true) else null
        }
        val name = overrides[iso] ?: region.of(iso) ?: iso
        val value = props("country" to name, "vpn" to vpn)
        if (iso == "JP" || iso == "CN" || vpn) value.org = asn?.org ?: undefinedValue()
        return value
    }

    val counts = linkedMapOf<Int, MutableMap<String, Count>>()
    var at = 0.0
    while (at <= 4_294_967_295.0) {
        val asn = asnAt(at)
        val country = countryAt(at)
        val vpn = vpnAt(at)
        val prefix = floor(at / PREFIX_SIZE).toInt()
        val end = min(min(asn.next, country.next), min(vpn.next, (prefix + 1) * PREFIX_SIZE)) - 1
        val key = candidate(asn.value, country.value as String?, vpn.value === true)
        if (key != null) {
            val byCandidate = counts.getOrPut(prefix) { linkedMapOf() }
            val count = byCandidate.getOrPut(ipCandidateKey(key)) { Count(key, 0.0) }
            count.addresses += end - at + 1
        }
        at = end + 1
    }
    val result = js("new Map()")
    for ((prefix, seen) in counts) {
        val best = seen.values.sortedByDescending { it.addresses }.filterIndexed { index, value ->
            index == 0 || value.addresses >= PREFIX_SIZE / 100
        }.take(8).map { it.candidate }.toTypedArray()
        result.set(prefix, best)
    }
    return result
}

internal fun normalizeBans(data: dynamic): dynamic {
    if (!record(data)) error("ban.json은 갤러리 → 아이디 목록 객체여야 합니다.")
    val values = ownKeys(data).sorted().map { gallery ->
        val uids = data[gallery]
        if (js("Array.isArray")(uids) != true || (uids as Array<dynamic>).any { jsTypeOf(it) != "string" }) {
            error("ban.json의 \"$gallery\"가 문자열 목록이 아닙니다.")
        }
        val sorted = (uids as Array<String>).map(String::trim).filter(String::isNotEmpty).distinct().sorted().toTypedArray()
        arrayOf<dynamic>(gallery, sorted)
    }
    return fromEntries(values.toTypedArray())
}
