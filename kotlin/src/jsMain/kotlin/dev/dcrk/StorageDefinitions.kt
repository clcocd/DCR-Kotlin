package dev.dcrk

internal fun rawStorageKey(key: String): String = key.drop("local:".length)
internal fun blockListKey(type: String): String = "local:refresher:block:$type"
internal fun memoMapKey(type: String): String = "local:refresher:memo:$type"
internal fun moduleSettingsKey(id: String): String = "local:refresher:module:$id:settings"
internal fun moduleDataKey(id: String): String = "local:refresher:module:$id:data"
private val settingsKey = Regex("^refresher:module:(.+):settings$")
private val moduleKey = Regex("^refresher:module:(.+):(?:settings|data)$")
private val blockListKeyPattern = Regex("^refresher:block:[A-Z]+$")
internal fun settingsKeyModule(key: String): dynamic = settingsKey.find(key)?.groupValues?.get(1) ?: undefinedValue()
internal fun moduleKeyModule(key: String): dynamic = moduleKey.find(key)?.groupValues?.get(1) ?: undefinedValue()
internal fun isBlockListKey(key: String): Boolean = blockListKeyPattern.containsMatchIn(key)

internal fun createStorageDefinitions(s: dynamic): dynamic {
    val storage = s.storage
    val definitions = mutableMapOf<String, dynamic>()
    fun lazy(key: String, fallback: dynamic): () -> dynamic {
        var item: dynamic = null
        return {
            if (item == null) item = storage.defineItem(key, props("fallback" to fallback))
            item
        }
    }
    fun moduleSettings(id: String): dynamic = definitions.getOrPut(id) { storage.defineItem(moduleSettingsKey(id), props("fallback" to props())) }
    fun moduleData(id: String, fallback: dynamic): dynamic = storage.defineItem(moduleDataKey(id), props("fallback" to fallback))
    val metadata = lazy(s.metaKey as String, props("version" to "", "lastUpdate" to 0))
    fun write(meta: dynamic, ip: String): dynamic =
        storage.setItems(arrayOf<dynamic>(props("item" to metadata(), "value" to meta),
            props("key" to s.keys.ip, "value" to ip)))
    var device: dynamic = null
    fun getDevice(): dynamic {
        if (device == null) {
            val init: () -> dynamic = { js("crypto.randomUUID()") }
            device = storage.defineItem("local:refresher:backup:device", props("init" to init))
        }
        return device
    }

    return props("blockDefaultsStorage" to lazy(s.blocksKey as String, mergeUiProps(s.defaults)),
        "modulesStorage" to lazy(s.modulesKey as String, props()), "moduleSettingsStorage" to ::moduleSettings,
        "moduleDataStorage" to ::moduleData, "dbMetaStorage" to metadata, "writeDatabase" to ::write,
        "backupStorage" to props("auto" to lazy("local:refresher:backup:auto", false), "error" to lazy("local:refresher:backup:error", ""),
            "pending" to lazy("local:refresher:backup:pending", false), "undo" to lazy("local:refresher:backup:undo", null), "device" to ::getDevice))
}

internal fun ownPreviewEntry(state: dynamic, now: Double): dynamic {
    val entry = previewEntry(state)
    return if (entry?.doc === now) entry else null
}

internal fun ownPreviewDepth(now: Double): Int = (ownPreviewEntry(js("history.state"), now)?.depth ?: 0) as Int
