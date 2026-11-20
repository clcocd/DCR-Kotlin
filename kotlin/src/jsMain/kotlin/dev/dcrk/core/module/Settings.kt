package dev.dcrk

import kotlin.js.jsTypeOf
import kotlin.math.max
import kotlin.math.min

private val keyPattern = Regex("^[a-z0-9]$")
private val colorPattern = Regex("^#[0-9a-f]{6}$", RegexOption.IGNORE_CASE)

fun moduleEnabled(id: String, defaultEnable: Boolean?, enables: dynamic): Boolean {
    val value = enables[id]
    return if (jsTypeOf(value) == "boolean") value as Boolean else defaultEnable ?: true
}

fun defaultSetting(schema: dynamic): dynamic =
    if (schema.type == "order") schema.default.slice() else schema.default

/** 저장값을 기존 스키마로 검증한다. 브라우저 저장소에는 같은 JS 값 형식으로 돌려준다. */
fun normalizeSetting(schema: dynamic, value: dynamic): dynamic {
    val objectApi: dynamic = js("Object")
    val fallback = schema.default

    return when (schema.type as String) {
        "check" -> if (jsTypeOf(value) == "boolean") value else fallback
        "text" -> if (jsTypeOf(value) == "string") value.slice(0, schema.maxLength) else fallback
        "option" -> if (jsTypeOf(value) == "string" && (objectApi.hasOwn(schema.items, value) as Boolean)) value else fallback
        "key" -> if (jsTypeOf(value) == "string" && keyPattern.matches(value as String)) value else fallback
        "color" -> if (jsTypeOf(value) == "string" && colorPattern.matches(value as String)) value else fallback
        "range" -> {
            val number: dynamic = js("Number")
            val math: dynamic = js("Math")
            if (number.isFinite(value) as Boolean) {
                val lower = schema.min as Double
                val upper = schema.max as Double
                val step = schema.step as Double
                val steps = math.round(((value as Double) - lower) / step) as Double
                min(upper, max(lower, lower + steps * step))
            } else fallback
        }
        "order" -> {
            val array: dynamic = js("Array")
            val allowed = (objectApi.keys(schema.items) as Array<String>).toSet()
            val stored = if (array.isArray(value) as Boolean) value else fallback
            val ordered = linkedSetOf<String>()
            for (key in stored as Array<dynamic>) {
                if (jsTypeOf(key) == "string" && (key as String) in allowed) ordered.add(key as String)
            }
            for (key in fallback as Array<String>) if (key in allowed) ordered.add(key)
            ordered.toTypedArray()
        }
        else -> js("undefined")
    }
}
