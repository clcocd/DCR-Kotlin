package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf
import kotlin.math.ceil

private const val BACKUP_FORMAT = 1

internal fun createCloudBackup(s: dynamic): dynamic {
    val local = s.storage.local
    val sync = s.storage.sync
    val quota = (sync.QUOTA_BYTES ?: 102_400) as Int
    val chunkSize = ((sync.QUOTA_BYTES_PER_ITEM ?: 8_192) as Int) - 192
    val totalSize = quota - 2400
    val keys = (s.keys as Array<String>).toHashSet()
    val console: dynamic = js("console")
    val integer: dynamic = js("Number.isInteger")

    fun slotKey(slot: String) = if (slot == "manual") "backup" else "autoBackup"
    fun isMeta(value: dynamic): Boolean = record(value) && integer(value.format) == true && integer(value.chunks) == true
    fun isTarget(key: String): Boolean = key in keys

    suspend fun readTargets(): dynamic {
        val all = awaitJs(local.getKeys()) as Array<String>
        val selected = all.filter(::isTarget).toTypedArray()
        return if (selected.isEmpty()) props() else awaitJs(local.get(selected))
    }

    suspend fun collect(): dynamic {
        val values = readTargets()
        val entries = entriesOf(values).map { entry ->
            val key = entry[0] as String
            val value = entry[1]
            val cleaned = if (s.isBlockListKey(key) == true && js("Array.isArray")(value) == true) {
                (value as Array<dynamic>).map {
                    if (record(it)) setField(it, "id", undefinedValue()) else it
                }.toTypedArray()
            } else value
            arrayOf<dynamic>(key, cleaned)
        }
        return fromEntries(entries.toTypedArray())
    }

    suspend fun save(slot: String) {
        val collected = collect()
        val serialized = js("JSON.stringify")(collected)
        val bytes = awaitJs(s.gzip(serialized))
        val encoded = bytes.toBase64() as String
        val key = slotKey(slot)
        val otherKey = slotKey(if (slot == "manual") "auto" else "manual")
        val otherValues = awaitJs(sync.get(otherKey))
        val other = otherValues[otherKey]
        val otherSize = if (isMeta(other)) other.size else 0
        if (encoded.length + (otherSize as Int) > totalSize) {
            fun kb(chars: Int) = ceil(chars / 1024.0).toInt()
            error("백업이 클라우드 한도를 넘습니다. (이번 ${kb(encoded.length)}KB + 다른 백업 ${kb(otherSize)}KB / ${totalSize / 1024}KB)")
        }
        val chunks = encoded.chunked(chunkSize)
        val hash = awaitJs(s.sha256(bytes))
        val device = awaitJs(s.device().getValue())
        val meta = props("format" to BACKUP_FORMAT, "chunks" to chunks.size, "hash" to hash,
            "size" to encoded.length, "createdAt" to Date.now(), "device" to device)
        val items = props(key to meta)
        for ((index, chunk) in chunks.withIndex()) items["$key:$index"] = chunk
        val all = awaitJs(sync.getKeys()) as Array<String>
        val stale = all.filter { items[it] === undefinedValue() && (it == key || it.startsWith("$key:")) }.toTypedArray()
        awaitJs(sync.set(items))
        val updated = awaitJs(sync.get(key))
        val current = updated[key]
        if (stale.isNotEmpty() && isMeta(current) && current.createdAt === meta.createdAt && current.hash === meta.hash) {
            try {
                awaitJs(sync.remove(stale))
            } catch (error: Throwable) {
                console.warn("오래된 백업 조각을 지우지 못했습니다:", error)
            }
        }
    }

    fun status(): Promise<dynamic> = jsPromise {
        val metas = awaitJs(sync.get(arrayOf(slotKey("manual"), slotKey("auto"))))
        val device = awaitJs(s.device().getValue())
        fun statusOf(slot: String): dynamic {
            val meta = metas[slotKey(slot)]
            return if (isMeta(meta)) props("createdAt" to meta.createdAt, "size" to meta.size, "other" to (meta.device !== device))
            else undefinedValue()
        }
        props("manual" to statusOf("manual"), "auto" to statusOf("auto"))
    }

    fun read(slot: String): Promise<dynamic> = jsPromise {
        val key = slotKey(slot)
        val metas = awaitJs(sync.get(key))
        val meta = metas[key]
        if (!isMeta(meta)) return@jsPromise null
        if ((meta.format as Int) > BACKUP_FORMAT) error("새 버전 확장에서 만든 백업입니다. 확장을 업데이트해 주세요.")
        val itemKeys = Array((meta.chunks as Int).coerceAtLeast(0)) { "$key:$it" }
        val stored = awaitJs(sync.get(itemKeys))
        val chunks = itemKeys.map { stored[it] }
        if (chunks.any { jsTypeOf(it) != "string" }) {
            error("백업 조각이 빠져 있습니다. 다른 기기에서 동기화가 아직 끝나지 않았을 수 있습니다.")
        }
        val encoded = chunks.joinToString("") { it as String }
        val decode: dynamic = js("(encoded) => { try { return Uint8Array.fromBase64(encoded); } catch (_) { return null; } }")
        val bytes = decode(encoded)
        val hash = if (bytes == null) null else awaitJs(s.sha256(bytes))
        if (bytes == null || hash !== meta.hash) {
            error("백업 데이터가 맞지 않습니다. 다른 기기에서 동기화가 아직 끝나지 않았을 수 있습니다.")
        }
        val text = awaitJs(s.gunzip(bytes))
        val data = js("JSON.parse")(text)
        if (!record(data)) error("백업 데이터가 손상되었습니다.")
        props("data" to data, "createdAt" to meta.createdAt)
    }

    fun run(slot: String): Promise<dynamic> = jsPromise {
        try {
            save(slot)
            awaitJs(s.backupError().setValue(""))
        } catch (error: Throwable) {
            console.error("백업하지 못했습니다.", error)
            awaitJs(s.backupError().setValue(s.friendlyMessage(error)))
            throw error
        }
        undefinedValue()
    }

    return props("quota" to quota, "isBackupTarget" to ::isTarget, "readBackupTargets" to { jsPromise { readTargets() } },
        "collectLocalData" to { jsPromise { collect() } }, "readCloudBackupStatus" to ::status,
        "clearCloudBackups" to { sync.clear() }, "readCloudBackup" to ::read, "runBackup" to ::run)
}
