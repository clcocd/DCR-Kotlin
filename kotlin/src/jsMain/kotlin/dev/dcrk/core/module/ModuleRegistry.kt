package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

private class RegistryInstance(val def: dynamic, val id: String) {
    val settings: dynamic = props()
    var running: RegistryRun? = null
    var failed = false
    var settling: dynamic = null
}

private class RegistryRun(val controller: dynamic) {
    val listeners = mutableListOf<(dynamic) -> Unit>()
    val revokers = mutableListOf<() -> Unit>()
    var ready = false
    var api: dynamic = undefinedValue()
    var setup: dynamic = undefinedValue()
}

fun createModuleRegistry(s: dynamic): dynamic {
    val instances = linkedMapOf<String, RegistryInstance>()
    val impls = mutableMapOf<String, dynamic>()
    val registering = mutableSetOf<String>()
    val console: dynamic = js("console")
    val promise: dynamic = js("Promise")
    var stopped = false
    var resync: (() -> Promise<dynamic>)? = null
    var loading: dynamic = undefinedValue()

    fun enabled(meta: dynamic, values: dynamic): Boolean = moduleEnabled(meta.id as String, meta.defaultEnable as Boolean?, values)

    fun runRevoke(instance: RegistryInstance, revoke: () -> Unit) {
        try {
            revoke()
        } catch (failure: Throwable) {
            console.error("Module revoke failed: ${instance.id}", failure)
        }
    }

    fun stop(instance: RegistryInstance, keepDom: Boolean = false) {
        val running = instance.running ?: return
        instance.running = null
        if (running.ready) s.moduleApisStore.setState(props(instance.id to undefinedValue()))
        instance.settling = running.setup?.catch { _: dynamic -> undefinedValue() }
        s.runningModulesStore.setState(props(instance.id to false))
        running.controller.abort()
        if (!keepDom) running.revokers.forEach { runRevoke(instance, it) }
    }

    fun start(instance: RegistryInstance): Promise<dynamic> = jsPromise start@{
        if (stopped) return@start undefinedValue()
        val active = instance.running
        if (active != null) return@start awaitJs(active.setup)

        val controller: dynamic = js("new AbortController()")
        val signal = controller.signal
        val running = RegistryRun(controller)
        fun addCleanup(dispose: () -> Unit) {
            if (signal.aborted == true) dispose()
            else signal.addEventListener("abort", { dispose() }, props("once" to true))
        }

        val add: (String, (dynamic) -> Unit) -> (() -> Unit) = add@{ scope, callback ->
            if (signal.aborted == true) return@add {}
            val dispose: () -> Unit = s.addFilter(scope, callback)
            addCleanup(dispose)
            dispose
        }
        val settingsListener: ((dynamic) -> Unit) -> Unit = { listen ->
            if (signal.aborted != true) running.listeners.add(listen)
        }
        val revoke: (() -> Unit) -> Unit = { cleanup ->
            if (signal.aborted != true) running.revokers.add(cleanup)
            else if (!stopped) runRevoke(instance, cleanup)
        }
        val ctx = props("settings" to instance.settings, "signal" to signal, "addFilter" to add,
            "addCleanup" to ::addCleanup, "onSettingsChanged" to settingsListener, "onRevoke" to revoke)
        instance.running = running
        s.runningModulesStore.setState(props(instance.id to true))

        val previous = instance.settling
        val setup = jsPromise setup@{
            try {
                if (previous != null) {
                    awaitJs(previous)
                    if (signal.aborted == true) return@setup undefinedValue()
                }
                val api = awaitJs(instance.def.setup(ctx))
                if (signal.aborted != true) {
                    running.api = api
                    running.ready = true
                    s.moduleApisStore.setState(props(instance.id to api))
                }
            } catch (failure: Throwable) {
                if (signal.aborted != true) {
                    stop(instance)
                    instance.failed = true
                }
                throw failure
            }
            undefinedValue()
        }
        running.setup = setup
        awaitJs(setup)
        undefinedValue()
    }

    fun applySettings(instance: RegistryInstance, stored: dynamic) {
        val changed: dynamic = js("new Set()")
        for (row in entriesOf(s.settingsOf(instance.def, stored))) {
            val key = row[0] as String
            if (s.shallow(instance.settings[key], row[1]) != true) {
                changed.add(key)
                instance.settings[key] = row[1]
            }
        }
        if (changed.size == 0) return
        s.moduleSettingsStore.setState(props(instance.id to js("Object.assign")(props(), instance.settings)))
        val listeners = instance.running?.listeners ?: return
        for (listener in listeners.toList()) {
            try {
                listener(changed)
            } catch (failure: Throwable) {
                console.error("Settings listener failed: ${instance.id}", failure)
            }
        }
    }

    fun moduleOf(id: String, loaders: dynamic): dynamic {
        val loader = loaders[id]
        val pending = impls[id] ?: (if (loader == null) null else loader())
        if (pending == null) error("$id has no loader.")
        impls[id] = pending
        pending.catch { _: dynamic -> impls.remove(id) }
        return pending
    }

    suspend fun prepare(before: dynamic) {
        if (before != null) awaitJs(before())
        else awaitJs(undefinedValue())
    }

    fun register(meta: dynamic, loaders: dynamic, stored: dynamic, enables: dynamic, before: dynamic): Promise<dynamic> = jsPromise register@{
        if (!enabled(meta, enables)) return@register undefinedValue()
        val id = meta.id as String
        if (instances.containsKey(id) || !registering.add(id)) error("already registered: $id")
        try {
            val def = awaitJs(moduleOf(id, loaders))
            val instance = RegistryInstance(def, id)
            instances[id] = instance
            if (def.settings != null) applySettings(instance, stored)
            prepare(before)
            awaitJs(start(instance))
        } finally {
            registering.remove(id)
        }
        undefinedValue()
    }

    fun ready(): List<Pair<RegistryInstance, RegistryRun>> = instances.values.mapNotNull { instance ->
        val run = instance.running
        if (run?.ready == true) instance to run else null
    }

    fun getApi(id: String): dynamic {
        val active = instances[id]?.running
        return if (active?.ready == true) active.api else undefinedValue()
    }

    fun runShortcut(command: String) {
        for ((instance, running) in ready()) {
            val shortcuts = instance.def.shortcuts
            if (shortcuts != null) {
                val action = shortcuts[command]
                if (action != null) action(running.api)
            }
        }
    }

    fun toggleStates(): Array<dynamic> = ready().flatMap { (instance, running) ->
        val toggles = instance.def.pageToggles as Array<dynamic>? ?: emptyArray()
        toggles.map { toggle ->
            val desc = if (jsTypeOf(toggle.desc) == "function") toggle.desc(running.api) else toggle.desc
            props("module" to instance.id, "id" to toggle.id, "desc" to desc, "on" to toggle.isOn(running.api))
        }
    }.toTypedArray()

    fun state(): dynamic = props("toggles" to toggleStates(), "failed" to instances.values.filter { it.failed }.map { it.id }.toTypedArray())
    fun settledState(): Promise<dynamic> = jsPromise {
        awaitJs(loading)
        awaitJs(resync?.invoke())
        state()
    }

    fun runToggle(action: dynamic) {
        val (instance, running) = ready().firstOrNull { it.first.id == action.module } ?: return
        val toggles = instance.def.pageToggles as Array<dynamic>? ?: emptyArray()
        toggles.firstOrNull { it.id === action.id }?.toggle(running.api)
    }

    fun stopAll() {
        stopped = true
        for (instance in instances.values) stop(instance, true)
    }

    fun load(metas: Array<dynamic>, loaders: dynamic, signal: dynamic, before: dynamic): Promise<dynamic> = jsPromise {
        val defaults = metas.filter { it.settings != null }.map { arrayOf<dynamic>(it.id, s.settingsOf(it, null)) }.toTypedArray()
        s.moduleSettingsStore.setState(fromEntries(defaults))

        val onPage = metas.filter { meta ->
            loaders[meta.id] != null && (meta.urls == null || (meta.urls as Array<dynamic>).any { it.test(s.documentUrl.pathname) == true })
        }
        val ids = onPage.map { it.id as String }.toTypedArray()
        val storedById = mutableMapOf<String, dynamic>()
        for (meta in onPage) {
            if (meta.settings == null) continue
            val id = meta.id as String
            val onChange: (dynamic) -> Unit = { next ->
                val instance = instances[id]
                if (instance == null) storedById[id] = next else applySettings(instance, next)
            }
            s.watchStorage(s.settingsKey(id), onChange, signal)
        }
        val loaded = awaitJs(s.readModuleStorage(ids))
        for (row in jsArray(loaded.settings)) {
            val id = row[0] as String
            if (!storedById.containsKey(id)) storedById[id] = row[1]
        }
        val jobs = onPage.map { register(it, loaders, storedById[it.id as String], loaded.enables, before) }.toTypedArray()
        val results = awaitJs(promise.allSettled(jobs)) as Array<dynamic>
        for ((index, result) in results.withIndex()) {
            if (result.status == "rejected") console.error("Failed to load module: ${onPage[index].id}", result.reason)
        }
        prepare(before)

        fun sync(next: dynamic): Promise<dynamic> = jsPromise sync@{
            try {
                prepare(before)
            } catch (failure: Throwable) {
                console.error(failure)
                return@sync undefinedValue()
            }
            val starts = mutableListOf<Promise<dynamic>>()
            for (instance in instances.values) {
                if (!enabled(instance.def, next)) {
                    instance.failed = false
                    stop(instance)
                } else if (!instance.failed) {
                    starts.add(start(instance).catch { console.error(it); undefinedValue() })
                }
            }
            for (meta in onPage) {
                val id = meta.id as String
                if (instances.containsKey(id) || id in registering || !enabled(meta, next)) continue
                starts.add(register(meta, loaders, storedById[id], next, before).catch {
                    console.error("Failed to load module: $id", it)
                    undefinedValue()
                })
            }
            awaitJs(promise.all(starts.toTypedArray()))
            undefinedValue()
        }

        val update: (dynamic) -> Unit = { next -> sync(s.enablesOf(next)) }
        s.watchStorage(s.modulesKey, update, signal)
        val restore: () -> dynamic = { jsPromise {
            val stored = awaitJs(s.readModuleStorage(ids))
            for (instance in instances.values) {
                if (instance.def.settings != null) applySettings(instance, stored.settings.get(instance.id))
            }
            for (row in jsArray(stored.settings)) storedById[row[0] as String] = row[1]
            awaitJs(sync(stored.enables))
        } }
        s.onBfcacheRestore(restore, signal)
        val refresh: () -> Promise<dynamic> = { jsPromise {
            val values = awaitJs(s.storage.getItem(s.modulesKey))
            awaitJs(sync(s.enablesOf(values)))
        } }
        resync = refresh
        signal.addEventListener("abort", { resync = null }, props("once" to true))
        awaitJs(refresh())
        undefinedValue()
    }

    fun loadAll(metas: Array<dynamic>, loaders: dynamic, signal: dynamic, before: dynamic): Promise<dynamic> {
        val done = load(metas, loaders, signal, before)
        loading = done.catch { undefinedValue() }
        return done
    }

    return props("loadAll" to ::loadAll, "stopAll" to ::stopAll, "getModuleApi" to ::getApi,
        "runShortcut" to ::runShortcut, "pageToggleStates" to ::toggleStates, "pageState" to ::state,
        "settledPageState" to ::settledState, "runPageToggle" to ::runToggle)
}
