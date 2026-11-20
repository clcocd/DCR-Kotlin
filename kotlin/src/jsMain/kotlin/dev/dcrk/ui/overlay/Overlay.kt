package dev.dcrk

private class OverlayDemand(val store: dynamic, val needed: (dynamic) -> Boolean)
private val overlayDemands = mutableListOf<OverlayDemand>()

fun needOverlayWhen(store: dynamic, needed: (dynamic) -> Boolean) {
    overlayDemands.add(OverlayDemand(store, needed))
}

fun overlayNeeded(): Boolean = overlayDemands.any { it.needed(it.store.getState()) }

fun watchOverlayDemands(listener: () -> Unit): () -> Unit {
    val cleaners = overlayDemands.map { it.store.subscribe(listener) }
    return { cleaners.forEach { clean -> clean() } }
}

fun mountOverlayWhenNeeded(ctx: dynamic, s: dynamic) {
    val ui = ReactUi(s.react)
    val doc: dynamic = js("document")
    val console: dynamic = js("console")
    var stopAppearance: (() -> Unit)? = null

    val mount: () -> dynamic = { jsPromise mount@{
        if (ctx.isInvalid == true) return@mount undefinedValue()
        val cssUrl = s.browser.runtime.getURL("/content-scripts/content.css")
        val css = (awaitJs(s.fetchText(cssUrl)) as String).replace(":root", ":host")
        val onMount: (dynamic) -> dynamic = { container ->
            container.classList.add("font-sans", "text-sm", "text-foreground", "antialiased")
            stopAppearance = s.followDcAppearance(container)
            val app = doc.createElement("div")
            val portal = doc.createElement("div")
            portal.id = "portal"
            container.append(app, portal)
            s.overlay.portal = portal
            val root = s.createRoot(app)
            root.render(ui.h(s.ContentRoot))
            root
        }
        val onRemove: (dynamic) -> Unit = { root ->
            stopAppearance?.invoke()
            root?.unmount()
        }
        val view = awaitJs(s.createShadowRootUi(ctx, props("name" to "refresher-root", "position" to "inline",
            "anchor" to "body", "css" to css, "inheritStyles" to true, "onMount" to onMount, "onRemove" to onRemove)))
        if (ctx.isInvalid != true) view.mount()
        undefinedValue()
    } }

    lateinit var stop: () -> Unit
    val check: () -> Unit = {
        if (overlayNeeded()) {
            stop()
            s.whenDomReady { mount().catch(console.error) }
        }
    }
    stop = watchOverlayDemands(check)
}

fun createContentRoot(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val hosts = js("Object.entries")(s.hosts) as Array<dynamic>
    return {
        val state = react.useState(null)
        val target = state[0]
        val close: () -> Unit = { state[1](null) }
        val dcconInfo: (String) -> Unit = { code -> s.usePreviewStore.setState(props("dcconInfo" to code)) }
        ui.h(s.TooltipProvider, null, ui.h(s.ToastHost),
            ui.h(s.BubbleHost, props("onBlockPackage" to state[1], "onDcconInfo" to dcconInfo)),
            if (target == null) null else ui.h(s.DcconPackageDialog, props("target" to target, "onClose" to close)),
            ui.h(s.MemoDialog),
            hosts.map { entry -> ui.h(entry[1].default, props("key" to entry[0])) }.toTypedArray())
    }
}

fun createPreviewHost(react: dynamic, frame: dynamic, popups: dynamic): () -> dynamic {
    val ui = ReactUi(react)
    return { ui.h(react.Fragment, null, ui.h(frame), ui.h(popups)) }
}

fun createPreviewDialogRoot(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val dialog = s.Dialog
    val store = s.usePreviewStore
    return { p ->
        val pressedOutside = react.useRef(false)
        val onOpenChange: (Boolean, dynamic) -> Unit = close@{ open, details ->
            if (open) return@close
            if (store.getState().visible != true) {
                details.cancel()
                details.allowPropagation()
                return@close
            }
            if (details.reason == "escape-key") {
                val state = s.useUiStore.getState()
                if (state.bubble != null) {
                    details.cancel()
                    state.closeBubble()
                    return@close
                }
                if (s.overlay.portal?.querySelector("${s.modalSelector}, [data-slot=popover-content]") != null) {
                    details.cancel()
                    details.allowPropagation()
                    return@close
                }
            }
            store.getState().requestClose()
        }
        val fading = if (p.fading == true) true else undefinedValue()
        val backdrop = s.cn(
            "fixed inset-0 bg-black/40 duration-150 animate-in fade-in data-fading:pointer-events-none data-fading:animate-out data-fading:fade-out data-fading:fill-mode-forwards",
            if (p.blur == true) "backdrop-blur-[5px]" else false,
        )
        val focus: (dynamic) -> Unit = { element -> p.scroller.current = element; element?.focus(props("preventScroll" to true)) }
        val cls = "refresher-frame-scroll fixed inset-0 flex overflow-y-auto overscroll-contain py-[5vh] outline-none duration-150 animate-in fade-in [scrollbar-color:var(--border)_transparent] [scrollbar-gutter:stable_both-edges] [scrollbar-width:thin] data-fading:pointer-events-none data-fading:animate-out data-fading:fade-out data-fading:fill-mode-forwards"

        ui.h(dialog.Root, props("open" to true, "modal" to false, "disablePointerDismissal" to true, "onOpenChange" to onOpenChange),
            ui.h(dialog.Portal, props("container" to s.overlay.portal),
                ui.h("div", props("className" to backdrop, "data-fading" to fading)),
                ui.h(dialog.Popup, props("className" to cls, "ref" to focus, "tabIndex" to -1, "data-fading" to fading,
                    "aria-busy" to p.busy, "aria-modal" to true, "initialFocus" to false, "finalFocus" to false, "onWheel" to p.onWheel,
                    "onPointerDown" to { event: dynamic -> pressedOutside.current = event.target === event.currentTarget },
                    "onPointerUp" to { event: dynamic -> pressedOutside.current = pressedOutside.current == true && event.target === event.currentTarget },
                    "onClick" to { event: dynamic -> if (pressedOutside.current == true && event.target === event.currentTarget) store.getState().requestClose() },
                    "onContextMenu" to { event: dynamic ->
                        if (event.target === event.currentTarget) {
                            event.preventDefault()
                            store.getState().requestClose()
                        }
                    }), p.children),
                p.after))
    }
}
