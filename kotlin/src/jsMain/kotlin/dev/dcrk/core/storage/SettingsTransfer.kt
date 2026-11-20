package dev.dcrk

import dev.dcrk.block.*

import kotlin.js.Promise
import kotlin.js.jsTypeOf

private val oldSetting = Regex("^refresher:(module:.+:(enable|setting:.+)|block:[A-Z]+:mode)$")
private val oldList = Regex("^refresher:(block|memo):[A-Z]+$")

private fun isArray(value: dynamic): Boolean = js("Array.isArray")(value) as Boolean
private fun isRecord(value: dynamic): Boolean = value != null && jsTypeOf(value) == "object" && !isArray(value)
private fun keysOf(value: dynamic): Array<String> = js("Object.keys")(value) as Array<String>
private fun jsIn(key: String, target: dynamic): Boolean = js("key in target") as Boolean
private fun fields(old: dynamic, next: dynamic): dynamic =
    js("Object.fromEntries([...Object.entries(old), ...Object.entries(next)])")

fun pinDefaultMode(list: Array<dynamic>, from: String, local: String): Array<dynamic> =
    if (from == local) list else list.map { if (truthy(it.mode)) it else fields(it, props("mode" to from)) }.toTypedArray()

fun createSettingsTransfer(services: dynamic): dynamic {
    val storage = services.storage
    val defaultsKey = services.defaultsKey as String
    val modulesKey = services.modulesKey as String
    val blockTypes = services.blockListTypes
    val console: dynamic = js("console")

    fun isMapKey(key: String): Boolean = key == modulesKey || key == defaultsKey ||
        jsTypeOf(services.settingsKeyModule(key)) != "undefined"

    fun write(data: dynamic, mode: String): Promise<dynamic> {
        val modify: () -> Promise<dynamic> = {
            jsPromise {
                val previous = awaitJs(services.readBackupTargets())
                if (mode == "replace") {
                    val leftovers = (awaitJs(storage.getKeys()) as Array<String>).filter {
                        jsTypeOf(services.settingsKeyModule(it)) != "undefined" && services.isBackupTarget(it) != true
                    }
                    if (leftovers.isNotEmpty()) {
                        val values = awaitJs(storage.get(leftovers.toTypedArray()))
                        js("Object.assign")(previous, values)
                    }
                }

                val next = props()
                for (key in keysOf(data)) {
                    if (services.isBackupTarget(key) == true) next[key] = data[key]
                }
                val localDefaults = normalizeDefaults(previous[defaultsKey])
                val importDefaults = normalizeDefaults(next[defaultsKey])
                for (entry in jsArray(blockTypes)) {
                    val key = entry[0] as String
                    if (!jsIn(key, next)) continue
                    val type = entry[1] as String
                    val list = normalizeBlockList(next[key], normalizeBlockList(previous[key], emptyArray<dynamic>())) as Array<dynamic>
                    next[key] = if (mode == "merge") pinDefaultMode(list, importDefaults[type] as String, localDefaults[type] as String) else list
                }
                if (mode == "merge") {
                    js("Reflect.deleteProperty")(next, defaultsKey)
                    for (key in keysOf(next)) {
                        val old = previous[key]
                        val value = next[key]
                        if (isMapKey(key) && isRecord(old) && isRecord(value)) next[key] = fields(old, value)
                    }
                }
                if (keysOf(data).isNotEmpty() && keysOf(next).isEmpty()) error("쓸 수 있는 설정이 없습니다.")
                val removed = if (mode == "replace") keysOf(previous).filterNot { jsIn(it, next) } else emptyList()

                try {
                    awaitJs(storage.remove(removed.toTypedArray()))
                    awaitJs(storage.set(next))
                } catch (error: Throwable) {
                    val touched = removed + keysOf(next)
                    try {
                        val restored = props()
                        for (key in touched) if (jsIn(key, previous)) restored[key] = previous[key]
                        awaitJs(storage.remove(touched.filterNot { jsIn(it, previous) }.toTypedArray()))
                        awaitJs(storage.set(restored))
                    } catch (rollbackError: Throwable) {
                        console.error("설정 되돌리기 실패:", rollbackError)
                    }
                    throw error
                }

                val createdAt: Double = js("Date.now()") as Double
                try {
                    awaitJs(services.undo().setValue(props("data" to previous, "createdAt" to createdAt)))
                    createdAt
                } catch (error: Throwable) {
                    console.error(error)
                    undefinedValue()
                }
            }
        }
        val locks: dynamic = js("navigator.locks")
        val names = services.lockNames
        return Promise.resolve<dynamic>(locks.request(names.blocks, { locks.request(names.memos, { locks.request(names.moduleSettings, modify) }) }))
    }

    fun merge(current: dynamic, backup: dynamic): dynamic {
        val localDefaults = normalizeDefaults(current[defaultsKey])
        val backupDefaults = normalizeDefaults(backup[defaultsKey])
        val pairs = mutableListOf<Array<dynamic>>()
        for (key in keysOf(backup)) {
            val local = current[key]
            val value = backup[key]
            val resolved = if (services.isBlockListKey(key) == true) {
                val type = blockTypes.get(key) as String?
                val kept = normalizeBlockList(local, emptyArray<dynamic>()) as Array<dynamic>
                val seen = kept.map { blockKey(it.content as String, it.gallery as String?) }.toSet()
                val added = (normalizeBlockList(value, emptyArray<dynamic>()) as Array<dynamic>).filterNot {
                    blockKey(it.content as String, it.gallery as String?) in seen
                }.toTypedArray()
                kept + if (type == null) added else pinDefaultMode(added, backupDefaults[type] as String, localDefaults[type] as String)
            } else when {
                jsTypeOf(local) == "undefined" -> value
                isRecord(local) && isRecord(value) -> fields(value, local)
                else -> local
            }
            pairs.add(arrayOf<dynamic>(key, resolved))
        }
        pairs.add(arrayOf<dynamic>(defaultsKey, localDefaults))
        return js("Object.fromEntries")(pairs.toTypedArray())
    }

    fun parse(input: String): dynamic {
        val parsed = js("JSON.parse")(input)
        if (!isRecord(parsed)) error("가져오기 데이터는 JSON 객체여야 합니다.")
        val keys = keysOf(parsed)
        if (keys.none { it.startsWith("refresher:") }) error("설정 데이터가 아닙니다.")
        if (keys.any { oldSetting.matches(it) || (oldList.matches(it) && jsTypeOf(parsed[it]) == "string") }) {
            error("v5 데이터는 가져올 수 없습니다.")
        }
        for (key in keys) {
            val value = parsed[key]
            if ((services.isBlockListKey(key) == true && !isArray(value)) ||
                ((key.startsWith("refresher:memo:") || isMapKey(key)) && !isRecord(value))
            ) error("설정 값의 형태가 올바르지 않습니다: $key")
        }
        return parsed
    }

    return props("write" to ::write, "merge" to ::merge, "parse" to ::parse)
}
