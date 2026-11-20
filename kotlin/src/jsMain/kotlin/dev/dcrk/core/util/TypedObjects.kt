package dev.dcrk

/** JS 표준 내장 메서드의 런타임 동작을 유지하면서 TypeScript 호출부에서는 키 타입을 보존한다. */
internal fun typedObjectKeys(value: dynamic): dynamic = js("Object.keys")(value)
internal fun typedObjectEntries(value: dynamic): dynamic = js("Object.entries")(value)
internal fun typedObjectFromEntries(entries: dynamic): dynamic = js("Object.fromEntries")(entries)
internal fun typedArrayIncludes(array: dynamic, item: dynamic): Boolean = array.includes(item) as Boolean
