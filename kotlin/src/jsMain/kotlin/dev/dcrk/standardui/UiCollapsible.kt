package dev.dcrk

internal fun createUiCollapsible(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Collapsible(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.CollapsiblePrimitive.Root, mergeUiProps(props("data-slot" to "collapsible"), rest))
    }

    fun CollapsibleTrigger(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.CollapsiblePrimitive.Trigger, mergeUiProps(props("data-slot" to "collapsible-trigger"), rest))
    }

    fun CollapsibleContent(input: dynamic): dynamic {
        val rest = input
        return ui.h(s.CollapsiblePrimitive.Panel, mergeUiProps(props("data-slot" to "collapsible-content"), rest))
    }

    views.Collapsible = ::Collapsible
    views.CollapsibleTrigger = ::CollapsibleTrigger
    views.CollapsibleContent = ::CollapsibleContent
    return views
}
