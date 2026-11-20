package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise

private class Database(private val s: dynamic) {
    private val storage = s.storage
    private val format = s.format
    private val console: dynamic = js("console")
    private val watching = js("new AbortController()")
    private val versionStore = s.versionStore
    private var lookup: dynamic = null
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
        val ip = awaitJs(get(urls.ip)) as String
        val data = s.parseIpData(ip) ?: error("IP 데이터 형식이 올바르지 않습니다.")
        s.createIpLookup(data)
        val stale = data.version !== undefinedValue() && data.version != version
        awaitJs(s.writeDatabase(props("version" to (data.version ?: version), "format" to format,
            "lastUpdate" to if (stale) meta.lastUpdate else Date.now()), ip))
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
        s.onBfcacheRestore(::restore, signal)
        undefinedValue()
    }

    private fun restore(): Promise<dynamic> = jsPromise {
        val meta = awaitJs(storage.getItem(s.metaKey))
        val stamp = stamp(meta)
        if (stamp == loadedStamp) return@jsPromise undefinedValue()
        val ip = awaitJs(storageValue(s.keys.ip as String)) as String
        loadedStamp = stamp
        loadIp(ip, meta?.format)
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
            "initDatabase" to init, "ipInfoOf" to ::ipInfo, "passesIpFilter" to ::passesIpFilter)
    }
}

internal fun createDatabase(services: dynamic): dynamic = Database(services).api()
