package dev.dcrk

import kotlin.js.Promise

internal fun createSettingsState(s: dynamic): dynamic {
    val features = s.features as Array<dynamic>
    val byId = features.associateBy { it.id as String }
    val withSchema = features.filter { truthy(it.settings) }
    val withSettings = withSchema.associateBy { s.moduleSettingsKey(it.id) as String }
    val stringify: dynamic = js("JSON.stringify")
    var store: dynamic = null
    var sync: dynamic = null

    fun same(a: dynamic, b: dynamic): Boolean = stringify(a) === stringify(b)
    fun resolve(stored: dynamic): dynamic = fromEntries(features.map {
        arrayOf<dynamic>(it.id, s.isModuleEnabled(it, stored))
    }.toTypedArray())
    fun enqueue(write: () -> dynamic): dynamic {
        val navigator: dynamic = js("navigator")
        return navigator.locks.request(s.lockName, write)
    }
    fun persist(write: () -> dynamic): dynamic {
        val locked: () -> dynamic = { enqueue(write) }
        return sync.save(locked, "모듈 설정을 저장하지 못했습니다.")
    }
    fun init(set: dynamic): dynamic {
        fun toggle(id: String, value: Boolean): Promise<dynamic> = jsPromise {
            val patch: (dynamic) -> dynamic = { props("enables" to setField(it.enables, id, value)) }
            set(patch)
            val write: () -> dynamic = {
                jsPromise {
                    val item = s.modulesStorage()
                    val current = awaitJs(item.getValue())
                    awaitJs(item.setValue(setField(current, id, value)))
                }
            }
            awaitJs(persist(write))
        }
        fun change(id: String, key: String, value: dynamic) = jsPromise {
            val feature = byId[id] ?: return@jsPromise
            val schema = feature.settings
            val definition = if (schema != null) schema[key] else null
            if (definition == null) return@jsPromise
            val next = s.normalizeSetting(definition, value)
            val patch: (dynamic) -> dynamic = { state ->
                val values = setField(state.values[id], key, next)
                props("values" to setField(state.values, id, values))
            }
            set(patch)
            val item = s.moduleSettingsStorage(id)
            val write: () -> dynamic = {
                jsPromise {
                    val current = awaitJs(item.getValue())
                    awaitJs(item.setValue(setField(current, key, next)))
                }
            }
            awaitJs(persist(write))
        }
        val values = fromEntries(features.map { arrayOf<dynamic>(it.id, s.settingsOf(it, null)) }.toTypedArray())
        return props("enables" to resolve(props()), "values" to values, "toggle" to ::toggle, "changeSetting" to ::change)
    }
    fun saveEnables(value: dynamic) {
        val next = resolve(s.enablesOf(value))
        if (!same(next, store.getState().enables)) store.setState(props("enables" to next))
    }
    fun saveValues(feature: dynamic, stored: dynamic) {
        val id = feature.id as String
        val values = s.settingsOf(feature, stored)
        if (!same(values, store.getState().values[id])) {
            val patch: (dynamic) -> dynamic = { props("values" to setField(it.values, id, values)) }
            store.setState(patch)
        }
    }
    fun attach(next: dynamic) {
        store = next
        val receive: (String, dynamic) -> Unit = { key, value ->
            val feature = withSettings[key]
            if (feature != null) saveValues(feature, value) else saveEnables(value)
        }
        sync = s.storageSync(arrayOf(s.modulesKey as String, *withSettings.keys.toTypedArray()), receive)
    }
    fun prune(): dynamic {
        val task: () -> dynamic = {
            jsPromise {
                val all: dynamic = js("Promise")
                val ids = withSchema.map { it.id as String }.toTypedArray()
                val pending = all.all(arrayOf<dynamic>(s.readModuleStorage(ids), s.listKeys()))
                val result = awaitJs(pending)
                val enables = result[0].enables
                val settings = result[0].settings
                val keys = result[1] as Array<String>
                val stale = ownKeys(enables).filter { it !in byId }.toMutableSet()
                for (key in keys) {
                    val id = s.moduleKeyModule(key) as String?
                    if (id != null && id !in byId) stale.add(id)
                }
                if (stale.isNotEmpty()) {
                    val remaining = fromEntries(entriesOf(enables).filterNot { it[0] in stale }.toTypedArray())
                    awaitJs(s.storage.setItem(s.modulesKey, remaining))
                    val old = stale.flatMap { listOf(s.moduleSettingsKey(it), s.moduleDataKey(it)) }.toTypedArray()
                    awaitJs(s.storage.removeItems(old))
                }
                for (feature in withSchema) {
                    val saved = settings.get(feature.id)
                    if (!record(saved)) continue
                    val fields = entriesOf(saved)
                    val required = fields.filter { js("Object").hasOwn(feature.settings, it[0]) == true }
                    if (required.size != fields.size) awaitJs(s.storage.setItem(s.moduleSettingsKey(feature.id), fromEntries(required.toTypedArray())))
                }
            }
        }
        return enqueue(task)
    }
    val initOnce = singleFlight {
        jsPromise {
            awaitJs(sync.start())
            val log: (dynamic) -> Unit = { console.error(it) }
            prune().catch(log)
            undefinedValue()
        }
    }
    val vars: () -> dynamic = {
        val enables = store { state: dynamic -> state.enables }
        val values = store { state: dynamic -> state.values }
        val effect: () -> dynamic = {
            val root = js("document.documentElement.style")
            val entries = features.flatMap { feature ->
                if (enables[feature.id] == true && truthy(feature.extensionPageVars)) entriesOf(feature.extensionPageVars(values[feature.id] ?: props())).asList()
                else emptyList()
            }
            for (pair in entries) root.setProperty(pair[0], pair[1])
            val dispose: () -> Unit = { for (pair in entries) root.removeProperty(pair[0]) }
            dispose
        }
        s.useLayoutEffect(effect, arrayOf<dynamic>(enables, values))
        null
    }
    return props("init" to ::init, "attach" to ::attach, "initModulesStore" to { initOnce(null) }, "ExtensionPageVars" to vars)
}
