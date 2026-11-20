package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun record(value: dynamic): Boolean =
    value != null && jsTypeOf(value) == "object" && js("Array.isArray")(value) != true

internal fun entriesOf(value: dynamic): Array<dynamic> = js("Object.entries")(value) as Array<dynamic>
internal fun ownKeys(value: dynamic): Array<String> = js("Object.keys")(value) as Array<String>
internal fun fromEntries(entries: Array<out dynamic>): dynamic = js("Object.fromEntries")(entries)
internal fun setField(base: dynamic, field: String, value: dynamic): dynamic =
    js("Object.fromEntries([...Object.entries(base), [field, value]])")

internal fun singleFlight(action: (dynamic) -> Promise<dynamic>): (dynamic) -> Promise<dynamic> {
    var started: Promise<dynamic>? = null
    return { argument ->
        started ?: action(argument).catch { failure ->
            started = null
            throw failure
        }.also { started = it }
    }
}

internal fun canonical(value: dynamic): dynamic {
    val replace: (String, dynamic) -> dynamic = { _, item ->
        if (record(item)) fromEntries(ownKeys(item).sorted().map { key -> arrayOf<dynamic>(key, item[key]) }.toTypedArray())
        else item
    }
    return js("JSON.stringify")(value, replace)
}

internal fun sizeOf(value: dynamic): Int? = when {
    js("Array.isArray")(value) == true -> value.length as Int
    record(value) -> ownKeys(value).size
    else -> null
}

fun createStorageSync(s: dynamic): dynamic {
    val storage = s.storage
    val console: dynamic = js("console")
    val promise: dynamic = js("Promise")

    fun watch(key: String, callback: (dynamic, dynamic) -> Unit, signal: dynamic): () -> Unit {
        if (signal?.aborted == true) return {}
        val unwatch = storage.watch(key, callback)
        val dispose: () -> Unit = { if (s.runtimeActive() == true) unwatch() }
        signal?.addEventListener("abort", dispose, props("once" to true))
        return dispose
    }

    fun read(keys: Array<String>): Promise<dynamic> = jsPromise {
        val items = awaitJs(storage.getItems(keys.copyOf())) as Array<dynamic>
        val index: dynamic = js("new Map()")
        for (item in items) index.set(item.key, item.value)
        index
    }

    fun sync(keys: Array<String>, onChange: (String, dynamic) -> Unit): dynamic {
        var pending = 0
        fun apply(key: String, value: dynamic) {
            if (pending == 0) onChange(key, value)
        }

        fun load(): Promise<dynamic> = jsPromise {
            val items = awaitJs(read(keys))
            for (key in keys) apply(key, items.get(key) ?: null)
            undefinedValue()
        }

        val start = singleFlight { signal -> jsPromise {
            val changed = mutableSetOf<String>()
            val unwatches = keys.map { key ->
                watch(key, { next, _ -> changed.add(key); apply(key, next) }, signal)
            }
            try {
                val values = awaitJs(read(keys))
                for (key in keys) if (key !in changed) apply(key, values.get(key) ?: null)
            } catch (failure: Throwable) {
                unwatches.forEach { it() }
                throw failure
            }
            s.onBfcacheRestore(::load, signal)
            undefinedValue()
        } }

        fun save(write: () -> dynamic, failure: String): Promise<dynamic> = jsPromise {
            pending++
            val writing = promise.resolve().then(write).finally { pending-- }
            awaitJs(s.saveOrReload(writing, ::load, failure))
            if (pending == 0) awaitJs(load().catch { console.error(it) })
            undefinedValue()
        }
        return props("load" to ::load, "start" to start, "save" to ::save)
    }

    fun lists(o: dynamic): dynamic {
        val types = o.types as Array<String>
        val extra = o.extra ?: props()
        val typeOf = types.associateBy { o.keyOf(it) as String }
        val changed: (String, dynamic) -> Unit = change@{ key, value ->
            val type = typeOf[key]
            if (type == null) {
                val listener = extra[key]
                if (listener != null) listener(value)
                return@change
            }
            val next = o.normalize(value)
            val lists = o.get()
            val current = lists[type]
            if (sizeOf(current) != sizeOf(next) || canonical(current) !== canonical(next)) o.set(setField(lists, type, next))
        }

        val bound = sync((typeOf.keys + ownKeys(extra)).toTypedArray(), changed)
        val lock = o.lock as String?
        val lockNames = if (s.allowLocks() == true) lock else null
        val queue = s.limit(1)
        fun enqueue(write: () -> dynamic): dynamic {
            val navigator: dynamic = js("navigator")
            return if (lockNames.isNullOrEmpty()) queue(write) else navigator.locks.request(lockNames, write)
        }

        fun update(type: String, change: (dynamic) -> dynamic): Promise<dynamic> = jsPromise {
            o.set(setField(o.get(), type, change(o.get()[type])))
            val write: () -> dynamic = {
                enqueue {
                    jsPromise {
                        val key = o.keyOf(type)
                        val previous = awaitJs(storage.getItem(key))
                        val next = change(o.normalize(previous))
                        awaitJs(storage.setItem(key, next))
                    }
                }
            }
            awaitJs(bound.save(write, o.failure))
            undefinedValue()
        }
        bound.update = ::update
        return bound
    }

    return props("watch" to ::watch, "read" to ::read, "sync" to ::sync, "lists" to ::lists)
}
