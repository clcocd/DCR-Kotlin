package dev.dcrk

private fun settingsEntries(feature: dynamic): Array<dynamic> =
    js("Object.entries")(feature.settings ?: props()) as Array<dynamic>

fun matchOptionSettings(feature: dynamic, needle: String): dynamic {
    val entries = settingsEntries(feature)
    fun matches(text: dynamic): Boolean = text is String && (text as String).lowercase().contains(needle)
    if (matches(feature.name) || matches(feature.description)) return entries
    val matched = entries.filter { entry ->
        val schema = entry[1]
        matches(schema.name) || matches(schema.desc) || matches(schema.group?.name ?: "")
    }
    return if (matched.isEmpty()) undefinedValue() else matched.toTypedArray()
}

private fun searchHighlights(container: dynamic, needle: String): dynamic {
    if (needle.isEmpty() || container == null) return undefinedValue()
    val doc: dynamic = js("document")
    val registry: dynamic = js("CSS.highlights")
    val showText: Int = js("NodeFilter.SHOW_TEXT") as Int
    val walker = doc.createTreeWalker(container, showText)
    val ranges = mutableListOf<dynamic>()
    var node = walker.nextNode()
    while (node != null) {
        val text = (node.nodeValue as String? ?: "").lowercase()
        var at = text.indexOf(needle)
        while (at >= 0) {
            val range = doc.createRange()
            range.setStart(node, at)
            range.setEnd(node, at + needle.length)
            ranges.add(range)
            at = text.indexOf(needle, at + needle.length)
        }
        node = walker.nextNode()
    }
    val constructor: dynamic = js("Highlight")
    registry.set("refresher-search", js("Reflect.construct")(constructor, ranges.toTypedArray()))
    val cleanup: () -> Unit = { registry.delete("refresher-search") }
    return cleanup
}

fun createGeneralTab(services: dynamic): () -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val features = services.features as Array<dynamic>
    val report: (dynamic) -> Unit = { services.notify(services.saveFailed) }

    val moduleSettings: (dynamic) -> dynamic = { input ->
        val feature = input.feature
        val values = services.useModulesStore { state: dynamic -> state.values[feature.id] }
        val change = services.useModulesStore { state: dynamic -> state.changeSetting }
        val settings = input.settings as Array<dynamic>
        val shown = input.shown as Array<dynamic>
        fun valueOf(key: String, schema: dynamic): dynamic =
            (if (values == null) null else values[key]) ?: defaultSetting(schema)
        fun item(entry: dynamic, compact: Boolean = false): dynamic {
            val key = entry[0] as String
            val schema = entry[1]
            val taken = if (schema.type == "key") {
                settings.filter { it[0] != key && it[1].type == "key" }
                    .map { js("String")(valueOf(it[0] as String, it[1])) }.toTypedArray()
            } else undefinedValue()
            val commit: (dynamic) -> Unit = { value -> change(feature.id, key, value).catch(report) }
            return ui.h(services.SettingItem, props("key" to key, "schema" to schema, "compact" to compact,
                "takenKeys" to taken, "value" to valueOf(key, schema), "onChange" to commit))
        }

        val groups = shown.groupBy { it[1].group ?: it[0] }
        val content = groups.values.map { entries ->
            val first = entries.first()
            val group = first[1].group
            val body = if (group == null) item(first) else {
                val wide = entries.any { it[1].type == "text" || it[1].type == "range" }
                val grid = if (wide) "grid grid-cols-1 gap-x-6" else "grid grid-cols-2 gap-x-6 sm:grid-cols-3"
                ui.h("div", props("className" to "py-3", "role" to "group", "aria-label" to group.name),
                    ui.tag("p", "font-medium", group.name),
                    ui.tag("p", "mb-2 text-xs text-muted-foreground", group.desc),
                    ui.div(grid, entries.map { item(it, true) }.toTypedArray()))
            }
            ui.h(react.Fragment, props("key" to first[0]), ui.h(services.Separator), body)
        }.toTypedArray()
        ui.div("pt-4", content)
    }

    val moduleCard: (dynamic) -> dynamic = { input ->
        val feature = input.feature
        val shown = input.shown as Array<dynamic>
        val enabled = services.useModulesStore { state: dynamic -> state.enables[feature.id] == true } as Boolean
        val toggle = services.useModulesStore { state: dynamic -> state.toggle }
        val commitToggle: (Boolean) -> Unit = { value -> toggle(feature.id, value).catch(report) }
        ui.h(services.Card, null,
            ui.h(services.CardHeader, null,
                ui.h(services.CardTitle, null, ui.h("h2", null, feature.name)),
                ui.h(services.CardDescription, null, feature.description),
                ui.h(services.CardAction, props("className" to "self-center"),
                    ui.h(services.Switch, props("checked" to enabled, "aria-label" to "${feature.name} 사용", "onCheckedChange" to commitToggle)))),
            if (shown.isEmpty()) null else ui.h(services.Collapsible, props("open" to (enabled || input.searching == true), "className" to "-mt-(--card-spacing)"),
                ui.h(services.CollapsibleContent, props(
                    "className" to "h-(--collapsible-panel-height) overflow-clip transition-[height,opacity] duration-200 ease-out [overflow-clip-margin:6px] data-ending-style:h-0 data-ending-style:opacity-0 data-starting-style:h-0 data-starting-style:opacity-0 motion-reduce:transition-none",
                ), ui.h(services.CardContent, props("className" to "mt-2"),
                    if (enabled) null else ui.tag("p", "pt-4 text-xs text-muted-foreground", "모듈을 켜야 적용됩니다."),
                    ui.h("div", props("className" to if (enabled) undefinedValue() else "opacity-60"),
                        ui.h(moduleSettings, props("feature" to feature, "settings" to settingsEntries(feature), "shown" to shown)))))))
    }

    return {
        val queryState = react.useState("")
        val query = queryState[0] as String
        val setQuery = queryState[1]
        val needle = query.trim().lowercase()
        val root = react.useRef(null)
        react.useEffect({ searchHighlights(root.current, needle) }, arrayOf<dynamic>(root, needle))
        val matched = features.mapNotNull { feature ->
            val shown = matchOptionSettings(feature, needle)
            if (shown == null) null else ui.h(moduleCard,
                props("key" to feature.id, "feature" to feature, "shown" to shown, "searching" to needle.isNotEmpty()))
        }.toTypedArray()
        val update: (dynamic) -> Unit = { event -> setQuery(event.target.value) }
        ui.h("div", props("ref" to root, "className" to "flex flex-col gap-4"),
            ui.h(services.SearchInput, props("placeholder" to "설정 검색", "aria-label" to "설정 검색", "value" to query, "onChange" to update)),
            matched,
            if (matched.isEmpty()) ui.h(services.EmptyList, null, "\"${query.trim()}\" 검색 결과 없음") else null)
    }
}

fun createShortcutTab(services: dynamic): () -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val browser = services.browser
    val window: dynamic = js("window")
    val console: dynamic = js("console")
    val open: () -> Unit = {
        if (services.firefox == true) browser.commands.openShortcutSettings()
        else browser.tabs.create(props("url" to "chrome://extensions/shortcuts"))
    }
    return {
        val data = react.useState(emptyArray<dynamic>())
        val shortcuts = data[0] as Array<dynamic>
        val setShortcuts = data[1]
        react.useEffect({
            val load: () -> Unit = { browser.commands.getAll().then(setShortcuts, console.error) }
            load()
            window.addEventListener("focus", load)
            val dispose: () -> Unit = { window.removeEventListener("focus", load) }
            dispose
        }, emptyArray<dynamic>())
        val button = ui.h(services.Button, props("variant" to "secondary", "onClick" to open),
            ui.h(services.ExternalLink, props("data-icon" to "inline-start")), " 단축키 설정")
        val rows = shortcuts.filter { truthy(it.description) }.map { shortcut ->
            ui.h("div", props("key" to shortcut.name, "className" to "flex items-center justify-between gap-3 py-3"),
                ui.h("span", null, shortcut.description),
                if (truthy(shortcut.shortcut)) ui.h(services.Kbd, null, shortcut.shortcut) else ui.span("text-muted-foreground", "없음"))
        }.toTypedArray()
        ui.h(services.Section, props("desc" to "단축키는 브라우저의 확장 프로그램 단축키 설정에서 변경할 수 있습니다.", "actions" to button),
            ui.div("flex flex-col divide-y", rows))
    }
}
