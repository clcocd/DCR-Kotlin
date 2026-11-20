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
            header("댓글 작성자 차단 (${comments.length}개)"),
            grid("기간", entriesOf(s.days), day),
            grid("사유", s.reasons, reason, if (reason[0] == "0") ui.h(s.Input, props("value" to custom[0],
                "aria-label" to "차단 사유", "placeholder" to "차단 사유 직접 입력 (한글 20자 이내)",
                "maxLength" to 20, "ref" to s.focusOnMount, "onChange" to change)) else null),
            ui.div("flex flex-col gap-2", checkbox("del", "선택한 댓글 삭제", delete),
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

    val popups: () -> dynamic = {
        val blockPopup = store { state: dynamic -> state.blockPopup }
        val selected = store { state: dynamic -> state.selectedComments }
        val userCaptcha = store { state: dynamic -> state.captcha }
        val challenge = store { state: dynamic -> state.turnstile }
        val info = store { state: dynamic -> state.dcconInfo }
        val viewer = store { state: dynamic -> state.viewer }
        ui.h(react.Fragment, null,
            if (blockPopup == "comments") ui.h(block, props("comments" to jsArray(selected))) else null,
            if (userCaptcha != null) ui.h(captcha, props("key" to userCaptcha.url, "captcha" to userCaptcha)) else null,
            if (challenge != null) ui.h(turnstile, props("turnstile" to challenge)) else null,
            if (truthy(info)) ui.h(s.DcconInfoPopup, props("key" to info, "code" to info)) else null,
            if (viewer != null) ui.h(s.ImageViewer, props("images" to viewer.images, "index" to viewer.index)) else null)
    }
    return props("Popups" to popups)
}
