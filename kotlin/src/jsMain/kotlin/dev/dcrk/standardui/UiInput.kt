package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiInput(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Input(input: dynamic): dynamic {
        val className = input["className"]
        val type = input["type"]
        val rest = omitUiProps(input, "className", "type")
        return ui.h(s.InputPrimitive, mergeUiProps(props("type" to type, "data-slot" to "input", "className" to s.cn("h-9 w-full min-w-0 rounded-md border border-input bg-transparent px-2.5 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm dark:bg-input/30 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40", className)), rest))
    }

    views.Input = ::Input
    return views
}
