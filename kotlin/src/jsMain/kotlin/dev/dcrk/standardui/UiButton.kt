package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiButton(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Button(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "default")
        val size = defaultUiValue(input["size"], "default")
        val rest = omitUiProps(input, "className", "variant", "size")
        return ui.h(s.ButtonPrimitive, mergeUiProps(props("data-slot" to "button", "className" to s.cn(s.buttonVariants(props("variant" to variant, "size" to size, "className" to className)))), rest))
    }

    views.Button = ::Button
    return views
}
