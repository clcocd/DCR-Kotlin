package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiDialog(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Dialog(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.DialogPrimitive.Root, mergeUiProps(props("data-slot" to "dialog"), rest))
    }

    fun DialogTrigger(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.DialogPrimitive.Trigger, mergeUiProps(props("data-slot" to "dialog-trigger"), rest))
    }

    fun DialogPortal(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.DialogPrimitive.Portal, mergeUiProps(props("data-slot" to "dialog-portal"), rest))
    }

    fun DialogClose(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.DialogPrimitive.Close, mergeUiProps(props("data-slot" to "dialog-close"), rest))
    }

    fun DialogOverlay(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.DialogPrimitive.Backdrop, mergeUiProps(props("data-slot" to "dialog-overlay", "className" to s.cn("fixed inset-0 isolate z-50 bg-black/10 duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0", className)), rest))
    }

    fun DialogContent(input: dynamic): dynamic {
        val className = input["className"]
        val children = input["children"]
        val showCloseButton = defaultUiValue(input["showCloseButton"], true)
        val rest = omitUiProps(input, "className", "children", "showCloseButton")
        return ui.h(views.DialogPortal, null,
            ui.h(views.DialogOverlay, null),
            ui.h(s.DialogPrimitive.Popup, mergeUiProps(props("data-slot" to "dialog-content", "className" to s.cn("fixed top-1/2 left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 -translate-y-1/2 gap-6 rounded-xl bg-popover p-6 text-sm text-popover-foreground ring-1 ring-foreground/10 duration-100 outline-none sm:max-w-md data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95", className)), rest),
                children,
                if (truthy(showCloseButton)) ui.h(s.DialogPrimitive.Close, props("data-slot" to "dialog-close", "render" to ui.h(s.Button, props("variant" to "ghost", "className" to "absolute top-4 right-4", "size" to "icon-sm"))),
                    ui.h(s.XIcon, null),
                    ui.h("span", props("className" to "sr-only"),
                        "Close"
                    )
                ) else null
            )
        )
    }

    fun DialogHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "dialog-header", "className" to s.cn("flex flex-col gap-2", className)), rest))
    }

    fun DialogFooter(input: dynamic): dynamic {
        val className = input["className"]
        val showCloseButton = defaultUiValue(input["showCloseButton"], false)
        val children = input["children"]
        val rest = omitUiProps(input, "className", "showCloseButton", "children")
        return ui.h("div", mergeUiProps(props("data-slot" to "dialog-footer", "className" to s.cn("flex flex-col-reverse gap-2 sm:flex-row sm:justify-end", className)), rest),
            children,
            if (truthy(showCloseButton)) ui.h(s.DialogPrimitive.Close, props("render" to ui.h(s.Button, props("variant" to "outline"))),
                "Close"
            ) else null
        )
    }

    fun DialogTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.DialogPrimitive.Title, mergeUiProps(props("data-slot" to "dialog-title", "className" to s.cn("leading-none font-medium", className)), rest))
    }

    fun DialogDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.DialogPrimitive.Description, mergeUiProps(props("data-slot" to "dialog-description", "className" to s.cn("text-sm text-muted-foreground *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground", className)), rest))
    }

    views.Dialog = ::Dialog
    views.DialogTrigger = ::DialogTrigger
    views.DialogPortal = ::DialogPortal
    views.DialogClose = ::DialogClose
    views.DialogOverlay = ::DialogOverlay
    views.DialogContent = ::DialogContent
    views.DialogHeader = ::DialogHeader
    views.DialogFooter = ::DialogFooter
    views.DialogTitle = ::DialogTitle
    views.DialogDescription = ::DialogDescription
    return views
}
