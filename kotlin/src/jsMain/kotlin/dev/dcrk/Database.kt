package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf

private class Database(private val s: dynamic) {
    private val storage = s.storage
    private val format = s.format
    private val console: dynamic = js("console")
    private val watching = js("new AbortController()")
    private val versionStore = s.versionStore
    private var lookup: dynamic = null
    private var findBan: ((String) -> dynamic)? = null
    private var bansRequested = false
    private var banVersion = 0
    private var loadedStamp = ""

    private fun stamp(meta: dynamic) = "${meta?.version ?: ""}\n${meta?.format ?: ""}"
    private fun storageValue(key: String): dynamic = storage.getItem(key, props("fallback" to ""))
    private fun get(url: dynamic): dynamic = s.http.get(url, props("retry" to 0, "cache" to "no-cache", "totalTimeout" to false)).text()
    private fun loadIp(stored: String, savedFormat: dynamic) {
        lookup = null
        try {
            if (stored.isNotEmpty() && savedFormat == format) lookup = s.createIpLookup(s.parseStoredIpData(stored))
        } catch (error: Throwable) {
            console.error("IP DB를 읽지 못했습니다.", error)
        }
        bump()
    }

    private fun bump() {
        val next: (Int) -> Int = { it + 1 }
        versionStore.setState(next)
    }

    private fun indexBans(json: dynamic): (String) -> dynamic {
        val reasonsOf = mutableMapOf<String, String>()
        if (record(json)) for (entry in entriesOf(json)) {
            val reason = entry[0] as String
            val uids = entry[1]
            if (js("Array.isArray")(uids) != true) continue
            for (uid in uids as Array<dynamic>) {
                if (jsTypeOf(uid) != "string") continue
                val account = uid as String
                val previous = reasonsOf[account]
                reasonsOf[account] = if (previous == null) reason else "$previous, $reason"
            }
        }
        val uids = reasonsOf.keys.sorted()
        val reasons = reasonsOf.values.distinct().toTypedArray()
        val index = reasons.withIndex().associate { it.value to it.index }
        val joined = uids.joinToString("")
        val starts = IntArray(uids.size + 1)
        val reasonOf = IntArray(uids.size)
        for ((i, uid) in uids.withIndex()) {
            starts[i + 1] = starts[i] + uid.length
            reasonOf[i] = index.getValue(reasonsOf.getValue(uid))
        }
        return lookup@{ uid ->
            var low = 0
            var high = reasonOf.lastIndex
            while (low <= high) {
                val middle = (low + high) ushr 1
                val key = joined.substring(starts[middle], starts[middle + 1])
                if (key == uid) return@lookup reasons[reasonOf[middle]]
                if (key < uid) low = middle + 1 else high = middle - 1
            }
            undefinedValue()
        }
    }

    private fun loadBans(stored: String) {
        try {
            findBan = indexBans(if (stored.isNotEmpty()) js("JSON.parse")(stored) else null)
        } catch (error: Throwable) {
            findBan = indexBans(null)
            console.error("밴 DB를 읽지 못했습니다.", error)
        }
        bump()
    }

    private fun update(force: dynamic): Promise<dynamic> = jsPromise {
        val urls = s.urls
        val metadata = s.dbMetaStorage()
        val initial = awaitResultOf(get(urls.version), metadata.getValue())
        val version = (initial[0] as String).trim()
        val meta = initial[1]
        if (force != true && version.isNotEmpty() && version == ((meta.version ?: "") as String).trim() && meta.format == format) {
            awaitJs(metadata.setValue(setField(meta, "lastUpdate", Date.now())))
            return@jsPromise undefinedValue()
        }
        val downloaded = awaitResultOf(get(urls.ip), get(urls.ban))
        val ip = downloaded[0]
        val ban = downloaded[1]
        val data = s.parseIpData(ip) ?: error("IP 데이터 형식이 올바르지 않습니다.")
        s.createIpLookup(data)
        js("JSON.parse")(ban)
        val stale = data.version !== undefinedValue() && data.version != version
        awaitJs(s.writeDatabase(props("version" to (data.version ?: version), "format" to format,
            "lastUpdate" to if (stale) meta.lastUpdate else Date.now()), ip, ban))
        undefinedValue()
    }

    private suspend fun awaitResultOf(first: dynamic, second: dynamic): Array<dynamic> {
        val promise: dynamic = js("Promise")
        return awaitJs(promise.all(arrayOf<dynamic>(first, second))) as Array<dynamic>
    }

    private fun init(): Promise<dynamic> = jsPromise {
        val attempt = js("new AbortController()")
        val signal = js("AbortSignal.any")(arrayOf<dynamic>(watching.signal, attempt.signal))
        var updated = false
        val ipChanged: (dynamic) -> Unit = {
            updated = true
            loadedStamp = ""
            loadIp((it ?: "") as String, format)
        }
        s.watchStorage(s.keys.ip, ipChanged, signal)
        val first = try { awaitResultOf(storageValue(s.keys.ip as String), storage.getItem(s.metaKey)) }
        catch (error: Throwable) {
            attempt.abort()
            throw error
        }
        if (!updated) {
            loadedStamp = stamp(first[1])
            loadIp(first[0] as String, first[1]?.format)
        }
        val banChanged: (dynamic) -> Unit = {
            if (bansRequested) {
                banVersion++
                loadBans((it ?: "") as String)
            }
        }
        s.watchStorage(s.keys.ban, banChanged, signal)
        s.onBfcacheRestore(::restore, signal)
        undefinedValue()
    }

    private fun restore(): Promise<dynamic> = jsPromise {
        val meta = awaitJs(storage.getItem(s.metaKey))
        val stamp = stamp(meta)
        if (stamp == loadedStamp) return@jsPromise undefinedValue()
        val values = awaitResultOf(storageValue(s.keys.ip as String), if (bansRequested) storageValue(s.keys.ban as String) else null)
        loadedStamp = stamp
        loadIp(values[0] as String, meta?.format)
        if (values[1] != null) loadBans(values[1] as String)
        undefinedValue()
    }

    private fun ipInfo(ip: String): dynamic {
        val lookup = lookup ?: return undefinedValue()
        val candidates = lookup(ip) as Array<dynamic>? ?: return undefinedValue()
        val first = candidates.firstOrNull() ?: return undefinedValue()
        val shown = candidates.map {
            val name = (it.org ?: "") as String
            val tag = if (it.vpn == true) "VPN" else (it.country ?: "") as String
            when {
                name.isNotEmpty() && tag.isNotEmpty() -> "$name ($tag)"
                name.isNotEmpty() -> name
                tag.isNotEmpty() -> tag
                else -> "조직 미상"
            }
        }.distinct()
        val category = when {
            first.vpn == true -> "vpn"
            !truthy(first.country) -> "korea"
            first.country == "일본" -> "japan"
            first.country == "중국" -> "china"
            else -> "foreign"
        }
        return props("label" to shown.joinToString(", "), "title" to shown.joinToString("\n"), "category" to category)
    }

    private fun passesIpFilter(info: dynamic, filter: String): Boolean =
        filter == "all" || (filter == "foreign" && info.category != "korea") || (filter == "vpn" && info.category == "vpn")

    private fun banReasons(uid: String): dynamic {
        if (!bansRequested) {
            bansRequested = true
            val version = banVersion
            val onValue: (String) -> Unit = { if (banVersion == version) loadBans(it) }
            val onError: (dynamic) -> Unit = { failure ->
                bansRequested = false
                console.error(failure)
            }
            storageValue(s.keys.ban as String).then(onValue, onError)
        }
        return findBan?.invoke(uid) ?: undefinedValue()
    }

    fun api(): dynamic {
        val initOnce = singleFlight { init() }
        val init: () -> Promise<dynamic> = { initOnce(null) }
        val subscribe: (dynamic) -> dynamic = {
            val log: (Throwable) -> Unit = { failure -> console.error(failure) }
            init().catch(log)
            versionStore.subscribe(it)
        }
        val onChange: (dynamic) -> dynamic = { versionStore.subscribe(it) }
        val version: () -> Int = { versionStore.getState() as Int }
        return props("updateDatabase" to ::update, "subscribeDatabase" to subscribe,
            "onDatabaseChange" to onChange, "databaseVersion" to version, "releaseDatabase" to { watching.abort() },
            "initDatabase" to init, "ipInfoOf" to ::ipInfo, "passesIpFilter" to ::passesIpFilter, "banReasonsOf" to ::banReasons)
    }
}

internal fun createDatabase(services: dynamic): dynamic = Database(services).api()
