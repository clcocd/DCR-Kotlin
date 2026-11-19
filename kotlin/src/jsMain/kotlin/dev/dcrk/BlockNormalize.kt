package dev.dcrk

import kotlin.js.jsTypeOf

private val detectModes = setOf("SAME", "CONTAIN", "NOT_SAME", "NOT_CONTAIN")
private val blockTypes = arrayOf("NICK", "ID", "IP", "TITLE", "TEXT", "COMMENT", "DCCON", "TAB")
private val defaultModes = arrayOf("SAME", "SAME", "SAME", "CONTAIN", "CONTAIN", "CONTAIN", "SAME", "SAME")

private fun isArray(value: dynamic): Boolean = js("Array.isArray")(value) as Boolean

/** JSON·저장소에서 읽은 값이 배열이 아닌 일반 객체인지 확인한다. */
private fun isRecord(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && !isArray(value)

private fun isValidMode(value: dynamic): Boolean =
    jsTypeOf(value) == "string" && (value as String) in detectModes

private fun isValidBlockEntry(value: dynamic): Boolean {
    if (!isRecord(value)) return false
    if (jsTypeOf(value.content) != "string" || (value.content as String).trim().isEmpty()) return false
    if (jsTypeOf(value.isRegex) != "boolean") return false
    if (jsTypeOf(value.gallery) != "undefined" && jsTypeOf(value.gallery) != "string") return false
    if (jsTypeOf(value.extra) != "undefined" && jsTypeOf(value.extra) != "string") return false
    if (jsTypeOf(value.mode) != "undefined" && !isValidMode(value.mode)) return false
    return true
}

/**
 * 원본의 차단 목록 유효성 검사, 기존 ID 복원, 중복 ID 교체 및
 * 레거시 extra 제거 규칙을 유지한다. 객체의 기존 키 순서도 보존한다.
 */
fun normalizeBlockList(value: dynamic, known: dynamic): dynamic {
    val result: dynamic = js("[]")
    if (!isArray(value)) return result

    val knownIds = mutableMapOf<String, String>()
    for (index in 0 until (known.length as Int)) {
        val entry: dynamic = known[index]
        val gallery = if (jsTypeOf(entry.gallery) == "string") entry.gallery as String else null
        knownIds[blockKey(entry.content as String, gallery)] = entry.id as String
    }

    val ids = mutableSetOf<String>()
    val copyFields: dynamic = js("Object.assign")
    val uuid: dynamic = js("crypto.randomUUID")
    for (index in 0 until (value.length as Int)) {
        val entry: dynamic = value[index]
        if (!isValidBlockEntry(entry)) continue

        val content = entry.content as String
        val gallery = if (jsTypeOf(entry.gallery) == "string") entry.gallery as String else null
        val mode = if (jsTypeOf(entry.mode) == "string") entry.mode as String else null
        val key = blockKey(content, gallery)
        val requested = if (jsTypeOf(entry.id) == "string") entry.id as String else knownIds[key]
        val id = if (requested != null && requested !in ids) requested else uuid() as String
        ids.add(id)

        val copy: dynamic = copyFields(js("({})"), entry)
        copy.id = id
        val oldExtra: dynamic = entry.extra
        if (jsTypeOf(oldExtra) == "string" && (oldExtra as String).isNotEmpty() &&
            oldExtra == composeExtra(entry.isRegex as Boolean, gallery, mode)
        ) {
            copy.extra = js("undefined")
        }
        result.push(copy)
    }
    return result
}

/** 알 수 없는 저장값은 유형별 원래 기본 모드로 돌린다. */
fun normalizeDefaults(value: dynamic): dynamic {
    val defaults: dynamic = js("({})")
    for (index in blockTypes.indices) defaults[blockTypes[index]] = defaultModes[index]
    if (!isRecord(value)) return defaults

    for (type in blockTypes) {
        val mode: dynamic = value[type]
        if (isValidMode(mode)) defaults[type] = mode
    }
    return defaults
}
