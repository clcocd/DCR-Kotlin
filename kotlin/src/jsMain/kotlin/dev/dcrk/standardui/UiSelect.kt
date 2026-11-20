package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiSelect(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun SelectGroup(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.Group, mergeUiProps(props("data-slot" to "select-group", "className" to s.cn("scroll-my-1 p-1", className)), rest))
    }

    fun SelectValue(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.Value, mergeUiProps(props("data-slot" to "select-value", "className" to s.cn("flex flex-1 text-left", className)), rest))
    }

    fun SelectTrigger(input: dynamic): dynamic {
        val className = input["className"]
        val size = defaultUiValue(input["size"], "default")
        val children = input["children"]
        val rest = omitUiProps(input, "className", "size", "children")
        return ui.h(s.SelectPrimitive.Trigger, mergeUiProps(props("data-slot" to "select-trigger", "data-size" to size, "className" to s.cn("flex w-fit items-center justify-between gap-1.5 rounded-md border border-input bg-transparent py-2 pr-2 pl-2.5 text-sm whitespace-nowrap shadow-xs transition-[color,box-shadow] outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 data-placeholder:text-muted-foreground data-[size=default]:h-9 data-[size=sm]:h-8 *:data-[slot=select-value]:line-clamp-1 *:data-[slot=select-value]:flex *:data-[slot=select-value]:items-center *:data-[slot=select-value]:gap-1.5 dark:bg-input/30 dark:hover:bg-input/50 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4", className)), rest),
            children,
            ui.h(s.SelectPrimitive.Icon, props("render" to ui.h(s.ChevronDownIcon, props("className" to "pointer-events-none size-4 text-muted-foreground"))))
        )
    }

    fun SelectContent(input: dynamic): dynamic {
        val className = input["className"]
        val children = input["children"]
        val side = defaultUiValue(input["side"], "bottom")
        val sideOffset = defaultUiValue(input["sideOffset"], 4)
        val align = defaultUiValue(input["align"], "center")
        val alignOffset = defaultUiValue(input["alignOffset"], 0)
        val alignItemWithTrigger = defaultUiValue(input["alignItemWithTrigger"], true)
        val rest = omitUiProps(input, "className", "children", "side", "sideOffset", "align", "alignOffset", "alignItemWithTrigger")
        return ui.h(s.SelectPrimitive.Portal, null,
            ui.h(s.SelectPrimitive.Positioner, props("side" to side, "sideOffset" to sideOffset, "align" to align, "alignOffset" to alignOffset, "alignItemWithTrigger" to alignItemWithTrigger, "className" to "isolate z-50"),
                ui.h(s.SelectPrimitive.Popup, mergeUiProps(props("data-slot" to "select-content", "data-align-trigger" to alignItemWithTrigger, "className" to s.cn("relative isolate z-50 max-h-(--available-height) w-(--anchor-width) min-w-36 origin-(--transform-origin) overflow-x-hidden overflow-y-auto rounded-md bg-popover text-popover-foreground shadow-md ring-1 ring-foreground/10 duration-100 data-[align-trigger=true]:animate-none data-[side=bottom]:slide-in-from-top-2 data-[side=inline-end]:slide-in-from-left-2 data-[side=inline-start]:slide-in-from-right-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95", className)), rest),
                    ui.h(views.SelectScrollUpButton, null),
                    ui.h(s.SelectPrimitive.List, null,
                        children
                    ),
                    ui.h(views.SelectScrollDownButton, null)
                )
            )
        )
    }

    fun SelectLabel(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.GroupLabel, mergeUiProps(props("data-slot" to "select-label", "className" to s.cn("px-2 py-1.5 text-xs text-muted-foreground", className)), rest))
    }

    fun SelectItem(input: dynamic): dynamic {
        val className = input["className"]
        val children = input["children"]
        val rest = omitUiProps(input, "className", "children")
        return ui.h(s.SelectPrimitive.Item, mergeUiProps(props("data-slot" to "select-item", "className" to s.cn("relative flex w-full cursor-default items-center gap-2 rounded-sm py-1.5 pr-8 pl-2 text-sm outline-hidden select-none focus:bg-accent focus:text-accent-foreground not-data-[variant=destructive]:focus:**:text-accent-foreground data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 *:[span]:last:flex *:[span]:last:items-center *:[span]:last:gap-2", className)), rest),
            ui.h(s.SelectPrimitive.ItemText, props("className" to "flex flex-1 shrink-0 gap-2 whitespace-nowrap"),
                children
            ),
            ui.h(s.SelectPrimitive.ItemIndicator, props("render" to ui.h("span", props("className" to "pointer-events-none absolute right-2 flex size-4 items-center justify-center"))),
                ui.h(s.CheckIcon, props("className" to "pointer-events-none"))
            )
        )
    }

    fun SelectSeparator(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.Separator, mergeUiProps(props("data-slot" to "select-separator", "className" to s.cn("pointer-events-none -mx-1 my-1 h-px bg-border", className)), rest))
    }

    fun SelectScrollUpButton(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.ScrollUpArrow, mergeUiProps(props("data-slot" to "select-scroll-up-button", "className" to s.cn("top-0 z-10 flex w-full cursor-default items-center justify-center bg-popover py-1 [&_svg:not([class*='size-'])]:size-4", className)), rest),
            ui.h(s.ChevronUpIcon, null)
        )
    }

    fun SelectScrollDownButton(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.SelectPrimitive.ScrollDownArrow, mergeUiProps(props("data-slot" to "select-scroll-down-button", "className" to s.cn("bottom-0 z-10 flex w-full cursor-default items-center justify-center bg-popover py-1 [&_svg:not([class*='size-'])]:size-4", className)), rest),
            ui.h(s.ChevronDownIcon, null)
        )
    }

    views.SelectGroup = ::SelectGroup
    views.SelectValue = ::SelectValue
    views.SelectTrigger = ::SelectTrigger
    views.SelectContent = ::SelectContent
    views.SelectLabel = ::SelectLabel
    views.SelectItem = ::SelectItem
    views.SelectSeparator = ::SelectSeparator
    views.SelectScrollUpButton = ::SelectScrollUpButton
    views.SelectScrollDownButton = ::SelectScrollDownButton
    return views
}
