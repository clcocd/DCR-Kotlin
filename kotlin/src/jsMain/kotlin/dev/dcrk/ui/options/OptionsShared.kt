package dev.dcrk

fun createImportDialog(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val console: dynamic = js("console")
    return { p ->
        val textState = react.useState("")
        val text = textState[0] as String
        val busyState = react.useState(false)
        val busy = busyState[0] as Boolean
        val actions = react.useRef(null)
        val input = react.useRef(null)
        val file = react.useRef(null)
        val placeholder = p.placeholder ?: "JSON 데이터"
        val desc = if (kotlin.js.jsTypeOf(p.desc) == "undefined") "내보낸 JSON 데이터를 붙여 넣거나 파일에서 불러와 주세요." else p.desc

        suspend fun submit(value: String) {
            busyState[1](true)
            val done = try { awaitJs(p.onSubmit(value)) } finally { busyState[1](false) }
            if (truthy(done)) {
                actions.current?.close()
                s.notifyDone(done)
            }
        }

        val fileChange: (dynamic) -> Unit = { event ->
            val files = event.currentTarget.files
            val picked = if (files == null) null else files[0]
            if (file.current != null) file.current.value = ""
            if (picked != null) jsPromise fileSubmit@{
                val value = try { awaitJs(picked.text()) as String } catch (error: Throwable) {
                    console.error(error)
                    s.notify("파일을 읽지 못했습니다.")
                    return@fileSubmit
                }
                submit(value)
            }
        }

        ui.h(s.ModalDialog, props("actionsRef" to actions, "dismissible" to !busy, "focusOnOpen" to input,
            "className" to "sm:max-w-[520px]", "onClose" to p.onClose),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, p.title), ui.h(s.DialogDescription, null, desc)),
            ui.h(s.Textarea, props("ref" to input, "placeholder" to placeholder, "aria-label" to placeholder, "value" to text,
                "rows" to 8, "className" to "max-h-[50vh]", "onChange" to { event: dynamic -> textState[1](event.target.value) })),
            ui.h(s.DialogActions, props("cancelDisabled" to busy),
                if (p.fromFile == false) null else ui.h(react.Fragment, null,
                    ui.h("input", props("ref" to file, "type" to "file", "accept" to ".json,application/json", "hidden" to true,
                        "aria-label" to "JSON 파일", "onChange" to fileChange)),
                    ui.h(s.Button, props("variant" to "secondary", "disabled" to busy, "onClick" to { file.current?.click() }),
                        ui.h(s.FileUp, props("data-icon" to "inline-start")), " 파일에서 불러오기")),
                ui.h(s.Button, props("disabled" to (busy || text.trim().isEmpty()), "onClick" to { jsPromise { submit(text) } }),
                    if (busy) ui.h(s.Spinner, props("aria-hidden" to true, "data-icon" to "inline-start")) else null, "가져오기")))
    }
}

fun createRefresherSelect(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    val none = "__none__"
    return { p ->
        val keys = js("Object.keys")(p.options) as Array<String>
        val values = props()
        for (key in keys) values[key.ifEmpty { none }] = p.options[key]
        val change: (dynamic) -> Unit = { next ->
            for (key in keys) if (key.ifEmpty { none } === next) p.onChange(key)
        }
        ui.h(s.Select, props("value" to if (truthy(p.value)) p.value else none, "items" to values,
            "disabled" to p.disabled, "onValueChange" to change),
            ui.h(s.SelectTrigger, props("aria-label" to p["aria-label"], "aria-describedby" to p["aria-describedby"], "className" to "min-w-[140px]"),
                ui.h(s.SelectValue)),
            ui.h(s.SelectContent, null, keys.map { key ->
                ui.h(s.SelectItem, props("key" to key, "value" to key.ifEmpty { none }), p.options[key])
            }.toTypedArray()))
    }
}

fun createOptionWidgets(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val section: (dynamic) -> dynamic = { p ->
        ui.h(s.Card, props("className" to "mb-4"),
            if (!truthy(p.title) && !truthy(p.desc) && !truthy(p.actions)) null else ui.h(s.CardHeader, null,
                if (truthy(p.title)) ui.h(s.CardTitle, null, ui.h("h2", null, p.title)) else null,
                if (truthy(p.desc)) ui.h(s.CardDescription, null, p.desc) else null,
                if (truthy(p.actions)) ui.h(s.CardAction, props("className" to "flex items-center gap-2"), p.actions) else null),
            if (truthy(p.children)) ui.h(s.CardContent, null, p.children) else null)
    }
    val searchInput: (dynamic) -> dynamic = { p ->
        val input = js("Object.assign")(props("type" to "search"), p)
        js("Reflect.deleteProperty")(input, "className")
        ui.h(s.InputGroup, props("className" to p.className),
            ui.h(s.InputGroupAddon, null, ui.h(s.Search)), ui.h(s.InputGroupInput, input))
    }
    val empty: (dynamic) -> dynamic = { p -> ui.h(s.Empty, null, ui.h(s.EmptyDescription, null, p.children)) }
    return props("Section" to section, "SearchInput" to searchInput, "EmptyList" to empty)
}

fun useOptionStorageItem(react: dynamic, item: dynamic): dynamic {
    val state = react.useState(item.fallback)
    val console: dynamic = js("console")
    react.useEffect({
        var watched = false
        item.getValue().then({ next: dynamic -> if (!watched) state[1](next) }, console.error)
        item.watch { next: dynamic -> watched = true; state[1](next) }
    }, arrayOf<dynamic>(item))
    return state[0]
}

fun optionStoreState(set: (dynamic) -> Unit): dynamic {
    val start: () -> Unit = {
        val match: dynamic = js("matchMedia")
        if (match("(prefers-reduced-motion: reduce)").matches == false) set(props("rain" to js("Date.now()")))
    }
    return props("rain" to 0, "notice" to null, "startRain" to start, "endRain" to { set(props("rain" to 0)) })
}

fun notifyOptionDone(showToast: dynamic, done: dynamic) {
    if (kotlin.js.jsTypeOf(done) == "string") showToast(done)
    else showToast(done.message, "info", 20_000, props("label" to "되돌리기", "run" to done.undo))
}

fun createDcconRain(react: dynamic, getState: () -> dynamic, packs: Array<dynamic>): () -> dynamic {
    val ui = ReactUi(react)
    val urls = packs.flatMap { pack -> (pack[1] as Array<String>).map { "https://image.dcinside.com/dccon.php?no=${pack[0]}$it" } }
    val window: dynamic = js("window")
    fun decimal(value: Double): String = js("String")(value) as String
    fun random(): Double = js("Math.random()") as Double
    return {
        react.useEffect({
            val timer = window.setTimeout({ getState().endRain() }, 3900)
            val dispose: () -> Unit = { window.clearTimeout(timer) }
            dispose
        }, emptyArray<dynamic>())
        val state = react.useState({
            (0 until 60).map {
                props("style" to props(
                    "left" to "${decimal(random() * 95)}%", "width" to "${decimal(60 + random() * 50)}px",
                    "animationDelay" to "${decimal(random() * 1.5)}s", "animationDuration" to "2.4s",
                ), "spin" to "${decimal((random() - 0.5) * 720)}deg")
            }.toTypedArray()
        })
        val drops = state[0] as Array<dynamic>
        ui.div("pointer-events-none fixed inset-0 z-100 overflow-hidden", drops.mapIndexed { index, drop ->
            ui.h("img", props("key" to index, "src" to urls[index % urls.size], "alt" to "",
                "className" to "absolute top-0 animate-dccon-fall rounded-lg", "style" to drop.style,
                "onError" to { event: dynamic -> event.currentTarget.remove() },
                "ref" to { el: dynamic -> el?.style?.setProperty("--spin", drop.spin); undefinedValue() }))
        }.toTypedArray())
    }
}
