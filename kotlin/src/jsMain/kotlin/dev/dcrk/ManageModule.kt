package dev.dcrk

/** GIF로 바뀐 video의 최초 onmousedown 값. 모듈이 다시 켜져도 원래 값을 기억한다. */
private val manageGifOriginals: dynamic = js("new WeakMap()")

private fun enableManageGif(video: dynamic) {
    if (video == null || video.tagName != "VIDEO" || (manageGifOriginals.has(video) as Boolean)) return
    val source: dynamic = video.dataset.src
    if (source != null && (source as String).contains("dcinside.com/dccon.php")) return
    manageGifOriginals.set(video, video.getAttribute("onmousedown"))
    video.removeAttribute("onmousedown")
    video.setAttribute("controls", "")
}

private fun disableManageGifs(selector: String) {
    val videos: dynamic = js("document").querySelectorAll(selector)
    for (i in 0 until (videos.length as Int)) {
        val video: dynamic = videos[i]
        if (!(manageGifOriginals.has(video) as Boolean)) continue
        val original: dynamic = manageGifOriginals.get(video)
        manageGifOriginals.delete(video)
        video.removeAttribute("controls")
        if (original != null) video.setAttribute("onmousedown", original)
    }
}

/**
 * manage의 이벤트·설정·수명주기는 Kotlin에서 관리한다.
 * HTTP 삭제, UI 도구, 문서 주소 및 DOM 준비는 원본 TS의 기존 함수에 연결한다.
 */
fun manageModuleImplementation(
    selectors: dynamic,
    onDomReady: (() -> Unit, dynamic) -> Unit,
    renderViewTools: (dynamic) -> Unit,
    removeViewTools: () -> Unit,
    isManager: () -> Boolean,
    postNo: (dynamic) -> dynamic,
    deleteAndNotify: (String, String, String) -> dynamic,
    gallery: String,
    documentHref: String
): dynamic {
    val impl: dynamic = js("({})")
    val setup: (dynamic) -> Unit = { ctx ->
        val doc: dynamic = js("document")
        val handled: dynamic = js("new WeakSet()")
        val deleting: dynamic = js("new Set()")
        val options: dynamic = js("({})")
        options.signal = ctx.signal

        val revoke: () -> Unit = {
            disableManageGifs(selectors.gifVideo as String)
            removeViewTools()
        }
        ctx.onRevoke(revoke)

        val onVideo: (dynamic) -> Unit = { video ->
            if (ctx.settings.enableGifControl as Boolean) enableManageGif(video)
        }
        ctx.addFilter(selectors.gifVideo, onVideo)

        onDomReady({ renderViewTools(ctx.settings) }, ctx.signal)
        val onSettingsChanged: (dynamic) -> Unit = { keys ->
            if ((keys.has("imageOrigin") as Boolean) || (keys.has("titleSearch") as Boolean)) {
                renderViewTools(ctx.settings)
            }
            if (keys.has("enableGifControl") as Boolean) {
                if (ctx.settings.enableGifControl as Boolean) {
                    val videos: dynamic = doc.querySelectorAll(selectors.gifVideo)
                    for (i in 0 until (videos.length as Int)) enableManageGif(videos[i])
                } else {
                    disableManageGifs(selectors.gifVideo as String)
                }
            }
        }
        ctx.onSettingsChanged(onSettingsChanged)

        // WeakSet은 DOM 교체로 복제되는 속성에 붙지 않으므로 새 요소에 다시 리스너를 단다.
        val onCheckbox: (dynamic) -> Unit = { checkbox ->
            if (checkbox.tagName == "INPUT" && !(handled.has(checkbox) as Boolean)) {
                handled.add(checkbox)
                val onClick: (dynamic) -> Unit = { event ->
                    val parent: dynamic = checkbox.closest(selectors.checkboxRow)
                    val writer: dynamic = if (parent != null) parent.querySelector(":scope > .ub-writer") else null
                    val data: dynamic = if (writer != null) writer.dataset else null
                    val uid: dynamic = if (data != null) data.uid else null
                    val ip: dynamic = if (data != null) data.ip else null
                    val nick: dynamic = if (data != null) data.nick else null
                    val hasUid = uid != null && uid != ""
                    val hasIp = ip != null && ip != ""
                    val hasNick = nick != null && nick != ""

                    if ((ctx.settings.checkAllTargetUser as Boolean) &&
                        (event.shiftKey as Boolean) && (hasUid || hasIp || hasNick)) {
                        val key = if (hasUid) "uid" else if (hasIp) "ip" else "nick"
                        val value: dynamic = if (hasUid) uid else if (hasIp) ip else nick
                        val escaped = js("CSS.escape")(value) as String
                        val selector = ".ub-writer[data-$key=\"$escaped\"]"
                        val writers: dynamic = doc.querySelectorAll(selector)
                        for (i in 0 until (writers.length as Int)) {
                            val otherParent: dynamic = writers[i].closest(selectors.checkboxRow)
                            if (otherParent == null) continue
                            val boxes: dynamic = otherParent.querySelectorAll(".article_chkbox")
                            for (j in 0 until (boxes.length as Int)) boxes[j].checked = checkbox.checked
                        }
                    }

                    if ((ctx.settings.checkCommentViaCtrl as Boolean) && (event.ctrlKey as Boolean)) {
                        val li: dynamic = checkbox.closest("li")
                        val id = if (li != null) li.id as String else ""
                        val commentNo = Regex("^comment_li_(\\d+)$").matchEntire(id)?.groupValues?.get(1)
                        if (commentNo != null) {
                            val replies: dynamic = doc.getElementById("reply_list_$commentNo")
                            if (replies != null) {
                                val boxes: dynamic = replies.querySelectorAll(".article_chkbox")
                                for (i in 0 until (boxes.length as Int)) boxes[i].checked = checkbox.checked
                            }
                        }
                    }
                }
                checkbox.addEventListener("click", onClick, options)

                val onMouse: (dynamic) -> Unit = { event ->
                    if ((ctx.settings.checkViaShift as Boolean) &&
                        (event.shiftKey as Boolean) && (event.buttons as Int) == 1) checkbox.checked = true
                }
                checkbox.addEventListener("mouseover", onMouse, options)
                checkbox.addEventListener("mouseout", onMouse, options)
            }
        }
        ctx.addFilter(".article_chkbox", onCheckbox)

        val onRow: (dynamic) -> Unit = { row ->
            if (!(handled.has(row) as Boolean)) {
                handled.add(row)
                val onClick: (dynamic) -> Unit = { event ->
                    if ((ctx.settings.deleteViaCtrl as Boolean) && (event.ctrlKey as Boolean) && isManager()) {
                        val target: dynamic = event.target
                        val ignored = target != null && target.nodeType == 1 &&
                            target.closest("td:has(.article_chkbox), .reply_numbox") != null
                        if (!ignored) {
                            val postId: dynamic = postNo(row)
                            if (postId != null && postId != "") {
                                event.preventDefault()
                                event.stopPropagation()
                                if (!(deleting.has(postId) as Boolean)) {
                                    deleting.add(postId)
                                    val removeIfDeleted: (dynamic) -> Unit = { deleted ->
                                        if (deleted == true) row.remove()
                                    }
                                    val clear: () -> Unit = { deleting.delete(postId) }
                                    deleteAndNotify(postId as String, gallery, documentHref)
                                        .then(removeIfDeleted).finally(clear)
                                }
                            }
                        }
                    }
                }
                row.addEventListener("click", onClick, options)
            }
        }
        ctx.addFilter(selectors.listRow, onRow)
    }
    impl.setup = setup
    return impl
}
