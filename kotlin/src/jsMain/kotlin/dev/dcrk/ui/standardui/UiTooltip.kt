package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiTooltip(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun TooltipProvider(input: dynamic): dynamic {
        val delay = defaultUiValue(input["delay"], 0)
        val rest = omitUiProps(input, "delay")
        return ui.h(s.TooltipPrimitive.Provider, mergeUiProps(props("data-slot" to "tooltip-provider", "delay" to delay), rest))
    }

    fun Tooltip(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.TooltipPrimitive.Root, mergeUiProps(props("data-slot" to "tooltip"), rest))
    }

    fun TooltipTrigger(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.TooltipPrimitive.Trigger, mergeUiProps(props("data-slot" to "tooltip-trigger"), rest))
    }

    fun TooltipContent(input: dynamic): dynamic {
        val className = input["className"]
        val side = defaultUiValue(input["side"], "top")
        val sideOffset = defaultUiValue(input["sideOffset"], 4)
        val align = defaultUiValue(input["align"], "center")
        val alignOffset = defaultUiValue(input["alignOffset"], 0)
        val children = input["children"]
        val rest = omitUiProps(input, "className", "side", "sideOffset", "align", "alignOffset", "children")
        return ui.h(s.TooltipPrimitive.Portal, null,
            ui.h(s.TooltipPrimitive.Positioner, props("align" to align, "alignOffset" to alignOffset, "side" to side, "sideOffset" to sideOffset, "className" to "isolate z-50"),
                ui.h(s.TooltipPrimitive.Popup, mergeUiProps(props("data-slot" to "tooltip-content", "className" to s.cn("z-50 inline-flex w-fit max-w-xs origin-(--transform-origin) items-center gap-1.5 rounded-md bg-foreground px-3 py-1.5 text-xs text-background has-data-[slot=kbd]:pr-1.5 data-[side=bottom]:slide-in-from-top-2 data-[side=inline-end]:slide-in-from-left-2 data-[side=inline-start]:slide-in-from-right-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 **:data-[slot=kbd]:relative **:data-[slot=kbd]:isolate **:data-[slot=kbd]:z-50 **:data-[slot=kbd]:rounded-sm data-[state=delayed-open]:animate-in data-[state=delayed-open]:fade-in-0 data-[state=delayed-open]:zoom-in-95 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95", className)), rest),
                    children,
                    ui.h(s.TooltipPrimitive.Arrow, props("className" to "z-50 size-2.5 translate-y-[calc(-50%-2px)] rotate-45 rounded-[2px] bg-foreground fill-foreground data-[side=bottom]:top-1 data-[side=inline-end]:top-1/2! data-[side=inline-end]:-left-1 data-[side=inline-end]:-translate-y-1/2 data-[side=inline-start]:top-1/2! data-[side=inline-start]:-right-1 data-[side=inline-start]:-translate-y-1/2 data-[side=left]:top-1/2! data-[side=left]:-right-1 data-[side=left]:-translate-y-1/2 data-[side=right]:top-1/2! data-[side=right]:-left-1 data-[side=right]:-translate-y-1/2 data-[side=top]:-bottom-2.5"))
                )
            )
        )
    }

    views.TooltipProvider = ::TooltipProvider
    views.Tooltip = ::Tooltip
    views.TooltipTrigger = ::TooltipTrigger
    views.TooltipContent = ::TooltipContent
    return views
}
