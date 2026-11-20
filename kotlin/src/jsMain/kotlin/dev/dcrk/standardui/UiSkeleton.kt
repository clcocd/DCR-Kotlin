package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiSkeleton(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Skeleton(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "skeleton", "className" to s.cn("animate-pulse rounded-md bg-muted", className)), rest))
    }

    views.Skeleton = ::Skeleton
    return views
}
