package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiTabs(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Tabs(input: dynamic): dynamic {
        val className = input["className"]
        val orientation = defaultUiValue(input["orientation"], "horizontal")
        val rest = omitUiProps(input, "className", "orientation")
        return ui.h(s.TabsPrimitive.Root, mergeUiProps(props("data-slot" to "tabs", "data-orientation" to orientation, "className" to s.cn("group/tabs flex gap-2 data-horizontal:flex-col", className)), rest))
    }

    fun TabsList(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "default")
        val rest = omitUiProps(input, "className", "variant")
        return ui.h(s.TabsPrimitive.List, mergeUiProps(props("data-slot" to "tabs-list", "data-variant" to variant, "className" to s.cn(s.tabsListVariants(props("variant" to variant)), className)), rest))
    }

    fun TabsTrigger(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.TabsPrimitive.Tab, mergeUiProps(props("data-slot" to "tabs-trigger", "className" to s.cn("relative inline-flex h-[calc(100%-1px)] flex-1 items-center justify-center gap-1.5 rounded-md border border-transparent px-2 py-1 text-sm font-medium whitespace-nowrap text-foreground/60 transition-all group-data-vertical/tabs:w-full group-data-vertical/tabs:justify-start hover:text-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1 focus-visible:outline-ring disabled:pointer-events-none disabled:opacity-50 has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 aria-disabled:pointer-events-none aria-disabled:opacity-50 dark:text-muted-foreground dark:hover:text-foreground group-data-[variant=default]/tabs-list:data-active:shadow-sm group-data-[variant=line]/tabs-list:data-active:shadow-none [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4", "group-data-[variant=line]/tabs-list:bg-transparent group-data-[variant=line]/tabs-list:data-active:bg-transparent dark:group-data-[variant=line]/tabs-list:data-active:border-transparent dark:group-data-[variant=line]/tabs-list:data-active:bg-transparent", "data-active:bg-background data-active:text-foreground dark:data-active:border-input dark:data-active:bg-input/30 dark:data-active:text-foreground", "after:absolute after:bg-foreground after:opacity-0 after:transition-opacity group-data-horizontal/tabs:after:inset-x-0 group-data-horizontal/tabs:after:bottom-[-5px] group-data-horizontal/tabs:after:h-0.5 group-data-vertical/tabs:after:inset-y-0 group-data-vertical/tabs:after:-right-1 group-data-vertical/tabs:after:w-0.5 group-data-[variant=line]/tabs-list:data-active:after:opacity-100", className)), rest))
    }

    fun TabsContent(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.TabsPrimitive.Panel, mergeUiProps(props("data-slot" to "tabs-content", "className" to s.cn("flex-1 text-sm outline-none", className)), rest))
    }

    views.Tabs = ::Tabs
    views.TabsList = ::TabsList
    views.TabsTrigger = ::TabsTrigger
    views.TabsContent = ::TabsContent
    return views
}
