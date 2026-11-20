package dev.dcrk

internal fun createListDom(s: dynamic): dynamic {
    var deferred: String? = null
    var bridge: dynamic = null
    fun document(): dynamic = js("document")
    fun writerSelector(filter: dynamic): String = ".ub-writer[data-${filter.kind}=\"${js("CSS").escape(filter.value)}\"]"
    fun notify(gallery: String) {
        val doc = document()
        if (doc.hidden == true) {
            val first = deferred == null
            deferred = gallery
            if (first) {
                val resume: () -> Unit = {
                    val last = deferred
                    deferred = null
                    if (last != null) notify(last)
                }
                doc.addEventListener("visibilitychange", resume, props("once" to true))
            }
            return
        }
        val send: () -> Unit = {
            val eventType = s.listReplacedEvent()
            val options = props("detail" to gallery)
            val event = js("new CustomEvent(eventType, options)")
            doc.dispatchEvent(event)
        }
        if (bridge == null) {
            val reset: (dynamic) -> Unit = { error -> bridge = null; throw error }
            bridge = s.sendMessage("refresher:installListBridge").catch(reset)
        }
        val ignore: () -> Unit = {}
        bridge.then(send, ignore)
    }
    fun checkboxFiller(list: dynamic, commentSearch: Boolean): (dynamic) -> Unit {
        if (list.closest("table")?.querySelector("thead .chkbox_th") == null) return {}
        val row = jsArray(list.querySelectorAll(":scope > tr")).firstOrNull {
            truthy(it.dataset.no) && it.querySelector(":scope > td .article_chkbox") != null
        }
        val doc = list.ownerDocument
        var sample = row?.querySelector(":scope > td:has(.article_chkbox)")
        if (sample == null) {
            val template = doc.createElement("template")
            val raw = doc.querySelector("script[type=\"text/x-jquery-tmpl\"][id$=\"_td-tmpl\"]")?.innerHTML as String?
            template.innerHTML = raw?.trim() ?: ""
            val cell = template.content.firstElementChild
            if (js("HTMLTableCellElement.prototype").isPrototypeOf(cell) == true) sample = cell
        }
        return fill@{ incoming ->
            if (incoming.querySelector(".article_chkbox") != null || (commentSearch && incoming.classList.contains("search_comment") != true)) return@fill
            val no = incoming.dataset.no
            val cell = if (sample == null || !truthy(no)) doc.createElement("td") else sample.cloneNode(true)
            val input = cell.querySelector("input")
            if (input != null) {
                input.checked = false
                if (row == null || input.value == row.dataset.no) input.value = no
            }
            incoming.prepend(cell)
        }
    }
    fun highlight(list: dynamic, search: String, commentSearch: Boolean) {
        if (search.isEmpty() || commentSearch) return
        for (title in jsArray(list.querySelectorAll(".gall_tit"))) {
            val anchor = title.querySelector("a:first-child") ?: continue
            val css = if (anchor.querySelector(".spoiler") != null) "mark spoiler" else "mark"
            val doc = anchor.ownerDocument
            val walker = doc.createTreeWalker(anchor, js("NodeFilter.SHOW_TEXT"))
            val nodes = mutableListOf<dynamic>()
            while (truthy(walker.nextNode())) nodes.add(walker.currentNode)
            for (node in nodes) {
                val parts = (node.data as String).split(search)
                if (parts.size < 2) continue
                val replacement = mutableListOf<dynamic>(parts.first())
                for (piece in parts.drop(1)) {
                    val mark = doc.createElement("span")
                    mark.className = css
                    mark.textContent = search
                    replacement.add(mark)
                    replacement.add(piece)
                }
                node.replaceWith.apply(node, replacement.toTypedArray())
            }
        }
    }
    return props("writerSelector" to ::writerSelector, "notifyListReplaced" to ::notify,
        "checkboxFiller" to ::checkboxFiller, "highlightSearchResults" to ::highlight)
}
