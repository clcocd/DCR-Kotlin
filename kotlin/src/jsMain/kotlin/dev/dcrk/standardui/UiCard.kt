package dev.dcrk

internal fun createUiCard(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Card(input: dynamic): dynamic {
        val className = input["className"]
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "size")
        return ui.h("div", mergeUiProps(props("data-slot" to "card", "data-size" to size, "className" to s.cn("group/card flex flex-col gap-(--card-spacing) overflow-hidden rounded-xl bg-card py-(--card-spacing) text-sm text-card-foreground shadow-xs ring-1 ring-foreground/10 [--card-spacing:--spacing(6)] has-[>img:first-child]:pt-0 data-[size=sm]:[--card-spacing:--spacing(4)] *:[img:first-child]:rounded-t-xl *:[img:last-child]:rounded-b-xl", className)), rest))
    }

    fun CardHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-header", "className" to s.cn("group/card-header @container/card-header grid auto-rows-min items-start gap-1 rounded-t-xl px-(--card-spacing) has-data-[slot=card-action]:grid-cols-[1fr_auto] has-data-[slot=card-description]:grid-rows-[auto_auto] [.border-b]:pb-(--card-spacing)", className)), rest))
    }

    fun CardTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-title", "className" to s.cn("text-base leading-normal font-medium group-data-[size=sm]/card:text-sm", className)), rest))
    }

    fun CardDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-description", "className" to s.cn("text-sm text-muted-foreground", className)), rest))
    }

    fun CardAction(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-action", "className" to s.cn("col-start-2 row-span-2 row-start-1 self-start justify-self-end", className)), rest))
    }

    fun CardContent(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-content", "className" to s.cn("flex flex-col gap-3 px-(--card-spacing)", className)), rest))
    }

    fun CardFooter(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "card-footer", "className" to s.cn("flex items-center rounded-b-xl px-(--card-spacing) [.border-t]:pt-(--card-spacing)", className)), rest))
    }

    views.Card = ::Card
    views.CardHeader = ::CardHeader
    views.CardTitle = ::CardTitle
    views.CardDescription = ::CardDescription
    views.CardAction = ::CardAction
    views.CardContent = ::CardContent
    views.CardFooter = ::CardFooter
    return views
}
