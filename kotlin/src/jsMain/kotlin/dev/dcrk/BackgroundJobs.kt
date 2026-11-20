package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun startAutoBackup(s: dynamic) {
    val browser = s.browser
    val storage = s.backupStorage
    val alarms = browser.alarms
    val promise: dynamic = js("Promise")
    val name = "refresher:autoBackup"
    var edits = 0
    fun arm(): dynamic = alarms.create(name, props("delayInMinutes" to 1))
    val log: (dynamic) -> dynamic = { console.error(it); undefinedValue() }
    val onChange: (dynamic) -> Unit = { changes ->
        if (ownKeys(changes).any { s.isBackupTarget(it) == true }) {
            edits++
            val check: (Boolean) -> Unit = {
                if (it) {
                    arm().catch(log)
                    storage.pending().setValue(true)
                }
            }
            storage.auto().getValue().then(check)
        }
    }
    browser.storage.local.onChanged.addListener(onChange)
    val rearm: () -> Unit = {
        val pending = promise.all(arrayOf<dynamic>(storage.pending().getValue(), alarms.get(name)))
        val update: (dynamic) -> dynamic = { result -> if (result[0] == true && result[1] == null) arm() else undefinedValue() }
        pending.then(update).catch(log)
    }
    if (s.firefox == true) rearm() else {
        browser.runtime.onStartup.addListener(rearm)
        browser.runtime.onInstalled.addListener(rearm)
    }
    val alarm: (dynamic) -> Unit = { event ->
        if (event.name == name) {
            val seen = edits
            val attempt = jsPromise {
                val auto = awaitJs(storage.auto().getValue())
                if (auto == true) awaitJs(s.runBackup("auto"))
                if (edits == seen) awaitJs(storage.pending().setValue(false))
            }
            attempt.catch { }
        }
    }
    alarms.onAlarm.addListener(alarm)
}

internal fun fillBlockIds(s: dynamic): Promise<dynamic> = jsPromise {
    for (type in s.blockTypes as Array<String>) {
        val key = s.blockListKey(type)
        val value = awaitJs(s.storage.getItem(key))
        if (js("Array.isArray")(value) != true) continue
        val entries = value as Array<dynamic>
        fun missing(entry: dynamic) = record(entry) && jsTypeOf(entry.id) != "string"
        if (!entries.any(::missing)) continue
        val patch = entries.map { if (missing(it)) setField(it, "id", js("crypto.randomUUID()")) else it }.toTypedArray()
        awaitJs(s.storage.setItem(key, patch))
    }
    undefinedValue()
}

internal fun startDatabaseUpdates(s: dynamic): (dynamic) -> dynamic {
    val name = "refresher:dbCheck"
    val period = 24 * 60
    val alarms = s.browser.alarms
    val promise: dynamic = js("Promise")
    val log: (dynamic) -> dynamic = { console.error(it); undefinedValue() }
    var updating: dynamic = null
    fun update(forceArg: dynamic): dynamic {
        val force = forceArg == true
        var current = updating
        if (current == null || force) {
            val start: () -> dynamic = { s.updateDatabase(force) }
            val next = promise.allSettled(arrayOf<dynamic>(current)).then(start)
            current = next
            updating = next
            val clear: () -> Unit = { if (updating === next) updating = null }
            promise.allSettled(arrayOf<dynamic>(next)).then(clear)
        }
        return if (force) current else current.catch(log)
    }
    if (s.production == true) {
        val ensure: (dynamic) -> Unit = { old ->
            if (old?.periodInMinutes != period) alarms.create(name, props("delayInMinutes" to 1, "periodInMinutes" to period))
        }
        alarms.get(name).then(ensure)
    }
    val listener: (dynamic) -> Unit = { event ->
        if (event.name == name) {
            val inspect: (dynamic) -> dynamic = { meta ->
                val last = (meta.lastUpdate ?: Double.NaN) as Double
                if (meta.format != s.format || last > Date.now() || Date.now() - last > 604_800_000) update(false) else undefinedValue()
            }
            s.dbMetaStorage().getValue().then(inspect)
        }
    }
    alarms.onAlarm.addListener(listener)
    return ::update
}
