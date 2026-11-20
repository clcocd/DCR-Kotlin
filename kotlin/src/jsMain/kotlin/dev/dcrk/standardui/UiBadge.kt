package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiBadge(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Badge(input: dynamic): dynamic {
        val className = input["className"]
        val variant = defaultUiValue(input["variant"], "default")
        val render = input["render"]
        val rest = omitUiProps(input, "className", "variant", "render")
        return s.useRender(props("defaultTagName" to "span", "props" to s.mergeProps(props("className" to s.cn(s.badgeVariants(props("variant" to variant)), className)), rest), "render" to render, "state" to props("slot" to "badge", "variant" to variant)))
    }

    views.Badge = ::Badge
    return views
}
