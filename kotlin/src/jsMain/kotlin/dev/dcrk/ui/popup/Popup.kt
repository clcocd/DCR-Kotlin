package dev.dcrk

private const val PAGE_STATE = "refresher:pageState"
private const val PAGE_ACTION = "refresher:pageAction"

fun createPopup(services: dynamic): () -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val browser = services.browser
    val promise: dynamic = js("Promise")
    val console: dynamic = js("console")
    val logo = browser.runtime.getURL("/icons/48.png")
    val version = browser.runtime.getManifest().version
    val features = services.features as Array<dynamic>

    fun readPage(): dynamic = browser.tabs.query(props("active" to true, "currentWindow" to true)).then { tabs: dynamic ->
        val tab = tabs[0]
        val url = if (truthy(tab?.url)) parseBrowserUrl(tab.url as String) else null
        val gallery = if (url?.hostname == "gall.dcinside.com") url.searchParams.get("id") else null
        if (jsTypeOfId(tab) == "undefined" || gallery.isNullOrEmpty()) {
            null
        } else {
            services.send(PAGE_STATE, undefinedValue(), tab.id)
                .catch { _: dynamic -> null }
                .then { state: dynamic -> props("tabId" to tab.id, "gallery" to gallery, "state" to state) }
        }
    }

    fun openOptions() {
        val window: dynamic = js("window")
        browser.runtime.openOptionsPage().then { _: dynamic -> window.close() }
    }

    fun sectionTitle(title: String, aside: dynamic): dynamic =
        ui.div("mb-2 flex items-center justify-between px-1", ui.span("text-xs font-bold text-muted-foreground", title), aside)

    val toggleRow: (dynamic) -> dynamic = { row ->
        val id = react.useId()
        ui.h(services.Field, props("orientation" to "horizontal", "className" to "items-center"),
            ui.h("span", props(
                "className" to "flex size-8 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground transition-colors data-on:bg-primary/15 data-on:text-link",
                "data-on" to if (row.checked == true) true else undefinedValue(),
            ), ui.h(row.icon, props("size" to 15))),
            ui.h(services.FieldContent, null,
                ui.h(services.FieldLabel, props("htmlFor" to id), row.label),
                ui.h(services.FieldDescription, null, row.desc)),
            ui.h(services.Switch, props("id" to id, "size" to "sm", "checked" to row.checked, "onCheckedChange" to row.onChange)))
    }

    val pageSection: (dynamic) -> dynamic = { page ->
        val blocks = services.useBlocksStore { state: dynamic -> state.entries }
        val memos = services.useMemosStore { state: dynamic -> state.memos }
        val tuple = react.useState(page.state)
        val state = tuple[0]
        val setState = tuple[1]

        react.useEffect(effect@{
            if (page.toggled == 0) return@effect undefinedValue()
            var current = true
            services.send(PAGE_STATE, undefinedValue(), page.tabId).then(
                { next: dynamic -> if (current) setState(next) },
                { _: dynamic -> if (current) setState(null) },
            )
            val dispose: () -> Unit = { current = false }
            dispose
        }, arrayOf<dynamic>(page.tabId, page.toggled))

        val latest = react.useRef(0)
        fun act(toggle: dynamic) {
            val id = (latest.current as Int) + 1
            latest.current = id
            val apply: (dynamic) -> Unit = { next -> if (latest.current == id) setState(next) }
            services.send(PAGE_ACTION, props("module" to toggle.module, "id" to toggle.id), page.tabId)
                .then(apply, { _: dynamic -> apply(null) })
        }

        fun visible(entry: dynamic): Boolean = !truthy(entry.gallery) || entry.gallery == page.gallery
        val blockCount = jsValues(blocks).sumOf { (it as Array<dynamic>).count(::visible) }
        val memoCount = jsValues(memos).sumOf { jsValues(it).count(::visible) }
        val failures = if (state == null) emptyList() else features.filter { state.failed.includes(it.id) == true }
        val toggles = state?.toggles as Array<dynamic>?

        ui.h("div", null,
            sectionTitle("현재 페이지", ui.div("flex gap-1",
                ui.h(services.Badge, props("variant" to "secondary"), ui.h(services.Ban), " 차단 ", blockCount),
                ui.h(services.Badge, props("variant" to "secondary"), ui.h(services.NotebookPen), " 메모 ", memoCount))),
            if (state == null) ui.tag("p", "text-center text-xs text-muted-foreground", "페이지를 새로고침하면 이 페이지 설정이 나옵니다.") else null,
            if (failures.isNotEmpty()) ui.h("p", props("className" to "mb-2 text-center text-xs text-destructive", "role" to "alert"),
                "이 페이지에서 시작하지 못한 모듈: ", failures.joinToString(", ") { it.name as String }, ". 페이지를 새로고침해 보세요.") else null,
            if (toggles?.isEmpty() == true) ui.tag("p", "text-center text-xs text-muted-foreground", "이 페이지에서 바꿀 수 있는 설정이 없습니다.") else null,
            if (!toggles.isNullOrEmpty()) ui.h(services.Card, props("className" to "py-3"),
                ui.h(services.CardContent, props("className" to "flex flex-col gap-3 px-3"),
                    toggles.map { toggle ->
                        val feature = features.firstOrNull { it.id == toggle.module }
                        val icons = feature?.icons?.toggles
                        val labels = feature?.labels?.toggles
                        ui.h(toggleRow, props(
                            "key" to "${toggle.module}:${toggle.id}",
                            "icon" to ((if (icons == null) null else icons[toggle.id]) ?: services.Puzzle),
                            "label" to ((if (labels == null) null else labels[toggle.id]) ?: toggle.id),
                            "desc" to toggle.desc,
                            "checked" to toggle.on,
                            "onChange" to { act(toggle) },
                        ))
                    }.toTypedArray())) else null)
    }

    val modulesSection: (dynamic) -> dynamic = { section ->
        val enables = services.useModulesStore { state: dynamic -> state.enables }
        val toggle = services.useModulesStore { state: dynamic -> state.toggle }
        val error = react.useState(false)
        val setFailed = error[1]
        val on = features.count { truthy(enables[it.id]) }
        val tiles = features.map { feature ->
            val onPressed: (Boolean) -> Unit = { next ->
                toggle(feature.id, next).then(
                    { _: dynamic -> setFailed(false); section.onToggled() },
                    { _: dynamic -> setFailed(true) },
                )
            }
            ui.h(services.Toggle, props(
                "key" to feature.id, "variant" to "outline", "className" to "min-w-0 justify-start font-normal",
                "title" to feature.description, "pressed" to (enables[feature.id] == true), "onPressedChange" to onPressed,
            ), ui.h(feature.icons.module),
                ui.span("grow truncate text-left", feature.name),
                ui.span("size-1.5 shrink-0 rounded-full bg-muted-foreground/40 group-data-pressed/toggle:bg-primary"))
        }.toTypedArray()
        ui.h("div", null,
            sectionTitle("모듈", ui.span("text-xs text-muted-foreground", "$on/${features.size} 켜짐")),
            ui.div("grid grid-cols-2 gap-2", tiles),
            if (error[0] == true) ui.h("p", props("className" to "mt-2 text-center text-xs text-destructive", "role" to "alert"),
                "저장하지 못했습니다. 팝업을 닫았다가 다시 열어 주세요.") else null)
    }

    return app@{
        val loadedState = react.useState(null)
        val loaded = loadedState[0]
        val setLoaded = loadedState[1]
        val toggledState = react.useState(0)
        val setToggled = toggledState[1]

        react.useLayoutEffect({
            val backup = services.backupStorage
            val backupError = promise.all(arrayOf<dynamic>(backup.auto().getValue(), backup.error().getValue()))
                .then { values: dynamic -> if (truthy(values[0])) values[1] else "" }
                .catch { _: dynamic -> "" }
            promise.all(arrayOf<dynamic>(
                readPage().catch { _: dynamic -> null },
                backupError,
                services.initBlocksStore().catch(console.error),
                services.initMemosStore().catch(console.error),
                services.initModulesStore().catch(console.error),
            )).then { values: dynamic -> setLoaded(props("page" to values[0], "backupError" to values[1])) }
            undefinedValue()
        }, emptyArray<dynamic>())

        if (loaded == null) return@app null
        val page = loaded.page
        ui.div("flex flex-col",
            ui.h(services.ExtensionPageVars),
            ui.div("flex items-center gap-3 border-b px-4 py-3",
                ui.h("img", props("src" to logo, "alt" to "", "width" to 32, "height" to 32, "className" to "rounded-md")),
                ui.div("min-w-0 grow", ui.div("font-bold", "DCRefresher Reborn"), ui.div("text-xs text-muted-foreground", "v", version)),
                ui.h(services.Button, props(
                    "variant" to "ghost", "size" to "icon", "className" to "text-muted-foreground",
                    "aria-label" to "설정", "title" to "설정", "onClick" to ::openOptions,
                ), ui.h(services.Settings))),
            ui.div("flex flex-col gap-4 p-3",
                if (truthy(loaded.backupError)) ui.tag("p", "text-center text-xs text-destructive",
                    "클라우드에 백업하지 못했습니다. 설정의 데이터 탭에서 확인해 주세요.") else null,
                if (page != null) ui.h(pageSection, props(
                    "tabId" to page.tabId, "gallery" to page.gallery, "state" to page.state, "toggled" to toggledState[0],
                )) else ui.tag("p", "text-center text-xs text-muted-foreground", "디시인사이드 갤러리에서 열면 이 페이지 설정이 나옵니다."),
                ui.h(modulesSection, props("onToggled" to { setToggled { count: Int -> count + 1 } }))))
    }
}

private fun jsTypeOfId(tab: dynamic): String = kotlin.js.jsTypeOf(tab?.id)
