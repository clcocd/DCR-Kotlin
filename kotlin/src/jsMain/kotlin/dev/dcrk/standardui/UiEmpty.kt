package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiEmpty(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Empty(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty", "className" to s.cn("flex w-full min-w-0 flex-1 flex-col items-center justify-center gap-4 rounded-lg border-dashed p-12 text-center text-balance", className)), rest))
    }

    fun EmptyHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty-header", "className" to s.cn("flex max-w-sm flex-col items-center gap-2", className)), rest))
    }

    fun EmptyMedia(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "default")
        val rest = omitUiProps(input, "className", "variant")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty-icon", "data-variant" to variant, "className" to s.cn(s.emptyMediaVariants(props("variant" to variant, "className" to className)))), rest))
    }

    fun EmptyTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty-title", "className" to s.cn("text-lg font-medium tracking-tight", className)), rest))
    }

    fun EmptyDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty-description", "className" to s.cn("text-sm/relaxed text-muted-foreground [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary", className)), rest))
    }

    fun EmptyContent(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "empty-content", "className" to s.cn("flex w-full max-w-sm min-w-0 flex-col items-center gap-4 text-sm text-balance", className)), rest))
    }

    views.Empty = ::Empty
    views.EmptyHeader = ::EmptyHeader
    views.EmptyMedia = ::EmptyMedia
    views.EmptyTitle = ::EmptyTitle
    views.EmptyDescription = ::EmptyDescription
    views.EmptyContent = ::EmptyContent
    return views
}
