package dev.dcrk.block

import dev.dcrk.*

import kotlin.js.Promise

internal fun createBlockRequest(s: dynamic): (dynamic, dynamic) -> Promise<dynamic> {
    val blockStore = s.blockStore
    val uiStore = s.uiStore
    val hex = Regex("^[0-9a-f]+$")
    fun escape(value: String): String = if (hex.matches(value)) value else js("RegExp.escape(value)") as String
    fun bundled(paths: List<String>): String {
        val unique = paths.distinct()
        val first = unique.first()
        if (unique.size == 1) return "^(${escape(first)})$"
        val common = unique.fold(first) { prefix, path -> prefix.commonPrefixWith(path) }
        return "^(?:${escape(common)}(?:${unique.joinToString("|") { escape(it.drop(common.length)) }}))$"
    }
    fun mode(type: String): dynamic {
        val defaults = blockStore.getState().defaults
        return if ((defaults[type] as String).startsWith("NOT_")) "SAME" else undefinedValue()
    }
    suspend fun blockUser(selected: dynamic, gallery: dynamic) {
        val type = when {
            truthy(selected.uid) -> "ID"
            truthy(selected.ip) -> "IP"
            truthy(selected.nick) -> "NICK"
            else -> return
        }
        val value = when (type) {
            "ID" -> selected.uid
            "IP" -> selected.ip
            else -> selected.nick
        }
        awaitJs(blockStore.getState().addEntry(type, props("content" to value, "isRegex" to false,
            "extra" to if (truthy(selected.nick)) selected.nick else value, "mode" to mode(type), "gallery" to gallery)))
        val off = if (s.runningModuleSettings("block") === undefinedValue()) " 콘텐츠 차단 모듈이 꺼져 있어 지금은 가리지 않습니다." else ""
        val scope = if (truthy(gallery)) ", 이 갤러리만" else ""
        uiStore.getState().showToast("차단 목록에 추가했습니다. (${s.typeNames[type]}: $value$scope)$off")
    }
    suspend fun blockDccon(selected: dynamic, packageMode: dynamic) {
        val code = selected.dccon
        if (!truthy(code)) return
        val response = awaitJs(s.getDcconPackage(code))
        val details = response.detail as Array<dynamic>
        val extra = "${response.info.title} [${response.info.package_idx}]"
        val mode = mode("DCCON")
        val store = blockStore.getState()
        val request = when (packageMode) {
            "bundle" -> store.addEntry("DCCON", props("content" to bundled(details.map { it.path as String }),
                "isRegex" to true, "extra" to "[묶음] $extra", "mode" to mode))
            "each" -> store.addEntries("DCCON", details.map {
                props("content" to it.path, "isRegex" to false, "extra" to extra, "mode" to mode)
            }.toTypedArray())
            else -> store.addEntry("DCCON", props("content" to code, "isRegex" to false, "extra" to extra, "mode" to mode))
        }
        awaitJs(request)
        uiStore.getState().showToast("디시콘을 차단했습니다. ($extra)")
    }
    return { options, selected -> jsPromise {
        val dccon = options.target == "dccon"
        try {
            if (dccon) blockDccon(selected, options.dcconPackage) else blockUser(selected, options.gallery)
        } catch (error: Throwable) {
            s.reportFailure(error, "Block request failed:", if (dccon) "디시콘을 차단하지 못했습니다." else "차단 목록에 저장하지 못했습니다.")
        }
        undefinedValue()
    } }
}
