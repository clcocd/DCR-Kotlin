package dev.dcrk

private val appIp = Regex("""^\d{1,3}\.\d{1,3}$""")
private val appMemoNewline = Regex("""\s*\n\s*""")

private fun objectEntries(value: dynamic): Array<dynamic> = js("Object.entries")(value) as Array<dynamic>
private fun copyMemoFields(current: dynamic, patch: dynamic): dynamic =
    js("Object.fromEntries([...Object.entries(current), ...Object.entries(patch)])")

fun parseAppMemos(text: String): dynamic {
    val memos = props("UID" to js("Object.create(null)"), "IP" to js("Object.create(null)"))
    var skipped = 0
    for (line in text.split(Regex("\\r?\\n"))) {
        if (line.trim().isEmpty()) continue
        val index = line.indexOf('-')
        val target = if (index < 0) "" else line.substring(0, index).trim()
        val memo = if (index < 0) "" else line.substring(index + 1).trim()
        if (target.isEmpty() || memo.isEmpty()) skipped++
        else memos[if (appIp.matches(target)) "IP" else "UID"][target] = memo
    }
    return props("memos" to memos, "skipped" to skipped)
}

fun formatAppMemos(memos: dynamic): dynamic {
    val lines = listOf("UID", "IP").flatMap { type ->
        objectEntries(memos[type]).map { entry ->
            "${entry[0]}-${(entry[1].text as String).replace(appMemoNewline, " ")}"
        }
    }
    return props("text" to lines.joinToString("\n"), "count" to lines.size, "skipped" to objectEntries(memos.NICK).size)
}

fun createMemoTab(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val types = s.types as Array<String>
    val search: (dynamic) -> Array<dynamic> = { arrayOf<dynamic>(it[0], it[1].text, it[1].gallery) }
    val gallery: (dynamic) -> dynamic = { it[1].gallery }
    val rowKey: (dynamic) -> dynamic = { it[0] }

    val formDialog: (dynamic) -> dynamic = { dialog ->
        val tuple = react.useState(dialog.initial)
        val state = tuple[0]
        val setState = tuple[1]
        val error = react.useState("")
        val setError = error[1]
        val id = react.useId() as String
        val userInput = react.useRef(null)
        val textInput = react.useRef(null)
        val editing = truthy(dialog.initial.user)
        val action = if (editing) "수정" else "추가"
        fun update(key: String, value: dynamic) {
            setState { previous: dynamic -> copyMemoFields(previous, props(key to value)) }
        }

        val submit: () -> dynamic = { jsPromise submit@{
            val user = (state.user as String).trim()
            val text = (state.text as String).trim()
            val message = when {
                user.isEmpty() -> "메모 대상을 입력해 주세요."
                text.isEmpty() -> "메모를 입력해 주세요."
                !editing && js("Object.hasOwn")(s.useMemosStore.getState().memos[state.type], user) == true -> "이미 메모가 있습니다."
                else -> null
            }
            if (message != null) {
                setError(message)
                return@submit undefinedValue()
            }
            try {
                awaitJs(dialog.onSubmit(if (editing) state else copyMemoFields(state, props("user" to user))))
            } catch (_: Throwable) {
                setError(s.saveFailed)
                return@submit undefinedValue()
            }
            dialog.onClose()
            undefinedValue()
        } }

        fun input(field: String, label: String, placeholder: String, ref: dynamic = null, disabled: Boolean = false, limit: Int? = null): dynamic =
            ui.h(s.Field, null, ui.h(s.FieldLabel, props("htmlFor" to "$id-$field"), label),
                ui.h(s.Input, props("id" to "$id-$field", "placeholder" to placeholder, "value" to state[field],
                    "ref" to ref, "disabled" to disabled, "maxLength" to limit,
                    "onChange" to { event: dynamic ->
                        val value = event.target.value as String
                        update(field, if (field == "gallery") value.trim() else value)
                    })))

        val horizontal = props("orientation" to "horizontal", "className" to "justify-between")
        ui.h(s.ModalDialog, props("onClose" to dialog.onClose, "focusOnOpen" to if (editing) textInput else userInput, "className" to "sm:max-w-[480px]"),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, "메모 $action")),
            ui.h(s.SubmitForm, props("onSubmit" to submit),
                ui.h(s.FieldGroup, null,
                    ui.h(s.Field, horizontal, ui.h(s.FieldLabel, null, "종류"),
                        ui.h(s.RefresherSelect, props("value" to state.type, "disabled" to editing, "aria-label" to "종류",
                            "onChange" to { value: String -> update("type", value) }, "options" to s.names))),
                    input("user", "대상", "아이디, 닉네임 또는 IP", userInput, editing),
                    input("text", "메모", "메모를 입력해 주세요 (160자 제한)", textInput, limit = 160),
                    input("gallery", "갤러리", "갤러리 ID (비우면 모든 갤러리)"),
                    ui.h(s.Field, horizontal, ui.h(s.FieldLabel, null, "색상"),
                        ui.div("flex items-center gap-2",
                            ui.h(s.ColorInput, props("aria-label" to "메모 색상", "value" to state.color,
                                "onChange" to { event: dynamic -> update("color", event.target.value) })),
                            ui.h(s.Button, props("type" to "button", "variant" to "secondary",
                                "onClick" to { update("color", s.randomColor()) }), "랜덤"))),
                    if (truthy(error[0])) ui.h(s.FieldError, props("role" to "alert"), error[0]) else null),
                ui.h(s.DialogActions, null, ui.h(s.Button, props("type" to "submit"), action))))
    }

    return {
        val memos = s.useMemosStore { state: dynamic -> state.memos }
        val setMemo = s.useMemosStore { state: dynamic -> state.setMemo }
        val updateMemos = s.useMemosStore { state: dynamic -> state.updateMemos }
        val sorted = react.useMemo({
            val result = props()
            for (type in types) result[type] = objectEntries(memos[type]).sortedBy { it[1].added as Double? ?: 0.0 }.toTypedArray()
            result
        }, arrayOf<dynamic>(memos))
        val items = react.useCallback({ type: String -> sorted[type] }, arrayOf<dynamic>(sorted))
        val formState = react.useState(null)
        val appImport = react.useState(false)
        val form = formState[0]
        val setForm = formState[1]
        val setImport = appImport[1]

        val importApp: (String) -> dynamic = { text -> jsPromise import@{
            val parsed = parseAppMemos(text)
            val count = objectEntries(parsed.memos.UID).size + objectEntries(parsed.memos.IP).size
            if (count == 0) {
                s.notify("공앱 메모가 없습니다. 한 줄에 하나씩 아이디-메모 형식으로 붙여 넣어 주세요.")
                return@import undefinedValue()
            }
            try {
                for (type in listOf("UID", "IP")) {
                    val incoming = objectEntries(parsed.memos[type]).map { Triple(it[0] as String, it[1] as String, s.randomColor()) }
                    val added: Double = js("Date.now()") as Double
                    awaitJs(updateMemos(type, { current: dynamic ->
                        val merged = js("Object.assign")(js("Object.create(null)"), current)
                        for ((target, memo, color) in incoming) {
                            merged[target] = copyMemoFields(s.ownMemo(merged, target) ?: props("color" to color, "added" to added),
                                props("text" to memo))
                        }
                        merged
                    }))
                }
            } catch (_: Throwable) {
                s.notify(s.saveFailed)
                return@import undefinedValue()
            }
            val suffix = if (parsed.skipped != 0) " (형식이 맞지 않는 ${parsed.skipped}줄은 건너뛰었습니다.)" else ""
            "공앱 메모 ${count}개를 가져왔습니다.$suffix"
        } }

        val copyApp: () -> Unit = {
            val data = formatAppMemos(memos)
            if (data.count == 0) s.notify("공앱으로 옮길 아이디, IP 메모가 없습니다.")
            else {
                val suffix = if (data.skipped != 0) " 닉네임 메모 ${data.skipped}개는 공앱에 없는 종류라 뺐습니다." else ""
                s.copyText(data.text, "공앱 형식으로 메모 ${data.count}개를 복사했습니다.$suffix", "복사하지 못했습니다.")
            }
        }

        val importMemos: (dynamic) -> dynamic = { data -> jsPromise {
            var count = 0
            for (type in types) {
                val imported = s.normalizeMemoMap(data[type])
                val size = objectEntries(imported).size
                if (size == 0) continue
                awaitJs(updateMemos(type, { current: dynamic -> copyMemoFields(current, imported) }))
                count += size
            }
            count
        } }

        val toolbar: () -> dynamic = {
            val icon = props("data-icon" to "inline-start")
            ui.div("flex flex-wrap gap-2",
                ui.h(s.Button, props("variant" to "secondary", "onClick" to { setImport(true) }), ui.h(s.Smartphone, icon), " 공앱 메모 가져오기"),
                ui.h(s.Button, props("variant" to "secondary", "onClick" to copyApp), ui.h(s.ClipboardCopy, icon), " 공앱 형식으로 복사"))
        }
        val removeMany: (String, Array<dynamic>) -> dynamic = { type, removed ->
            val users = removed.map { it[0] as String }.toSet()
            updateMemos(type, { current: dynamic ->
                js("Object.fromEntries")(objectEntries(current).filterNot { it[0] in users }.toTypedArray())
            })
        }
        val restore: (String, dynamic) -> dynamic = { type, transform ->
            updateMemos(type, { current: dynamic -> js("Object.fromEntries")(transform(objectEntries(current))) })
        }
        val row: (String, dynamic, dynamic) -> dynamic = { type, item, remove ->
            val user = item[0] as String
            val entry = item[1]
            val head = ui.span("flex items-center gap-2",
                ui.h("span", props("className" to "size-2.5 shrink-0 rounded-full", "style" to props("backgroundColor" to entry.color))),
                ui.span("font-medium", user),
                if (truthy(entry.gallery)) ui.h(s.Badge, props("variant" to "secondary"), entry.gallery) else null)
            ui.h(s.ListRow, props("key" to user, "head" to head, "label" to user, "info" to ui.span("text-muted-foreground", entry.text),
                "onEdit" to { setForm(props("type" to type, "user" to user, "text" to entry.text, "color" to entry.color, "gallery" to (entry.gallery ?: ""))) },
                "onRemove" to remove))
        }
        val list = ui.h(s.ListTabs, props("types" to types, "names" to s.names, "label" to "메모", "fileName" to "dcrefresher-memo",
            "columns" to arrayOf("대상", "메모"), "emptyText" to { type: String -> "${s.names[type]} 메모 없음" },
            "exportData" to { memos }, "importData" to importMemos,
            "onClear" to { type: String -> updateMemos(type, { props() }) },
            "onAdd" to { type: String -> setForm(props("type" to type, "user" to "", "text" to "", "color" to s.randomColor(), "gallery" to "")) },
            "toolbar" to toolbar, "items" to items, "searchText" to search, "galleryOf" to gallery,
            "onRemoveMany" to removeMany, "itemKey" to rowKey, "onRestore" to restore, "row" to row))
        val description = ui.h(react.Fragment, null, "공앱에서 복사한 메모를 붙여 넣어 주세요. 한 줄에 하나씩 ",
            ui.h("code", null, "아이디-메모"), " 형식이고, 아이디 자리가 IP(예: ", ui.h("code", null, "123.45"), ")면 IP 메모로 넣습니다.")
        val save: (dynamic) -> dynamic = { next -> setMemo(next.type, next.user,
            props("text" to (next.text as String).trim(), "color" to next.color,
                "gallery" to if (truthy(next.gallery)) next.gallery else undefinedValue())) }
        ui.h(react.Fragment, null, list,
            if (appImport[0] == true) ui.h(s.ImportDialog, props("title" to "공앱 메모 가져오기", "desc" to description,
                "placeholder" to "아이디-메모", "fromFile" to false, "onClose" to { setImport(false) }, "onSubmit" to importApp)) else null,
            if (form != null) ui.h(formDialog, props("initial" to form, "onClose" to { setForm(null) }, "onSubmit" to save)) else null)
    }
}
