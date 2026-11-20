package dev.dcrk

private val restoreDescriptions = mapOf(
    "replace" to "현재 설정과 차단/메모 목록을 고른 백업으로 통째로 교체합니다. 백업에 없는 항목은 지워집니다.",
    "merge" to "현재 설정과 목록은 그대로 두고, 백업에만 있는 차단, 메모와 설정을 더합니다. 겹치면 현재 것을 남깁니다.",
)

private class DataConfirmation(val title: String, val label: String, val confirm: () -> Unit)

fun createDataTab(services: dynamic): () -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val console: dynamic = js("console")
    val storage = services.browser.storage
    val iconProps = props("data-icon" to "inline-start")
    return {
        val backup = services.backupStorage
        val version = (services.useStorageItem(services.dbMetaStorage()).version as String).trim().ifEmpty { "없음" }
        val backupError = services.useStorageItem(backup.error())
        val autoBackup = services.useStorageItem(backup.auto()) as Boolean
        val undo = services.useStorageItem(backup.undo())
        val cloudState = react.useState(null)
        val cloud = cloudState[0]
        val setCloud = cloudState[1]
        val restore = react.useState(false)
        val setRestoreOpen: (Boolean) -> Unit = restore[1]
        val modeState = react.useState("replace")
        val mode = modeState[0] as String
        val setMode = modeState[1]
        val confirmation = react.useState(null)
        val confirm = confirmation[0] as String?
        val setConfirm = confirmation[1]
        val importDialog = react.useState(false)
        val setImport = importDialog[1]
        val restoreRef = react.useRef(null)
        val id = react.useId() as String
        val actions = useOptionDataActions(services, restoreRef, setRestoreOpen)
        val loading = actions.loading as Boolean

        react.useEffect({
            var serial = 0
            var active = true
            val load: () -> Unit = {
                val request = ++serial
                services.readCloudBackupStatus().then(
                    { value: dynamic -> if (active && request == serial) setCloud(value) },
                    { error: dynamic ->
                        console.error(error)
                        if (active) setCloud { previous: dynamic -> previous ?: props() }
                    },
                )
            }
            load()
            storage.sync.onChanged.addListener(load)
            val dispose: () -> Unit = { active = false; storage.sync.onChanged.removeListener(load) }
            dispose
        }, emptyArray<dynamic>())

        val manual = cloud?.manual
        val auto = cloud?.auto
        fun time(slot: dynamic): String = optionFormatTime(slot?.createdAt as Double? ?: 0.0)
        val requested = when (confirm) {
            "reset" -> DataConfirmation("모든 설정과 차단/메모 데이터를 초기화할까요?", "초기화") { actions.clearData() }
            "manual" -> DataConfirmation("${time(manual)}에 다른 기기에서 올린 수동 백업을 이 기기의 설정으로 덮어씁니다. 백업할까요?", "백업") { actions.backupCloud() }
            "cloudClear" -> {
                val suffix = if (autoBackup) " 자동 백업도 꺼집니다." else ""
                DataConfirmation("클라우드에 올라간 수동, 자동 백업을 모두 지울까요?$suffix", "삭제") { actions.clearCloud() }
            }
            "auto" -> {
                val owner = if (auto?.other == true) "다른 기기에서 올린 " else ""
                DataConfirmation("자동 백업을 켜면 $owner${time(auto)} 자동 백업을 이 기기의 설정으로 덮어씁니다. 켤까요?", "켜기") { actions.toggleAutoBackup(true) }
            }
            else -> null
        }

        fun button(icon: dynamic, label: String, disabled: Boolean = loading, variant: String = "secondary", run: () -> Unit): dynamic =
            ui.h(services.Button, props("variant" to variant, "disabled" to disabled, "onClick" to run), ui.h(icon, iconProps), " ", label)

        fun restoreButton(slot: String, label: String, snapshot: dynamic): dynamic =
            ui.h(services.Button, props("key" to slot, "variant" to "secondary", "size" to "lg", "className" to "justify-between",
                "disabled" to (snapshot == null), "onClick" to { actions.recoverCloud(slot, mode) }),
                label, ui.span("text-muted-foreground", if (snapshot == null) "없음" else time(snapshot)))

        val toggleAuto: (Boolean) -> Unit = { on ->
            if (on && auto != null) setConfirm("auto") else actions.toggleAutoBackup(on)
        }
        val autoToggle = ui.h(services.Field, props("orientation" to "horizontal"),
            ui.h(services.Switch, props("id" to "$id-auto", "checked" to autoBackup, "disabled" to (loading || cloud == null),
                "onCheckedChange" to toggleAuto)),
            ui.h(services.FieldLabel, props("htmlFor" to "$id-auto"), "자동 백업"))

        val chooseMode: (Array<String>) -> Unit = { modes ->
            val next = modes.firstOrNull()
            if (next in restoreDescriptions) setMode(next)
        }
        val finalFocus: () -> dynamic = {
            val element = restoreRef.current
            if (element?.disabled == true) services.panelOf(element) ?: true else true
        }
        val restoreDialog = ui.h(services.Dialog, props("open" to restore[0], "onOpenChange" to setRestoreOpen),
            ui.div("flex flex-wrap gap-2",
                button(services.CloudUpload, "백업", loading || cloud == null) {
                    if (manual?.other == true) setConfirm("manual") else actions.backupCloud()
                },
                ui.h(services.DialogTrigger, props("render" to ui.h(services.Button,
                    props("ref" to restoreRef, "variant" to "secondary", "disabled" to loading))),
                    ui.h(services.CloudDownload, iconProps), " 복원"),
                button(services.Trash2, "삭제", loading || (manual == null && auto == null)) { setConfirm("cloudClear") }),
            ui.h(services.DialogContent, props("showCloseButton" to false, "className" to "sm:max-w-[420px]", "finalFocus" to finalFocus),
                ui.h(services.DialogHeader, null, ui.h(services.DialogTitle, null, "어느 백업으로 복원할까요?")),
                ui.h(services.ToggleGroup, props("aria-label" to "복원 방식", "variant" to "outline", "spacing" to 0,
                    "value" to arrayOf(mode), "onValueChange" to chooseMode),
                    ui.h(services.ToggleGroupItem, props("value" to "replace"), "덮어쓰기"),
                    ui.h(services.ToggleGroupItem, props("value" to "merge"), "합치기")),
                ui.h(services.DialogDescription, null, restoreDescriptions[mode]),
                ui.div("flex flex-col gap-2", restoreButton("manual", "수동 백업", manual), restoreButton("auto", "자동 백업", auto)),
                ui.h(services.DialogActions)))

        val reset = button(services.Trash2, "데이터 초기화", variant = "destructive") { setConfirm("reset") }
        ui.h("div", null,
            ui.h(services.Section, props("title" to "IP/밴 데이터베이스", "desc" to "버전: $version",
                "actions" to button(services.RefreshCw, "갱신") { actions.forceUpdate() })),
            ui.h(services.Section, props("title" to "클라우드 백업", "desc" to "브라우저 동기화 저장소에 설정을 백업합니다.", "actions" to autoToggle),
                ui.tag("p", "mb-3 text-muted-foreground", "수동 백업: ", time(manual), ui.h("br"), "자동 백업: ", time(auto)),
                restoreDialog,
                if (autoBackup) ui.tag("p", "mt-2 text-xs text-muted-foreground", "설정이 바뀌면 1분 뒤에 자동으로 백업합니다.") else null,
                if (truthy(backupError)) ui.tag("p", "mt-2 text-xs text-destructive", "마지막 백업 실패: ", backupError) else null),
            ui.h(services.Section, props("title" to "내보내기 / 가져오기", "desc" to "설정을 내보냅니다."),
                ui.div("flex flex-wrap gap-2",
                    button(services.ClipboardCopy, "클립보드로 내보내기") { actions.exportData() },
                    button(services.FileDown, "파일로 내보내기") { actions.exportFile() },
                    button(services.Upload, "가져오기") { setImport(true) })),
            if (undo != null) ui.h(services.Section, props("title" to "되돌리기",
                "desc" to "마지막 초기화, 복원, 가져오기, 되돌리기(${time(undo)}) 직전의 설정과 차단/메모 목록으로 돌아갑니다. 그 뒤에 바꾼 것은 사라집니다.",
                "actions" to button(services.Undo2, "되돌리기") { actions.revert() })) else null,
            ui.h(services.Section, props("title" to "초기화", "desc" to "모든 설정을 삭제합니다.", "actions" to reset)),
            if (requested == null) null else ui.h(services.ConfirmDialog, props(
                "title" to requested.title, "confirmLabel" to requested.label, "danger" to true,
                "onClose" to { setConfirm(null) },
                "onConfirm" to { setConfirm(null); requested.confirm() })),
            if (importDialog[0] == true) ui.h(services.ImportDialog, props("title" to "데이터 가져오기", "desc" to "차단, 메모는 통째로 덮어씁니다.",
                "onClose" to { setImport(false) }, "onSubmit" to actions.submitImport)) else null)
    }
}
