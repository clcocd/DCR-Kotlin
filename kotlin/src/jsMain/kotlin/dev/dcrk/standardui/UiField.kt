package dev.dcrk

internal fun createUiField(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun FieldSet(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("fieldset", mergeUiProps(props("data-slot" to "field-set", "className" to s.cn("flex flex-col gap-6 has-[>[data-slot=checkbox-group]]:gap-3 has-[>[data-slot=radio-group]]:gap-3", className)), rest))
    }

    fun FieldLegend(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "legend")
        val rest = omitUiProps(input, "className", "variant")
        return ui.h("legend", mergeUiProps(props("data-slot" to "field-legend", "data-variant" to variant, "className" to s.cn("mb-3 font-medium data-[variant=label]:text-sm data-[variant=legend]:text-base", className)), rest))
    }

    fun FieldGroup(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "field-group", "className" to s.cn("group/field-group @container/field-group flex w-full flex-col gap-7 data-[slot=checkbox-group]:gap-3 *:data-[slot=field-group]:gap-4", className)), rest))
    }

    fun Field(input: dynamic): dynamic {
        val className = input["className"]
        val orientation = defaultUiValue(input["orientation"], "vertical")
        val rest = omitUiProps(input, "className", "orientation")
        return ui.h("div", mergeUiProps(props("role" to "group", "data-slot" to "field", "data-orientation" to orientation, "className" to s.cn(s.fieldVariants(props("orientation" to orientation)), className)), rest))
    }

    fun FieldContent(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "field-content", "className" to s.cn("group/field-content flex flex-1 flex-col gap-1 leading-snug", className)), rest))
    }

    fun FieldLabel(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.Label, mergeUiProps(props("data-slot" to "field-label", "className" to s.cn("group/field-label peer/field-label flex w-fit gap-2 leading-snug group-data-[disabled=true]/field:opacity-50 has-data-checked:border-primary/30 has-data-checked:bg-primary/5 has-[>[data-slot=field]]:rounded-md has-[>[data-slot=field]]:border has-[>[data-slot=field]]:not-has-[:disabled,[data-disabled]]:hover:bg-muted/50 has-[>[data-slot=field]]:has-[:focus-visible]:border-ring has-[>[data-slot=field]]:has-[:focus-visible]:ring-3 has-[>[data-slot=field]]:has-[:focus-visible]:ring-ring/50 *:data-[slot=field]:p-3 dark:has-data-checked:border-primary/20 dark:has-data-checked:bg-primary/10", "has-[>[data-slot=field]]:w-full has-[>[data-slot=field]]:flex-col", className)), rest))
    }

    fun FieldTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "field-label", "className" to s.cn("flex w-fit items-center gap-2 text-sm font-medium group-data-[disabled=true]/field:opacity-50", className)), rest))
    }

    fun FieldDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("p", mergeUiProps(props("data-slot" to "field-description", "className" to s.cn("text-left text-sm leading-normal font-normal text-muted-foreground group-has-data-horizontal/field:text-balance [[data-variant=legend]+&]:-mt-1.5", "last:mt-0 nth-last-2:-mt-1", "[&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary", className)), rest))
    }

    fun FieldSeparator(input: dynamic): dynamic {
        val children = input["children"]
        val className = input["className"]
        val rest = omitUiProps(input, "children", "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "field-separator", "data-content" to truthy(children), "className" to s.cn("relative -my-2 h-5 text-sm group-data-[variant=outline]/field-group:-mb-2", className)), rest),
            ui.h(s.Separator, props("className" to "absolute inset-0 top-1/2")),
            if (truthy(children)) ui.h("span", props("className" to "relative mx-auto block w-fit bg-background px-2 text-muted-foreground", "data-slot" to "field-separator-content"),
                children
            ) else null
        )
    }

    views.FieldSet = ::FieldSet
    views.FieldLegend = ::FieldLegend
    views.FieldGroup = ::FieldGroup
    views.Field = ::Field
    views.FieldContent = ::FieldContent
    views.FieldLabel = ::FieldLabel
    views.FieldTitle = ::FieldTitle
    views.FieldDescription = ::FieldDescription
    views.FieldSeparator = ::FieldSeparator
    return views
}
