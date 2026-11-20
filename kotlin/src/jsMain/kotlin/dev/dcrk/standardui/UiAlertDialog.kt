package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiAlertDialog(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun AlertDialog(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.AlertDialogPrimitive.Root, mergeUiProps(props("data-slot" to "alert-dialog"), rest))
    }

    fun AlertDialogTrigger(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.AlertDialogPrimitive.Trigger, mergeUiProps(props("data-slot" to "alert-dialog-trigger"), rest))
    }

    fun AlertDialogPortal(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.AlertDialogPrimitive.Portal, mergeUiProps(props("data-slot" to "alert-dialog-portal"), rest))
    }

    fun AlertDialogOverlay(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.AlertDialogPrimitive.Backdrop, mergeUiProps(props("data-slot" to "alert-dialog-overlay", "className" to s.cn("fixed inset-0 isolate z-50 bg-black/10 duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0", className)), rest))
    }

    fun AlertDialogContent(input: dynamic): dynamic {
        val className = input["className"]
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "size")
        return ui.h(views.AlertDialogPortal, null,
            ui.h(views.AlertDialogOverlay, null),
            ui.h(s.AlertDialogPrimitive.Popup, mergeUiProps(props("data-slot" to "alert-dialog-content", "data-size" to size, "className" to s.cn("group/alert-dialog-content fixed top-1/2 left-1/2 z-50 grid w-full -translate-x-1/2 -translate-y-1/2 gap-6 rounded-xl bg-popover p-6 text-popover-foreground ring-1 ring-foreground/10 duration-100 outline-none data-[size=default]:max-w-xs data-[size=sm]:max-w-xs data-[size=default]:sm:max-w-lg data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95", className)), rest))
        )
    }

    fun AlertDialogHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-dialog-header", "className" to s.cn("grid grid-rows-[auto_1fr] place-items-center gap-1.5 text-center has-data-[slot=alert-dialog-media]:grid-rows-[auto_auto_1fr] has-data-[slot=alert-dialog-media]:gap-x-6 sm:group-data-[size=default]/alert-dialog-content:place-items-start sm:group-data-[size=default]/alert-dialog-content:text-left sm:group-data-[size=default]/alert-dialog-content:has-data-[slot=alert-dialog-media]:grid-rows-[auto_1fr]", className)), rest))
    }

    fun AlertDialogFooter(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-dialog-footer", "className" to s.cn("flex flex-col-reverse gap-2 group-data-[size=sm]/alert-dialog-content:grid group-data-[size=sm]/alert-dialog-content:grid-cols-2 sm:flex-row sm:justify-end", className)), rest))
    }

    fun AlertDialogMedia(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-dialog-media", "className" to s.cn("mb-2 inline-flex size-16 items-center justify-center rounded-md bg-muted sm:group-data-[size=default]/alert-dialog-content:row-span-2 *:[svg:not([class*='size-'])]:size-8", className)), rest))
    }

    fun AlertDialogTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.AlertDialogPrimitive.Title, mergeUiProps(props("data-slot" to "alert-dialog-title", "className" to s.cn("text-lg font-medium sm:group-data-[size=default]/alert-dialog-content:group-has-data-[slot=alert-dialog-media]/alert-dialog-content:col-start-2", className)), rest))
    }

    fun AlertDialogDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.AlertDialogPrimitive.Description, mergeUiProps(props("data-slot" to "alert-dialog-description", "className" to s.cn("text-sm text-balance text-muted-foreground md:text-pretty *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground", className)), rest))
    }

    fun AlertDialogAction(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.Button, mergeUiProps(props("data-slot" to "alert-dialog-action", "className" to s.cn(className)), rest))
    }

    fun AlertDialogCancel(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "outline")
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "variant", "size")
        return ui.h(s.AlertDialogPrimitive.Close, mergeUiProps(props("data-slot" to "alert-dialog-cancel", "className" to s.cn(className), "render" to ui.h(s.Button, props("variant" to variant, "size" to size))), rest))
    }

    views.AlertDialog = ::AlertDialog
    views.AlertDialogTrigger = ::AlertDialogTrigger
    views.AlertDialogPortal = ::AlertDialogPortal
    views.AlertDialogOverlay = ::AlertDialogOverlay
    views.AlertDialogContent = ::AlertDialogContent
    views.AlertDialogHeader = ::AlertDialogHeader
    views.AlertDialogFooter = ::AlertDialogFooter
    views.AlertDialogMedia = ::AlertDialogMedia
    views.AlertDialogTitle = ::AlertDialogTitle
    views.AlertDialogDescription = ::AlertDialogDescription
    views.AlertDialogAction = ::AlertDialogAction
    views.AlertDialogCancel = ::AlertDialogCancel
    return views
}
