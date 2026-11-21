package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise

private val iconPattern = Regex("""writer_nikcon[^>]*>\s*<img\b[^>]*?\ssrc=["']?([^"'\s>]+)""")
private val ipPattern = Regex("""class=["']?ip["']?[^>]*>\s*\(([^)]+)\)""")
private val dcconPattern = Regex("""<(img|video) class=|<div class="coment_dccon_txt|class="double_txtcon(?=[ "	])""")
private const val THREAD_OPEN = "after:pointer-events-none after:absolute after:bottom-0 after:left-11 after:h-2 after:border-l-2 after:border-zinc-300 after:content-[''] dark:after:border-zinc-700"
private const val REPLY = "ml-8 before:pointer-events-none before:absolute before:top-0 before:left-3 before:h-4.5 before:w-3.5 before:rounded-bl-[10px] before:border-b-2 before:border-l-2 before:border-zinc-300 before:content-[''] dark:before:border-zinc-700"
private const val REPLY_CONTINUES = "after:pointer-events-none after:absolute after:inset-y-0 after:left-3 after:border-l-2 after:border-zinc-300 after:content-[''] dark:after:border-zinc-700"

internal fun extractCommentIcon(html: dynamic): dynamic =
    (html as String?)?.let { iconPattern.find(it)?.groupValues?.get(1) } ?: undefinedValue()
internal fun extractCommentIp(html: dynamic): dynamic =
    (html as String?)?.let { ipPattern.find(it)?.groupValues?.get(1) } ?: undefinedValue()

internal fun extractCommentNickname(html: dynamic): dynamic {
    val markup = html as String? ?: return undefinedValue()
    val doc = js("new DOMParser()").parseFromString(markup, "text/html")
    val nickname = doc.querySelector(".nickname.an")?.textContent as String?
    return if (nickname.isNullOrBlank()) undefinedValue() else nickname.trim()
}

internal fun createCommentView(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val string: dynamic = js("String")

    val body: (dynamic) -> dynamic = { p ->
        val ref = react.useRef(null)
        val dccon = p.isDccon == true
        val dependencies = arrayOf<dynamic>(p.html, dccon)
        val fit: () -> dynamic = {
            if (!dccon || ref.current == null) undefinedValue()
            else {
                val stops = jsArray(ref.current.querySelectorAll(".coment_dccon_txt")).map { s.fitTxtconNearView(it) }
                val dispose: () -> Unit = { for (stop in stops) stop() }
                dispose
            }
        }
        react.useLayoutEffect(fit, dependencies)
        val watch: () -> dynamic = { if (dccon && ref.current != null) s.watchGifVideos(ref.current) else undefinedValue() }
        react.useEffect(watch, dependencies)
        val mark: () -> dynamic = {
            if (dccon && ref.current != null) s.markPressable(ref.current, false)
            undefinedValue()
        }
        react.useEffect(mark, dependencies)
        ui.h("div", props("ref" to ref, "className" to s.cn("refresher-html refresher-comment-html mt-1",
            if (dccon) false else "whitespace-pre-line", if (p.isDeleted == true) "text-muted-foreground" else false),
            "data-dccon" to if (dccon) true else undefinedValue(), "onClick" to if (dccon) s.openDcconInfo else undefinedValue(),
            "onKeyDown" to if (dccon) s.pressContents else undefinedValue(), "dangerouslySetInnerHTML" to props("__html" to p.html)))
    }
    val comment: (dynamic) -> dynamic = { p ->
        val item = p.comment
        val no = item.no
        val depth = p.depth as Int
        val replyCount = p.replyCount as Int
        val admin = p.isAdmin == true
        val replying = store { state: dynamic -> state.reply.replyNo === no } == true
        val collapsed = store { state: dynamic -> state.collapsed.has(no) } == true
        val fresh = store { state: dynamic -> state.freshComments.has(no) } == true
        val toggleCollapse = store { state: dynamic -> state.toggleCollapse }
        val selected = store { state: dynamic -> state.selectedComments.has(no) }
        val toggleSelected = store { state: dynamic -> state.toggleSelected }
        val author = store { state: dynamic -> state.post?.user?.id ?: undefinedValue() }
        val allowReply = store { state: dynamic -> state.allowReply } == true
        val ip = if (truthy(item.ip)) item.ip else extractCommentIp(item.gallog_icon) ?: extractCommentIp(item.nickname)
        val user = props("nick" to item.name, "id" to item.user_id, "ip" to ip, "image" to extractCommentIcon(item.gallog_icon))
        val op = truthy(user.id) && user.id === author
        val who = user.nick ?: user.id ?: user.ip
        val deleted = item.is_delete == "1"
        val nested = (item.depth as Int) > 0
        val canReply = !deleted && allowReply && (nested || item.voice !== undefinedValue() || item.reply_w != "N")
        val canDelete = !deleted && (item.del_btn == "Y" || item.my_cmt == "Y" || admin || (!truthy(item.user_id) && truthy(item.ip)))
        val deleting = store { state: dynamic -> state.deletingComments.has(no) }
        val html = item.memo as String
        val isDccon = dcconPattern.containsMatchIn(html)
        val variants: (dynamic) -> dynamic = { extra -> s.cn(s.buttonVariants(props("size" to "icon-xs", "variant" to "ghost", "className" to extra))) }
        val checkbox = if (admin && !deleted) ui.h(s.Checkbox, props("className" to "mr-1", "aria-label" to "$who 댓글 선택",
            "checked" to selected, "onCheckedChange" to { toggleSelected(no) })) else null
        val expand = if (depth == 0 && replyCount > 1) ui.h("button",
            props("type" to "button", "className" to variants("aria-expanded:not-hover:bg-transparent"), "aria-label" to "$who 댓글의 답글 ${replyCount}개",
                "aria-expanded" to !collapsed, "onClick" to { toggleCollapse(no) }, "title" to if (collapsed) "답글 펼치기" else "답글 접기"),
            ui.h(s.ChevronDown, props("className" to s.cn("transition-transform motion-reduce:transition-none", if (collapsed) "-rotate-90" else false)))) else null
        fun toggleReply() {
            val next = if (replying) s.noReply else props("commentNo" to if (nested) item.c_no else no, "replyNo" to no)
            store.setState(props("reply" to next))
        }
        val clickEmpty: (dynamic) -> Unit = click@{ event ->
            if (!canReply || event.button != 0) return@click
            val target = event.target
            if (js("Element.prototype").isPrototypeOf(target) != true ||
                target.closest("a, button, input, textarea, select, label, [role='button'], [contenteditable], .refresher-user, img, video, audio, iframe, .coment_dccon_txt, .comment_dccon") != null ||
                js("window.getSelection()")?.isCollapsed == false) return@click
            if (isDccon && target.closest(".refresher-comment-html") != null) return@click
            toggleReply()
        }
        val reply = if (canReply) ui.h("button", props("type" to "button", "className" to variants(if (replying) "text-link" else false),
            "aria-label" to "$who 댓글에 답글", "aria-pressed" to replying, "onClick" to { toggleReply() }),
            ui.h(if (replying) s.Check else s.ReplyIcon)) else null
        val remove = if (canDelete) ui.h("button", props("type" to "button", "className" to variants(undefinedValue()),
            "aria-label" to "$who 댓글 삭제", "disabled" to deleting, "onClick" to {
                val kind = if (admin || truthy(item.user_id)) "confirm" else "password"
                store.setState(props("deletePrompt" to props("no" to no, "kind" to kind)))
            }), ui.h(s.X)) else null
        val voice = item.voice
        val player = if (voice != null) ui.div("mt-1", if (truthy(voice.iframe)) ui.h("iframe",
            props("src" to voice.src, "width" to 280, "height" to 54, "className" to "block border-0", "title" to "음성 댓글"))
        else ui.h("audio", props("controls" to true, "src" to voice.src, "className" to "block"))) else null
        val css = s.cn("refresher-comment relative px-8 py-2 [contain-intrinsic-size:auto_48px] [content-visibility:auto] hover:bg-muted/50",
            if (fresh) "animate-fresh-comment" else false, if (deleted) "opacity-60" else false,
            if (p.threadOpen == true) THREAD_OPEN else false, if (depth == 1) REPLY else false,
            if (depth == 1 && p.lastReply != true) REPLY_CONTINUES else false)
        val row = ui.h("div", props("className" to css, "data-no" to no, "data-deleted" to if (deleted) true else undefinedValue(),
            "data-blocked" to item.blocked, "data-low-activity" to item.lowActivity, "onClick" to clickEmpty,
            "data-duplicate" to if (item.duplicates === 0) true else undefinedValue(), "data-fresh" to if (fresh) true else undefinedValue()),
            ui.div("flex items-center justify-between gap-2",
                ui.div("flex min-w-0 items-center gap-1", checkbox, ui.h(s.UserCard, props("user" to user, "op" to op)),
                    if (truthy(item.duplicates)) ui.span("text-xs whitespace-nowrap text-muted-foreground", "같은 댓글 ×${item.duplicates}") else null, expand),
                ui.div("flex shrink-0 items-center gap-3", reply, remove,
                    ui.h(s.TimeStamp, props("date" to string(item.reg_date ?: item.date_time ?: ""))))),
            player, ui.h(body, props("html" to html, "isDccon" to isDccon, "isDeleted" to deleted)))
        val inlineForm = if (replying && canReply) ui.h(s.WriteComment, props("inlineReply" to true, "key" to no)) else null
        ui.h(react.Fragment, null, row, inlineForm)
    }

    fun notify(pre: dynamic, request: dynamic): Promise<Boolean> = jsPromise {
        val deleted = awaitJs(s.notifyManage(request, "댓글을 삭제했습니다.", "댓글을 삭제하지 못했습니다. 잠시 후 다시 시도해 주세요.")) as Boolean
        if (deleted) s.refreshCommentsOf(pre)
        deleted
    }
    fun setDeleting(no: dynamic, deleting: Boolean) {
        val update: (dynamic) -> dynamic = { props("deletingComments" to s.toggled(it.deletingComments, no, deleting)) }
        store.setState(update)
    }
    fun remove(no: dynamic, admin: Boolean, password: String) = jsPromise {
        store.setState(props("deletePrompt" to null))
        val state = store.getState()
        val pre = state.preData ?: return@jsPromise
        val post = state.post ?: return@jsPromise
        val id = state.signalId
        setDeleting(no, true)
        val request = if (admin) s.adminDeleteComments(pre, arrayOf<dynamic>(no)) else s.deleteOwnComment(pre, post, no, password)
        awaitJs(notify(pre, request))
        if (store.getState().signalId === id) setDeleting(no, false)
    }
    fun warning(fields: Array<dynamic>, date: String): dynamic {
        fun field(key: String): dynamic {
            val entry = fields.firstOrNull { it[0] == key }
            return if (entry == null) undefinedValue() else entry[1]
        }
        if (field("notice_comment_del_avoid") != "1" || date.isEmpty() ||
            Date.now() - (s.parseDate(date).getTime() as Double) >= 5 * 60_000) return undefinedValue()
        return "공지글에 작성한 댓글을 5분 내 삭제 시 매니저에 의해 [${field("notice_comment_del_avoid_msg") ?: ""}] 사유로 차단됩니다."
    }
    val passwordDialog: (dynamic) -> dynamic = { p ->
        val initial: () -> dynamic = { s.savedNonmember().pw }
        val saved = react.useState(initial)[0]
        val password = react.useState("")
        val input = react.useRef(null)
        val value = if (truthy(password[0])) password[0] else saved
        val submit: () -> Unit = { if (truthy(value)) p.onSubmit(value) }
        val change: (dynamic) -> Unit = { password[1](it.target.value) }
        ui.h(s.ModalDialog, props("onClose" to p.onClose, "className" to "sm:max-w-[320px]", "focusOnOpen" to input),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, "댓글 비밀번호"),
                if (truthy(p.warning)) ui.tag("p", "text-sm text-destructive", p.warning) else null),
            ui.h(s.SubmitForm, props("onSubmit" to submit),
                ui.h(s.Input, props("ref" to input, "type" to "password", "value" to password[0], "aria-label" to "댓글 비밀번호",
                    "placeholder" to if (truthy(saved)) "비밀번호 (저장됨)" else "비밀번호", "onChange" to change)),
                ui.h(s.DialogActions, null, ui.h(s.Button, props("type" to "submit", "variant" to "destructive", "disabled" to !truthy(value)), "삭제"))))
    }
    val prompt: (dynamic) -> dynamic = prompt@{ p ->
        val current = store { state: dynamic -> state.deletePrompt }
        val fields = store { state: dynamic -> state.post?.commentForm?.fields }
        val target = if (current != null) (p.comments as Array<dynamic>).firstOrNull { it.no == current.no && it.is_delete != "1" } else null
        val gone = current != null && target == null
        val close: () -> Unit = { store.setState(props("deletePrompt" to null)) }
        react.useEffect({ if (gone) close(); undefinedValue() }, arrayOf(gone))
        if (current == null || target == null) return@prompt null
        val admin = p.isAdmin == true
        val notice = if (admin || fields == null) null else warning(fields as Array<dynamic>, string(target.reg_date ?: target.date_time ?: "") as String)
        val submit: (String) -> Unit = { remove(target.no, admin, it) }
        if (current.kind == "confirm") ui.h(s.ConfirmDialog, props("title" to if (truthy(notice)) "$notice 댓글을 삭제할까요?" else "댓글을 삭제할까요?",
            "confirmLabel" to "삭제", "danger" to true, "onConfirm" to { submit("") }, "onClose" to close))
        else ui.h(passwordDialog, props("warning" to notice, "onSubmit" to submit, "onClose" to close))
    }

    return props("Comment" to react.memo(comment), "CommentDeletePrompt" to prompt, "notifyDeleted" to ::notify, "noticeDeleteWarning" to ::warning)
}
