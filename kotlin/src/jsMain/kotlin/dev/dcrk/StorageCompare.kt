package dev.dcrk

import kotlin.js.jsTypeOf

/** 저장·JSON 계층에서 사용하는 배열이 아닌 JS 객체 검사. */
fun isJsRecord(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && !(js("Array.isArray")(value) as Boolean)

/**
 * 원본 core/storage/sync.ts의 JSON.stringify replacer.
 * 객체 키만 정렬하고 배열 순서와 원래 JSON 변환 방식은 그대로 둔다.
 */
fun canonicalStorageValue(value: dynamic): dynamic {
    val replacer: (String, dynamic) -> dynamic = { _, item ->
        if (!isJsRecord(item)) {
            item
        } else {
            val keys: dynamic = js("Object.keys")(item)
            keys.sort()
            val entries: dynamic = js("[]")
            for (index in 0 until (keys.length as Int)) {
                val key = keys[index] as String
                entries.push(arrayOf<dynamic>(key, item[key]))
            }
            js("Object.fromEntries")(entries)
        }
    }
    return js("JSON.stringify")(value, replacer)
}

/** 크기가 같은 목록에만 비용이 큰 비교를 수행한다. */
fun storageSizeOf(value: dynamic): dynamic = when {
    js("Array.isArray")(value) as Boolean -> value.length
    isJsRecord(value) -> js("Object.keys")(value).length
    else -> js("undefined")
}
