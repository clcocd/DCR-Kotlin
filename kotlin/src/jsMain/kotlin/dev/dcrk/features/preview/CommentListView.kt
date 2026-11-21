package dev.dcrk

private const val FIRST_THREADS = 60
private const val MORE_THREADS = 150
private val noReplies = emptyArray<dynamic>()
private val mentionPattern = Regex("""^@(.+?)(?:\(([^()]*)\))?$""")

internal fun threadParents(comments: Array<dynamic>, order: dynamic): Array<dynamic> {
    val top = comments.filter { it.depth == 0 }.map { it.no as String }.toHashSet()
    val parents = comments.filter { it.depth == 0 || it.c_no !in top }
    return when (order) {
        "newest" -> parents.asReversed().toTypedArray()
        "replies" -> {
            val counts = comments.filter { it.depth == 1 && it.c_no in top }.groupingBy { it.c_no as String }.eachCount()
            parents.sortedByDescending { if (it.depth == 0) counts[it.no as String] ?: 0 else 0 }.toTypedArray()
        }
        else -> parents.toTypedArray()
    }
}

internal fun commentSummary(comments: Array<dynamic>): dynamic {
    val blocked = comments.count { truthy(it.blocked) && !truthy(it.lowActivity) }
    val low = comments.count { truthy(it.lowActivity) }
    val folded = comments.count { it.duplicates === 0 }
    val hidden = buildList {
        if (blocked > 0) add("차단 ${blocked}개")
        if (low > 0) add("깡계 ${low}개")
        if (folded > 0) add("같은 댓글 ${folded}개 접음")
    }.joinToString(", ")
    return props("counts" to "스레드 ${threadParents(comments, null).size}개, 총 댓글 ${comments.size}개", "hidden" to hidden)
}

internal fun groupCommentReplies(replies: Array<dynamic>, previous: dynamic): dynamic {
    val result = js("new Map()")
    val groups = replies.groupBy { it.c_no as String }
    for ((no, group) in groups) {
        val old = previous?.get(no) as Array<dynamic>?
        result.set(no, if (old != null && old.size == group.size && group.indices.all { group[it] === old[it] }) old else group.toTypedArray())
    }
    return result
}

internal fun mentionComment(comments: Array<dynamic>, from: String, mention: String): dynamic {
    val match = mentionPattern.matchEntire(mention) ?: return undefinedValue()
    val nick = match.groupValues[1]
    val ip = match.groupValues[2]
    val index = comments.indexOfFirst { it.no == from }
    for (i in (if (index < 0) comments.lastIndex else index - 1) downTo 0) {
        val c = comments[i]
        if (c.name == nick && (if (ip.isNotEmpty()) c.ip == ip else truthy(c.user_id))) return c
    }
    return undefinedValue()
}

internal fun createCommentList(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val window: dynamic = js("window")
    val noFresh = s.noFresh
    val orders = mapOf("oldest" to "등록순", "newest" to "최신순", "replies" to "답글순")
    val summary: (dynamic) -> dynamic = { p ->
        val page = s.useUiStore { state: dynamic -> state.blockRevealed } == true
        val revealed = store { state: dynamic -> state.commentsRevealed } == true
        val result = commentSummary(p.comments as Array<dynamic>)
        val hidden = truthy(result.hidden)
        ui.span("text-muted-foreground", if (hidden) "${result.counts} (${result.hidden})" else result.counts,
            if (hidden && !page) ui.h(s.Button, props("variant" to "link", "size" to "xs",
                "className" to "ml-1.5 h-auto p-0 align-baseline text-sm text-link",
                "onClick" to { store.setState(props("commentsRevealed" to !revealed)) }),
                if (revealed) "다시 숨기기" else "가린 댓글 보기") else null)
    }
    val order: () -> dynamic = {
        val selected = store { state: dynamic -> state.commentOrder }
        val select: (Array<String>) -> Unit = { values ->
            val value = values.firstOrNull()
            if (value in orders) store.setState(props("commentOrder" to value))
        }
        val buttons = orders.entries.map { (value, label) -> ui.h(s.ToggleGroupItem,
            props("key" to value, "value" to value, "className" to "h-6 min-w-0 px-2 text-xs"), label)
        }.toTypedArray()
        ui.h(s.ToggleGroup, props("aria-label" to "댓글 순서", "variant" to "outline", "spacing" to 0,
            "value" to arrayOf<dynamic>(selected), "onValueChange" to select), buttons)
    }
    fun comment(c: dynamic, depth: Int, total: Int, admin: dynamic, extra: dynamic): dynamic {
        val props = props("key" to c.no, "comment" to c, "depth" to depth, "replyCount" to total, "isAdmin" to admin)
        return ui.h(s.Comment, js("Object.assign")(props, extra))
    }
    val threadRender: (dynamic) -> dynamic = { p ->
        val replies = p.replies as Array<dynamic>
        val parent = p.parent
        val collapsed = store { state: dynamic -> replies.size > 1 && state.collapsed.has(parent.no) == true && state.reply.commentNo !== parent.no } == true
        val children = replies.mapIndexed { index, child ->
            comment(child, 1, 0, p.isAdmin, props("lastReply" to (index == replies.lastIndex)))
        }.toTypedArray()
        val content = if (replies.size > 1) ui.h(s.Collapsible, props("open" to !collapsed),
            ui.h(s.CollapsibleContent, props("className" to
                "h-(--collapsible-panel-height) overflow-clip transition-[height,opacity] duration-200 ease-out data-ending-style:h-0 data-ending-style:opacity-0 data-starting-style:h-0 data-starting-style:opacity-0 motion-reduce:transition-none"),
                children)) else children
        ui.h(react.Fragment, null, comment(parent, 0, replies.size, p.isAdmin,
            props("threadOpen" to (!collapsed && replies.isNotEmpty()))), content)
    }
    val thread = react.memo(threadRender)

    val selectionTools: (dynamic) -> dynamic = { p ->
        val collapsed = store { state: dynamic -> state.collapsed }
        val selected = store { state: dynamic -> state.selectedComments }
        val selectable = (p.shownSelectable as Array<dynamic>).filter { comment ->
            !(comment.depth == 1 && p.foldable.has(comment.c_no) == true && collapsed.has(comment.c_no) == true)
        }.map { it.no as String }.toTypedArray()
        val picked = selectable.filter { selected.has(it) == true }.toTypedArray()
        val all = picked.isNotEmpty() && picked.size == selectable.size
        val id = react.useId()
        val confirming = react.useState(false)
        val deleting = react.useState(false)
        val remove: () -> Unit = {
            jsPromise {
                confirming[1](false)
                val state = store.getState()
                val pre = state.preData ?: return@jsPromise
                val signal = state.signalId
                deleting[1](true)
                val success = awaitJs(s.notifyDeleted(pre, s.adminDeleteComments(pre, picked)))
                deleting[1](false)
                if (truthy(success) && store.getState().signalId === signal) store.setState(props("selectedComments" to noFresh))
            }
        }
        val check: () -> Unit = {
            store.setState(props("selectedComments" to if (all) noFresh else js("new Set(selectable)")))
        }
        val block: () -> Unit = { store.setState(props("selectedComments" to js("new Set(picked)"), "blockPopup" to "comments")) }
        ui.div("flex items-center gap-2 px-8 py-1",
            ui.h(s.Field, props("orientation" to "horizontal", "className" to "w-auto"),
                ui.h(s.Checkbox, props("id" to id, "checked" to all, "indeterminate" to (picked.isNotEmpty() && !all), "onCheckedChange" to check)),
                ui.h(s.FieldLabel, props("htmlFor" to id, "className" to "font-normal"), "전체 선택")),
            if (picked.isNotEmpty()) ui.span("text-xs text-muted-foreground", "${picked.size}개 선택") else null,
            ui.div("ml-auto flex gap-1",
                ui.h(s.Button, props("size" to "xs", "variant" to "destructive",
                    "disabled" to (picked.isEmpty() || deleting[0] == true), "onClick" to { confirming[1](true) }),
                    ui.h(s.Trash2, props("data-icon" to "inline-start")), "삭제"),
                ui.h(s.Button, props("size" to "xs", "variant" to "destructive", "disabled" to picked.isEmpty(), "onClick" to block),
                    ui.h(s.Ban, props("data-icon" to "inline-start")), "차단")),
            if (confirming[0] == true) ui.h(s.ConfirmDialog, props("title" to "선택한 댓글 ${picked.size}개를 삭제할까요?",
                "confirmLabel" to "삭제", "danger" to true, "onConfirm" to remove, "onClose" to { confirming[1](false) })) else null)
    }

    val list: (dynamic) -> dynamic = { p ->
        val comments = p.comments as Array<dynamic>
        val page = s.useUiStore { state: dynamic -> state.blockRevealed } == true
        val post = store { state: dynamic -> state.commentsRevealed } == true
        val selectedOrder = store { state: dynamic -> state.commentOrder }
        val revealed = page || post
        val last = react.useRef()
        val models: () -> dynamic = {
            val visible = if (revealed) comments else comments.filter { it.blocked != "hide" && it.duplicates !== 0 }.toTypedArray()
            val shown = js("new Set(visible)")
            val replies = comments.filter { it.depth == 1 && shown.has(it) == true }.toTypedArray()
            last.current = groupCommentReplies(replies, last.current)
            props("shown" to shown, "parents" to threadParents(comments, selectedOrder), "replies" to last.current)
        }
        val model = react.useMemo(models, arrayOf<dynamic>(comments, revealed, selectedOrder))
        val shown = model.shown
        val parents = model.parents as Array<dynamic>
        val replies = model.replies
        val admin = s.isGalleryManager() == true
        val limit = react.useState(FIRST_THREADS)
        val replying = store { state: dynamic -> state.reply.replyNo !== null } == true
        val count = if (replying) Double.POSITIVE_INFINITY else limit[0] as Double
        val unfinished = count < parents.size
        val onReply: () -> dynamic = {
            if (replying) limit[1](Double.POSITIVE_INFINITY)
            undefinedValue()
        }
        react.useEffect(onReply, arrayOf(replying))
        val more: () -> dynamic = {
            if (!unfinished) undefinedValue()
            else {
                val increment: (Double) -> Double = { it + MORE_THREADS }
                val tick: () -> Unit = { limit[1](increment) }
                val timer = window.setTimeout(tick)
                val clear: () -> Unit = { window.clearTimeout(timer) }
                clear
            }
        }
        react.useEffect(more, arrayOf<dynamic>(count, parents.size))
        val endScroller = react.useRef(null)
        val bindEnd: () -> dynamic = {
            if (!unfinished) undefinedValue()
            else {
                val key: (dynamic) -> Unit = { event ->
                    if (event.key == "End" && s.isTyping(event) != true) {
                        val target = s.eventTarget(event)
                        val scroller = if (js("Element.prototype").isPrototypeOf(target) == true) target.closest(".refresher-frame-scroll") else null
                        if (scroller != null) {
                            event.preventDefault()
                            endScroller.current = scroller
                        }
                        limit[1](Double.POSITIVE_INFINITY)
                    }
                }
                window.addEventListener("keydown", key, true)
                val clear: () -> Unit = { window.removeEventListener("keydown", key, true) }
                clear
            }
        }
        react.useLayoutEffect(bindEnd, arrayOf(unfinished))
        val scrollEnd: () -> dynamic = {
            val scroller = endScroller.current
            if (!unfinished && scroller != null) {
                endScroller.current = null
                scroller.scrollTop = scroller.scrollHeight
            }
            undefinedValue()
        }
        react.useLayoutEffect(scrollEnd, arrayOf(unfinished))
        val root = react.useRef(null)
        val jump: (dynamic) -> Unit = jump@{ event ->
            val candidate = event.target
            val anchor = if (js("Element.prototype").isPrototypeOf(candidate) == true) candidate.closest("a.mention") else null
            if (anchor == null) return@jump
            val from = (anchor.closest("[data-no]")?.getAttribute("data-no") ?: "") as String
            val text = ((anchor.textContent ?: "") as String).trim()
            val target = mentionComment(comments, from, text) ?: return@jump
            if (shown.has(target) != true) return@jump
            val threadSize = (replies.get(target.c_no)?.length ?: 0) as Int
            if ((target.depth as Int) > 0 && threadSize > 1 && store.getState().collapsed.has(target.c_no) == true) {
                store.getState().toggleCollapse(target.c_no)
            }
            if (parents.indexOfFirst { it.no == target.no || it.no == target.c_no } >= count) limit[1](Double.POSITIVE_INFINITY)
            var tries = 0
            fun step() {
                val css: dynamic = js("CSS")
                val row = root.current?.querySelector("[data-no=\"${css.escape(target.no)}\"]")
                if (row == null) {
                    if (++tries < 60) window.requestAnimationFrame(::step)
                    return
                }
                row.scrollIntoView(props("block" to "center"))
                row.classList.add("animate-fresh-comment")
                val clear: () -> Unit = { row.classList.remove("animate-fresh-comment") }
                row.addEventListener("animationend", clear, props("once" to true))
            }
            window.requestAnimationFrame(::step)
        }
        val tools = if (admin) {
            val selectable = comments.filter { shown.has(it) == true && it.is_delete != "1" }.toTypedArray()
            val folded = parents.filter { shown.has(it) == true && ((replies.get(it.no)?.length ?: 0) as Int) > 1 }
                .map { it.no as String }.toTypedArray()
            ui.h(selectionTools, props("shownSelectable" to selectable, "foldable" to js("new Set(folded)")))
        } else null
        val rendered = parents.take(if (unfinished) count.toInt() else parents.size).map { parent ->
            val children = (replies.get(parent.no) as Array<dynamic>?) ?: noReplies
            if (shown.has(parent) != true) ui.h(react.Fragment, props("key" to parent.no),
                children.map { comment(it, 0, 0, admin, null) }.toTypedArray())
            else ui.h(thread, props("key" to parent.no, "parent" to parent, "replies" to children, "isAdmin" to admin))
        }.toTypedArray()
        ui.h("div", props("className" to "py-1", "ref" to root, "onClick" to jump, "data-comments-revealed" to if (post) true else undefinedValue()),
            tools, rendered, ui.h(s.CommentDeletePrompt, props("comments" to comments, "isAdmin" to admin)))
    }
    return props("CommentList" to list, "CommentOrder" to order, "CommentSummary" to summary)
}
