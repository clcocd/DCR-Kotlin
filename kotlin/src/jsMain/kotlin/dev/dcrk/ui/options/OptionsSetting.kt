package dev.dcrk

import kotlin.js.jsTypeOf

private const val FOCUSABLE = ":is(button, input, [tabindex]:not([tabindex='-1'])):not(:disabled)"
private val shortcutPattern = Regex("^[a-z0-9]$")

private fun formatRange(value: Double, unit: String): String =
    if (unit == "ms") "${js("String")(value / 1000)}초" else "${js("String")(value)}$unit"

private fun formatDefault(schema: dynamic): String {
    val default = schema.default
    return when (schema.type) {
        "check" -> if (default == true) "사용" else "미사용"
        "range" -> formatRange(default as Double, schema.unit as String)
        "option" -> (schema.items[default] ?: default) as String
        "order" -> (default as Array<String>).joinToString(", ") { (schema.items[it] ?: it) as String }
        "key" -> (default as String).uppercase()
        else -> (js("String")(default) as String).ifEmpty { "비어 있음" }
    }
}

private fun refocusSetting(within: dynamic) {
    val doc: dynamic = js("document")
    val active = doc.activeElement
    if (active != null && active !== doc.body && !(js("active instanceof HTMLButtonElement") as Boolean && active.disabled == true)) return
    within?.querySelector(FOCUSABLE)?.focus()
}

fun createSettingItem(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)

    fun draft(value: dynamic): dynamic {
        val draft = react.useState(value)
        val synced = react.useState(value)
        if (synced[0] !== value) {
            synced[1](value)
            draft[1](value)
        }
        return draft
    }

    fun fieldProps(p: dynamic, vararg extras: Pair<String, dynamic>): dynamic =
        props("aria-label" to p.schema.name, "aria-describedby" to p.descId, *extras)

    val textControl: (dynamic) -> dynamic = { p ->
        val state = draft(p.value)
        val value = state[0]
        ui.h(s.Input, fieldProps(p,
            "placeholder" to (p.schema.placeholder ?: js("String")(p.schema.default)),
            "maxLength" to p.schema.maxLength, "value" to value,
            "onChange" to { event: dynamic -> state[1](event.target.value) },
            "onBlur" to { if (value !== p.value) p.onChange(value) },
            "onKeyDown" to { event: dynamic ->
                if (event.key == "Enter" && event.nativeEvent.isComposing != true) event.currentTarget.blur()
            }))
    }

    val colorControl: (dynamic) -> dynamic = { p ->
        val state = draft(p.value)
        val value = state[0]
        val listen: (dynamic) -> dynamic = { element ->
            if (element == null) undefinedValue() else {
                val commit: () -> Unit = { p.onChange(element.value) }
                element.addEventListener("change", commit)
                val dispose: () -> Unit = { element.removeEventListener("change", commit) }
                dispose
            }
        }
        ui.div("flex items-center gap-2",
            if (p.compact == true) null else ui.span("text-muted-foreground tabular-nums", value),
            ui.h(s.ColorInput, fieldProps(p, "title" to value, "value" to value, "ref" to listen,
                "onChange" to { event: dynamic -> state[1](event.target.value) })))
    }

    val keyControl: (dynamic) -> dynamic = { p ->
        val state = react.useState(false)
        val conflict = react.useState("")
        val listening = state[0] as Boolean
        val taken = conflict[0] as String
        val value = p.value as String
        val keys = p.takenKeys as Array<String>? ?: emptyArray()
        val shown = when {
            taken.isNotEmpty() -> "${taken.uppercase()}: 이미 사용 중"
            listening -> "키 입력..."
            else -> value.uppercase()
        }
        val stop: () -> Unit = { state[1](false); conflict[1]("") }
        val keyDown: (dynamic) -> Unit = down@{ event ->
            if (!listening) return@down
            if (event.key != "Tab") event.preventDefault()
            val key = s.pressedKey(event) as String
            val valid = shortcutPattern.matches(key)
            if (valid && key != value && key in keys) {
                conflict[1](key)
                return@down
            }
            if (valid) p.onChange(key)
            stop()
        }
        ui.h(s.Button, props("variant" to if (taken.isNotEmpty()) "destructive" else if (listening) "default" else "secondary",
            "className" to "min-w-18", "aria-label" to "${p.schema.name}: $shown", "aria-describedby" to p.descId,
            "onClick" to { state[1](true) }, "onBlur" to stop, "onKeyDown" to keyDown),
            if (taken.isNotEmpty() || listening) shown else ui.h(s.Kbd, null, shown))
    }

    val rangeControl: (dynamic) -> dynamic = { p ->
        val schema = p.schema
        val state = draft(p.value)
        val value = state[0] as Double
        val text = formatRange(value, schema.unit as String)
        val slider = s.Slider
        fun first(value: dynamic): dynamic = if (js("Array.isArray")(value) == true) value[0] else value
        val onChange: (dynamic) -> Unit = { next ->
            val changed = first(next)
            if (changed != null) state[1](changed)
        }
        val onCommit: (dynamic) -> Unit = { next ->
            val changed = first(next)
            if (changed != null && changed !== p.value) p.onChange(changed)
        }
        ui.div("flex w-60 items-center gap-3",
            ui.h(slider.Root, props("className" to "data-horizontal:w-full data-vertical:h-full flex-1", "data-slot" to "slider",
                "value" to arrayOf(value), "min" to schema.min, "max" to schema.max, "step" to schema.step, "thumbAlignment" to "edge",
                "onValueChange" to onChange, "onValueCommitted" to onCommit),
                ui.h(slider.Control, props("className" to "relative flex w-full touch-none items-center select-none data-disabled:opacity-50 data-vertical:h-full data-vertical:min-h-40 data-vertical:w-auto data-vertical:flex-col"),
                    ui.h(slider.Track, props("data-slot" to "slider-track",
                        "className" to "relative grow overflow-hidden rounded-full bg-muted select-none data-horizontal:h-1.5 data-horizontal:w-full data-vertical:h-full data-vertical:w-1.5"),
                        ui.h(slider.Indicator, props("data-slot" to "slider-range", "className" to "bg-primary select-none data-horizontal:h-full data-vertical:w-full"))),
                    ui.h(slider.Thumb, fieldProps(p, "data-slot" to "slider-thumb", "getAriaValueText" to { text },
                        "className" to "block size-4 shrink-0 rounded-full border border-primary bg-white shadow-sm ring-ring/50 transition-[color,box-shadow] select-none hover:ring-4 focus-visible:ring-4 focus-visible:outline-hidden disabled:pointer-events-none disabled:opacity-50")))),
            ui.span("min-w-14 text-right font-bold tabular-nums", text))
    }

    val orderControl: (dynamic) -> dynamic = { p ->
        val order = p.value as Array<String>
        val drag = react.useState(null)
        val over = react.useState(null)
        val root = react.useRef(null)
        val moved = react.useRef(null)
        val css: dynamic = js("CSS")
        react.useLayoutEffect({
            if (moved.current != null) {
                refocusSetting(root.current?.querySelector("[data-key=\"${css.escape(moved.current)}\"]"))
                moved.current = null
            }
            undefinedValue()
        }, arrayOf<dynamic>(order))

        fun move(from: Int, to: Int) {
            val next = order.toMutableList()
            val key = next.removeAt(from)
            next.add(to, key)
            p.onChange(next.toTypedArray())
        }
        fun clearDrag() {
            drag[1](null)
            over[1](null)
        }
        val rows = order.mapIndexed { index, key ->
            val label = (p.schema.items[key] ?: key) as String
            fun button(icon: dynamic, direction: String, to: Int): dynamic =
                ui.h(s.Button, props("variant" to "ghost", "size" to "icon-xs", "aria-label" to "$label $direction",
                    "disabled" to (to !in order.indices), "onClick" to { moved.current = key; move(index, to) }), ui.h(icon))
            ui.h("div", props("key" to key, "data-key" to key, "draggable" to true, "className" to s.cn(
                "flex cursor-grab items-center gap-2 rounded-md border bg-background py-1 pr-1 pl-2",
                if (drag[0] == index) "opacity-40" else false,
                if (over[0] == index && drag[0] != null) "bg-primary/10" else false,
            ), "onDragStart" to { event: dynamic ->
                event.dataTransfer.setData("application/x-refresher-order", index.toString())
                event.dataTransfer.effectAllowed = "move"
                drag[1](index)
            }, "onDragEnd" to ::clearDrag, "onDragOver" to { event: dynamic -> event.preventDefault(); over[1](index) },
                "onDragLeave" to { over[1](null) }, "onDrop" to { event: dynamic ->
                    event.preventDefault()
                    val from = drag[0] as Int?
                    if (from != null && from != index) move(from, index)
                    clearDrag()
                }),
                ui.h(s.GripVertical, props("className" to "size-3.5 text-muted-foreground")), ui.span("flex-1", label),
                button(s.ChevronUp, "위로", index - 1), button(s.ChevronDown, "아래로", index + 1))
        }.toTypedArray()
        ui.h("div", fieldProps(p, "ref" to root, "className" to "flex min-w-50 flex-col gap-1", "role" to "group"), rows)
    }

    return { setting ->
        val schema = setting.schema
        val value = setting.value
        val compact = setting.compact == true
        val descId = react.useId()
        val control = react.useRef(null)
        val default = defaultSetting(schema)
        val text = if (jsTypeOf(value) == "string") value else js("String")(default)
        val blockReset = schema.type == "key" && setting.takenKeys?.includes(schema.default) == true
        val reset: () -> Unit = {
            setting.onChange(defaultSetting(schema))
            val request: dynamic = js("requestAnimationFrame")
            request({ refocusSetting(control.current) })
        }
        val resetButton = if (s.shallow(value, default) == true || blockReset) null
        else ui.h(s.WithTooltip, props("tip" to "기본값으로 되돌리기 (${formatDefault(schema)})",
            "trigger" to ui.h(s.Button, props("variant" to "ghost", "size" to "icon-xs",
                "aria-label" to "${schema.name} 기본값으로 되돌리기", "onClick" to reset))), ui.h(s.Undo2))
        val name = ui.h("span", props("className" to "font-medium", "title" to if (compact) schema.desc else undefinedValue()), schema.name)
        fun controlProps(next: dynamic): dynamic = props("schema" to schema, "value" to next, "descId" to descId,
            "onChange" to setting.onChange, "takenKeys" to setting.takenKeys, "compact" to compact)
        val field = when (schema.type) {
            "check" -> ui.h(s.Switch, props("aria-label" to schema.name, "aria-describedby" to descId, "checked" to (value == true),
                "onCheckedChange" to setting.onChange))
            "option" -> ui.h(s.RefresherSelect, props("value" to text, "aria-label" to schema.name, "aria-describedby" to descId,
                "options" to schema.items, "onChange" to setting.onChange))
            "text" -> ui.h(textControl, controlProps(text))
            "color" -> ui.h(colorControl, controlProps(text))
            "range" -> ui.h(rangeControl, controlProps(if (jsTypeOf(value) == "number") value else schema.default))
            "order" -> ui.h(orderControl, controlProps(if (js("Array.isArray")(value) == true) value else default))
            "key" -> ui.h(keyControl, controlProps(text))
            else -> null
        }
        val description = if (compact) ui.h("span", props("id" to descId, "hidden" to true), schema.desc)
        else ui.h("p", props("id" to descId, "className" to "text-xs text-muted-foreground"), schema.desc)
        ui.div(if (compact) "flex items-center justify-between gap-2 py-1" else "flex items-center justify-between flex-wrap gap-4 py-3 sm:flex-nowrap",
            ui.div("min-w-0 grow", ui.div("flex items-center gap-1", name, resetButton), description),
            ui.h("div", props("ref" to control, "className" to "shrink-0"), field))
    }
}
