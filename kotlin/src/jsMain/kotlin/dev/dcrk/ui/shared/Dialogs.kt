package dev.dcrk

private const val focusable = "a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]"
private const val dialogClass = "fixed top-1/2 left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 -translate-y-1/2 gap-6 rounded-xl bg-popover p-6 text-sm text-popover-foreground ring-1 ring-foreground/10 duration-100 outline-none sm:max-w-md data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
private const val alertClass = "group/alert-dialog-content fixed top-1/2 left-1/2 z-50 grid w-full -translate-x-1/2 -translate-y-1/2 gap-6 rounded-xl bg-popover p-6 text-sm text-popover-foreground ring-1 ring-foreground/10 duration-100 outline-none data-[size=default]:max-w-xs data-[size=default]:sm:max-w-lg data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

internal fun keepTabInside(event: dynamic) {
    if (event.key != "Tab" || event.defaultPrevented == true) return
    val box = event.currentTarget
    val items = jsArray(box.querySelectorAll(focusable)).filter { (it.tabIndex as Int) >= 0 && it.checkVisibility() == true }
    val first = items.firstOrNull() ?: return
    val last = items.last()
    val active = box.getRootNode().activeElement
    val back = event.shiftKey == true
    if ((back && (first.contains(active) == true || active === box)) || (!back && last.contains(active) == true)) {
        event.preventDefault()
        val target = if (back) last else first
        target.focus()
    }
}

internal fun createDialogs(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val assign: dynamic = js("Object.assign")

    fun modal(onClose: dynamic, auto: dynamic): dynamic {
        val open = react.useState(true)
        val popup = react.useRef(null)
        val focus = s.useReturnFocus(popup)
        val blur = s.useModuleSettings("preview")?.popupBlur != false
        val complete: (Boolean) -> Unit = { next ->
            if (!next) {
                focus.restoreFocus()
                onClose()
            }
        }
        val close: () -> Unit = { open[1](false) }
        val initial = if (kotlin.js.jsTypeOf(auto) == "object") auto else auto == "first" || focus.keyboard == true
        return props("close" to close, "root" to props("open" to open[0], "onOpenChangeComplete" to complete),
            "popup" to props("ref" to popup, "onKeyDown" to ::keepTabInside, "initialFocus" to initial, "finalFocus" to focus.finalFocus),
            "over" to props("forceRender" to true, "className" to if (blur) undefinedValue() else "supports-backdrop-filter:backdrop-blur-none"))
    }
    fun content(state: dynamic, alert: Boolean, extraClass: dynamic, vararg children: dynamic): dynamic {
        val primitive = if (alert) s.AlertDialogPrimitive else s.DialogPrimitive
        val prefix = if (alert) "alert-dialog" else "dialog"
        val popupProps = assign(props("data-slot" to "$prefix-content",
            "className" to s.cn(if (alert) alertClass else dialogClass, extraClass)), state.popup)
        if (alert) popupProps["data-size"] = "default"
        return ui.h(primitive.Portal, props("data-slot" to "$prefix-portal", "container" to s.overlay.portal),
            ui.h(if (alert) s.AlertDialogOverlay else s.DialogOverlay, state.over),
            ui.h(primitive.Popup, popupProps, *children))
    }

    val dialog: (dynamic) -> dynamic = { p ->
        val state = modal(p.onClose, p.focusOnOpen ?: "first")
        val change: (Boolean, dynamic) -> Unit = { next, details ->
            if (!next && (p.dismissible != false || details.reason == "imperative-action")) state.close()
        }
        val attributes = assign(props("actionsRef" to p.actionsRef, "disablePointerDismissal" to p.disablePointerDismissal),
            state.root, props("onOpenChange" to change))
        ui.h(s.Dialog, attributes, content(state, false, p.className, p.children))
    }
    val actions: (dynamic) -> dynamic = { p ->
        val label = if (p.cancelLabel === undefinedValue()) "취소" else p.cancelLabel
        val cancel = if (label != null) ui.h(s.DialogClose, props("disabled" to p.cancelDisabled,
            "render" to ui.h(s.Button, props("variant" to "outline"))), label) else null
        ui.h(s.DialogFooter, null, cancel, p.children)
    }
    val form: (dynamic) -> dynamic = { p ->
        val submit: (dynamic) -> Unit = { event -> event.preventDefault(); p.onSubmit() }
        ui.h("form", props("className" to s.cn("flex flex-col gap-4", p.className), "onSubmit" to submit), p.children)
    }
    val close: () -> dynamic = { ui.h(s.DialogClose, props("render" to ui.h(s.Button, props("variant" to "ghost",
        "size" to "icon-sm", "aria-label" to "닫기"))), ui.h(s.X)) }
    val confirm: (dynamic) -> dynamic = { p ->
        val state = modal(p.onClose, "first")
        val change: (Boolean) -> Unit = { if (!it) state.close() }
        val cancel = if (p.cancelLabel === undefinedValue()) "취소" else p.cancelLabel
        ui.h(s.AlertDialog, assign(props(), state.root, props("onOpenChange" to change)), content(state, true,
            "text-sm data-[size=default]:max-w-[calc(100%-2rem)] data-[size=default]:sm:max-w-110",
            ui.h(s.AlertDialogTitle, props("className" to "text-sm leading-snug wrap-anywhere"), p.title),
            ui.h(s.AlertDialogFooter, null, if (cancel != null) ui.h(s.AlertDialogCancel, null, cancel) else null,
                ui.h(s.AlertDialogAction, props("variant" to if (p.danger == true) "destructive" else "default", "onClick" to p.onConfirm),
                    p.confirmLabel ?: "확인"))))
    }
    val notice: (dynamic) -> dynamic = { p ->
        if (truthy(p.message)) ui.h(confirm, props("title" to p.message, "cancelLabel" to null,
            "onConfirm" to p.onClose, "onClose" to p.onClose)) else null
    }
    return props("ModalDialog" to dialog, "DialogActions" to actions, "SubmitForm" to form,
        "DialogCloseButton" to close, "ConfirmDialog" to confirm, "Notice" to notice)
}

internal fun focusedElement(): dynamic {
    val doc: dynamic = js("document")
    var element = doc.activeElement
    while (element?.shadowRoot?.activeElement != null) element = element.shadowRoot.activeElement
    return if (js("HTMLElement.prototype").isPrototypeOf(element) == true) element else null
}

internal fun panelOf(from: dynamic): dynamic = if (from?.parentElement == null) undefinedValue()
    else from.parentElement.closest("[tabindex]")

internal fun focusPanel(from: dynamic) { panelOf(from)?.focus(props("preventScroll" to true)) }

internal fun createReturnFocus(react: dynamic): (dynamic) -> dynamic = { popup ->
    val opener = react.useState(::focusedElement)[0]
    val origin: () -> Boolean = { opener?.matches(":focus-visible") == true }
    val keyboard = react.useState(origin)[0]
    fun target(): dynamic = if (opener?.matches(":disabled") == true) panelOf(opener) else opener
    fun returnFocus() { target()?.focus(props("preventScroll" to true)) }
    val restore: () -> Unit = {
        val current = focusedElement()
        if (current == null || current === js("document.body") || popup.current?.contains(current) == true) returnFocus()
    }
    val fix: () -> dynamic = { restore }
    react.useLayoutEffect(fix, emptyArray<dynamic>())
    val final: () -> dynamic = { target() ?: true }
    props("keyboard" to keyboard, "finalFocus" to final, "restoreFocus" to restore, "returnFocus" to ::returnFocus)
}
