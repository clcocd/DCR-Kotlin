package dev.dcrk

private class OptionsTab(val id: String, val label: String, val icon: dynamic, val render: () -> dynamic)

fun createOptionsPage(services: dynamic): () -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val runtime = services.browser.runtime
    val window: dynamic = js("window")
    val promise: dynamic = js("Promise")
    val console: dynamic = js("console")
    val logo = runtime.getURL("/icons/128.png")
    val version = (runtime.getManifest().version as String) + if (services.develop == true) "-dev" else ""
    val rainComponent = react.lazy(services.loadRain)
    val tabs = listOf(
        OptionsTab("general", "설정", services.Settings) { ui.h(services.GeneralTab) },
        OptionsTab("block", "차단", services.Ban) { ui.h(services.BlockTab) },
        OptionsTab("memo", "메모", services.NotebookPen) { ui.h(services.MemoTab) },
        OptionsTab("shortcut", "단축키", services.Keyboard) { ui.h(services.ShortcutTab) },
        OptionsTab("data", "데이터", services.Database) { ui.h(services.DataTab) },
        OptionsTab("about", "정보", services.Info) { ui.h(services.AboutTab, props("logo" to logo, "version" to version)) },
    )

    fun readHash(): String {
        val id = (window.location.hash as String).drop(1)
        return tabs.firstOrNull { it.id == id }?.id ?: tabs.first().id
    }

    fun subscribeHash(onChange: () -> Unit): () -> Unit {
        window.addEventListener("hashchange", onChange)
        return { window.removeEventListener("hashchange", onChange) }
    }

    fun sidebar(tab: String): dynamic {
        val navigation = tabs.map { item ->
            val active = item.id == tab
            val selectTab: () -> Unit = { window.location.hash = item.id }
            ui.h(services.Button, props(
                "key" to item.id,
                "size" to "lg",
                "variant" to "ghost",
                "aria-current" to if (active) "page" else undefinedValue(),
                "className" to if (active) "justify-start bg-muted" else "justify-start",
                "onClick" to selectTab,
            ), ui.h(item.icon, props("data-icon" to "inline-start")), " ", item.label)
        }.toTypedArray()
        return ui.div("flex w-full shrink-0 flex-col gap-4 border-r p-4 md:sticky md:top-0 md:h-screen md:w-60",
            ui.div("flex items-center gap-3 px-2",
                ui.h("img", props("src" to logo, "alt" to "", "width" to 36, "height" to 36, "className" to "rounded-md")),
                ui.tag("p", "font-bold", "DCRefresher Reborn")),
            ui.tag("nav", "flex flex-wrap gap-1 md:flex-col md:flex-nowrap", navigation),
            ui.div("mt-auto hidden md:block",
                ui.h(services.Separator, props("className" to "mb-3")),
                ui.tag("p", "px-2 text-xs text-muted-foreground", "v", version)))
    }

    fun onMount() {
        if ((js("Math.random")() as Double) < 0.01) services.useOptionsStore.getState().startRain()
    }

    return {
        val tab = react.useSyncExternalStore(::subscribeHash, ::readHash) as String
        val rain = services.useOptionsStore { state: dynamic -> state.rain } as Double
        val notice = services.useOptionsStore { state: dynamic -> state.notice }
        val current = tabs.firstOrNull { it.id == tab } ?: tabs.first()
        val statusTuple = react.useState("loading")
        val status = statusTuple[0] as String
        val setStatus = statusTuple[1]

        react.useEffect({ onMount(); undefinedValue() }, emptyArray<dynamic>())
        react.useEffect({
            promise.all(arrayOf<dynamic>(services.initBlocksStore(), services.initMemosStore(), services.initModulesStore()))
                .then(
                    { _: dynamic -> setStatus("ready") },
                    { error: dynamic -> console.error(error); setStatus("failed") },
                )
            undefinedValue()
        }, emptyArray<dynamic>())

        val content = when (status) {
            "ready" -> current.render()
            "failed" -> ui.tag("p", "text-destructive", "저장된 데이터를 읽지 못했습니다. 페이지를 새로고침해 주세요.")
            else -> null
        }
        val closeNotice: () -> Unit = { services.useOptionsStore.setState(props("notice" to null)) }
        ui.div("flex min-h-screen flex-col md:flex-row",
            ui.h(services.ExtensionPageVars),
            sidebar(current.id),
            if (rain > 0) ui.h(react.Suspense, null, ui.h(rainComponent, props("key" to rain))) else null,
            ui.h(services.Notice, props("message" to notice, "onClose" to closeNotice)),
            ui.h(services.ToastHost),
            ui.div("min-w-0 grow px-4 py-8 md:px-8",
                ui.h("div", props("key" to current.id, "className" to "tab-enter mx-auto max-w-[880px] outline-none", "tabIndex" to -1),
                    ui.tag("h1", "mb-6 text-2xl font-bold", current.label), content)))
    }
}
