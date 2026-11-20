package dev.dcrk

/** 검색 모듈의 URL 제한과 기본 비활성화 및 설정 스키마를 Kotlin에서 관리한다. */
internal fun searchMeta(urls: Array<dynamic>): dynamic = props(
    "id" to "search",
    "urls" to urls,
    "defaultEnable" to false,
    "settings" to staticFeatureSettings("search")
)

private const val RESULT_ROW = ":scope > tr:is([data-no], [data-cmt])"
private const val ADDED_ROW = "refresherSearchAdded"
private const val REUSE_TTL = 60_000
private val searchIp = Regex("""^\d{1,3}\.\d{1,3}$""")
private val searchUid = Regex("""^[\w.-]+$""")

private class SearchFill(val key: String, var rows: Array<dynamic>, val paging: String, val loadedAt: Double)

fun setupSearch(ctx: dynamic, s: dynamic): dynamic {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val location = window.location
    val css: dynamic = js("CSS")
    val console: dynamic = js("console")
    val listSelector = s.listSelector as String
    val pagingSelector = s.pagingSelector as String
    val filter = s.writerFilter()
    val filterSelector = if (filter == null) null else s.writerSelector(filter) as String
    var removeStyle: (() -> Unit)? = null

    ctx.onRevoke(revoke@{
        removeStyle?.invoke()
        removeWriterTypes()
        for (row in jsArray(doc.querySelectorAll(".$ADDED_ROW"))) row.remove()
        for (table in jsArray(doc.querySelectorAll("[data-refresher-search-empty]"))) {
            table.classList.add("empty")
            js("Reflect.deleteProperty")(table.dataset, "refresherSearchEmpty")
        }
        val paging = doc.querySelector(pagingSelector) ?: return@revoke
        val original = paging.dataset.refresherSearchPaging ?: return@revoke
        paging.innerHTML = original
        js("Reflect.deleteProperty")(paging.dataset, "refresherSearchPaging")
    })

    fun searchWriter(next: dynamic) {
        val value = next.value as String
        if (next.kind == "ip") {
            if (searchIp.matches(value)) location.href = writerSearchUrl(next, null)
            else s.showToast("IP는 123.45처럼 앞 두 자리를 입력해 주세요.", "warning")
            return
        }
        if (!searchUid.matches(value)) {
            s.showToast("아이디를 입력해 주세요.", "warning")
            return
        }
        jsPromise {
            try {
                val html = awaitJs(s.gallogPage(value))
                val parser: dynamic = js("new DOMParser()")
                val nick = if (html == null) null else
                    (parser.parseFromString(html, "text/html").querySelector(".nick_name")?.textContent as String?)?.trim()
                if (nick.isNullOrEmpty()) s.showToast("갤로그에서 그 아이디를 찾지 못했습니다.", "warning")
                else location.href = writerSearchUrl(next, nick)
            } catch (error: Throwable) {
                console.error("Gallog lookup failed:", error)
                s.showToast("갤로그를 불러오지 못했습니다.", "error")
            }
        }
    }

    val searchUserPosts: (dynamic, String) -> Unit = { user, by ->
        val nick = user.nick as String?
        if (by == "nick") {
            if (!nick.isNullOrEmpty()) location.href = writerSearchUrl(null, nick)
        } else {
            val identity = when {
                truthy(user.uid) -> props("kind" to "uid", "value" to user.uid)
                truthy(user.ip) -> props("kind" to "ip", "value" to user.ip)
                else -> null
            }
            if (identity != null) location.href = writerSearchUrl(identity, nick)
        }
    }
    val api = props("searchUserPosts" to searchUserPosts)
    s.whenDomReady({ addWriterTypes(filter, ::searchWriter, ctx.signal) }, ctx.signal)
    if (filterSelector != null) removeStyle = writeStyle("refresher-search-writer",
        "$listSelector > tr:not(:has($filterSelector)) { display: none !important; }")

    val searching = truthy(s.queryString("s_keyword"))
    if (s.listPage.test(s.documentUrl.pathname) != true || (!searching && filter == null)) return api

    fun counted(row: dynamic): Boolean = truthy(row.dataset.no) && (filterSelector == null || row.querySelector(filterSelector) != null)
    fun duplicate(list: dynamic, id: String): Boolean = list.querySelector(":scope > tr[data-no=\"${css.escape(id)}\"]") != null
    fun lastPage(paging: dynamic): Boolean {
        val next = paging.querySelector("em")?.nextElementSibling
        return next == null || next.classList.contains("search_next") == true
    }

    val gallery = s.pageGallery ?: ""
    val pages = s.createPageCache(ctx.settings.maxSearches)
    ctx.onSettingsChanged { _: dynamic -> pages.resize(ctx.settings.maxSearches) }
    val filled: dynamic = js("new WeakSet()")
    var running: dynamic = null
    var lastFill: SearchFill? = null

    val fill: (dynamic) -> Unit = { list -> jsPromise fill@{
        if (filled.has(list) == true) return@fill
        filled.add(list)
        if (list.querySelector(".$ADDED_ROW") != null) return@fill
        running?.abort()
        running = null

        val paging = doc.querySelector(pagingSelector) ?: return@fill
        paging.dataset.refresherSearchPaging = paging.innerHTML
        if (filter != null) keepWriterFilter(paging, filter)
        if (searching && !lastPage(paging)) return@fill

        val preferred = js("Number")(doc.querySelector("#list_num")?.value)
        val target = if (truthy(preferred)) preferred as Double else 50.0
        val own = jsArray(list.querySelectorAll(":scope > tr")).filter(::counted)
        var count = own.size
        if (count >= target) return@fill
        val here = s.ownPreviewEntry(window.history.state)?.back?.url ?: location.href
        val rowIds = own.joinToString(",") { it.dataset.no as String }
        val key = "$here|$rowIds"
        val previous = lastFill
        if (!searching && previous?.key == key && (js("Date.now()") as Double) - previous.loadedAt < REUSE_TTL) {
            val rows = previous.rows.filterNot { duplicate(list, it.dataset.no as String? ?: "") }.toTypedArray()
            for (row in rows) list.append(row)
            paging.innerHTML = previous.paging
            if (filter != null) keepWriterFilter(paging, filter)
            previous.rows = rows
            if (rows.isNotEmpty()) s.notifyListReplaced(gallery)
            return@fill
        }
        lastFill = null

        val keyword = doc.querySelector("#sch_q")?.value ?: ""
        val commentSearch = s.queryString("s_type") == "search_comment"
        val fillCheckbox = s.checkboxFiller(list, commentSearch)
        val max = ctx.settings.maxSearches as Int
        val status = doc.createElement("p")
        status.className = "refresherSearchStatus"
        status.setAttribute("role", "status")
        paging.after(status)
        var added = 0
        if (!searching) pages.clear()
        val controller: dynamic = js("new AbortController()")
        running = controller
        val abortSignal: dynamic = js("AbortSignal")
        val signal = abortSignal.any(arrayOf<dynamic>(ctx.signal, controller.signal))

        try {
            for (step in 1..max) {
                if (count >= target) break
                val next = paging.querySelector(if (searching) "a.search_next" else "em + a") ?: break
                status.textContent = "${if (searching) "다음 검색" else "다음 쪽 확인"} 중... ($step/$max)"
                var page = pages.get(next.href)
                if (page == null) {
                    val html = awaitJs(s.fetchText(next.href, signal))
                    val parser: dynamic = js("new DOMParser()")
                    val incoming = parser.parseFromString(html, "text/html")
                    val rows = incoming.querySelector(listSelector)
                    val pager = incoming.querySelector(pagingSelector)
                    if (rows == null || pager == null) error("검색 결과 페이지에 목록이 없습니다.")
                    s.highlightSearchResults(rows, keyword, commentSearch)
                    val owned = jsArray(rows.querySelectorAll(RESULT_ROW)).map { doc.adoptNode(it) }.toTypedArray()
                    page = props("rows" to owned, "paging" to pager.innerHTML)
                    pages.set(next.href, page)
                }
                for (cached in page.rows as Array<dynamic>) {
                    val id = cached.dataset.no as String?
                    if (!searching && !id.isNullOrEmpty() && duplicate(list, id)) continue
                    val row = doc.importNode(cached, true)
                    val empty = list.closest("table.empty")
                    if (empty != null) {
                        empty.classList.remove("empty")
                        empty.dataset.refresherSearchEmpty = ""
                    }
                    row.classList.add(ADDED_ROW)
                    fillCheckbox(row)
                    list.append(row)
                    if (counted(row)) count++
                    added++
                }
                paging.innerHTML = page.paging
                if (filter != null) keepWriterFilter(paging, filter)
                if (searching && !lastPage(paging)) break
            }
            if (!searching) lastFill = SearchFill(key, jsArray(list.querySelectorAll(":scope > .$ADDED_ROW")), paging.innerHTML as String, js("Date.now()") as Double)
        } catch (failure: Throwable) {
            if (signal.aborted != true) s.reportFailure(failure, "Search continuation failed:", "다음 검색 결과를 불러오지 못했습니다.")
        } finally {
            if (running === controller) running = null
            status.remove()
            if (added > 0) s.notifyListReplaced(gallery)
        }
    } }
    s.whenDomReady({ ctx.addFilter(listSelector, fill) }, ctx.signal)
    return api
}
