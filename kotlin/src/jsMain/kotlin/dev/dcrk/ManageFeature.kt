package dev.dcrk

private const val GIF_VIDEO = ".gallview_contents video"
private val gifOriginals: dynamic = js("new WeakMap()")
private val commentId = Regex("""^comment_li_(\d+)$""")

fun setupManage(ctx: dynamic, s: dynamic) {
    val doc: dynamic = js("document")
    val css: dynamic = js("CSS")
    val options = props("signal" to ctx.signal)
    val checkboxRow = "${s.writerRowSelector}, .cmt_nickbox"
    val handled: dynamic = js("new WeakSet()")
    val deleting = mutableSetOf<String>()

    fun enableGif(element: dynamic) {
        if (!(js("element instanceof HTMLVideoElement") as Boolean) || gifOriginals.has(element) == true) return
        val source = element.dataset.src as String?
        if (source?.contains("dcinside.com/dccon.php") == true) return
        gifOriginals.set(element, element.getAttribute("onmousedown"))
        element.removeAttribute("onmousedown")
        element.setAttribute("controls", "")
    }
    fun disableGif() {
        for (video in jsArray(doc.querySelectorAll(GIF_VIDEO))) {
            if (gifOriginals.has(video) != true) continue
            val original = gifOriginals.get(video)
            gifOriginals.delete(video)
            video.removeAttribute("controls")
            if (original != null) video.setAttribute("onmousedown", original)
        }
    }
    ctx.onRevoke { disableGif(); removeManageViewTools() }
    ctx.addFilter(GIF_VIDEO, { element: dynamic -> if (ctx.settings.enableGifControl == true) enableGif(element) })
    s.whenDomReady({ s.renderViewTools(ctx.settings) }, ctx.signal)
    ctx.onSettingsChanged { keys: dynamic ->
        if (keys.has("imageOrigin") == true || keys.has("titleSearch") == true) s.renderViewTools(ctx.settings)
        if (keys.has("enableGifControl") == true) {
            if (ctx.settings.enableGifControl == true) jsArray(doc.querySelectorAll(GIF_VIDEO)).forEach(::enableGif)
            else disableGif()
        }
    }
    val checkbox: (dynamic) -> Unit = add@{ box ->
        if (!(js("box instanceof HTMLInputElement") as Boolean) || handled.has(box) == true) return@add
        handled.add(box)
        val click: (dynamic) -> Unit = { event ->
            if (ctx.settings.checkAllTargetUser == true && event.shiftKey == true) {
                val author = box.closest(checkboxRow)?.querySelector(":scope > .ub-writer")?.dataset
                if (author != null) {
                    val key = listOf("uid", "ip", "nick").firstOrNull { truthy(author[it]) }
                    if (key != null) {
                        val selector = ".ub-writer[data-$key=\"${css.escape(author[key])}\"]"
                        for (other in jsArray(doc.querySelectorAll(selector))) {
                            val related = other.closest(checkboxRow)?.querySelectorAll(".article_chkbox")
                            if (related != null) for (found in jsArray(related)) found.checked = box.checked
                        }
                    }
                }
            }
            if (ctx.settings.checkCommentViaCtrl == true && event.ctrlKey == true) {
                val id = box.closest("li")?.id as String?
                val number = id?.let { commentId.matchEntire(it)?.groupValues?.get(1) }
                val replies = if (number == null) null else doc.getElementById("reply_list_$number")?.querySelectorAll(".article_chkbox")
                if (replies != null) for (reply in jsArray(replies)) reply.checked = box.checked
            }
        }
        val drag: (dynamic) -> Unit = { event ->
            if (ctx.settings.checkViaShift == true && event.shiftKey == true && event.buttons == 1) box.checked = true
        }
        box.addEventListener("click", click, options)
        box.addEventListener("mouseover", drag, options)
        box.addEventListener("mouseout", drag, options)
    }
    ctx.addFilter(".article_chkbox", checkbox)
    val row: (dynamic) -> Unit = add@{ element ->
        if (handled.has(element) == true) return@add
        handled.add(element)
        val click: (dynamic) -> Unit = click@{ event ->
            if (ctx.settings.deleteViaCtrl != true || event.ctrlKey != true || s.isGalleryManager() != true) return@click
            val target = event.target
            if (js("target instanceof Element") as Boolean && target.closest("td:has(.article_chkbox), .reply_numbox") != null) return@click
            val id = s.rowPostNo(element) as String?
            if (id.isNullOrEmpty()) return@click
            event.preventDefault()
            event.stopPropagation()
            if (!deleting.add(id)) return@click
            val post = props("gallery" to (s.pageGallery ?: ""), "id" to id, "link" to s.documentUrl.href)
            s.notifyManage(s.deletePost(post), "게시글을 삭제했습니다.", "게시글을 삭제하지 못했습니다. 잠시 후 다시 시도해 주세요.")
                .then { deleted: dynamic -> if (truthy(deleted)) element.remove() }
                .finally { deleting.remove(id) }
        }
        element.addEventListener("click", click, options)
    }
    ctx.addFilter(s.listRowSelector, row)
}

private const val ORIGIN_CLASS = "refresherImageOrigin"
private const val TITLE_SEARCH = "refresherTitleSearch"
private val downloadedImage = Regex("""^([a-z0-9_]+)-\d{8}-\d{6}-\d{3}(?:-resize)?\.\w+$""")
private val imageKey = "4dddb04685b258c60edfb6d576b1445d".chunked(2).map { it.toInt(16) }

fun removeManageViewTools() {
    val doc: dynamic = js("document")
    for (element in jsArray(doc.querySelectorAll(".$ORIGIN_CLASS, .$TITLE_SEARCH"))) element.remove()
}

private fun uploadedGallery(src: String): String? {
    val url = parseBrowserUrl(src)
    if (url?.hostname?.startsWith("dcimg") != true) return null
    val id = url.searchParams.get("id") ?: return null
    return id.chunked(2).filter { it.length == 2 }.mapIndexed { index, byte ->
        val parsed = js("parseInt")(byte, 16) as Double
        (parsed.toInt() xor imageKey[index % imageKey.size]).toChar()
    }.joinToString("").ifEmpty { null }
}

fun renderManageViewTools(show: dynamic, s: dynamic) {
    removeManageViewTools()
    val doc: dynamic = js("document")
    val window: dynamic = js("window")
    val console: dynamic = js("console")
    val head = doc.querySelector(".gallview_head") ?: return

    if (show.imageOrigin == true && s.pageGallery != "dcbest") {
        val gallery = s.pageGallery as String? ?: ""
        val own = setOf(gallery.take(16), ("mi$" + gallery).take(16))
        val foreign = linkedMapOf<String, MutableList<String>>()
        fun check(source: String?, label: String) {
            if (!source.isNullOrEmpty() && source.take(16) !in own) foreign.getOrPut(source) { mutableListOf() }.add(label)
        }
        var count = 0
        for (media in jsArray(doc.querySelectorAll(".write_div :is(img:not(.written_dccon, .og-img), video)"))) {
            val source = (media.dataset.original ?: media.dataset.src ?: media.src) as String
            if (s.numberedSource.test(source) == true) check(uploadedGallery(source), "${++count}번")
        }
        for ((index, file) in jsArray(doc.querySelectorAll(".appending_file li")).withIndex()) {
            val name = (file.textContent as String? ?: "").trim()
            check(downloadedImage.matchEntire(name)?.groupValues?.get(1), "첨부 ${index + 1}번")
        }
        if (foreign.isNotEmpty()) {
            val details = foreign.entries.joinToString(", ") { (source, labels) -> "$source(${labels.joinToString(", ")})" }
            val notice = doc.createElement("p")
            notice.className = ORIGIN_CLASS
            notice.textContent = "다른 갤러리의 이미지: $details"
            head.prepend(notice)
        }
    }
    if (show.titleSearch != true) return
    val title = textWithoutScripts(head.querySelector(".title_subject"))
    val counts = head.querySelector(".gall_writer .fr") ?: return
    if (title.isEmpty()) return
    val search = doc.createElement("button")
    search.type = "button"
    search.className = TITLE_SEARCH
    search.textContent = "같은 제목 찾기"
    search.addEventListener("click", { jsPromise {
        val toast = s.showToast()
        val action = props("label" to "검색 결과 보기", "run" to { window.open(postSearchUrl(title), "_blank", "noopener") })
        search.disabled = true
        try {
            val text = awaitJs(s.searchPosts(title))
            val parser: dynamic = js("new DOMParser()")
            val dom = parser.parseFromString(text, "text/html")
            val matches = jsArray(dom.querySelectorAll("ul.sch_result_list > li")).mapNotNull { item ->
                val link = item.querySelector(".tit_txt")
                val params = parseBrowserUrl(link?.getAttribute("href") as String? ?: "")?.searchParams
                val self = params?.get("id") == s.pageGallery && params?.get("no") == s.pagePostNo
                val incoming = (link?.textContent as String?)?.trim()
                if (incoming != title || self) null else (item.querySelector(".sub_txt")?.textContent as String?)?.trim() ?: ""
            }
            if (matches.isEmpty()) toast("제목이 같은 글을 찾지 못했습니다.", "info", 5000, action)
            else toast("제목이 같은 글 ${matches.size}개: ${matches.distinct().joinToString(", ")}", "warning", 10_000, action)
        } catch (error: Throwable) {
            console.error("Title search failed:", error)
            toast("통합검색을 불러오지 못했습니다.", "error")
        } finally {
            search.disabled = false
        }
    } })
    counts.prepend(search)
}

fun listenManageBackground(s: dynamic) {
    val search: (dynamic) -> dynamic = { message -> s.fetchText(postSearchUrl(message.data as String)) }
    s.onMessage("refresher:searchPosts", search)
}
