package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiSlider(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    val slider = s.SliderPrimitive
    return { input ->
        val className = input.className
        val defaultValue = input.defaultValue
        val value = input.value
        val min = defaultUiValue(input.min, 0)
        val max = defaultUiValue(input.max, 100)
        val count = when {
            js("Array").isArray(value) == true -> value.length as Int
            js("Array").isArray(defaultValue) == true -> defaultValue.length as Int
            else -> 2
        }
        val rest = omitUiProps(input, "className", "defaultValue", "value", "min", "max")
        val thumbs = (0 until count).map { index ->
            ui.h(slider.Thumb, props("data-slot" to "slider-thumb", "key" to index,
                "className" to "block size-4 shrink-0 rounded-full border border-primary bg-white shadow-sm ring-ring/50 transition-[color,box-shadow] select-none hover:ring-4 focus-visible:ring-4 focus-visible:outline-hidden disabled:pointer-events-none disabled:opacity-50"))
        }.toTypedArray()
        ui.h(slider.Root, mergeUiProps(props("className" to s.cn("data-horizontal:w-full data-vertical:h-full", className),
            "data-slot" to "slider", "defaultValue" to defaultValue, "value" to value, "min" to min, "max" to max,
            "thumbAlignment" to "edge"), rest),
            ui.h(slider.Control, props("className" to "relative flex w-full touch-none items-center select-none data-disabled:opacity-50 data-vertical:h-full data-vertical:min-h-40 data-vertical:w-auto data-vertical:flex-col"),
                ui.h(slider.Track, props("data-slot" to "slider-track",
                    "className" to "relative grow overflow-hidden rounded-full bg-muted select-none data-horizontal:h-1.5 data-horizontal:w-full data-vertical:h-full data-vertical:w-1.5"),
                    ui.h(slider.Indicator, props("data-slot" to "slider-range", "className" to "bg-primary select-none data-horizontal:h-full data-vertical:w-full"))),
                thumbs))
    }
}

internal fun createInputGroupAddon(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    val click: (dynamic) -> Unit = { event ->
        if (event.target.closest("button") == null) event.currentTarget.parentElement?.querySelector("input")?.focus()
    }
    return { input ->
        val align = defaultUiValue(input.align, "inline-start")
        val rest = omitUiProps(input, "className", "align")
        ui.h("div", mergeUiProps(props("role" to "group", "data-slot" to "input-group-addon", "data-align" to align,
            "className" to s.cn(s.variants(props("align" to align)), input.className), "onClick" to click), rest))
    }
}

internal fun createFieldError(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    return { input ->
        val children = input.children
        val errors = input.errors as Array<dynamic>?
        val build: () -> dynamic = build@{
            if (truthy(children)) return@build children
            if (errors.isNullOrEmpty()) return@build null
            val indexed = js("new Map()")
            for (error in errors) indexed.set(error?.message ?: undefinedValue(), error)
            val unique = jsArray(indexed.values())
            if (unique.size == 1) return@build unique[0]?.message
            ui.h("ul", props("className" to "ml-4 flex list-disc flex-col gap-1"),
                unique.mapIndexed { index, error ->
                    if (truthy(error?.message)) ui.h("li", props("key" to index), error.message) else null
                }.toTypedArray())
        }
        val content = react.useMemo(build, arrayOf<dynamic>(children, errors))
        if (!truthy(content)) null else ui.h("div", mergeUiProps(props("role" to "alert", "data-slot" to "field-error",
            "className" to s.cn("text-sm font-normal text-destructive", input.className)),
            omitUiProps(input, "children", "errors", "className")), content)
    }
}
