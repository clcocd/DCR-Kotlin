package dev.dcrk

import kotlin.math.ceil

internal fun fitPolls(root: dynamic): () -> Unit {
    val observers = js("new Map()")
    val listeners = js("new AbortController()")
    for (frame in jsArray(root.querySelectorAll("iframe[name=\"pollFrame\"]"))) {
        val fit: () -> Unit = fit@{
            observers.get(frame)?.disconnect()
            val doc = frame.contentDocument
            val container = doc?.body?.querySelector(":scope > :not(script, style)")
            if (container == null || doc.defaultView.HTMLElement.prototype.isPrototypeOf(container) != true) return@fit
            doc.documentElement.style.overflow = "hidden"
            val update: () -> Unit = {
                if (frame.contentDocument === doc) {
                    val rect = container.getBoundingClientRect()
                    val width = ceil((rect.width as Double) + (container.offsetLeft as Double) * 2)
                    val height = ceil((rect.height as Double) + (container.offsetTop as Double) * 2)
                    frame.style.width = "${width.asDynamic().toString()}px"
                    frame.style.height = "${height.asDynamic().toString()}px"
                }
            }
            val observer = js("new ResizeObserver(update)")
            observer.observe(container)
            observers.set(frame, observer)
        }
        val doc = frame.contentDocument
        if (doc?.readyState == "complete" && doc.URL != "about:blank") fit()
        frame.addEventListener("load", fit, props("signal" to listeners.signal))
    }
    return {
        listeners.abort()
        for (observer in jsArray(observers.values())) observer.disconnect()
    }
}

internal fun watchGifVideos(root: dynamic): () -> Unit {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val listeners = js("new AbortController()")
    val timers = mutableListOf<dynamic>()
    val videos = jsArray(root.querySelectorAll("video"))
    fun stop(video: dynamic) {
        video.removeAttribute("src")
        video.replaceChildren()
        video.load()
    }
    fun fallback(video: dynamic, gif: String) {
        val image = doc.createElement("img")
        image.className = video.className
        image.src = gif
        image.loading = "lazy"
        for (name in arrayOf("alt", "title", "data-block", "data-blocked", "tabindex", "role", "aria-label")) {
            val value = video.getAttribute(name)
            if (value != null) image.setAttribute(name, value)
        }
        video.replaceWith(image)
        stop(video)
    }
    for (video in jsArray(root.querySelectorAll("video[data-src]"))) {
        val gif = (video.dataset.src ?: "") as String
        var painted = false
        val ready: () -> Unit = { painted = true }
        video.requestVideoFrameCallback(ready)
        val replace: () -> Unit = { if (video.isConnected == true && !painted) fallback(video, gif) }
        video.addEventListener("error", replace, props("capture" to true, "signal" to listeners.signal))
        timers.add(window.setTimeout(replace, 3000))
    }
    return {
        listeners.abort()
        for (timer in timers) window.clearTimeout(timer)
        for (video in videos) if (video.isConnected != true) stop(video)
    }
}

private var txtconObserver: dynamic = null
private val waitingTxtcon = js("new Set()")

private fun resetTxtcon() {
    txtconObserver?.disconnect()
    txtconObserver = null
    waitingTxtcon.clear()
}

private fun unwatchTxtcon(box: dynamic) {
    if (waitingTxtcon.delete(box) != true) return
    txtconObserver?.unobserve(box)
    if (waitingTxtcon.size == 0) resetTxtcon()
}

private fun measureTxtcon(box: dynamic) {
    val txt = box.querySelector(".txtcon_txt") ?: return
    if (box.getClientRects().length == 0) return
    val assign: dynamic = js("Object.assign")
    val compute: dynamic = js("getComputedStyle")
    val parseNumber: dynamic = js("Number.parseFloat")
    assign(txt.style, props("wordBreak" to "keep-all", "overflowWrap" to "normal", "whiteSpace" to "pre-line", "letterSpacing" to "", "transform" to ""))
    val measure = js("document").createElement("span")
    val text = jsArray(txt.childNodes).joinToString("") { if (it.nodeName == "BR") "\n" else (it.textContent ?: "") as String }
    measure.textContent = wrapTxtcon(text)
    txt.replaceChildren(measure)
    val style = compute(box)
    val width = (box.clientWidth as Double) - (parseNumber(style.paddingLeft) as Double) - (parseNumber(style.paddingRight) as Double)
    val height = (box.clientHeight as Double) - (parseNumber(style.paddingTop) as Double) - (parseNumber(style.paddingBottom) as Double)
    val tolerance = 0.5 / (js("devicePixelRatio") as Double)

    fun fit(start: Int, keepLines: Boolean) {
        txt.style.fontSize = "${start}px"
        val lines = if (keepLines) measure.getClientRects().length as Double else Double.POSITIVE_INFINITY
        var min = start
        var max = 72
        var best = start
        while (min <= max) {
            val next = (min + max) ushr 1
            txt.style.fontSize = "${next}px"
            val rect = measure.getBoundingClientRect()
            if ((rect.width as Double) <= width + tolerance && (rect.height as Double) <= height + tolerance &&
                (measure.getClientRects().length as Double) <= lines) {
                best = next
                min = next + 1
            } else max = next - 1
        }
        txt.style.fontSize = "${best}px"
    }

    fit(16, true)
    if ((measure.getBoundingClientRect().width as Double) > width + tolerance) {
        txt.style.wordBreak = "break-all"
        fit(16, true)
    }
    val rect = measure.getBoundingClientRect()
    if ((rect.height as Double) - height > 4 || (rect.width as Double) - width > 4) fit(10, false)
    val longest = (measure.innerHTML as String).split('\n').maxOf { txtconGraphemes(it).size }
    val slack = width - (measure.getBoundingClientRect().width as Double)
    if (longest > 1 && slack > 1) {
        val spacing = slack / longest
        txt.style.letterSpacing = "calc(-0.045em + ${spacing.asDynamic().toFixed(2)}px)"
        txt.style.transform = "translate(${(spacing / 2).asDynamic().toFixed(2)}px, -0.05em)"
    }
}

private fun onTxtcon(entries: Array<dynamic>) {
    for (entry in entries) {
        val target = entry.target
        if (entry.isIntersecting == true && js("HTMLElement.prototype").isPrototypeOf(target) == true) {
            unwatchTxtcon(target)
            measureTxtcon(target)
        }
    }
}

internal fun fitTxtconNearView(box: dynamic): () -> Unit {
    val root = box.closest(".refresher-frame-scroll")
    if (txtconObserver == null || txtconObserver.root !== root) {
        resetTxtcon()
        val callback = ::onTxtcon
        val options = props("root" to root, "rootMargin" to "300px 0px")
        txtconObserver = js("new IntersectionObserver(callback, options)")
    }
    waitingTxtcon.add(box)
    txtconObserver.observe(box)
    return { unwatchTxtcon(box) }
}

internal fun createPreviewMediaHooks(s: dynamic): dynamic {
    val react = s.react
    fun mark(root: dynamic, gallery: dynamic, block: dynamic, only: dynamic) {
        val mode = if (block == null) null else if (only == true) "hide" else s.blockMode(block)
        for (icon in jsArray(root.querySelectorAll(".written_dccon"))) {
            val code = s.dcconCode(icon)
            if (mode != null && truthy(code) && s.isBlocked("DCCON", code, gallery) == true) icon.dataset.blocked = mode
            else icon.removeAttribute("data-blocked")
        }
    }
    fun useMedia(ref: dynamic, html: dynamic, gallery: dynamic, only: dynamic, hideMedia: dynamic) {
        val block = s.useRunningModuleSettings("block")
        val entries = s.useBlocksStore { state: dynamic -> state.entries }
        val defaults = s.useBlocksStore { state: dynamic -> state.defaults }
        val polls: () -> dynamic = { if (ref.current == null) undefinedValue() else fitPolls(ref.current) }
        val gifs: () -> dynamic = { if (ref.current == null) undefinedValue() else watchGifVideos(ref.current) }
        val update: () -> dynamic = {
            if (ref.current != null) mark(ref.current, gallery, block, only)
            undefinedValue()
        }
        react.useEffect(polls, arrayOf<dynamic>(html))
        react.useEffect(gifs, arrayOf<dynamic>(html))
        react.useEffect(update, arrayOf<dynamic>(html, gallery, block, entries, defaults, only))
    }
    return props("markBlockedDccons" to ::mark, "usePreviewMedia" to ::useMedia)
}

internal fun createScrollLock(react: dynamic): (Boolean) -> Unit = { active ->
    val effect: () -> dynamic = lock@{
        if (!active) return@lock undefinedValue()
        val focused = focusedElement()
        val opener = if (focused?.matches(":focus-visible") == true) focused else null
        val doc: dynamic = js("document")
        val window: dynamic = js("window")
        val root = doc.documentElement
        val style = root.style
        val overflow = style.overflow
        val gutter = style.scrollbarGutter
        if ((window.innerWidth as Double) > (root.clientWidth as Double)) style.scrollbarGutter = "stable"
        style.overflow = "hidden"
        val blocked = jsArray(doc.body.querySelectorAll(":scope > :not(refresher-root, [data-refresher-ui], [inert])"))
        for (element in blocked) element.inert = true
        val release: () -> Unit = {
            style.overflow = overflow
            style.scrollbarGutter = gutter
            for (element in blocked) element.inert = false
            opener?.focus(props("preventScroll" to true))
        }
        release
    }
    react.useEffect(effect, arrayOf(active))
}

internal fun createCountDown(s: dynamic): () -> dynamic {
    val ui = ReactUi(s.react)
    val remaining: (dynamic) -> dynamic = { p ->
        s.useTick(1000)
        val left = (p.expire.getTime() as Double) - kotlin.js.Date.now()
        val hours = kotlin.math.floor(left / 3_600_000).toInt()
        val minutes = kotlin.math.floor((left % 3_600_000) / 60_000).toInt()
        val seconds = kotlin.math.floor((left % 60_000) / 1000).toInt()
        val text = if (left <= 0) "만료됨" else if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분 ${seconds}초"
        ui.h(s.WithTooltip, props("tip" to "자동 삭제까지 남은 시간", "trigger" to ui.h(s.Badge, props("className" to "bg-amber-500/15 text-amber-700 dark:text-amber-400"))),
            ui.h(s.Clock), ui.span("sr-only", if (left <= 0) "자동 삭제 시간 " else "자동 삭제까지 "), text)
    }
    return {
        val expire = s.store { state: dynamic -> state.post?.expire }
        val date = if (truthy(expire)) s.parseDate(expire) else null
        if (date != null && !(date.getTime() as Double).isNaN()) ui.h(remaining, props("expire" to date)) else null
    }
}
