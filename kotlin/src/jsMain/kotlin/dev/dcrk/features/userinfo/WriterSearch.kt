package dev.dcrk

private const val WRITER_CLASS = "refresherWriterType"
private val writerNames = mapOf("uid" to "아이디", "ip" to "IP")

private fun writerParams(url: BrowserUrl, filter: dynamic): BrowserUrl {
    url.searchParams.set(if (filter.kind == "uid") "rf_uid" else "rf_ip", filter.value as String)
    return url
}

fun writerSearchUrl(filter: dynamic, nick: String?): String {
    val href: String = js("location.href") as String
    val url = BrowserUrl(listUrl(href))
    for (key in listOf("page", "s_type", "s_keyword", "search_pos", "search_head", "exception_mode")) url.searchParams.delete(key)
    if (!nick.isNullOrEmpty()) {
        url.searchParams.set("s_type", "search_name")
        url.searchParams.set("s_keyword", nick)
    }
    return if (filter == null) url.href else writerParams(url, filter).href
}

fun keepWriterFilter(paging: dynamic, filter: dynamic) {
    for (anchor in jsArray(paging.querySelectorAll("a[href]"))) {
        val href = anchor.href as String
        if (href.startsWith("http")) anchor.href = writerParams(BrowserUrl(href), filter).href
    }
}

fun removeWriterTypes() {
    val doc: dynamic = js("document")
    for (item in jsArray(doc.querySelectorAll(".$WRITER_CLASS"))) item.remove()
}

fun addWriterTypes(filter: dynamic, search: (dynamic) -> Unit, signal: dynamic) {
    val doc: dynamic = js("document")
    val layer = doc.querySelector("#searchTypeLayer") ?: return
    val label = doc.querySelector("#search_type_txt") ?: return
    val input = doc.querySelector(".bottom_search input[name='search_keyword']") ?: return
    val button = doc.querySelector(".bottom_search .bnt_search") ?: return

    removeWriterTypes()
    for ((key, name) in writerNames) {
        val item = doc.createElement("li")
        item.className = WRITER_CLASS
        item.textContent = name
        item.dataset.kind = key
        layer.append(item)
    }
    var chosen = filter?.kind as String?
    if (filter != null) {
        label.textContent = writerNames[chosen]
        input.value = filter.value
    }

    val pick: (dynamic) -> Unit = pick@{ event ->
        val target = event.target
        val item = if (js("target instanceof Element") as Boolean) target.closest("li") else null
        if (item == null) return@pick
        chosen = (item.dataset.kind as String?)?.takeIf { it in writerNames }
        if (chosen != null) {
            label.textContent = writerNames[chosen]
            layer.style.display = "none"
        }
    }
    val submit: (dynamic) -> Unit = submit@{ event ->
        val kind = chosen ?: return@submit
        event.preventDefault()
        event.stopImmediatePropagation()
        search(props("kind" to kind, "value" to (input.value as String).trim()))
    }
    val capture = props("capture" to true, "signal" to signal)
    layer.addEventListener("click", pick, props("signal" to signal))
    button.addEventListener("click", submit, capture)
    input.addEventListener("keydown", { event: dynamic -> if (event.key == "Enter" && event.isComposing != true) submit(event) }, capture)
}

fun listenSearchBackground(s: dynamic) {
    val request: (dynamic) -> dynamic = { message -> jsPromise {
        try {
            awaitJs(s.fetchText("https://gallog.dcinside.com/${js("encodeURIComponent")(message.data)}"))
        } catch (error: Throwable) {
            if (s.isHTTPError(error) == true && error.asDynamic().response.status == 404) null else throw error
        }
    } }
    s.onMessage("refresher:gallogPage", request)
}
