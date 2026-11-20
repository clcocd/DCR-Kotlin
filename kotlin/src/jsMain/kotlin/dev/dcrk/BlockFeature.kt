package dev.dcrk

private val hiddenClasses = arrayOf("refresherBlocked", "refresherBlur", "refresherDuplicate")
private val blurKeys = arrayOf("blurReveal", "blurStrength")
private val writerTypes = arrayOf("NICK", "ID", "IP", "TITLE", "TAB", "COMMENT")
private const val REVEAL_CLASS = "refresherBlockReveal"

private class BlockFeature(private val ctx: dynamic, private val s: dynamic) {
    private val document: dynamic = js("document")
    private val root = document.documentElement
    private val state = s.useUiStore
    private val gallery = s.pageGallery ?: undefinedValue()
    private val checks = mutableListOf<Pair<String, (dynamic) -> Unit>>()

    private fun isRevealed(): Boolean = root.classList.contains(REVEAL_CLASS) == true
    private fun hiddenCount(): Int = document.querySelectorAll("${s.hiddenRowSelector}, .refresherDuplicate").length as Int
    private fun publishRevealed() { state.setState(props("blockRevealed" to isRevealed())) }

    private fun toggleReveal() {
        root.classList.toggle(REVEAL_CLASS)
        publishRevealed()
        state.getState().showToast(if (isRevealed()) "이 페이지에서 가린 내용을 보입니다. (${hiddenCount()}개)"
            else "가린 내용을 다시 숨겼습니다.")
    }

    private fun blurStyle() {
        root.style.setProperty("--refresher-blur", "${ctx.settings.blurStrength}px")
        root.classList.toggle("refresherBlurReveal", ctx.settings.blurReveal)
    }

    private fun restore() {
        for (node in jsArray(document.querySelectorAll(".refresherBlocked, .refresherBlur, .refresherDuplicate"))) {
            node.classList.remove.apply(node.classList, hiddenClasses)
        }
        for (node in jsArray(document.querySelectorAll(".refresherTextNotice, .refresherDuplicateBadge"))) node.remove()
    }

    private fun hide(node: dynamic) {
        node.classList.add(if (ctx.settings.blur == true) "refresherBlur" else "refresherBlocked")
    }

    private fun replyBoxOf(node: dynamic): dynamic {
        val next = node.nextElementSibling
        return if (next != null && js("HTMLElement.prototype").isPrototypeOf(next) == true &&
            next.classList.contains("ub-content") != true && next.querySelector(":scope > .reply") != null
        ) next else null
    }

    private fun hideWithReply(node: dynamic) {
        hide(node)
        if (ctx.settings.replyRemove == true) {
            val box = replyBoxOf(node)
            if (box != null) hide(box)
        }
    }

    private fun checkWriter(element: dynamic) {
        val entries = s.useBlocksStore.getState().entries
        fun available(type: String): Boolean = (entries[type].length as Int) > 0
        if (writerTypes.none(::available)) return
        fun read(type: String, value: () -> dynamic): dynamic = if (available(type)) value() else null
        val row = element.closest(s.writerRowSelector)
        val dataset = element.dataset
        val values = props(
            "NICK" to read("NICK") { dataset.nick },
            "ID" to read("ID") { dataset.uid },
            "IP" to read("IP") { dataset.ip },
            "TITLE" to read("TITLE") { s.textWithoutScripts(row?.querySelector(".gall_tit > a:not([class]), .title_subject")) },
            "TAB" to read("TAB") {
                val subject = row?.querySelector(".gall_subject .subject_inner, .title_headtext") ?: row?.querySelector(".gall_subject")
                (s.textWithoutScripts(subject) as String).replace(Regex("^\\[(.*)\\]$"), "$1")
            },
            "COMMENT" to read("COMMENT") {
                val wrapper = if (s.isViewPage == true) element.closest(".reply_info, .cmt_info") else null
                val text = wrapper?.querySelector(".usertxt, .txtcon_txt")?.textContent ?: row?.querySelector(".sch_cmt")?.textContent
                (text as String?)?.trim()
            }
        )
        if (s.isAnyBlocked(values, gallery) == true) hideWithReply(row ?: element)
    }

    private fun checkDccon(element: dynamic) {
        val code = s.dcconCode(element)
        if (!truthy(code) || s.isBlocked("DCCON", code, gallery) != true) return
        val target = element.closest(".ub-content") ?: element.closest(".comment_dccon")
        if (target != null) hideWithReply(target) else hide(element)
    }

    private fun checkText() {
        val area = document.querySelector(".write_div") ?: return
        val headBlocked = document.querySelector(".gallview_head:is(.refresherBlocked, .refresherBlur)") != null
        if (!headBlocked && s.isBlocked("TEXT", (area.textContent as String?)?.trim() ?: "", gallery) != true) return
        for (node in jsArray(document.querySelectorAll(".refresherTextNotice"))) node.remove()
        hide(area)
        if (ctx.settings.blur == true) return
        val notice = document.createElement("div")
        notice.className = "refresherTextNotice"
        notice.textContent = s.blockedText
        area.before(notice)
    }

    private fun foldDuplicates(list: dynamic) {
        val duplicate = s.duplicateOf(ctx.settings) ?: return
        val textOf: (dynamic) -> String = { item ->
            (item.querySelector(".usertxt, .txtcon_txt")?.textContent ?: "") as String
        }
        val all = jsArray(list.querySelectorAll("li.ub-content"))
        val shown = all.filter { it.closest(s.hiddenRowSelector) == null }
        val foldedBoxes = js("new Set()")
        val groups = s.groupDuplicates(shown.filter { it.closest(".reply") == null }.toTypedArray(), textOf, duplicate)
        for (entry in jsArray(groups)) {
            val box = if (entry[1] == 0) replyBoxOf(entry[0]) else null
            if (box != null) foldedBoxes.add(box)
        }
        val boxes = jsArray(foldedBoxes)
        val retained = shown.filter { item -> boxes.none { box -> box.contains(item) == true } }
        val visibleGroups = s.groupDuplicates(retained.toTypedArray(), textOf, duplicate)
        for (item in all) {
            val repeats = visibleGroups.get(item)
            item.classList.toggle("refresherDuplicate", repeats === 0)
            val box = replyBoxOf(item)
            box?.classList?.toggle("refresherDuplicate", foldedBoxes.has(box))
            val text = if (truthy(repeats)) "같은 댓글 ×$repeats" else undefinedValue()
            val existing = item.querySelector(".refresherDuplicateBadge")
            if (existing?.textContent == text) continue
            existing?.remove()
            if (!truthy(text)) continue
            val badge = document.createElement("span")
            badge.className = "refresherDuplicateBadge"
            badge.textContent = text
            badge.setAttribute(s.filterSkip, "")
            item.querySelector(".usertxt, .comment_dccon")?.after(badge)
        }
    }

    private fun isAdditive(previous: dynamic, next: dynamic): Boolean {
        if (next.defaults !== previous.defaults) return false
        return (s.blockTypes as Array<String>).all { type ->
            val old = previous.entries[type] as Array<dynamic>
            val current = next.entries[type] as Array<dynamic>
            current === old || (current.size > old.size &&
                old.indices.all { old[it] === current[it] } &&
                (old.size until current.size).all { index ->
                    !((current[index].mode ?: next.defaults[type]) as String).startsWith("NOT_")
                })
        }
    }

    private fun recheck(skipHidden: Boolean = false) {
        if (!skipHidden) restore()
        for ((selector, check) in checks) for (node in jsArray(document.querySelectorAll(selector))) {
            if (skipHidden && selector != ".cmt_list" && node.closest(s.hiddenRowSelector) != null) continue
            check(node)
        }
        if (s.isViewPage == true && document.readyState != "loading") checkText()
    }

    private fun contextMenu(event: dynamic) {
        if (event.shiftKey == true || event.defaultPrevented == true) return
        val target = s.eventTarget(event)
        val dccon = if (js("Element.prototype").isPrototypeOf(target) == true) target.closest(".written_dccon") else null
        if (dccon == null) {
            s.openWriterBubble(event)
            return
        }
        val code = s.dcconCode(dccon)
        if (!truthy(code)) return
        event.preventDefault()
        state.getState().openBubble(props("dccon" to code), event.clientX, event.clientY)
    }

    fun setup(): dynamic {
        val revoke: () -> Unit = {
            restore()
            state.setState(props("blockRevealed" to false))
            root.style.removeProperty("--refresher-blur")
            root.classList.remove("refresherBlurReveal", REVEAL_CLASS)
        }
        ctx.onRevoke(revoke)
        blurStyle()
        publishRevealed()
        checks += ".ub-writer" to ::checkWriter
        checks += ".written_dccon" to ::checkDccon
        if (s.isViewPage == true) {
            val queue: dynamic = js("queueMicrotask")
            val foldLater: (dynamic) -> Unit = { list ->
                val callback: () -> Unit = { if (ctx.signal.aborted != true && list.isConnected == true) foldDuplicates(list) }
                queue(callback)
            }
            checks += ".cmt_list" to foldLater
        }
        for ((selector, check) in checks) ctx.addFilter(selector, check)
        if (s.isViewPage == true) {
            s.whenDomReady(::checkText, ctx.signal)
            val rowsHidden: () -> Unit = { for (list in jsArray(document.querySelectorAll(".cmt_list"))) foldDuplicates(list) }
            document.addEventListener(s.rowsHiddenEvent, rowsHidden, props("signal" to ctx.signal))
        }
        val updated: (dynamic, dynamic) -> Unit = { next, previous ->
            if (next.entries !== previous.entries || next.defaults !== previous.defaults) recheck(isAdditive(previous, next))
        }
        ctx.addCleanup(s.useBlocksStore.subscribe(updated))
        document.addEventListener("contextmenu", ::contextMenu, props("capture" to true, "signal" to ctx.signal))
        val onlyBlur = blurKeys
        val blurOnly = js("new Set(onlyBlur)")
        val settings: (dynamic) -> Unit = { keys ->
            if (keys.isDisjointFrom(blurOnly) != true) blurStyle()
            if (keys.isSubsetOf(blurOnly) != true) recheck()
        }
        ctx.onSettingsChanged(settings)
        return props("isRevealed" to ::isRevealed, "hiddenCount" to ::hiddenCount, "toggleReveal" to ::toggleReveal)
    }
}

internal fun setupBlock(ctx: dynamic, services: dynamic): dynamic = BlockFeature(ctx, services).setup()
