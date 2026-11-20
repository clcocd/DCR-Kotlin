package dev.dcrk

internal fun createPreviewPopups(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val dialog = s.ModalDialog

    fun header(title: String, description: String? = null): dynamic = ui.h(s.DialogHeader, null,
        ui.h(s.DialogTitle, null, title), if (description != null) ui.h(s.DialogDescription, null, description) else null)

    val radioGrid: (dynamic) -> dynamic = { p ->
        val id = react.useId() as String
        val items = p.items as Array<dynamic>
        val change: (dynamic) -> Unit = { next ->
            val found = items.firstOrNull { it[0] === next }
            if (found != null) p.onChange(found[0])
        }
        ui.h(s.RadioGroup, props("value" to p.value, "aria-label" to p.label, "className" to "grid-cols-3", "onValueChange" to change),
            items.map { entry ->
                val key = entry[0] as String
                ui.h(s.Field, props("key" to key, "orientation" to "horizontal"),
                    ui.h(s.RadioGroupItem, props("id" to "$id-$key", "value" to key)),
                    ui.h(s.FieldLabel, props("htmlFor" to "$id-$key", "className" to "font-normal"), entry[1]))
            }.toTypedArray())
    }

    val block: (dynamic) -> dynamic = { p ->
        val pre = store { state: dynamic -> state.preData }
        val comments = p.comments
        val day = react.useState("1")
        val reason = react.useState("1")
        val custom = react.useState("")
        val delete = react.useState(false)
        val type = react.useState(false)
        val sendingState = react.useState(false)
        val id = react.useId() as String
        val sending = sendingState[0] == true
        val close: () -> Unit = { store.setState(props("blockPopup" to false)) }
        val submit: () -> Unit = {
            if (pre != null && !sending) jsPromise {
                sendingState[1](true)
                val state = store.getState()
                val options = props("avoidHour" to day[0], "avoidReason" to reason[0],
                    "avoidReasonTxt" to if (reason[0] == "0") custom[0] else "",
                    "delChk" to delete[0], "userTypeChk" to type[0])
                val done = awaitJs(state.requestBlock(pre, options, comments))
                if (store.getState().signalId === state.signalId) {
                    if (truthy(done)) close()
                    else sendingState[1](false)
                }
            }
        }
        fun grid(label: String, items: dynamic, state: dynamic, extra: dynamic = null): dynamic = ui.div("flex flex-col gap-2",
            ui.tag("p", "font-bold", label), ui.h(radioGrid, props("label" to label, "items" to items, "value" to state[0], "onChange" to state[1])), extra)
        fun checkbox(key: String, label: String, state: dynamic): dynamic = ui.h(s.Field, props("orientation" to "horizontal"),
            ui.h(s.Checkbox, props("id" to "$id-$key", "checked" to state[0], "onCheckedChange" to state[1])),
            ui.h(s.FieldLabel, props("htmlFor" to "$id-$key", "className" to "font-normal"), label))
        val change: (dynamic) -> Unit = { custom[1](it.target.value) }
        ui.h(dialog, props("onClose" to close, "className" to "sm:max-w-[440px]", "focusOnOpen" to "keyboard"),
            header(if (comments != null) "댓글 작성자 차단 (${comments.length}개)" else "유저 차단"),
            grid("기간", entriesOf(s.days), day),
            grid("사유", s.reasons, reason, if (reason[0] == "0") ui.h(s.Input, props("value" to custom[0],
                "aria-label" to "차단 사유", "placeholder" to "차단 사유 직접 입력 (한글 20자 이내)",
                "maxLength" to 20, "ref" to s.focusOnMount, "onChange" to change)) else null),
            ui.div("flex flex-col gap-2", checkbox("del", if (comments != null) "선택한 댓글 삭제" else "선택한 글 삭제", delete),
                checkbox("ip", "식별 코드 차단 시 IP 동시 차단", type)),
            ui.h(s.DialogActions, null, ui.h(s.Button, props("variant" to "destructive", "disabled" to sending, "onClick" to submit),
                if (sending) ui.h(s.Spinner, props("aria-hidden" to true, "data-icon" to "inline-start")) else null, "차단")))
    }

    val captcha: (dynamic) -> dynamic = { p ->
        val prompt = p.captcha
        val code = react.useState("")
        val input = react.useRef(null)
        val trimmed = (code[0] as String).trim()
        fun end(value: String) {
            prompt.resolve(value)
            store.setState(props("captcha" to null))
        }
        val send: () -> Unit = { if (trimmed.isNotEmpty()) end(trimmed) }
        val change: (dynamic) -> Unit = { code[1](it.target.value) }
        ui.h(dialog, props("onClose" to { end("") }, "className" to "sm:max-w-[320px]", "focusOnOpen" to input),
            header("자동입력 방지 코드"),
            ui.h("img", props("src" to prompt.url, "alt" to "자동입력 방지 코드", "className" to "block w-full rounded-lg")),
            ui.h(s.SubmitForm, props("onSubmit" to send),
                ui.h(s.Input, props("ref" to input, "value" to code[0], "aria-label" to "자동입력 방지 코드",
                    "placeholder" to "자동입력 방지 코드", "onChange" to change)),
                ui.h(s.DialogActions, null, ui.h(s.Button, props("type" to "submit", "disabled" to trimmed.isEmpty()), "전송"))))
    }

    val turnstile: (dynamic) -> dynamic = { p ->
        val prompt = p.turnstile
        val host = react.useRef(null)
        val wait: () -> dynamic = {
            val element = host.current
            var done = false
            val ignore: () -> dynamic = { undefinedValue() }
            val finish: (dynamic) -> Unit = { value ->
                done = true
                prompt.resolve(value)
                val update: (dynamic) -> dynamic = { state ->
                    if (state.turnstile === prompt) props("turnstile" to null) else state
                }
                store.setState(update)
            }
            s.sendMessage("refresher:turnstileToken", props("config" to prompt.config, "visible" to true)).catch(ignore).then(finish)
            val cancel: () -> Unit = {
                if (!done && element != null) {
                    val event = s.cancelEvent
                    element.dispatchEvent(js("new Event(event)"))
                }
            }
            cancel
        }
        react.useEffect(wait, arrayOf<dynamic>(prompt))
        val close: () -> Unit = { prompt.resolve(undefinedValue()); store.setState(props("turnstile" to null)) }
        ui.h(dialog, props("onClose" to close, "className" to "sm:max-w-[360px]"),
            header("보안 확인", "체크박스를 누르면 댓글을 보냅니다."),
            ui.h("div", props("ref" to host, (s.turnstileHost as String) to "", "className" to "flex min-h-[65px] justify-center")),
            ui.h(s.DialogActions))
    }

    val admin: () -> dynamic = {
        val notice = store { state: dynamic -> state.preData?.notice == true } == true
        val recommended = store { state: dynamic -> state.preData?.recommend == true } == true
        val request = store { state: dynamic -> state.requestManage }
        val settings = s.useContentModuleSettings("preview")
        val confirm = react.useRef(createDoublePress(3000.0)).current
        val keys = settings.useKeyPress == true
        val holdFocus: (dynamic) -> Unit = { it.preventDefault() }
        fun item(id: String, label: String, details: String, icon: dynamic, active: Boolean = false,
            danger: Boolean = false, hint: dynamic = null): dynamic {
            val choose: () -> Unit = {
                if (id == "block") store.setState(props("blockPopup" to "post"))
                else if (confirm("$id:${store.getState().signalId}") == true) request(id)
                else s.useUiStore.getState().showToast("한 번 더 누르면 ${details}합니다.")
            }
            return ui.h(s.Button, props("key" to id, "variant" to if (danger) "destructive" else if (active) "default" else "secondary",
                "className" to "justify-start", "onMouseDown" to holdFocus, "onClick" to choose),
                ui.h(icon, props("data-icon" to "inline-start")), ui.span("flex-1 text-left", label),
                if (truthy(hint)) ui.h(s.Kbd, null, hint) else null)
        }
        ui.div("pointer-events-auto fixed top-[20%] left-0 w-[150px] rounded-r-xl bg-card p-2 text-card-foreground shadow-md ring-1 ring-foreground/10 duration-150 animate-in fade-in",
            ui.tag("p", "mb-2 ml-1 text-xs font-medium text-muted-foreground", "관리"),
            ui.div("flex flex-col gap-1",
                item("notice", if (notice) "공지 해제" else "공지 등록", s.labels.notice[if (notice) 1 else 0] as String, s.Megaphone, active = notice),
                item("recommend", if (recommended) "개념글 해제" else "개념글 등록", s.labels.recommend[if (recommended) 1 else 0] as String, s.Star, active = recommended),
                item("bump", "끌올", "게시글을 끌올", s.ArrowBigUpDash),
                item("block", "차단", "차단", s.Ban, danger = true, hint = if (keys) (settings.blockKey as String).uppercase() else null),
                item("delete", "삭제", "게시글을 삭제", s.Trash2, danger = true, hint = if (keys) (settings.deleteKey as String).uppercase() else null)))
    }

    val popups: () -> dynamic = {
        val blockPopup = store { state: dynamic -> state.blockPopup }
        val selected = store { state: dynamic -> state.selectedComments }
        val userCaptcha = store { state: dynamic -> state.captcha }
        val challenge = store { state: dynamic -> state.turnstile }
        val info = store { state: dynamic -> state.dcconInfo }
        val viewer = store { state: dynamic -> state.viewer }
        ui.h(react.Fragment, null,
            if (blockPopup == "post") ui.h(block) else null,
            if (blockPopup == "comments") ui.h(block, props("comments" to jsArray(selected))) else null,
            if (userCaptcha != null) ui.h(captcha, props("key" to userCaptcha.url, "captcha" to userCaptcha)) else null,
            if (challenge != null) ui.h(turnstile, props("turnstile" to challenge)) else null,
            if (truthy(info)) ui.h(s.DcconInfoPopup, props("key" to info, "code" to info)) else null,
            if (viewer != null) ui.h(s.ImageViewer, props("images" to viewer.images, "index" to viewer.index)) else null)
    }
    return props("AdminPanel" to admin, "Popups" to popups)
}
