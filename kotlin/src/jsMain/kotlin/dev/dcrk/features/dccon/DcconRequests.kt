package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise

internal fun createDcconRequests(s: dynamic): dynamic {
    val cache = s.cache
    val urls = s.urls
    val isArray: dynamic = js("Array.isArray")
    val string: dynamic = js("String")
    val number: dynamic = js("Number")
    var listCache: dynamic = null

    fun cachedList(): dynamic {
        val saved = listCache
        return if (saved != null && Date.now() - (saved.at as Double) < 10 * 60_000) saved.list else null
    }
    fun saveList(list: dynamic) { listCache = props("list" to list, "at" to Date.now()) }
    fun clear() {
        cache.clear()
        listCache = null
    }

    fun fetchPackage(code: String, signal: dynamic): Promise<dynamic> = jsPromise {
        val body = awaitJs(s.csrfBody(props("code" to code)))
        val text = awaitJs(s.ajax.post(urls.detail, props("body" to body, "signal" to signal)).text()) as String
        if (text.trim() == "error") error("디시콘 정보가 잘못되었습니다.")
        val response = js("JSON.parse")(text)
        if (!record(response) || !record(response.info) || isArray(response.detail) != true || isArray(response.tags) != true) {
            error("디시콘 정보가 아닙니다.")
        }
        response
    }
    fun getPackage(code: String, signal: dynamic): Promise<dynamic> = jsPromise {
        val value = cache.get(code)
        if (truthy(value)) return@jsPromise value
        val response = awaitJs(fetchPackage(code, signal))
        cache.set(code, response)
        response
    }
    fun addPackage(id: dynamic): Promise<String> = jsPromise {
        val body = awaitJs(s.csrfBody(props("package_idx" to string(id))))
        val response = (awaitJs(s.ajax.post(urls.buy, props("body" to body)).text()) as String).trim()
        if (response == "ok" || response == "not_login") response else "fail"
    }
    fun page(number: Int, signal: dynamic): Promise<dynamic> = jsPromise {
        val body = awaitJs(s.csrfBody(props("target" to "icon", "page" to number.toString())))
        val text = awaitJs(s.ajax.post(urls.lists, props("body" to body, "signal" to signal)).text()) as String
        if (Regex("^\"?not_login\"?$").matches(text.trim())) return@jsPromise "not_login"
        val result = js("JSON.parse")(text)
        if (!record(result) || (result.target != "shop" && isArray(result.list) != true)) error("디시콘 목록이 아닙니다.")
        result
    }
    fun fetchList(signal: dynamic): Promise<dynamic> = jsPromise {
        val first = awaitJs(page(0, signal))
        if (first == "not_login") return@jsPromise first
        if (first.target == "shop") return@jsPromise "shop"

        val maxPage = (number(first.max_page) as Double).toInt().coerceIn(0, 19)
        val all: dynamic = js("Promise")
        val remaining = (1..maxPage).map { page(it, signal) }.toTypedArray()
        val rest = awaitJs(all.all(remaining)) as Array<dynamic>
        val result = mutableListOf<dynamic>()
        for (part in arrayOf<dynamic>(first) + rest) {
            if (part != "not_login" && isArray(part.list) == true) result.addAll((part.list as Array<dynamic>).asList())
        }
        result.toTypedArray()
    }
    fun fetchRecent(signal: dynamic): Promise<dynamic> = jsPromise {
        val body = awaitJs(s.csrfBody(props("target" to "recent", "page" to "0")))
        val text = awaitJs(s.ajax.post(urls.lists, props("body" to body, "signal" to signal)).text()) as String
        if (text.trim() == "not_login" || text.trim() == "\\"not_login\\"") return@jsPromise "not_login"
        val result = js("JSON.parse")(text)
        if (result?.target == "shop") return@jsPromise emptyArray<dynamic>()
        if (!record(result) || isArray(result.list) != true) error("최근 디시콘 목록이 아닙니다.")
        result.list
    }
    return props("cachedDcconList" to ::cachedList, "setCachedDcconList" to ::saveList, "clearDcconCaches" to ::clear,
        "fetchDcconPackage" to ::fetchPackage, "fetchRecentDccons" to ::fetchRecent, "getDcconPackage" to ::getPackage, "addDcconPackage" to ::addPackage, "fetchDcconList" to ::fetchList)
}
