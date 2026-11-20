package dev.dcrk

internal fun whenDomReady(run: () -> Unit, signal: dynamic) {
    val doc: dynamic = js("document")
    if (doc.readyState == "loading") doc.addEventListener("DOMContentLoaded", run, props("once" to true, "signal" to signal))
    else run()
}

internal fun onBfcacheRestore(run: () -> dynamic, signal: dynamic) {
    val log: (dynamic) -> Unit = { console.error(it) }
    val restore: (dynamic) -> Unit = { event -> if (event.persisted == true) run().catch(log) }
    js("window").addEventListener("pageshow", restore, props("signal" to signal))
}

internal fun smoothScroll(): String =
    if (js("matchMedia")("(prefers-reduced-motion: reduce)").matches == true) "auto" else "smooth"

internal fun followSystemAppearance() {
    val root = js("document.documentElement")
    val window: dynamic = js("window")
    val query = window.matchMedia("(prefers-color-scheme: dark)")
    setAppearance(root, query.matches == true)
    val change: (dynamic) -> Unit = { setAppearance(root, it.matches == true) }
    query.addEventListener("change", change)
}

internal fun followDcAppearance(root: dynamic): () -> Unit {
    val doc: dynamic = js("document")
    val apply: () -> Unit = { setAppearance(root, doc.getElementById("css-darkmode") != null) }
    apply()
    val observer = js("new MutationObserver(apply)")
    for (target in arrayOf<dynamic>(doc.head, doc.body)) {
        if (target != null) observer.observe(target, props("childList" to true))
    }
    return { observer.disconnect() }
}

internal fun isGalleryManager(): Boolean =
    js("document").querySelector(".useradmin_btnbox button, .gall_list .chkbox_th, a[onclick^=\"listSearchHead(999)\"]") != null

internal fun loggedInUserId(): dynamic {
    val raw = js("document").querySelector("#login_box .user_info .writer_nikcon")?.getAttribute("onclick") as String? ?: return undefinedValue()
    return Regex("""gallog\.dcinside\.com/([\w-]+)""").find(raw)?.groupValues?.get(1) ?: undefinedValue()
}

internal fun badgeViewOf(settings: dynamic): dynamic = props("order" to settings.badgeOrder,
    "fixedUid" to settings.showFixedNickUID, "halfFixedUid" to settings.showHalfFixedNickUID, "ipFilter" to settings.ipInfoFilter)

internal fun showsUid(view: dynamic, image: dynamic): Boolean {
    val type = if (truthy(image)) nickType(image as String) else "UNFIXED"
    return when (type) {
        "FIXED" -> view.fixedUid == true
        "HALF_FIXED" -> view.halfFixedUid == true
        else -> true
    }
}

internal fun loadedDocumentUrl(): dynamic {
    val performance: dynamic = js("performance")
    val scope: dynamic = js("globalThis")
    val name = if (performance.getEntriesByType == null) "" else performance.getEntriesByType("navigation")[0]?.name ?: ""
    val fallback = scope.location?.href ?: "https://gall.dcinside.com/"
    return js("URL.parse(name)") ?: js("new URL(fallback)")
}

internal fun writerFilter(input: dynamic, fields: dynamic): dynamic {
    val href = if (input === undefinedValue()) loadedDocumentUrl().href else input
    val url = js("new URL(href)")
    for (kind in arrayOf("uid", "ip")) {
        val value = url.searchParams.get(fields[kind])
        if (truthy(value)) return props("kind" to kind, "value" to value)
    }
    return null
}

internal fun queryString(name: String): dynamic = loadedDocumentUrl().searchParams.get(name)

internal fun rowPostNo(row: dynamic, gallery: dynamic): dynamic {
    val number = row.dataset.no
    if (truthy(number)) return number
    val href = row.querySelector(".gall_tit > a")?.getAttribute("href")
    if (!truthy(href)) return undefinedValue()
    val url = js("URL.parse(href, location.href)")
    val params = url?.searchParams ?: return undefinedValue()
    val no = params.get("no")
    return if (params.get("id") === gallery && truthy(no)) no else undefinedValue()
}
