package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiKbd(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Kbd(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("kbd", mergeUiProps(props("data-slot" to "kbd", "className" to s.cn("pointer-events-none inline-flex h-5 w-fit min-w-5 items-center justify-center gap-1 rounded-sm bg-muted px-1 font-sans text-xs font-medium text-muted-foreground select-none in-data-[slot=tooltip-content]:bg-background/20 in-data-[slot=tooltip-content]:text-background dark:in-data-[slot=tooltip-content]:bg-background/10 [&_svg:not([class*='size-'])]:size-3", className)), rest))
    }

    fun KbdGroup(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("kbd", mergeUiProps(props("data-slot" to "kbd-group", "className" to s.cn("inline-flex items-center gap-1", className)), rest))
    }

    views.Kbd = ::Kbd
    views.KbdGroup = ::KbdGroup
    return views
}
