package dev.dcrk

private val youtubeCode = Regex("^[A-Za-z0-9_-]{11}$")
private val instagramCode = Regex("^[A-Za-z0-9_-]+$")
private val xStatusCode = Regex("^[0-9]{1,24}$")

private fun mediaAddress(href: String): BrowserUrl? = try {
    BrowserUrl(href, "https://gall.dcinside.com")
} catch (_: Throwable) {
    null
}

private fun youtubeId(href: String): String? {
    val url = mediaAddress(href) ?: return null
    if (url.protocol != "https:" && url.protocol != "http:") return null
    val host = url.hostname.lowercase()
    val parts = url.pathname.trim('/').split('/')
    val id = when {
        host == "youtu.be" -> parts.firstOrNull()
        host == "youtube.com" || host == "www.youtube.com" || host == "m.youtube.com" ||
            host == "www.youtube-nocookie.com" -> when (parts.firstOrNull()) {
            "watch" -> url.searchParams.get("v") as String?
            "shorts", "live", "embed" -> parts.getOrNull(1)
            else -> null
        }
        else -> null
    }
    return id?.takeIf { youtubeCode.matches(it) }
}

/** 기존 동영상 iframe을 유지하며 일반 YouTube 링크에서 누락된 iframe만 보충한다. */
internal fun supplementYoutubeEmbeds(html: String): String {
    if (!html.contains("youtu", ignoreCase = true)) return html
    val doc: dynamic = js("new DOMParser()").parseFromString("<div id='refresher-youtube-root'>" + html + "</div>", "text/html")
    val root = doc.getElementById("refresher-youtube-root") ?: return html
    val seen = mutableSetOf<String>()
    for (media in jsArray(root.querySelectorAll("iframe[src], embed[src]"))) {
        val src = media.getAttribute("src") as String? ?: continue
        youtubeId(src)?.let { seen.add(it) }
    }
    for (link in jsArray(root.querySelectorAll("a[href]"))) {
        if (link.closest(".og-div, .og-wrap") != null) continue
        val href = link.getAttribute("href") as String? ?: continue
        val id = youtubeId(href) ?: continue
        if (!seen.add(id)) continue
        val url = mediaAddress(href) ?: continue
        val iframe = doc.createElement("iframe")
        iframe.setAttribute("src", "https://www.youtube.com/embed/" + id)
        iframe.setAttribute("width", "560")
        iframe.setAttribute("height", "315")
        iframe.setAttribute("allowfullscreen", "")
        iframe.setAttribute("loading", "lazy")
        iframe.setAttribute("title", "YouTube 동영상")
        if (url.pathname.startsWith("/shorts/")) iframe.classList.add("dc-video-portrait")
        val place = link.closest("p") ?: link
        place.parentNode?.insertBefore(iframe, place)
    }
    return root.innerHTML as String
}

private fun xStatus(href: String): String? {
    val url = mediaAddress(href) ?: return null
    if (url.protocol != "https:" && url.protocol != "http:") return null
    if (url.hostname !in arrayOf("x.com", "www.x.com", "twitter.com", "www.twitter.com", "mobile.twitter.com")) return null
    val path = url.pathname.trim('/').split('/')
    return if (path.size >= 3 && path[1] == "status" && xStatusCode.matches(path[2])) path[2] else null
}

private fun instagramEmbed(href: String, captioned: Boolean): String? {
    val url = mediaAddress(href) ?: return null
    if (url.protocol != "https:" && url.protocol != "http:") return null
    if (url.hostname !in arrayOf("instagram.com", "www.instagram.com")) return null
    val path = url.pathname.trim('/').split('/')
    if (path.size < 2 || path[0] !in arrayOf("p", "reel", "tv") || !instagramCode.matches(path[1])) return null
    return "https://www.instagram.com/" + path[0] + "/" + path[1] + "/embed/" +
        if (captioned) "captioned/" else ""
}

/** 미리보기 본문 안의 SNS 임베드. 반환하는 정리 함수는 타이머·이벤트와 원래 DOM을 복원한다. */
internal fun installExternalEmbeds(root: dynamic): () -> Unit {
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val events: dynamic = js("new AbortController()")
    val listeners = props("signal" to events.signal)
    val timers = mutableListOf<dynamic>()
    val restored = mutableListOf<Pair<dynamic, dynamic>>()

    for (quote in jsArray(root.querySelectorAll("blockquote.instagram-media"))) {
        val href = (quote.getAttribute("data-instgrm-permalink") as String?)
            ?: (quote.querySelector("a[href]")?.getAttribute("href") as String?) ?: continue
        val captioned = quote.hasAttribute("data-instgrm-captioned") == true || quote.hasAttribute("data-captioned") == true
        val src = instagramEmbed(href, captioned) ?: continue
        val frame = doc.createElement("iframe")
        frame.className = "refresher-instagram-embed"
        frame.setAttribute("src", src)
        frame.setAttribute("title", "Instagram 게시물")
        frame.setAttribute("loading", "lazy")
        frame.setAttribute("allowfullscreen", "")
        quote.replaceWith(frame)
        restored.add(frame to quote)
    }

    for (anchor in jsArray(root.querySelectorAll("a[href]"))) {
        if (anchor.closest(".x-wrap, .og-div, .og-wrap") != null) continue
        val href = anchor.getAttribute("href") as String? ?: continue
        val id = xStatus(href) ?: continue
        val wrap = doc.createElement("div")
        wrap.className = "x-wrap"
        wrap.dataset.refresherXState = "loading"
        val stage = doc.createElement("div")
        stage.className = "refresher-x-stage"
        stage.dataset.refresherXId = id
        stage.dataset.refresherXTheme = if (doc.documentElement.classList.contains("dark") == true) "dark" else "light"
        val fallback = anchor.cloneNode(true)
        fallback.textContent = "원본 X 게시물 보기"
        fallback.classList.add("refresher-x-original")
        val notice = doc.createElement("p")
        notice.className = "refresher-x-notice"
        notice.textContent = "X 게시물을 불러오는 중입니다."
        wrap.append(stage, notice, fallback)
        anchor.replaceWith(wrap)
        restored.add(wrap to anchor)

        val fail: () -> Unit = {
            if (wrap.isConnected == true && wrap.dataset.refresherXState == "loading") {
                wrap.dataset.refresherXState = "error"
                notice.textContent = "X 게시물을 표시하지 못했습니다."
            }
        }
        val done: (dynamic) -> Unit = {
            if (wrap.isConnected == true && wrap.dataset.refresherXState == "loading") {
                val iframe = stage.querySelector("iframe")
                val height = if (iframe == null) 0.0 else (iframe.getBoundingClientRect().height as Double)
                if (height >= 100) wrap.dataset.refresherXState = "ready"
                else timers.add(window.setTimeout({
                    val current = stage.querySelector("iframe")
                    if (current != null && (current.getBoundingClientRect().height as Double) >= 100)
                        wrap.dataset.refresherXState = "ready"
                    else fail()
                }, 500))
            }
        }
        stage.addEventListener("refresher-x-done", done, listeners)
        stage.addEventListener("refresher-x-error", { _: dynamic -> fail() }, listeners)
        timers.add(window.setTimeout(fail, 5000))
        stage.dispatchEvent(js("new Event('refresher-x-render', {bubbles:true,composed:true})"))
    }
    return {
        events.abort()
        for (timer in timers) window.clearTimeout(timer)
        for ((inserted, original) in restored) if (inserted.isConnected == true) inserted.replaceWith(original)
    }
}
