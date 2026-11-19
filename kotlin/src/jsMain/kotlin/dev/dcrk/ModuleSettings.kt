package dev.dcrk

import kotlin.js.jsTypeOf

private val singleKey = Regex("^[a-z0-9]$")
private val hexColor = Regex("^#[0-9a-fA-F]{6}$")

/** 원본 utils/record.ts와 같은 판정: 배열과 null은 설정 객체가 아니다. */
private fun settingRecord(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && !(js("Array.isArray")(value) as Boolean)

/** 브라우저 저장소의 문자열 "false"는 boolean이 아니므로 기본 활성화 상태를 따른다. */
fun isModuleEnabledKotlin(def: dynamic, enables: dynamic): Boolean {
    val value = enables[def.id]
    if (jsTypeOf(value) == "boolean") return value as Boolean
    val fallback = def.defaultEnable
    return if (jsTypeOf(fallback) == "boolean") fallback as Boolean else true
}

/** 순서(order) 설정 기본값은 항상 원본 배열과 독립된 복사본을 반환한다. */
fun defaultSettingValue(schema: dynamic): dynamic =
    if ((schema.type as String) == "order") schema.default.slice() else schema.default

/** 원본 core/module/settings.ts의 검증, 반올림, 중복 제거 및 새 항목 보강 규칙. */
fun normalizeModuleSetting(schema: dynamic, value: dynamic): dynamic {
    return when (schema.type as String) {
        "check" -> if (jsTypeOf(value) == "boolean") value else schema.default
        "text" -> if (jsTypeOf(value) == "string") {
            (value as String).asDynamic().slice(0, schema.maxLength)
        } else schema.default
        "option" -> if (jsTypeOf(value) == "string" &&
            (js("Object.hasOwn")(schema.items, value) as Boolean)) value else schema.default
        "key" -> if (jsTypeOf(value) == "string" && singleKey.matches(value as String)) value else schema.default
        "color" -> if (jsTypeOf(value) == "string" && hexColor.matches(value as String)) value else schema.default
        "range" -> {
            if (jsTypeOf(value) != "number" || !(js("Number.isFinite")(value) as Boolean)) {
                schema.default
            } else {
                val min = schema.min as Double
                val max = schema.max as Double
                val step = schema.step as Double
                // kotlin.math.round는 tie-to-even을 사용하므로 JS Math.round로 원본 반올림을 보존한다.
                val rounded = js("Math.round")(((value as Double) - min) / step) as Double
                kotlin.math.min(max, kotlin.math.max(min, min + rounded * step))
            }
        }
        "order" -> {
            val chosen: dynamic = js("[]")
            val raw: dynamic = if (js("Array.isArray")(value) as Boolean) value else schema.default
            val keys: dynamic = js("Object.keys")(schema.items)
            val length = raw.length as Int
            for (index in 0 until length) {
                val key: dynamic = raw[index]
                if (jsTypeOf(key) == "string" &&
                    (keys.includes(key) as Boolean) &&
                    !(chosen.includes(key) as Boolean)) {
                    chosen.push(key)
                }
            }
            val defaults: dynamic = schema.default
            for (index in 0 until (defaults.length as Int)) {
                val key: dynamic = defaults[index]
                if ((keys.includes(key) as Boolean) && !(chosen.includes(key) as Boolean)) {
                    chosen.push(key)
                }
            }
            chosen
        }
        else -> schema.default
    }
}

fun moduleEnablesOf(stored: dynamic): dynamic = if (settingRecord(stored)) stored else js("({})")

/** 스키마에 정의된 키만 출력. 객체 prototype을 변경하지 않도록 Object.fromEntries로 구성한다. */
fun moduleSettingsOf(def: dynamic, stored: dynamic): dynamic {
    val values: dynamic = if (settingRecord(stored)) stored else js("({})")
    val schemaMap: dynamic = def.settings ?: js("({})")
    val keys: dynamic = js("Object.keys")(schemaMap)
    val entries: dynamic = js("[]")
    for (index in 0 until (keys.length as Int)) {
        val key = keys[index] as String
        entries.push(arrayOf<dynamic>(key, normalizeModuleSetting(schemaMap[key], values[key])))
    }
    return js("Object.fromEntries")(entries)
}
