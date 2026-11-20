package dev.dcrk

private val WRITER_SEARCH_RESET = listOf("page", "s_type", "s_keyword", "search_pos", "search_head", "exception_mode")
private const val WRITER_TYPE_CLASS = "refresherWriterType"

private fun writerName(kind: String): String = if (kind == "uid") "아이디" else "IP"

private fun writerFilteredHref(href: String, filter: dynamic, params: dynamic): String {
    val url = BrowserUrl(href)
    url.searchParams.set(params[filter.kind] as String, filter.value as String)
    return url.href
}

/** 검색 조건을 초기화한 목록 주소에서 아이디·IP 거르기와 닉네임 검색을 함께 보존한다. */
fun writerSearchUrlKotlin(filter: dynamic, nick: String?, paramNames: dynamic): String {
    val href = js("globalThis.location.href") as String
    val url = BrowserUrl(listUrl(href))
    for (key in WRITER_SEARCH_RESET) url.searchParams.delete(key)
    if (!nick.isNullOrEmpty()) {
        url.searchParams.set("s_type", "search_name")
        url.searchParams.set("s_keyword", nick)
    }
    return if (filter != null) writerFilteredHref(url.href, filter, paramNames) else url.href
}

/** 다음 검색 구간에서도 uid/IP 거르기가 풀리지 않게 페이징 주소에 전달한다. */
fun keepWriterFilterKotlin(paging: dynamic, filter: dynamic, paramNames: dynamic) {
    val anchors: dynamic = paging.querySelectorAll("a[href]")
    for (i in 0 until (anchors.length as Int)) {
        val anchor: dynamic = anchors[i]
        val href = anchor.href as String
        if (href.startsWith("http")) anchor.href = writerFilteredHref(href, filter, paramNames)
    }
}

/** 재주입 시 전에 더한 검색 종류만 제거하고 디시 자체 옵션은 보존한다. */
fun removeWriterTypesKotlin() {
    val items: dynamic = js("document").querySelectorAll(".$WRITER_TYPE_CLASS")
    for (i in 0 until (items.length as Int)) items[i].remove()
}

/**
 * 검색 종류 선택 UI와 선행 캡처 이벤트는 Kotlin/JS에서 관리한다.
 * 실제 검색 요청은 기존 TS 모듈 콜백을 사용한다.
 */
fun addWriterTypesKotlin(filter: dynamic, search: (dynamic) -> Unit, signal: dynamic) {
    val doc: dynamic = js("document")
    val layer: dynamic = doc.querySelector("#searchTypeLayer")
    val label: dynamic = doc.querySelector("#search_type_txt")
    val input: dynamic = doc.querySelector(".bottom_search input[name='search_keyword']")
    val button: dynamic = doc.querySelector(".bottom_search .bnt_search")
    if (layer == null || label == null || input == null || button == null) return

    removeWriterTypesKotlin()
    for (kind in listOf("uid", "ip")) {
        val item: dynamic = doc.createElement("li")
        item.textContent = writerName(kind)
        item.className = WRITER_TYPE_CLASS
        item.dataset.kind = kind
        layer.append(item)
    }

    var chosen: String? = if (filter == null) null else filter.kind as String
    if (filter != null) {
        label.textContent = writerName(filter.kind as String)
        input.value = filter.value
    }

    val options: dynamic = js("({})")
    options.signal = signal
    val onSelect: (dynamic) -> Unit = { event ->
        val target: dynamic = event.target
        val item: dynamic = if (target != null && target.nodeType == 1) target.closest("li") else null
        if (item != null) {
            val kind: dynamic = item.dataset.kind
            chosen = if (kind == "uid" || kind == "ip") kind as String else null
            if (chosen != null) {
                label.textContent = writerName(chosen!!)
                layer.style.display = "none"
            }
        }
    }
    layer.addEventListener("click", onSelect, options)

    // 디시의 클릭/Enter 처리기보다 먼저 받아, 실제 작성자 검색을 호출한다.
    val capture: dynamic = js("({capture:true})")
    capture.signal = signal
    val submit: (dynamic) -> Unit = { event ->
        val kind = chosen
        if (kind != null) {
            event.preventDefault()
            event.stopImmediatePropagation()
            val value: dynamic = js("({})")
            value.kind = kind
            value.value = (input.value as String).trim()
            search(value)
        }
    }
    button.addEventListener("click", submit, capture)
    val onKeyDown: (dynamic) -> Unit = { event ->
        if (event.key == "Enter" && event.isComposing != true) submit(event)
    }
    input.addEventListener("keydown", onKeyDown, capture)
}
