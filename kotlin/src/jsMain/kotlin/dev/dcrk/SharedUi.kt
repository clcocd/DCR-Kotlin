package dev.dcrk

internal fun createTooltipViews(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val primitive = s.TooltipPrimitive
    val content: (dynamic) -> dynamic = { input ->
        val side = defaultUiValue(input.side, "top")
        val sideOffset = defaultUiValue(input.sideOffset, 4)
        val align = defaultUiValue(input.align, "center")
        val alignOffset = defaultUiValue(input.alignOffset, 0)
        val rest = omitUiProps(input, "className", "side", "sideOffset", "align", "alignOffset", "children")
        val style = "z-50 inline-flex w-fit max-w-xs origin-(--transform-origin) items-center gap-1.5 rounded-md bg-foreground px-3 py-1.5 text-xs text-background whitespace-pre-line has-data-[slot=kbd]:pr-1.5 data-[side=bottom]:slide-in-from-top-2 data-[side=inline-end]:slide-in-from-left-2 data-[side=inline-start]:slide-in-from-right-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 **:data-[slot=kbd]:relative **:data-[slot=kbd]:isolate **:data-[slot=kbd]:z-50 **:data-[slot=kbd]:rounded-sm data-[state=delayed-open]:animate-in data-[state=delayed-open]:fade-in-0 data-[state=delayed-open]:zoom-in-95 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
        val arrowStyle = "z-50 size-2.5 translate-y-[calc(-50%-2px)] rotate-45 rounded-[2px] bg-foreground fill-foreground data-[side=bottom]:top-1 data-[side=inline-end]:top-1/2! data-[side=inline-end]:-left-1 data-[side=inline-end]:-translate-y-1/2 data-[side=inline-start]:top-1/2! data-[side=inline-start]:-right-1 data-[side=inline-start]:-translate-y-1/2 data-[side=left]:top-1/2! data-[side=left]:-right-1 data-[side=left]:-translate-y-1/2 data-[side=right]:top-1/2! data-[side=right]:-left-1 data-[side=right]:-translate-y-1/2 data-[side=top]:-bottom-2.5"
        ui.h(primitive.Portal, props("container" to s.overlay.portal),
            ui.h(primitive.Positioner, props("align" to align, "alignOffset" to alignOffset, "side" to side,
                "sideOffset" to sideOffset, "className" to "isolate z-50"),
                ui.h(primitive.Popup, mergeUiProps(props("data-slot" to "tooltip-content", "className" to s.cn(style, input.className)), rest),
                    input.children, ui.h(primitive.Arrow, props("className" to arrowStyle)))))
    }
    val tooltip: (dynamic) -> dynamic = {
        ui.h(s.Tooltip, null, ui.h(s.TooltipTrigger, props("render" to it.trigger), it.children),
            ui.h(content, props("side" to it.side), it.tip))
    }
    val button: (dynamic) -> dynamic = {
        val rest = omitUiProps(it, "tip", "side", "children")
        val trigger = ui.h(s.Button, mergeUiProps(props("aria-label" to it.tip), rest))
        ui.h(tooltip, props("tip" to it.tip, "side" to it.side, "trigger" to trigger), it.children)
    }
    return props("WithTooltip" to tooltip, "TooltipButton" to button)
}

internal fun createColorInput(react: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(react)
    return { input ->
        val width = defaultUiValue(input.width, 36)
        val height = defaultUiValue(input.height, 28)
        ui.h("input", mergeUiProps(props("type" to "color"), omitUiProps(input, "width", "height"),
            props("className" to "cursor-pointer border-0 bg-transparent p-0", "style" to props("width" to width, "height" to height))))
    }
}

internal fun createOverlayPopover(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    val primitive = s.PopoverPrimitive
    return { input ->
        val rest = omitUiProps(input, "className", "align", "alignOffset", "side", "sideOffset", "anchor")
        val style = "z-50 flex w-72 origin-(--transform-origin) flex-col gap-4 rounded-md bg-popover p-4 text-sm text-popover-foreground shadow-md ring-1 ring-foreground/10 outline-hidden duration-100 data-[side=bottom]:slide-in-from-top-2 data-[side=inline-end]:slide-in-from-left-2 data-[side=inline-start]:slide-in-from-right-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
        ui.h(primitive.Portal, props("container" to s.overlay.portal),
            ui.h(primitive.Positioner, props("align" to defaultUiValue(input.align, "center"), "alignOffset" to defaultUiValue(input.alignOffset, 0),
                "side" to defaultUiValue(input.side, "bottom"), "sideOffset" to defaultUiValue(input.sideOffset, 4),
                "anchor" to input.anchor, "className" to "isolate z-50"),
                ui.h(primitive.Popup, mergeUiProps(props("data-slot" to "popover-content", "className" to s.cn(style, input.className)), rest))))
    }
}
