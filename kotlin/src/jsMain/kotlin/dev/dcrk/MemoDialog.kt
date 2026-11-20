package dev.dcrk

internal fun createMemoDialog(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val types = s.types as Array<String>
    val uiStore = s.uiStore
    val memoStore = s.memoStore
    val inner: (dynamic) -> dynamic = { p ->
        val state = p.state
        val close = uiStore { selected: dynamic -> selected.closeMemo }
        val toast = uiStore { selected: dynamic -> selected.showToast }
        val memos = memoStore { selected: dynamic -> selected.memos }
        val setMemo = memoStore { selected: dynamic -> selected.setMemo }
        val removeMemo = memoStore { selected: dynamic -> selected.removeMemo }
        val id = react.useId() as String
        val input = react.useRef(null)
        val gallery = s.selectedGallery(state.gallery)
        fun memoOf(kind: String, user: String): dynamic = s.ownMemo(memos[kind], user)
        fun prefill(kind: String): dynamic {
            val memo = memoOf(kind, (state.targets[kind] ?: "") as String)
            return props("text" to (memo?.text ?: ""), "color" to (memo?.color ?: s.randomColor()), "scope" to (memo?.gallery ?: undefinedValue()))
        }
        val typeState = react.useState(state.initialType)
        val kind = typeState[0] as String
        val initial: () -> dynamic = { prefill(state.initialType as String) }
        val formState = react.useState(initial)
        val form = formState[0]
        val touched = react.useState(false)
        fun edit(key: String, value: dynamic) {
            touched[1](true)
            val update: (dynamic) -> dynamic = { setField(it, key, value) }
            formState[1](update)
        }
        val value = (state.targets[kind] ?: "") as String
        val existing = truthy(memoOf(kind, value))
        fun save(write: dynamic, done: String) = jsPromise {
            try {
                awaitJs(write)
            } catch (_: Throwable) {
                toast(s.saveFailed, "error")
                return@jsPromise
            }
            toast(done)
            close()
        }
        fun remove() = save(removeMemo(kind, value), "메모를 삭제했습니다.")
        fun submit() {
            val trimmed = (form.text as String).trim()
            if (trimmed.isNotEmpty()) save(setMemo(kind, value, props("text" to trimmed, "color" to form.color, "gallery" to form.scope)), "메모를 저장했습니다.")
            else if (existing) remove()
            else {
                toast("메모 내용이 없어 저장하지 않았습니다.", "error")
                close()
            }
        }
        val choose: (Array<String>) -> Unit = { values ->
            val type = values.firstOrNull()
            if (type != null && type in types) {
                typeState[1](type)
                if (touched[0] != true) formState[1](prefill(type))
            }
        }
        val typeButtons = types.filter { truthy(state.targets[it]) }.map { type ->
            ui.h(s.ToggleGroupItem, props("key" to type, "value" to type), s.typeNames[type])
        }.toTypedArray()
        val toggle = ui.h(s.ToggleGroup, props("aria-label" to "메모 대상", "variant" to "outline", "spacing" to 0,
            "value" to arrayOf(kind), "onValueChange" to choose), typeButtons)
        val changeColor: (dynamic) -> Unit = { edit("color", it.target.value) }
        val changeText: (dynamic) -> Unit = { edit("text", it.target.value) }
        val editor = ui.h(s.InputGroup, null,
            ui.h(s.InputGroupAddon, null, ui.h(s.ColorInput, props("width" to 20, "height" to 20, "aria-label" to "색상",
                "value" to form.color, "onChange" to changeColor))),
            ui.h(s.InputGroupInput, props("ref" to input, "maxLength" to 160, "aria-label" to "메모",
                "placeholder" to "메모를 입력해 주세요 (160자 제한)", "value" to form.text, "onChange" to changeText)),
            ui.h(s.InputGroupAddon, props("align" to "inline-end"),
                ui.h(s.InputGroupButton, props("size" to "icon-xs", "aria-label" to "랜덤 색상",
                    "onClick" to { edit("color", s.randomColor()) }), ui.h(s.Shuffle))))
        val changeScope: (Boolean) -> Unit = { edit("scope", if (it) gallery else undefinedValue()) }
        val scope = if (truthy(gallery)) ui.h(s.Field, props("orientation" to "horizontal"),
            ui.h(s.Checkbox, props("id" to "$id-scope", "checked" to (form.scope === gallery), "onCheckedChange" to changeScope)),
            ui.h(s.FieldLabel, props("htmlFor" to "$id-scope"), "이 갤러리에서만 ($gallery)")) else null
        val scopeNotice = if (truthy(form.scope) && form.scope != gallery)
            ui.tag("p", "text-muted-foreground", "지금은 ${form.scope} 갤러리 전용 메모입니다.") else null
        ui.h(s.ModalDialog, props("onClose" to close, "focusOnOpen" to input, "className" to "sm:max-w-[400px]"),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, "메모"),
                ui.h(s.DialogDescription, null, "${s.typeNames[kind]}: ", ui.tag("strong", "text-foreground", value))),
            ui.h(s.SubmitForm, props("onSubmit" to ::submit), ui.div("flex flex-col gap-3", toggle, editor, scope, scopeNotice),
                ui.h(s.DialogFooter, null,
                    if (existing) ui.h(s.Button, props("type" to "button", "variant" to "destructive", "onClick" to { remove() }), "삭제") else null,
                    ui.h(s.DialogClose, props("render" to ui.h(s.Button, props("variant" to "outline"))), "취소"),
                    ui.h(s.Button, props("type" to "submit"), "저장"))))
    }
    return {
        val memo = uiStore { state: dynamic -> state.memo }
        if (memo != null) ui.h(inner, props("key" to js("JSON.stringify")(memo), "state" to memo)) else null
    }
}
