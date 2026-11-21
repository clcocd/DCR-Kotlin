package dev.dcrk

import kotlin.js.Promise
import kotlin.math.max

internal fun createWriteCommentView(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val drafts = s.drafts
    val sendingKeys = mutableSetOf<String>()
    val window: dynamic = js("window")
    val document: dynamic = js("document")
    val performance: dynamic = js("performance")
    val console: dynamic = js("console")
    val noDccon = props("list" to emptyArray<dynamic>(), "big" to false)

    fun randomPassword(): String {
        val bytes = js("crypto.getRandomValues(new Uint8Array(8))")
        return (0..7).joinToString("") { ((bytes[it] as Int) % 36).toString(36) }
    }

    return { p: dynamic ->
        val isInline = p?.inlineReply == true
        val selectedReply = store { state: dynamic -> state.reply }
        val reply = if (isInline) selectedReply else s.noReply
        val replyName = store { state: dynamic ->
            val list = state.comments as Array<dynamic>?
            list?.firstOrNull { it.no == state.reply.replyNo }?.name ?: undefinedValue()
        }
        val initialKey: () -> String = {
            val pre = store.getState().preData
            if (pre != null) (s.postKey(pre) as String) + (if (isInline) "|reply:${reply.replyNo}" else "") else ""
        }
        val key = react.useState(initialKey)[0] as String
        val initialDraft: () -> dynamic = { if (key in sendingKeys) "" else drafts.get(key) ?: "" }
        val initialText = react.useState(initialDraft)[0]
        val initialLogin: () -> Boolean = { document.querySelector("#login_box .user_info .nickname > em") != null }
        val login = react.useState(initialLogin)[0] == true
        val account = react.useState(s.loggedInUserId)[0]
        val saved = react.useState(s.savedNonmember)[0]
        val nickState = react.useState(if (truthy(saved.nick)) saved.nick else "ㅇㅇ")
        val nick = nickState[0] as String
        val initialPw: () -> dynamic = { if (truthy(saved.pw)) saved.pw else randomPassword() }
        val passwordState = react.useState(initialPw)
        val password = passwordState[0] as String
        val passwordEdited = react.useState(false)
        val dcconState = react.useState(noDccon)
        val dcconOpen = react.useState(false)
        val txtconState = react.useState(false)
        val txtcon = txtconState[0] == true
        val doubleState = react.useState(null)
        val slots = doubleState[0] as Array<dynamic>?
        val activeSlot = react.useState(0)
        val activeIndex = activeSlot[0] as Int
        val pickerMode = react.useState(false)
        val selectedImage = slots?.get(activeIndex)?.type == "dccon"
        val colors = react.useState(props("bg" to "3b4890", "txt" to "ffffff"))
        val showInputs = react.useState(false)
        val box = react.useRef(null)
        val bottom = react.useRef(null)
        val toggleInputs: () -> Unit = {
            bottom.current = box.current?.getBoundingClientRect()?.bottom
            val flip: (Boolean) -> Boolean = { !it }
            showInputs[1](flip)
        }
        val keepPosition: () -> dynamic = {
            val before = bottom.current
            bottom.current = null
            val element = box.current
            val scroller = element?.closest(".refresher-frame-scroll")
            if (before != null && element != null && scroller != null) {
                scroller.scrollTop = (scroller.scrollTop as Double) + (element.getBoundingClientRect().bottom as Double) - (before as Double)
            }
            undefinedValue()
        }
        react.useLayoutEffect(keepPosition, arrayOf<dynamic>(showInputs[0]))
        val sendingState = react.useState(false)
        val sending = sendingState[0] == true
        val textarea = react.useRef(null)
        val focusedAt = react.useRef(null)
        val touch: () -> Unit = {
            if (focusedAt.current == null) focusedAt.current = performance.now()
            val config = store.getState().post?.commentForm?.turnstile
            if (config != null) s.prefetchTurnstile(config)
        }
        val beforeTxtcon = react.useRef(null)
        val focusReply: () -> dynamic = {
            if (isInline && truthy(reply.replyNo)) textarea.current?.focus()
            undefinedValue()
        }
        react.useEffect(focusReply, arrayOf<dynamic>(reply.replyNo))

        fun saveDraft(text: String) {
            if (text.isNotEmpty()) drafts.set(key, text) else drafts.delete(key)
        }

        fun textSlot(text: String, color: dynamic): dynamic =
            props("type" to "txtcon", "text" to text, "colors" to color)

        fun editSlot(update: (dynamic) -> dynamic) {
            if (slots == null || selectedImage) return
            val edit: (Array<dynamic>?) -> Array<dynamic>? = { current ->
                current?.copyOf()?.also { it[activeIndex] = update(it[activeIndex]) }
            }
            doubleState[1](edit)
        }

        fun selectSlot(index: Int) {
            val slot = slots?.get(index) ?: return
            activeSlot[1](index)
            val node = textarea.current
            if (node != null) node.value = if (slot.type == "txtcon") slot.text else ""
            if (slot.type == "txtcon") colors[1](slot.colors)
        }

        fun closePicker() {
            dcconOpen[1](false)
            pickerMode[1](false)
        }

        fun startDouble(second: dynamic) {
            val node = textarea.current
            val first = textSlot(s.normalizeTxtcon(node?.value ?: "") as String, colors[0])
            doubleState[1](arrayOf<dynamic>(first, second))
            val editSecond = second.type == "txtcon"
            activeSlot[1](if (editSecond) 1 else 0)
            if (node != null) node.value = if (editSecond) second.text else first.text
            closePicker()
        }

        fun removeSlot(index: Int) {
            val kept = slots?.get(1 - index) ?: return
            val node = textarea.current
            doubleState[1](null)
            activeSlot[1](0)
            if (kept.type == "txtcon") {
                if (node != null) node.value = kept.text
                colors[1](kept.colors)
                saveDraft(kept.text as String)
            } else {
                dcconState[1](props("list" to arrayOf<dynamic>(kept), "big" to false))
                txtconState[1](false)
                beforeTxtcon.current = null
            }
        }

        fun swapSlots() {
            val pair = slots ?: return
            doubleState[1](arrayOf<dynamic>(pair[1], pair[0]))
            activeSlot[1](1 - activeIndex)
        }

        fun submit(pick: dynamic = dcconState[0], useDouble: Boolean = true): Promise<dynamic> = jsPromise {
            val state = store.getState()
            val post = state.post ?: return@jsPromise
            val pre = state.preData ?: return@jsPromise
            val raw = (textarea.current?.value ?: "") as String
            val useDccon = (pick.list.length as Int) > 0
            val double = if (useDouble && !useDccon) slots?.copyOf() else null
            val sendTxtcon = txtcon && !useDccon && double == null
            val text = (if (sendTxtcon) s.normalizeTxtcon(raw) as String else raw).trim()
            if (sending || (!useDccon && double == null && text.isEmpty())) return@jsPromise
            val toast = s.useUiStore.getState().showToast
            if (double != null) {
                val active = double[activeIndex]
                if (active.type == "txtcon") double[activeIndex] = textSlot(s.normalizeTxtcon(raw) as String, active.colors)
                if (double.any { it.type == "txtcon" && (it.text as String).isBlank() }) {
                    toast("글자콘 내용을 입력해주세요.", "warning")
                    return@jsPromise
                }
            }
            val target = s.postKey(pre) as String
            val draftAtSend = if (double == null) raw else drafts.get(key)
            if (target in sendingKeys) {
                toast("앞서 보낸 댓글을 처리하는 중입니다. 잠시 후 다시 시도해 주세요.", "warning")
                return@jsPromise
            }
            if (!login && (nick.isEmpty() || password.isEmpty())) {
                toast("닉네임과 비밀번호를 입력해 주세요.", "error")
                return@jsPromise
            }
            if (useDccon) dcconState[1](pick)
            sendingState[1](true)
            sendingKeys.add(target)
            val sentReply = if (isInline) state.reply else s.noReply
            fun samePost() = store.getState().signalId === state.signalId
            try {
                val code = if (post.requireCommentCaptcha == true) {
                    val response = awaitJs(state.openCaptcha(s.captchaImage(pre, "comment")))
                    if (!truthy(response) || !samePost()) return@jsPromise
                    response
                } else undefinedValue()
                val user = props("name" to if (login) "" else nick, "pw" to if (login) undefinedValue() else password)
                val config = post.commentForm?.turnstile
                val turnstile = if (config != null) {
                    val focused = focusedAt.current as Double?
                    focusedAt.current = null
                    val elapsed: () -> Double = {
                        if (focused == null) 0.0 else max(1.0, js("Math").round((performance.now() as Double) - focused) as Double)
                    }
                    val ask: (dynamic) -> dynamic = {
                        if (samePost()) state.openTurnstile(it) else Promise.resolve<dynamic>(undefinedValue())
                    }
                    val response = awaitJs(s.turnstileFields(config, elapsed, ask))
                    if (!truthy(response) || !samePost()) return@jsPromise
                    response
                } else undefinedValue()
                val payload = props("text" to text, "dccons" to pick.list, "bigDccon" to pick.big,
                    "txtcon" to if (sendTxtcon) colors[0] else undefinedValue(),
                    "txtconDouble" to (double ?: undefinedValue()))
                val outcome = awaitJs(s.postComment(pre, post, user, payload, sentReply, code, turnstile))
                when {
                    outcome.ok == true -> {
                        if (!useDccon) {
                            if (textarea.current?.value === raw) textarea.current.value = ""
                            if (drafts.get(key) === draftAtSend) drafts.delete(key)
                        }
                        if (txtcon) txtconState[1](false)
                        if (double != null) doubleState[1](null)
                        if (!login) s.saveNonmember(nick, password)
                        val clearReply: (dynamic) -> dynamic = { current ->
                            if (current.reply === sentReply) props("reply" to s.noReply) else current
                        }
                        store.setState(clearReply)
                        s.refreshCommentsOf(pre)
                    }
                    outcome.captcha == true -> {
                        val open: () -> Unit = { window.open(pre.link, "_blank", "noopener") }
                        toast("자동입력 방지 확인이 필요합니다. 원문에서 작성해 주세요.", "warning", 8000,
                            props("label" to "원문 열기", "run" to open))
                    }
                    else -> toast(outcome.message, "error")
                }
            } catch (error: Throwable) {
                console.error("Comment submit failed:", error)
                s.refreshCommentsOf(pre)
                toast(if (s.isTimeoutError(error) == true) "응답이 없어 작성됐는지 알 수 없습니다. 댓글 목록을 확인해 주세요."
                    else "댓글을 작성하지 못했습니다. 잠시 후 다시 시도해 주세요.", "error")
            } finally {
                sendingKeys.remove(target)
                sendingState[1](false)
                val clear: (dynamic) -> dynamic = { if (it.list === pick.list) noDccon else it }
                dcconState[1](clear)
            }
            undefinedValue()
        }

        fun applyTxtcon() {
            val node = textarea.current ?: return
            val next = s.normalizeTxtcon(node.value)
            if (node.value != next) node.value = next
            if (slots == null) saveDraft(node.value as String)
        }

        fun exitTxtcon() {
            val node = textarea.current
            val before = beforeTxtcon.current
            if (node != null && before != null && node.value === s.normalizeTxtcon(before)) {
                node.value = before
                saveDraft(before as String)
            }
            beforeTxtcon.current = null
            txtconState[1](false)
            doubleState[1](null)
            pickerMode[1](false)
        }

        val mode = when {
            truthy(reply.replyNo) -> "답글"
            txtcon -> "글자콘"
            (dcconState[0].list.length as Int) > 0 -> "디시콘"
            else -> "댓글"
        }
        val fields = if (!login && showInputs[0] == true) {
            val changeNick: (dynamic) -> Unit = { nickState[1](it.target.value) }
            val changePw: (dynamic) -> Unit = {
                passwordEdited[1](true)
                passwordState[1](it.target.value)
            }
            val edited = passwordEdited[0] == true
            val placeholder = if (edited) "비밀번호" else if (truthy(saved.pw)) "비밀번호 (저장됨)" else "비밀번호 (자동 생성)"
            ui.div("mb-2 flex gap-2",
                ui.h(s.Input, props("value" to nick, "aria-label" to "닉네임", "placeholder" to "닉네임",
                    "maxLength" to 20, "className" to "flex-1", "onChange" to changeNick)),
                ui.h(s.Input, props("type" to "password", "value" to if (edited) password else "", "aria-label" to "비밀번호",
                    "placeholder" to placeholder, "className" to "flex-1", "onChange" to changePw)))
        } else null
        val toReply = if (truthy(reply.replyNo)) {
            val cancel: () -> Unit = {
                store.setState(props("reply" to s.noReply))
                val focus: (dynamic) -> Unit = { _: dynamic ->
                    document.querySelector("refresher-root")?.shadowRoot?.querySelector("[aria-label='댓글 입력']")?.focus()
                }
                window.requestAnimationFrame(focus)
            }
            ui.div("mb-1 flex items-center gap-1 text-xs text-muted-foreground",
                ui.span("truncate", replyName, "에게 답글"),
                ui.h(s.TooltipButton, props("tip" to "답글 취소", "variant" to "ghost", "size" to "icon-xs", "onClick" to cancel), ui.h(s.X)))
        } else null
        fun rememberInput(text: String) {
            if (slots == null) saveDraft(text)
            else editSlot { textSlot(text, it.colors) }
        }
        val change: (dynamic) -> Unit = { event ->
            val native = event.nativeEvent
            if (txtcon && !(js("InputEvent.prototype").isPrototypeOf(native) == true && native.isComposing == true)) applyTxtcon()
            rememberInput(event.target.value as String)
        }
        val composition: (dynamic) -> Unit = { event ->
            if (txtcon) applyTxtcon()
            rememberInput(event.currentTarget.value as String)
        }
        val keyDown: (dynamic) -> Unit = { event ->
            if (event.key == "Enter" && event.shiftKey != true && event.nativeEvent.isComposing != true) {
                event.preventDefault()
                submit()
            }
        }
        val edit = ui.h(s.Textarea, props("ref" to textarea, "rows" to 2, "defaultValue" to initialText,
            "disabled" to (selectedImage || (dcconState[0].list.length as Int) > 0 || (slots != null && sending)),
            "aria-label" to "$mode 입력", "placeholder" to if (txtcon) "글자콘 입력... (최대 20자, 4줄, 줄당 5자로 나뉨, Shift+Enter 줄바꿈)"
            else "댓글 입력... (Shift+Enter 줄바꿈)", "className" to "min-h-16 flex-1 resize-y",
            "onChange" to change, "onCompositionEnd" to composition, "onKeyDown" to keyDown))
        val toggleTxtcon: () -> Unit = {
            dcconState[1](noDccon)
            if (txtcon) exitTxtcon()
            else {
                beforeTxtcon.current = textarea.current?.value
                applyTxtcon()
                txtconState[1](true)
            }
        }
        val tools = ui.div("flex flex-col gap-2",
            ui.div("flex gap-2",
                ui.h(s.TooltipButton, props("tip" to "디시콘", "variant" to "secondary", "size" to "icon",
                    "disabled" to (slots != null && sending), "onClick" to { pickerMode[1](false); dcconOpen[1](true) }), ui.h(s.Smile)),
                ui.h(s.WithTooltip, props("tip" to if (txtcon) "글자콘 취소" else "글자콘",
                    "trigger" to ui.h(s.Button, props("variant" to if (txtcon) "default" else "secondary",
                        "size" to "icon", "aria-label" to "글자콘", "aria-pressed" to txtcon,
                        "disabled" to (slots != null && sending), "onClick" to toggleTxtcon))), ui.h(s.Type))),
            ui.h(s.Button, props("aria-label" to if (isInline) "답글 작성" else "작성", "className" to "w-full", "disabled" to sending, "onClick" to { submit() }),
                if (sending) ui.h(s.Spinner, props("aria-hidden" to true)) else ui.h(s.Send)))
        fun swatches(label: String, key: String, palette: Array<String>): dynamic {
            val select: (Array<String>) -> Unit = { values ->
                val first = values.firstOrNull()
                if (first != null) {
                    val update: (dynamic) -> dynamic = { setField(it, key, first) }
                    colors[1](update)
                    editSlot { textSlot(it.text as String, setField(it.colors, key, first)) }
                }
            }
            return ui.h(s.ToggleGroup, props("aria-label" to label, "value" to arrayOf<dynamic>(colors[0][key]), "disabled" to (selectedImage || (slots != null && sending)),
                "onValueChange" to select),
                palette.map { color -> ui.h(s.ToggleGroupItem, props("key" to color, "value" to color, "aria-label" to "$label #$color",
                    "className" to "size-4.5 min-w-0 cursor-pointer rounded-full border border-foreground/25 p-0 aria-pressed:outline-2 aria-pressed:outline-offset-2 aria-pressed:outline-solid aria-pressed:outline-primary",
                    "style" to props("background" to "#$color"))) }.toTypedArray())
        }
        val doubleEditor = if (!txtcon) null else if (slots == null) {
            ui.div("mt-2", ui.h(s.Button, props("type" to "button", "size" to "sm", "variant" to "secondary", "disabled" to sending,
                "onClick" to { pickerMode[1](true); dcconOpen[1](true) }), "더블콘 만들기"))
        } else {
            val tiles = slots.mapIndexed { index, slot ->
                val isText = slot.type == "txtcon"
                val label = if (isText) "글자콘 ${index + 1}" else (slot.title as String? ?: "디시콘")
                val preview = if (isText) ui.h("span", props("className" to "refresher-doublecon-text",
                    "style" to props("backgroundColor" to "#${slot.colors.bg}", "color" to "#${slot.colors.txt}")),
                    if (truthy(slot.text)) slot.text else "글자콘")
                else ui.h("img", props("src" to slot.list_img, "alt" to label))
                ui.h("div", props("key" to index, "className" to "flex flex-col gap-1"),
                    ui.h("button", props("type" to "button", "className" to "refresher-doublecon-tile", "aria-label" to label,
                        "aria-pressed" to (activeIndex == index), "disabled" to sending, "onClick" to { selectSlot(index) }), preview),
                    ui.h(s.Button, props("type" to "button", "size" to "xs", "variant" to "ghost",
                        "aria-label" to "${index + 1}번 콘 삭제", "disabled" to sending, "onClick" to { removeSlot(index) }), "삭제"))
            }.toTypedArray()
            ui.div("mt-2 flex flex-wrap items-center gap-2", ui.div("flex items-center gap-1", *tiles),
                ui.h(s.Button, props("type" to "button", "size" to "xs", "variant" to "secondary", "disabled" to sending,
                    "onClick" to { swapSlots() }), "위치 바꾸기"))
        }
        val palettes = if (txtcon) ui.div("mt-2 flex flex-wrap items-center gap-2",
            ui.h("span", props("className" to "text-xs text-muted-foreground", "aria-hidden" to true), "배경"),
            swatches("배경색", "bg", s.backgrounds as Array<String>),
            ui.h("span", props("className" to "ml-2 text-xs text-muted-foreground", "aria-hidden" to true), "글자"),
            swatches("글자색", "txt", s.colors as Array<String>)) else null
        val byline = if (login) account ?: "회원 계정" else ui.h(s.Button, props("variant" to "link", "size" to "xs",
            "className" to "h-auto p-0 text-xs text-link", "aria-expanded" to showInputs[0], "onClick" to toggleInputs),
            nick.ifEmpty { "닉네임 입력" })
        val choose: (Array<dynamic>, Boolean) -> Unit = { list, big ->
            if (pickerMode[0] == true) {
                val icon = list.firstOrNull()
                if (icon != null) startDouble(js("Object").assign(props(), icon, props("type" to "dccon")))
            } else {
                if (txtcon) exitTxtcon()
                closePicker()
                submit(props("list" to list, "big" to big), useDouble = false)
            }
        }
        val chooseText: () -> Unit = { startDouble(textSlot("", colors[0])) }
        ui.h("div", props("ref" to box, "className" to if (isInline) "refresher-inline-reply border-l-2 border-border py-3 pr-14 pl-8" else "pt-3 pr-14 pb-6 pl-8", "onFocus" to touch), fields,
            toReply, ui.div("flex items-end gap-2", edit, tools), doubleEditor, palettes,
            ui.tag("p", "mt-2 text-xs text-muted-foreground", byline, "(으)로 $mode 작성 중"),
            if (dcconOpen[0] == true) ui.h(s.DcconPopup, props("onSelect" to choose, "onClose" to ::closePicker,
                "onTxtcon" to if (pickerMode[0] == true) chooseText else undefinedValue())) else null)
    }
}
