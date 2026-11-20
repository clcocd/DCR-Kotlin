package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiLabel(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Label(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("label", mergeUiProps(props("data-slot" to "label", "className" to s.cn("flex items-center gap-2 text-sm leading-none font-medium select-none group-data-[disabled=true]:pointer-events-none group-data-[disabled=true]:opacity-50 peer-disabled:cursor-not-allowed peer-disabled:opacity-50", className)), rest))
    }

    views.Label = ::Label
    return views
}
