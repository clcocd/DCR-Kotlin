package dev.dcrk

internal fun props(vararg entries: Pair<String, dynamic>): dynamic {
    val result: dynamic = js("({})")
    for ((name, value) in entries) result[name] = value
    return result
}

internal fun jsValues(value: dynamic): Array<dynamic> = js("Object.values")(value) as Array<dynamic>
internal fun jsArray(value: dynamic): Array<dynamic> = js("Array.from")(value) as Array<dynamic>
internal fun truthy(value: dynamic): Boolean = js("Boolean")(value) as Boolean
internal fun undefinedValue(): dynamic = js("undefined")

internal class ReactUi(private val react: dynamic) {
    fun h(type: dynamic, attributes: dynamic = null, vararg children: dynamic): dynamic {
        val args: dynamic = js("[type, attributes]")
        for (child in children) args.push(child)
        return react.createElement.apply(null, args)
    }

    fun tag(name: String, css: String, vararg children: dynamic): dynamic =
        h(name, props("className" to css), *children)

    fun div(css: String, vararg children: dynamic): dynamic = tag("div", css, *children)
    fun span(css: String, vararg children: dynamic): dynamic = tag("span", css, *children)
}

fun mountExtensionPage(react: dynamic, createRoot: (dynamic) -> dynamic, app: dynamic, provider: dynamic) {
    val doc: dynamic = js("document")
    val ui = ReactUi(react)
    val page = if (provider == null) ui.h(app) else ui.h(provider, null, ui.h(app))
    createRoot(doc.getElementById("root")).render(ui.h(react.StrictMode, null, page))
}

internal fun omitUiProps(input: dynamic, vararg keys: String): dynamic {
    val omitted = keys.toSet()
    return fromEntries(entriesOf(input).filterNot { (it[0] as String) in omitted }.toTypedArray())
}

internal fun mergeUiProps(vararg parts: dynamic): dynamic =
    js("Object.assign").apply(null, arrayOf<dynamic>(props()) + parts)

internal fun defaultUiValue(value: dynamic, fallback: dynamic): dynamic =
    if (value === undefinedValue()) fallback else value

internal fun uiOr(value: dynamic, fallback: dynamic): dynamic =
    if (truthy(value)) value else fallback
