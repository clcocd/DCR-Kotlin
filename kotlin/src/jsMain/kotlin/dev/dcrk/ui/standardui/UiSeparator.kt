package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiSeparator(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Separator(input: dynamic): dynamic {
        val className = input["className"]
        val orientation = defaultUiValue(input["orientation"], "horizontal")
        val rest = omitUiProps(input, "className", "orientation")
        return ui.h(s.SeparatorPrimitive, mergeUiProps(props("data-slot" to "separator", "orientation" to orientation, "className" to s.cn("shrink-0 bg-border data-horizontal:h-px data-horizontal:w-full data-vertical:w-px data-vertical:self-stretch", className)), rest))
    }

    views.Separator = ::Separator
    return views
}
