package dev.dcrk

internal fun createUiToggle(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Toggle(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "default")
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "variant", "size")
        return ui.h(s.TogglePrimitive, mergeUiProps(props("data-slot" to "toggle", "className" to s.cn(s.toggleVariants(props("variant" to variant, "size" to size, "className" to className)))), rest))
    }

    views.Toggle = ::Toggle
    return views
}
