package dev.dcrk

internal fun createUiInputGroup(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun InputGroup(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "input-group", "role" to "group", "className" to s.cn("group/input-group relative flex h-9 w-full min-w-0 items-center rounded-md border border-input shadow-xs transition-[color,box-shadow] outline-none in-data-[slot=combobox-content]:focus-within:border-inherit in-data-[slot=combobox-content]:focus-within:ring-0 has-[[data-slot=input-group-control]:focus-visible]:border-ring has-[[data-slot=input-group-control]:focus-visible]:ring-3 has-[[data-slot=input-group-control]:focus-visible]:ring-ring/50 has-[[data-slot][aria-invalid=true]]:border-destructive has-[[data-slot][aria-invalid=true]]:ring-3 has-[[data-slot][aria-invalid=true]]:ring-destructive/20 has-[>[data-align=block-end]]:h-auto has-[>[data-align=block-end]]:flex-col has-[>[data-align=block-start]]:h-auto has-[>[data-align=block-start]]:flex-col has-[>textarea]:h-auto dark:bg-input/30 dark:has-[[data-slot][aria-invalid=true]]:ring-destructive/40 has-[>[data-align=block-end]]:[&>input]:pt-3 has-[>[data-align=block-start]]:[&>input]:pb-3 has-[>[data-align=inline-end]]:[&>input]:pr-1.5 has-[>[data-align=inline-start]]:[&>input]:pl-1.5", className)), rest))
    }

    fun InputGroupButton(input: dynamic): dynamic {
        val className = input["className"]
        val type = defaultUiValue(input["type"], "button")
        val variant = defaultUiValue(input["variant"], "ghost")
        val size = defaultUiValue(input["size"], "xs")
        val rest = omitUiProps(input, "className", "type", "variant", "size")
        return ui.h(s.Button, mergeUiProps(props("type" to type, "data-size" to size, "variant" to variant, "className" to s.cn(s.inputGroupButtonVariants(props("size" to size)), className)), rest))
    }

    fun InputGroupText(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("span", mergeUiProps(props("className" to s.cn("flex items-center gap-2 text-sm text-muted-foreground [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4", className)), rest))
    }

    fun InputGroupInput(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.Input, mergeUiProps(props("data-slot" to "input-group-control", "className" to s.cn("flex-1 rounded-none border-0 bg-transparent shadow-none ring-0 focus-visible:ring-0 aria-invalid:ring-0 dark:bg-transparent", className)), rest))
    }

    fun InputGroupTextarea(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.Textarea, mergeUiProps(props("data-slot" to "input-group-control", "className" to s.cn("flex-1 resize-none rounded-none border-0 bg-transparent py-2 shadow-none ring-0 focus-visible:ring-0 aria-invalid:ring-0 dark:bg-transparent", className)), rest))
    }

    views.InputGroup = ::InputGroup
    views.InputGroupButton = ::InputGroupButton
    views.InputGroupText = ::InputGroupText
    views.InputGroupInput = ::InputGroupInput
    views.InputGroupTextarea = ::InputGroupTextarea
    return views
}
