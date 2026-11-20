package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.math.floor
import kotlin.math.pow

private const val dayMs = 86_400_000.0
private const val maxMindLimit = 45 * dayMs
private const val vpnUrl = "https://raw.githubusercontent.com/X4BNet/lists_vpn/refs/heads/main/ipv4.txt"
private const val kisaUrl = "https://xn--3e0bx5euxnjje69i70af08bea817g.xn--3e0b707e/jsp/business/management/asList.jsp"

internal fun buildIpDatabase(s: dynamic): Promise<dynamic> = jsPromise {
    val ky = s.http
    val out = "db"
    val stringify: dynamic = js("JSON.stringify")
    val all: dynamic = js("Promise")

    fun readMmdb(edition: String, pick: (dynamic) -> dynamic): Promise<dynamic> = jsPromise {
        val url = "https://github.com/green1052/maxmind-geoip2/raw/master/dist/$edition/$edition.mmdb"
        val bytes = awaitJs(ky.get(url).arrayBuffer())
        val reader = s.reader(bytes)
        val age = Date.now() - (reader.metadata.buildEpoch.getTime() as Double)
        if (age > maxMindLimit) error("${edition}가 ${floor(age / dayMs).toInt()}일 전 데이터입니다. 미러가 갱신되는지 확인하세요.")
        val result = mutableListOf<dynamic>()
        var start = 0.0
        while (start <= 4_294_967_295.0) {
            val lookup = reader.getWithPrefixLength(s.long2ip(start))
            val end = start + 2.0.pow(32 - (lookup[1] as Int)) - 1
            val value = if (truthy(lookup[0])) pick(lookup[0]) else undefinedValue()
            if (value !== undefinedValue()) result.add(props("start" to start, "end" to end, "value" to value))
            start = end + 1
        }
        result.toTypedArray()
    }
    val countries = readMmdb("GeoLite2-Country") { row ->
        row.country?.iso_code ?: row.registered_country?.iso_code ?: undefinedValue()
    }
    val asns = readMmdb("GeoLite2-ASN") { row -> props("asn" to row.autonomous_system_number, "org" to row.autonomous_system_organization) }
    val vpns = jsPromise {
        val raw = awaitJs(ky.get(vpnUrl).text()) as String
        raw.lineSequence().map(String::trim).filter(String::isNotEmpty).map { cidr ->
            val network = s.netmask(cidr)
            val start = network.netLong as Double
            props("start" to start, "end" to (start + (network.size as Double) - 1), "value" to true)
        }.sortedBy { it.start as Double }.toList().toTypedArray()
    }
    val kisa = jsPromise {
        val raw = awaitJs(ky.get(kisaUrl).text()) as String
        val entries = js("new Map()")
        val pattern = Regex("""<td[^>]*>([^<]+)</td>\s*<td[^>]*>AS(\d+)</td>""")
        for (item in pattern.findAll(raw)) entries.set(item.groupValues[2].toDouble(), shortenOrg(item.groupValues[1].trim()))
        entries
    }
    val inputs = awaitJs(all.all(arrayOf<dynamic>(asns, countries, vpns, kisa)))
    val asnRows = inputs[0] as Array<dynamic>
    val countryRows = inputs[1] as Array<dynamic>
    val vpnRows = inputs[2] as Array<dynamic>
    val kisaRows = inputs[3]
    if (asnRows.size < 100_000 || countryRows.size < 100_000) error("MaxMind 데이터가 너무 작습니다.")
    if (vpnRows.size < 10_000) error("VPN 목록이 너무 작습니다: ${vpnRows.size}")
    if ((kisaRows.size as Int) < 500) error("KISA 목록을 읽지 못했습니다: ${kisaRows.size}")

    val candidates = buildIpCandidates(asnRows, countryRows, vpnRows, kisaRows)
    if ((candidates.size as Int) < 40_000) error("대역이 너무 적습니다: ${candidates.size}")
    val version = Date().toISOString().take(16)
    val ip = stringify(setField(encodeIpData(candidates), "version", version)) as String
    awaitJs(s.write("$out/ip.json", ip))
    awaitJs(s.write("$out/version", version))

    val max = jsArray(candidates.values()).maxOf { it.length as Int }
    val base = "prefixes=${candidates.size} maxCandidates=$max kisa=${kisaRows.size} vpnRanges=${vpnRows.size} ip=${ip.length}"
    console.log(base)
    undefinedValue()
}
