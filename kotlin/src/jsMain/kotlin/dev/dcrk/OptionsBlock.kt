package dev.dcrk

import dev.dcrk.block.*

private val firstCode = Regex("""^\^\(((?:\\x[0-9a-f]{2})?\w+)[|)]""")
private val factoredCode = Regex("""^\^\(\?:((?:\\x[0-9a-f]{2})?\w+)\(\?:((?:\\x[0-9a-f]{2})?\w+)\|""")
private val initialHex = Regex("""^\\x([0-9a-f]{2})""")

fun optionDcconImage(entry: dynamic): String {
    val content = entry.content as String
    fun unhex(value: String): String = initialHex.replace(value) { it.groupValues[1].toInt(16).toChar().toString() }
    val code = if (entry.isRegex != true) content else {
        val plain = firstCode.find(content)?.groupValues?.get(1)
        val factored = factoredCode.find(content)?.groupValues
        val first = plain ?: factored?.let { unhex(it[1]) + unhex(it[2]) }
        if (first == null) content else unhex(first)
    }
    return "https://image.dcinside.com/dccon.php?no=$code"
}

fun createBlockDialog(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val modeOptions = js("Object.assign")(props("" to "기본값"), s.modeNames)
    return { dialog ->
        val initial = dialog.initial
        val type = dialog.type as String
        val name = s.typeNames[type] as String
        val content = react.useState(initial?.content ?: "")
        val regex = react.useState(initial?.isRegex ?: false)
        val gallery = react.useState(initial?.gallery ?: "")
        val alias = react.useState(initial?.extra ?: "")
        val mode = react.useState(initial?.mode ?: "")
        val errorText = react.useState("")
        val setError = errorText[1]
        val id = react.useId() as String

        val submit: () -> dynamic = {
            jsPromise submit@{
                val value = (content[0] as String).trim()
                if (value.isEmpty()) {
                    setError("$name 값을 입력해 주세요.")
                    return@submit undefinedValue()
                }
                if (regex[0] == true) try {
                    js("new RegExp(value)")
                } catch (error: Throwable) {
                    setError("정규식이 올바르지 않습니다. ${s.messageOf(error)}")
                    return@submit undefinedValue()
                }
                val galleryName = (gallery[0] as String).trim().ifEmpty { null }
                val key = blockKey(value, galleryName)
                val entries = s.useBlocksStore.getState().entries[type] as Array<dynamic>
                if (entries.any { it.id != initial?.id && blockKey(it.content as String, it.gallery as String?) == key }) {
                    setError("이미 같은 차단 항목이 있습니다.")
                    return@submit undefinedValue()
                }
                try {
                    awaitJs(dialog.onSubmit(props("content" to value, "isRegex" to regex[0],
                        "mode" to if (truthy(mode[0])) mode[0] else undefinedValue(),
                        "gallery" to (galleryName ?: undefinedValue()),
                        "extra" to ((alias[0] as String).trim().ifEmpty { null } ?: undefinedValue()))))
                } catch (_: Throwable) {
                    setError(s.saveFailed)
                }
                undefinedValue()
            }
        }
        fun input(suffix: String, label: String, placeholder: String, state: dynamic): dynamic =
            ui.h(s.Field, null, ui.h(s.FieldLabel, props("htmlFor" to "$id-$suffix"), label),
                ui.h(s.Input, props("id" to "$id-$suffix", "placeholder" to placeholder, "value" to state[0],
                    "onChange" to { event: dynamic -> state[1](event.target.value) })))

        val editing = initial != null
        val action = if (editing) "수정" else "추가"
        val description = if (editing) "${name} 항목을 수정합니다." else "${name} 차단 항목을 추가합니다."
        ui.h(s.ModalDialog, props("onClose" to dialog.onClose, "className" to "sm:max-w-[480px]"),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, "$name 차단 $action"), ui.h(s.DialogDescription, null, description)),
            ui.h(s.SubmitForm, props("onSubmit" to submit),
                ui.h(s.FieldGroup, null,
                    input("content", "값", "${name} 값을 입력해 주세요", content),
                    ui.h(s.Field, props("orientation" to "horizontal"),
                        ui.h(s.Checkbox, props("id" to "$id-regex", "checked" to regex[0], "onCheckedChange" to regex[1])),
                        ui.h(s.FieldLabel, props("htmlFor" to "$id-regex"), "정규식")),
                    input("name", "정보 (선택)", "목록에 보일 정보. 비우면 값으로 보입니다.", alias),
                    input("gallery", "특정 갤러리 차단 (선택)", "갤러리 ID", gallery),
                    ui.h(s.Field, null,
                        ui.div("flex items-center gap-1.5", ui.h(s.FieldLabel, null, "차단 모드"),
                            ui.h(s.TooltipButton, props("variant" to "ghost", "size" to "icon-xs",
                                "tip" to (s.modeDescriptions[mode[0]] ?: "기본 차단 모드를 따릅니다.")),
                                ui.h(s.Info, props("className" to "size-3.5")))),
                        ui.h(s.RefresherSelect, props("value" to mode[0], "aria-label" to "차단 모드", "onChange" to mode[1], "options" to modeOptions))),
                    if (truthy(errorText[0])) ui.h(s.FieldError, props("role" to "alert"), errorText[0]) else null),
                ui.h(s.DialogActions, null, ui.h(s.Button, props("type" to "submit"), action))))
    }
}

fun createBlockTab(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val types = s.types as Array<String>
    val searchText: (dynamic) -> Array<dynamic> = { arrayOf<dynamic>(it.content, it.gallery, it.extra) }
    val galleryOf: (dynamic) -> dynamic = { it.gallery }
    val itemKey: (dynamic) -> String = { blockKey(it.content as String, it.gallery as String?) }
    return {
        val entries = s.useBlocksStore { state: dynamic -> state.entries }
        val defaults = s.useBlocksStore { state: dynamic -> state.defaults }
        val addEntry = s.useBlocksStore { state: dynamic -> state.addEntry }
        val updateEntry = s.useBlocksStore { state: dynamic -> state.updateEntry }
        val updateEntries = s.useBlocksStore { state: dynamic -> state.updateEntries }
        val setDefault = s.useBlocksStore { state: dynamic -> state.setDefault }
        val addEntries = s.useBlocksStore { state: dynamic -> state.addEntries }
        val items = react.useCallback({ type: String -> entries[type] }, arrayOf<dynamic>(entries))
        val state = react.useState(null)
        val dialog = state[0]
        val setDialog = state[1]
        val close: () -> Unit = { setDialog(null) }

        val importBlocks: (dynamic) -> dynamic = { parsed -> jsPromise {
            val source = normalizeDefaults(parsed.defaults)
            var count = 0
            for (type in types) {
                val list = normalizeBlockList(parsed[type], emptyArray<dynamic>()) as Array<dynamic>
                if (list.isEmpty()) continue
                awaitJs(addEntries(type, pinDefaultMode(list, source[type] as String, defaults[type] as String)))
                count += list.size
            }
            count
        } }

        val submit: (dynamic) -> dynamic = { fields -> jsPromise {
            if (dialog != null) {
                if (dialog.initial == null) awaitJs(addEntry(dialog.type, fields))
                else awaitJs(updateEntry(dialog.type, dialog.initial.id, fields))
                close()
            }
            undefinedValue()
        } }

        val toolbar: (String) -> dynamic = { type ->
            val nextMode: (String) -> Unit = { next -> setDefault(type, next).catch { _: dynamic -> s.notify(s.saveFailed) } }
            ui.div("flex items-center gap-2", ui.span("text-muted-foreground", "기본 차단 모드"),
                ui.h(s.RefresherSelect, props("value" to defaults[type], "aria-label" to "기본 차단 모드", "onChange" to nextMode, "options" to s.modeNames)),
                ui.h(s.TooltipButton, props("tip" to s.modeDescriptions[defaults[type]], "variant" to "ghost", "size" to "icon-xs"),
                    ui.h(s.Info, props("className" to "size-3.5"))))
        }

        val removeMany: (String, Array<dynamic>) -> dynamic = { type, removed ->
            val ids = removed.map { it.id as String }.toSet()
            updateEntries(type, { current: Array<dynamic> -> current.filterNot { it.id in ids }.toTypedArray() })
        }

        val row: (String, dynamic, dynamic) -> dynamic = { type, entry, remove ->
            val head = if (type == "DCCON") ui.h("img", props("src" to optionDcconImage(entry),
                "alt" to if (truthy(entry.extra)) entry.extra else entry.content, "loading" to "lazy", "decoding" to "async", "className" to "block h-10"))
            else ui.span("font-medium", entry.content)
            val extra = listOfNotNull(composeExtra(entry.isRegex as Boolean, entry.gallery as String?, entry.mode as String?)
                .ifEmpty { null }, (entry.extra as String?)?.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "-" }
            ui.h(s.ListRow, props("key" to entry.id, "head" to head, "label" to s.blockLabel(entry),
                "info" to ui.span("text-muted-foreground", extra), "onEdit" to { setDialog(props("type" to type, "initial" to entry)) }, "onRemove" to remove))
        }
        val list = ui.h(s.ListTabs, props(
            "types" to types, "names" to s.typeNames, "label" to "차단 목록", "fileName" to "dcrefresher-block", "columns" to arrayOf("항목", "정보"),
            "emptyText" to { type: String -> "차단된 ${s.typeNames[type]} 없음" },
            "exportData" to { js("Object.assign")(props(), entries, props("defaults" to defaults)) },
            "importData" to importBlocks, "onClear" to { type: String -> updateEntries(type, { emptyArray<dynamic>() }) },
            "onAdd" to { type: String -> setDialog(props("type" to type, "initial" to null)) },
            "toolbar" to toolbar, "items" to items, "searchText" to searchText, "galleryOf" to galleryOf,
            "onRemoveMany" to removeMany, "itemKey" to itemKey, "onRestore" to updateEntries, "row" to row,
        ))
        ui.h(react.Fragment, null, list,
            if (dialog == null) null else ui.h(s.BlockDialog, props("type" to dialog.type, "initial" to dialog.initial, "onClose" to close, "onSubmit" to submit)))
    }
}
