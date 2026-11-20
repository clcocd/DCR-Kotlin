package dev.dcrk

internal fun createUiToggleGroup(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun ToggleGroup(input: dynamic): dynamic {
        val className = input["className"]
        val variant = input["variant"]
        val size = input["size"]
        val spacing = defaultUiValue(input["spacing"], 2)
        val orientation = defaultUiValue(input["orientation"], "horizontal")
        val children = input["children"]
        val rest = omitUiProps(input, "className", "variant", "size", "spacing", "orientation", "children")
        return ui.h(s.ToggleGroupPrimitive, mergeUiProps(props("data-slot" to "toggle-group", "data-variant" to variant, "data-size" to size, "data-spacing" to spacing, "data-orientation" to orientation, "style" to props("--gap" to spacing), "className" to s.cn("group/toggle-group flex w-fit flex-row items-center gap-[--spacing(var(--gap))] rounded-md data-[spacing=0]:data-[variant=outline]:shadow-xs data-vertical:flex-col data-vertical:items-stretch", className)), rest),
            ui.h(s.ToggleGroupContext.Provider, props("value" to props("variant" to variant, "size" to size, "spacing" to spacing, "orientation" to orientation)),
                children
            )
        )
    }

    fun ToggleGroupItem(input: dynamic): dynamic {
        val className = input["className"]
        val children = input["children"]
        val variant = defaultUiValue(input["variant"], "default")
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "children", "variant", "size")
        val context = s.React.useContext(s.ToggleGroupContext)
        return ui.h(s.TogglePrimitive, mergeUiProps(props("data-slot" to "toggle-group-item", "data-variant" to uiOr(context.variant, variant), "data-size" to uiOr(context.size, size), "data-spacing" to context.spacing, "className" to s.cn("shrink-0 group-data-[spacing=0]/toggle-group:rounded-none group-data-[spacing=0]/toggle-group:px-2 group-data-[spacing=0]/toggle-group:shadow-none focus:z-10 focus-visible:z-10 group-data-[spacing=0]/toggle-group:has-data-[icon=inline-end]:pr-1.5 group-data-[spacing=0]/toggle-group:has-data-[icon=inline-start]:pl-1.5 group-data-horizontal/toggle-group:data-[spacing=0]:first:rounded-l-md group-data-vertical/toggle-group:data-[spacing=0]:first:rounded-t-md group-data-horizontal/toggle-group:data-[spacing=0]:last:rounded-r-md group-data-vertical/toggle-group:data-[spacing=0]:last:rounded-b-md data-[state=on]:bg-muted group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:border-l-0 group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:border-t-0 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-l group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-t", s.toggleVariants(props("variant" to uiOr(context.variant, variant), "size" to uiOr(context.size, size))), className)), rest),
            children
        )
    }

    views.ToggleGroup = ::ToggleGroup
    views.ToggleGroupItem = ::ToggleGroupItem
    return views
}
