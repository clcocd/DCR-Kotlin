package dev.dcrk

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private const val QUIET_FACTOR_LIMIT = 4.0
private const val BACKOFF_LIMIT = 60_000.0

private fun closestRefreshElement(target: dynamic, selector: String): dynamic =
    if (target != null && js("target instanceof Element") as Boolean) target.closest(selector) else null

/** 콘텐츠 모듈의 상태, 자동 주기, 페이지 이동 및 직접 새로고침을 관리한다. */
fun setupRefresh(ctx: dynamic, helpers: dynamic): dynamic {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val history: dynamic = window.history
    val location: dynamic = window.location
    val signal = ctx.signal
    val options: dynamic = js("({})")
    options.signal = signal

    ctx.onRevoke {
        doc.documentElement.classList.remove("refresherDoNotColorVisited")
        for (row in js("Array.from")(doc.querySelectorAll(".refresherDeleted")) as Array<dynamic>) row.remove()
        for (row in js("Array.from")(doc.querySelectorAll(".refresherNewPost")) as Array<dynamic>) clearNewPost(row)
    }

    fun searching(): Boolean = !(helpers.queryString("s_keyword") as String?).isNullOrEmpty() || helpers.writerFilter() != null
    val unseen = helpers.createUnseenCounter(ctx)
    val state: dynamic = js("({lastRefresh: 0, quiet: 0, scrollAfter: null, offscreen: false, missed: false})")
    state.paused = searching() && ctx.settings.noRefreshOnSearch == true
    val loader = helpers.createListLoader(ctx, state, unseen, helpers.pageGallery ?: "")
    var timer = 0
    var button: dynamic = null

    fun label(): String = if (state.paused == true) "자동 새로고침: 꺼짐" else "자동 새로고침: 켜짐"

    fun setPaused(next: Boolean) {
        state.paused = next
        if (button != null) button.textContent = label()
    }

    val addButton: (dynamic) -> Unit = { element ->
        if (button == null || element.contains(button) == false) {
            element.querySelector("button[data-refresher-refresh]")?.remove()
            button = doc.createElement("button")
            button.type = "button"
            button.textContent = label()
            button.dataset.refresherRefresh = "true"
            button.addEventListener("click", { _: dynamic -> setPaused(state.paused == false) })
            element.append(button)
        }
    }
    helpers.whenDomReady({ ctx.addFilter(".page_head > .gall_issuebox", addButton) }, signal)
    ctx.onRevoke { button?.remove() }

    fun applyVisitedColor() {
        doc.documentElement.classList.toggle("refresherDoNotColorVisited", ctx.settings.doNotColorVisited)
    }
    applyVisitedColor()

    lateinit var loadNow: (String?, Boolean?) -> dynamic
    fun armNext() {
        window.clearTimeout(timer)
        if (signal.aborted == true || (doc.hidden == true && ctx.settings.backgroundRefresh == false)) return

        val settings = ctx.settings
        val rate = (if (doc.hidden == true) settings.backgroundRefreshRate else settings.refreshRate) as Double
        val quietInterval = rate * min(1.0 + (state.quiet as Double) / 2, QUIET_FACTOR_LIMIT)
        val retryInterval = min(rate * 2.0.pow(loader.failures() as Double), max(rate, BACKOFF_LIMIT))
        val jitter = 500.0 + (js("Math.random")() as Double) * 1500.0
        timer = window.setTimeout({ loadNow(null, null) }, max(quietInterval, retryInterval) + jitter) as Int
    }

    loadNow = { url, force ->
        window.clearTimeout(timer)
        loader.load(url ?: js("undefined"), force ?: js("undefined")).finally { armNext() }
    }
    armNext()

    fun catchUp() {
        if (state.missed != true) return
        state.missed = false
        loadNow(null, null)
    }

    val attachTableObserver: () -> Unit = observer@{
        val table = doc.querySelector(helpers.listSelector)?.closest("table") ?: return@observer
        val listener: (dynamic) -> Unit = { entries ->
            state.offscreen = entries.at(-1)?.isIntersecting == false
            if (state.offscreen == false) catchUp()
        }
        val observer: dynamic = js("new IntersectionObserver(listener)")
        observer.observe(table)
        ctx.addCleanup { observer.disconnect() }
    }
    val isView = helpers.isViewPage as Boolean
    if (isView) helpers.whenDomReady(attachTableObserver, signal)

    ctx.onSettingsChanged { keys: dynamic ->
        val settings = ctx.settings
        if (keys.has("doNotColorVisited") == true) applyVisitedColor()
        if (keys.has("titleCount") == true && settings.titleCount == false) unseen.clearTitle()
        if (keys.has("readMark") == true && settings.readMark == false) unseen.clearMark()
        if (keys.has("refreshRate") == true || keys.has("backgroundRefresh") == true || keys.has("backgroundRefreshRate") == true) {
            armNext()
        }
        if (keys.has("noRefreshOnSearch") == true && searching()) setPaused(settings.noRefreshOnSearch as Boolean)
    }

    doc.addEventListener("visibilitychange", { _: dynamic ->
        if (doc.hidden == true) {
            armNext()
        } else {
            state.quiet = 0
            if (loader.failures() == 0) loadNow(null, null) else armNext()
        }
    }, options)

    window.addEventListener("popstate", { _: dynamic ->
        if (helpers.ownPreviewEntry(history.state) == null && helpers.listUrl(location.href) != helpers.listUrl(loader.currentUrl())) {
            loadNow(location.href as String, true)
        }
    }, options)

    val activity: (dynamic) -> Unit = { event ->
        if (state.quiet != 0) {
            val inList = if (event.type == "pointerover") {
                closestRefreshElement(event.target, ".gall_list") != null
            } else {
                helpers.isTyping(event) == false
            }
            if (inList) {
                state.quiet = 0
                armNext()
            }
        }
    }
    val passive: dynamic = js("({passive: true})")
    passive.signal = signal
    doc.addEventListener("pointerover", activity, passive)
    doc.addEventListener("keydown", activity, passive)
    ctx.addCleanup { window.clearTimeout(timer) }
    ctx.addCleanup(loader.abort)

    val pagingClick: (dynamic) -> Unit = click@{ event ->
        if (event.button != 0 || helpers.hasModifier(event) == true || ctx.settings.useBetterBrowse == false) return@click
        val anchor = closestRefreshElement(event.target, "${helpers.pagingSelector} a") ?: return@click
        if ((anchor.getAttribute("href") as String?)?.startsWith("javascript:") == true) return@click
        event.preventDefault()

        val href = location.href as String
        val target = (if (isView) mergeParamURL(href, anchor.href as String) else anchor.href) as String
        val next = BrowserUrl(target, href).href
        try {
            if (next != href) history.pushState(null, doc.title, next)
        } catch (error: Throwable) {
            window.console.error("페이지 이동 기록 쌓기 실패:", error)
        }
        state.scrollAfter = next
        loadNow(next, true)
    }
    doc.addEventListener("click", pagingClick, options)

    val api: dynamic = js("({})")
    api.refreshLists = {
        val elapsed = (js("Date.now")() as Double) - (state.lastRefresh as Double)
        if (elapsed < MINIMUM_INTERVAL) {
            helpers.showToast("잠시 후 다시 새로고침해 주세요.")
            js("Promise.resolve()")
        } else {
            loader.load(js("undefined"), true).then { _: dynamic -> js("undefined") }
        }
    }
    api.togglePause = {
        setPaused(state.paused == false)
        helpers.showToast(if (state.paused == true) "이 페이지의 자동 새로고침을 멈췄습니다." else "이 페이지의 자동 새로고침을 다시 켰습니다.")
    }
    api.isPaused = { state.paused as Boolean }
    api.reload = { loadNow(null, true).then { _: dynamic -> js("undefined") } }
    api.catchUp = ::catchUp
    return api
}
