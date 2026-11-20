package dev.dcrk

import kotlin.js.Date

private const val WORD = ".gall_list .ub-word"
private const val SELECTED_ROW = "refresherSelected"
private val previewRowsDocument: dynamic get() = js("document")
private val previewRowsWindow: dynamic get() = js("window")
private fun elementTarget(target: dynamic): dynamic =
    if (js("Element.prototype").isPrototypeOf(target) == true) target else null

internal fun createPreviewRows(s: dynamic): dynamic {
    val path = Regex("""/board/view/(?:id/)?([^/]+)/(\d+)""")
    fun preData(element: dynamic): dynamic {
        val anchor = element.querySelector("a:not(.reply_numbox)") ?: return null
        val href = anchor.getAttribute("href")
        if (!truthy(href)) return null
        val location = previewRowsWindow.location
        val url = js("URL.parse(href, location.href)") ?: return null
        if (url.host == location.host) url.protocol = location.protocol
        if (!truthy(s.dcinsideHref(url))) return null
        var gallery = url.searchParams.get("id")
        var id = url.searchParams.get("no")
        if (!truthy(gallery) || !truthy(id)) {
            val match = path.find(url.pathname as String) ?: return null
            gallery = match.groupValues[1]
            id = match.groupValues[2]
        }
        val row = element.closest(".ub-content") ?: element
        val classes = row.querySelector(".icon_img")?.getAttribute("class") as String?
        val title = (anchor.textContent as String?)?.trim()
        val reply = ((row.querySelector(".reply_num")?.textContent ?: "") as String).drop(1)
        val parsed = js("Number.parseInt(reply, 10)")
        return props("gallery" to gallery, "id" to id, "title" to if (truthy(title)) title else undefinedValue(),
            "link" to url.href, "notice" to (classes?.contains("icon_notice") == true),
            "recommend" to (classes?.contains("icon_recom") == true), "type" to (classes?.split(' ')?.lastOrNull() ?: "icon_txt"),
            "commentCount" to if (truthy(parsed)) parsed else 0)
    }

    fun blurHidden(row: dynamic): Boolean =
        s.uiStore.getState().blockRevealed != true && row.closest(s.blurredRowSelector) != null

    fun rows(): Array<dynamic> {
        val seen = mutableSetOf<String>()
        val rows = mutableListOf<dynamic>()
        for (row in jsArray(previewRowsDocument.querySelectorAll(s.rowSelector))) {
            if (row.checkVisibility() != true) continue
            val pre = preData(row) ?: continue
            if (seen.add(s.postKey(pre) as String)) rows.add(props("row" to row, "pre" to pre))
        }
        return rows.toTypedArray()
    }

    fun adjacent(from: dynamic, direction: Int): dynamic {
        val rows = rows()
        val key = s.postKey(from)
        val index = rows.indexOfFirst { s.postKey(it.pre) == key }
        if (index < 0) return null
        val ahead = if (direction > 0) index + 1 until rows.size else index - 1 downTo 0
        for (i in ahead) if (!blurHidden(rows[i].row)) return rows[i].pre
        return null
    }

    val isText: (dynamic) -> Boolean = { it.type == "icon_txt" || it.type == "icon_recomtxt" }
    return props("buildPreData" to ::preData, "isBlurHidden" to ::blurHidden, "listRows" to ::rows,
        "adjacentPreData" to ::adjacent, "isTextPost" to isText)
}

internal fun bindPreviewRows(ctx: dynamic, handlers: dynamic, s: dynamic) {
    val doc = previewRowsDocument
    val window = previewRowsWindow
    val store = s.store
    var pressStart = 0.0
    var preventOpen = false

    fun resolve(event: dynamic): dynamic {
        val target = elementTarget(event.target) ?: return null
        val element = target.closest(WORD) ?: target.closest(s.rowSelector) ?: return null
        val reply = target.closest("a.reply_numbox")
        if (reply == null && ((element.classList.contains("ub-content") == true && ctx.settings.expandRecognizeRange != true) ||
            target.closest(".ub-writer, td:has(.article_chkbox)") != null)) return null
        val pre = s.buildPreData(element) ?: return null
        return props("preData" to pre, "commentsOnly" to (reply != null), "link" to (s.dcinsideHref(reply?.href) ?: pre.link))
    }
    val options = props("capture" to true, "signal" to ctx.signal)
    val down: (dynamic) -> Unit = mouseDown@{ event ->
        if (event.button != 2) return@mouseDown
        pressStart = Date.now()
        preventOpen = false
        if (event.shiftKey == true || ctx.settings.reversePreviewKey == true) return@mouseDown
        val target = resolve(event) ?: return@mouseDown
        handlers.prefetch(target.preData)
        val warm: () -> Unit = { store.setState(props("warm" to true)) }
        if (store.getState().warm != true) window.setTimeout(warm)
    }
    val up: (dynamic) -> Unit = { event ->
        if (event.button == 2 && pressStart != 0.0) {
            if (Date.now() - (ctx.settings.longPressDelay as Double) > pressStart) preventOpen = true
            pressStart = 0.0
        }
    }
    val menu: (dynamic) -> Unit = contextMenu@{ event ->
        if (event.shiftKey == true) return@contextMenu
        val target = resolve(event) ?: return@contextMenu
        if (preventOpen) {
            preventOpen = false
            return@contextMenu
        }
        event.preventDefault()
        if (ctx.settings.reversePreviewKey == true) window.location.href = target.link
        else handlers.open(target.preData, target.commentsOnly)
    }
    val click: (dynamic) -> Unit = click@{ event ->
        if (s.hasModifier(event) == true || ctx.settings.reversePreviewKey != true) return@click
        val target = resolve(event) ?: return@click
        event.preventDefault()
        handlers.open(target.preData, target.commentsOnly)
    }
    doc.addEventListener("mousedown", down, options)
    doc.addEventListener("mouseup", up, options)
    doc.addEventListener("contextmenu", menu, options)
    doc.addEventListener("click", click, options)


}

internal fun bindPreviewListKeys(ctx: dynamic, open: (dynamic) -> Unit, s: dynamic) {
    val doc = previewRowsDocument
    val window = previewRowsWindow
    var selected: String? = null
    fun rows() = (s.listRows() as Array<dynamic>).filter { s.isBlurHidden(it.row) != true }
    fun select(entry: dynamic) {
        for (row in jsArray(doc.querySelectorAll(".$SELECTED_ROW"))) row.classList.remove(SELECTED_ROW)
        selected = if (entry != null) s.postKey(entry.pre) as String else null
        if (entry != null) {
            entry.row.classList.add(SELECTED_ROW)
            entry.row.scrollIntoView(props("block" to "nearest", "behavior" to s.smoothScroll()))
        }
    }
    val keydown: (dynamic) -> Unit = keydown@{ event ->
        if (ctx.settings.listKeyboard != true || event.defaultPrevented == true || event.ctrlKey == true ||
            event.altKey == true || event.metaKey == true || s.isTyping(event) == true || s.store.getState().visible == true) return@keydown
        val idle = doc.activeElement == null || doc.activeElement === doc.body
        when (val code = event.code as String) {
            "KeyJ", "KeyK" -> {
                val list = rows()
                val index = list.indexOfFirst { s.postKey(it.pre) == selected }
                val next = if (index < 0) list.firstOrNull { (it.row.getBoundingClientRect().top as Double) >= 0 } ?: list.firstOrNull()
                else list[(index + if (code == "KeyJ") 1 else -1).coerceIn(0, list.lastIndex)]
                select(next)
            }
            "Enter", "NumpadEnter", "KeyO" -> {
                if (selected == null || !idle || event.repeat == true) return@keydown
                val entry = rows().firstOrNull { s.postKey(it.pre) == selected } ?: return@keydown
                if (code == "KeyO") window.location.href = entry.pre.link else open(entry.pre)
            }
            "Escape" -> {
                if (selected == null) return@keydown
                select(null)
            }
            else -> return@keydown
        }
        event.preventDefault()
    }
    window.addEventListener("keydown", keydown, props("signal" to ctx.signal))
    val filter: (dynamic) -> Unit = { row ->
        if (selected != null) {
            val pre = s.buildPreData(row)
            if (pre != null && s.postKey(pre) == selected) row.classList.add(SELECTED_ROW)
        }
    }
    ctx.addFilter(s.rowSelector, filter)
    val watcher: (dynamic, dynamic) -> Unit = { state, previous ->
        if (selected != null && previous.visible == true && state.visible != true && previous.preData != null) {
            val key = s.postKey(previous.preData)
            val entry = rows().firstOrNull { s.postKey(it.pre) == key }
            if (entry != null) select(entry)
        }
    }
    ctx.addCleanup(s.store.subscribe(watcher))
    val clear: () -> Unit = { select(null) }
    ctx.addCleanup(clear)
    val change: (dynamic) -> Unit = { keys -> if (keys.has("listKeyboard") == true && ctx.settings.listKeyboard != true) select(null) }
    ctx.onSettingsChanged(change)
}
