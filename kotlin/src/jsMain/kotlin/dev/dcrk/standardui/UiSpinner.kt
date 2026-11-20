package dev.dcrk

internal fun createUiSpinner(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Spinner(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h(s.Loader2Icon, mergeUiProps(props("data-slot" to "spinner", "role" to "status", "aria-label" to "Loading", "className" to s.cn("size-4 animate-spin", className)), rest))
    }

    views.Spinner = ::Spinner
    return views
}
