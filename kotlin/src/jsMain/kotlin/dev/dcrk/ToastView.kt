package dev.dcrk

internal fun createToastHost(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val toast = s.Toast
    val store = s.store

    fun icon(level: dynamic): dynamic = when (level) {
        "info" -> ui.h(s.Info, props("className" to "size-4 shrink-0 text-link"))
        "warning" -> ui.h(s.TriangleAlert, props("className" to "size-4 shrink-0 text-amber-600 dark:text-amber-400"))
        "error" -> ui.h(s.CircleAlert, props("className" to "size-4 shrink-0 text-destructive"))
        else -> null
    }
    val list: () -> dynamic = {
        val manager = toast.useToastManager()
        val add = manager.add
        val close = manager.close
        val queued = store { state: dynamic -> state.toasts } as Array<dynamic>
        val deliver: () -> dynamic = {
            if (queued.isNotEmpty()) {
                val drain: (dynamic) -> dynamic = { props("toasts" to it.toasts.slice(queued.size)) }
                store.setState(drain)
                for (entry in queued) {
                    val action = entry.action
                    var id: dynamic = null
                    val click: () -> Unit = { close(id); action.run() }
                    id = add(props("title" to entry.content, "timeout" to entry.autoClose,
                        "priority" to if (entry.type == "error") "high" else "low", "data" to props("level" to entry.type),
                        "actionProps" to if (action == null) undefinedValue() else props("children" to action.label, "onClick" to click)))
                }
            }
            undefinedValue()
        }
        react.useEffect(deliver, arrayOf<dynamic>(queued, add, close))
        val entries = (manager.toasts as Array<dynamic>).map { entry ->
            ui.h(toast.Root, props("key" to entry.id, "toast" to entry, "data-slot" to "toast",
                "className" to "pointer-events-auto flex max-w-[360px] items-center gap-3 rounded-xl bg-popover p-3 text-popover-foreground shadow-lg ring-1 ring-foreground/10 duration-200 animate-in fade-in slide-in-from-bottom-2 data-limited:hidden motion-reduce:animate-none"),
                icon(entry.data?.level), ui.h(toast.Title, props("className" to "flex-1")),
                ui.h(toast.Action, props("render" to ui.h(s.Button, props("size" to "xs", "variant" to "ghost", "className" to "text-link")))),
                ui.h(toast.Close, props("render" to ui.h(s.Button, props("size" to "icon-xs", "variant" to "ghost", "aria-label" to "닫기"))), ui.h(s.X)))
        }.toTypedArray()
        ui.h(toast.Viewport, props("aria-label" to "알림", "className" to "pointer-events-none fixed right-4 bottom-4 z-60 flex max-w-[360px] flex-col-reverse items-end gap-2"), entries)
    }
    return { ui.h(toast.Provider, props("limit" to 3), ui.h(list)) }
}
