package dev.dcrk

import kotlin.js.Promise

internal const val MINIMUM_INTERVAL = 2000
private const val LIST_TIMEOUT = 30_000

private fun listMarkup(response: String, tableStart: String): String {
    val table = response.indexOf(tableStart)
    if (table == -1) return ""
    val start = response.indexOf("<tbody", table)
    if (start == -1) return ""
    val end = response.indexOf("</tbody>", start)
    return if (end == -1) "" else response.substring(start, end)
}

/** 글 목록 요청, 취소, 응답 비교 및 새 목록을 화면에 반영하는 순서를 관리한다. */
fun createRefreshLoader(ctx: dynamic, state: dynamic, unseen: dynamic, gallery: String, helpers: dynamic): dynamic {
    val doc: dynamic = js("document")
    val location: dynamic = js("location")
    var originalLocation = location.href as String
    var loading = false
    var rerun = false
    var failures = 0
    var inflight: dynamic = null
    var lastListHash = ""

    fun holdList(customUrl: String?, force: Boolean): Boolean {
        if (!force && ctx.settings.pauseOnHover == true && doc.hidden == false) {
            val list = doc.querySelector(helpers.listSelector)
            if (list != null && (list.matches(":hover") == true || list.querySelector(":focus-visible") != null)) return true
        }
        val held = customUrl.isNullOrEmpty() &&
            doc.querySelector(".gall_list:not([id]) :is(.article_chkbox:checked, .user_data.add)") != null
        if (held && force) {
            helpers.showToast("고른 글이나 열린 유저 메뉴가 있어 글 목록을 새로고침하지 않았습니다.", "warning")
        }
        return held
    }

    fun load(customUrl: String?, force: Boolean): Promise<Boolean> = jsPromise load@{
        // 주소가 바뀌면 이전 주소의 응답을 끊고, 아직 로드 중이면 종료한 뒤 강제 요청을 처리한다.
        if (!customUrl.isNullOrEmpty() && customUrl != originalLocation) {
            originalLocation = customUrl
            inflight?.abort()
        }
        if (loading && force) rerun = true
        if (ctx.signal.aborted == true || loading) return@load false

        if (!force) {
            val since = (js("Date.now")() as Double) - (state.lastRefresh as Double)
            if ((doc.hidden == true && ctx.settings.backgroundRefresh == false) || since < MINIMUM_INTERVAL || state.paused == true) {
                return@load false
            }
            val params = BrowserUrl(originalLocation).searchParams
            val page = params.get("page")
            if (!page.isNullOrEmpty() && page != "1") return@load false
            if (state.offscreen == true || helpers.previewApi()?.isOpen() == true) {
                state.missed = true
                return@load false
            }
        }
        if (holdList(customUrl, force)) return@load false

        loading = true
        val target = originalLocation
        val controller: dynamic = js("new AbortController()")
        inflight = controller

        fun fail(): Boolean {
            failures++
            if (force) helpers.showToast("글 목록을 불러오지 못했습니다.", "error")
            return false
        }

        try {
            state.lastRefresh = js("Date.now")()
            val request: dynamic = js("({})")
            request.signal = controller.signal
            request.totalTimeout = LIST_TIMEOUT
            if (!force) request.retry = 0
            val response = (helpers.fetchText(listUrl(target), request) as Promise<String>).awaitResult()
            val listHtml = listMarkup(response, helpers.tableStart as String)
            val listHash = if (listHtml.isEmpty()) "" else {
                val encoder: dynamic = js("new TextEncoder()")
                (helpers.sha256(encoder.encode(listHtml)) as Promise<String>).awaitResult()
            }
            if (target != originalLocation || ctx.signal.aborted == true) return@load false
            if (customUrl.isNullOrEmpty() && listHash.isNotEmpty() && listHash == lastListHash) {
                failures = 0
                state.quiet = if (force) 0 else (state.quiet as Int) + 1
                unseen.count(emptyArray<dynamic>())
                return@load true
            }
            if (holdList(customUrl, force)) return@load false

            val filter = helpers.writerFilter(target)
            val writer = filter != null
            val searching = !(helpers.queryString("s_keyword") as String?).isNullOrEmpty()
            val partial = !force && failures == 0 && !searching && !writer && listHtml.isNotEmpty()
            val parser: dynamic = js("new DOMParser()")
            val dom = parser.parseFromString(if (partial) "<table class=\"gall_list\">$listHtml" else response, "text/html")
            val oldList = doc.querySelector(helpers.listSelector)
            val newList = dom.querySelector(helpers.listSelector)

            syncRefreshPaging(dom, helpers.pagingSelector as String)
            if (oldList == null || newList == null) return@load fail()
            failures = 0

            val options: dynamic = js("({})")
            val navigated = !customUrl.isNullOrEmpty()
            options.navigated = navigated
            options.search = when {
                searching -> doc.querySelector("#sch_q")?.value ?: ""
                writer -> ""
                else -> js("undefined")
            }
            options.searchType = BrowserUrl(target).searchParams.get("s_type")
            options.fadeIn = ctx.settings.fadeIn
            options.keepDeleted = helpers.previewApi()?.archiveArticle() == true && helpers.isWholeFirstPage(target) == true

            val result = helpers.replaceList(oldList, newList, options)
            val added = result.added as Array<dynamic>
            lastListHash = listHash
            if (result.changed == true) {
                helpers.notifyListReplaced(gallery)
                unseen.showMark()
            }
            if (target == state.scrollAfter) {
                state.scrollAfter = null
                val selector = if (helpers.isViewPage == true) ".view_bottom_btnbox" else ".page_head"
                val element = doc.querySelector(selector)
                if (element != null) {
                    val scrollOptions: dynamic = js("({block: 'start'})")
                    scrollOptions.behavior = helpers.smoothScroll()
                    element.scrollIntoView(scrollOptions)
                }
            }
            state.quiet = if (force || added.isNotEmpty()) 0 else (state.quiet as Int) + 1
            if (!navigated && added.isNotEmpty()) {
                val checked = if (filter == null) added else {
                    val selector = helpers.writerSelector(filter)
                    added.filter { it.querySelector(selector) != null }.toTypedArray()
                }
                if (checked.isNotEmpty()) helpers.userinfoApi()?.checkNewPosts(checked)
            }
            unseen.count(if (navigated) emptyArray<dynamic>() else added)
            true
        } catch (error: Throwable) {
            if (controller.signal.aborted == true) return@load false
            val console: dynamic = js("console")
            console.error("Refresh failed:", error)
            fail()
        } finally {
            loading = false
            inflight = null
            if (target == state.scrollAfter) state.scrollAfter = null
            if (ctx.signal.aborted == false && (target != originalLocation || rerun)) {
                rerun = false
                load(if (target != originalLocation) originalLocation else null, true)
            }
        }
    }

    val loader: dynamic = js("({})")
    loader.load = { customUrl: String?, force: Boolean? -> load(customUrl, force == true) }
    loader.failures = { failures }
    loader.currentUrl = { originalLocation }
    loader.abort = { inflight?.abort() }
    return loader
}
