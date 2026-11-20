package dev.dcrk

import kotlin.js.Promise

internal fun useOptionDataActions(services: dynamic, restoreRef: dynamic, setRestoreOpen: (Boolean) -> Unit): dynamic {
    val data = services.react.useState(false)
    val setLoading = data[1]
    val doc: dynamic = js("document")
    val console: dynamic = js("console")
    val clipboard: dynamic = js("navigator.clipboard")
    val backup = services.backupStorage
    val emptyBackupText = " 클라우드 백업을 지키려고 자동 백업을 껐습니다."

    fun run(failure: String, action: suspend () -> dynamic): Promise<dynamic> = jsPromise {
        if (doc.activeElement !== doc.body) services.focusPanel(doc.activeElement)
        setLoading(true)
        var done: dynamic = null
        var failed = ""
        try {
            done = action()
        } catch (error: Throwable) {
            console.error(error)
            failed = "$failure ${services.friendlyMessage(error)}"
        }
        if (doc.activeElement === doc.body) services.focusPanel(restoreRef.current)
        if (truthy(done)) services.notifyDone(done) else services.notify(failed)
        setLoading(false)
        undefinedValue()
    }

    suspend fun pauseAutoIfEmpty(settings: dynamic): Boolean {
        val keys = js("Object.keys")(settings) as Array<String>
        if (keys.isNotEmpty() || !truthy(awaitJs(backup.auto().getValue()))) return false
        awaitJs(backup.auto().setValue(false))
        return true
    }

    fun revert(at: Double? = null, auto: Boolean = false): Promise<dynamic> = run("되돌리지 못했습니다.") {
        val before = awaitJs(backup.undo().getValue())
        when {
            before == null -> "되돌릴 데이터가 없습니다."
            at != null && at != before.createdAt -> "그 뒤에 다른 작업이 있어 이 알림으로는 되돌릴 수 없습니다."
            else -> {
                val off = pauseAutoIfEmpty(before.data)
                awaitJs(services.writeSettings(before.data, "replace"))
                if (auto) awaitJs(backup.auto().setValue(true))
                val suffix = if (auto) " 자동 백업도 다시 켰습니다." else if (off) emptyBackupText else ""
                "${optionFormatTime(before.createdAt as Double)} 직전 상태로 되돌렸습니다.$suffix"
            }
        }
    }

    fun undoable(message: String, at: Double?, auto: Boolean = false): dynamic =
        if (at == null) message else props("message" to message, "undo" to { revert(at, auto) })

    fun recover(slot: String, mode: String): Promise<dynamic> = run("복원하지 못했습니다.") restore@{
        setRestoreOpen(false)
        val snapshot = awaitJs(services.readCloudBackup(slot)) ?: return@restore "클라우드에 백업이 없습니다."
        val merged = mode == "merge"
        val settings = if (merged) {
            val local = awaitJs(services.readBackupTargets())
            services.mergeBackup(local, snapshot.data)
        } else snapshot.data
        val at = awaitJs(services.writeSettings(settings, mode)) as Double?
        val label = if (merged) "합쳤습니다" else "복원했습니다"
        undoable("${optionFormatTime(snapshot.createdAt as Double)} 백업을 $label.", at)
    }

    fun toggleAuto(on: Boolean): Promise<dynamic> = jsPromise toggle@{
        try {
            awaitJs(backup.auto().setValue(on))
        } catch (error: Throwable) {
            console.error(error)
            services.notify("자동 백업 설정을 저장하지 못했습니다. ${services.friendlyMessage(error)}")
            return@toggle undefinedValue()
        }
        if (on) awaitJs(run("자동 백업을 켰지만 첫 백업을 올리지 못했습니다.") {
            awaitJs(services.runBackup("auto"))
            "자동 백업을 켰습니다. 지금 설정을 자동 백업으로 올렸습니다."
        })
        undefinedValue()
    }

    fun importSettings(text: String): Promise<dynamic> = jsPromise {
        try {
            val at = awaitJs(services.writeSettings(services.parseImport(text), "merge")) as Double?
            undoable("데이터를 가져왔습니다.", at)
        } catch (error: Throwable) {
            console.error(error)
            services.notify("가져오지 못했습니다. ${services.friendlyMessage(error)}")
            undefinedValue()
        }
    }

    fun clearData(): Promise<dynamic> = run("초기화하지 못했습니다.") {
        val wasAuto = pauseAutoIfEmpty(props())
        val at = awaitJs(services.writeSettings(props(), "replace")) as Double?
        undoable("데이터를 초기화했습니다." + if (wasAuto) emptyBackupText else "", at, wasAuto)
    }

    return props(
        "loading" to data[0],
        "forceUpdate" to { run("데이터베이스를 갱신하지 못했습니다.") {
            awaitJs(services.sendMessage("refresher:updateDatabase"))
            "데이터베이스를 갱신했습니다."
        } },
        "backupCloud" to { run("클라우드에 백업하지 못했습니다.") {
            awaitJs(services.runBackup("manual"))
            "데이터를 클라우드에 백업했습니다."
        } },
        "recoverCloud" to ::recover,
        "toggleAutoBackup" to ::toggleAuto,
        "exportData" to { run("클립보드로 내보내지 못했습니다.") {
            val settings = awaitJs(services.collectLocalData())
            awaitJs(clipboard.writeText(js("JSON.stringify")(settings)))
            "데이터를 클립보드로 내보냈습니다."
        } },
        "exportFile" to { run("파일로 내보내지 못했습니다.") {
            optionDownloadJson("dcrefresher", awaitJs(services.collectLocalData()))
            "데이터를 파일로 내보냈습니다."
        } },
        "submitImport" to ::importSettings,
        "clearCloud" to { run("클라우드 백업을 지우지 못했습니다.") {
            val wasAuto = truthy(awaitJs(backup.auto().getValue()))
            if (wasAuto) awaitJs(backup.auto().setValue(false))
            awaitJs(services.clearCloudBackups())
            "클라우드 백업을 지웠습니다." + if (wasAuto) " 자동 백업도 껐습니다." else ""
        } },
        "clearData" to ::clearData,
        "revert" to { revert() },
    )
}
