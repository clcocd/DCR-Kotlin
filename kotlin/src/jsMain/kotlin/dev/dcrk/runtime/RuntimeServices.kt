package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun defineFeature(meta: dynamic, impl: dynamic): dynamic {
    val result = mergeUiProps(meta, props("setup" to impl.setup, "shortcuts" to impl.shortcuts))
    val toggles = meta.toggles as Array<String>?
    val mapped = toggles?.mapNotNull { id ->
        val actions = impl.pageToggles
        val action = if (actions != null) actions[id] else null
        if (action == null) null else mergeUiProps(props("id" to id), action)
    }
    result.pageToggles = mapped?.toTypedArray() ?: undefinedValue()
    return result
}

internal fun defineBackgroundFeature(meta: dynamic, impl: dynamic): dynamic =
    mergeUiProps(props("id" to meta.id, "defaultEnable" to meta.defaultEnable, "settings" to meta.settings), impl)

internal fun moduleEnables(stored: dynamic): dynamic = if (record(stored)) stored else props()

internal fun settingsOf(definition: dynamic, stored: dynamic): dynamic {
    val schema = definition.settings ?: props()
    val values = moduleEnables(stored)
    return fromEntries(entriesOf(schema).map { pair ->
        val key = pair[0]
        arrayOf<dynamic>(key, normalizeSetting(pair[1], values[key]))
    }.toTypedArray())
}

internal fun createModuleRead(s: dynamic): (Array<String>) -> Promise<dynamic> = { ids ->
    jsPromise {
        val paths = ids.map { s.moduleSettingsKey(it) as String }
        val modulesKey = s.modulesKey as String
        val batch = awaitJs(s.readItems((listOf(modulesKey) + paths).toTypedArray()))
        val settings = js("new Map()")
        for ((index, id) in ids.withIndex()) settings.set(id, batch.get(paths[index]) ?: null)
        props("enables" to moduleEnables(batch.get(modulesKey)), "settings" to settings)
    }
}

internal fun createModuleHooks(s: dynamic): dynamic {
    fun current(id: String): dynamic {
        val select: (dynamic) -> dynamic = { state -> s.moduleSettings(id, state) }
        return s.useStore(s.moduleSettingsStore, select)
    }
    fun content(id: String): dynamic = current(id) ?: error("Module settings are not loaded: $id")
    fun running(id: String): dynamic {
        val selector: (dynamic) -> Boolean = { state -> state[id] === true }
        val active = s.useStore(s.runningModulesStore, selector)
        val settings = current(id)
        return if (active == true) settings else undefinedValue()
    }
    return props("useModuleSettings" to ::current, "useContentModuleSettings" to ::content, "useRunningModuleSettings" to ::running)
}

internal fun formBody(fields: dynamic): dynamic {
    val entries = entriesOf(fields).filter { jsTypeOf(it[1]) == "string" }.toTypedArray()
    return js("new URLSearchParams(entries)")
}

internal fun csrfBody(fields: dynamic): Promise<dynamic> = jsPromise {
    val cookies: dynamic = js("cookieStore")
    val token = awaitJs(cookies.get("ci_c"))
    formBody(mergeUiProps(props("ci_t" to (token?.value ?: "")), fields))
}

internal fun sha256(data: dynamic): Promise<String> = jsPromise {
    val hash = awaitJs(js("crypto").subtle.digest("SHA-256", data))
    val bytes = js("new Uint8Array(hash)")
    bytes.toHex() as String
}

internal fun once(action: dynamic): dynamic {
    var pending: dynamic = null
    val call: (Array<dynamic>) -> dynamic = { args ->
        if (pending == null) {
            val retry: (dynamic) -> dynamic = { error -> pending = null; throw error }
            pending = action.apply(null, args).catch(retry)
        }
        pending
    }
    return js("(...args) => call(args)")
}

internal fun focusOnMount(element: dynamic) { element?.focus() }
