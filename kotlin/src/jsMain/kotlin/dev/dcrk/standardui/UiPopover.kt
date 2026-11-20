package dev.dcrk

internal fun createUiPopover(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Popover(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.PopoverPrimitive.Root, mergeUiProps(props("data-slot" to "popover"), rest))
    }

    fun PopoverTrigger(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.PopoverPrimitive.Trigger, mergeUiProps(props("data-slot" to "popover-trigger"), rest))
    }

    fun PopoverContent(input: dynamic): dynamic {
        val className = input["className"]
        val align = defaultUiValue(input["align"], "center")
        val alignOffset = defaultUiValue(input["alignOffset"], 0)
        val side = defaultUiValue(input["side"], "bottom")
        val sideOffset = defaultUiValue(input["sideOffset"], 4)
        val rest = omitUiProps(input, "className", "align", "alignOffset", "side", "sideOffset")
        return ui.h(s.PopoverPrimitive.Portal, null,
            ui.h(s.PopoverPrimitive.Positioner, props("align" to align, "alignOffset" to alignOffset, "side" to side, "sideOffset" to sideOffset, "className" to "isolate z-50"),
                ui.h(s.PopoverPrimitive.Popup, mergeUiProps(props("data-slot" to "popover-content", "className" to s.cn("z-50 flex w-72 origin-(--transform-origin) flex-col gap-4 rounded-md bg-popover p-4 text-sm text-popover-foreground shadow-md ring-1 ring-foreground/10 outline-hidden duration-100 data-[side=bottom]:slide-in-from-top-2 data-[side=inline-end]:slide-in-from-left-2 data-[side=inline-start]:slide-in-from-right-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95", className)), rest))
            )
        )
    }

    fun PopoverHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "popover-header", "className" to s.cn("flex flex-col gap-1 text-sm", className)), rest))
    }

    fun PopoverTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.PopoverPrimitive.Title, mergeUiProps(props("data-slot" to "popover-title", "className" to s.cn("font-medium", className)), rest))
    }

    fun PopoverDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.PopoverPrimitive.Description, mergeUiProps(props("data-slot" to "popover-description", "className" to s.cn("text-muted-foreground", className)), rest))
    }

    views.Popover = ::Popover
    views.PopoverTrigger = ::PopoverTrigger
    views.PopoverContent = ::PopoverContent
    views.PopoverHeader = ::PopoverHeader
    views.PopoverTitle = ::PopoverTitle
    views.PopoverDescription = ::PopoverDescription
    return views
}
