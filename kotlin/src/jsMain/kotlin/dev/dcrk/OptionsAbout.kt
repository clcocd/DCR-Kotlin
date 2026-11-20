package dev.dcrk

private const val PROJECT_REPO = "https://github.com/green1052/DCRefresher-Reborn"

fun optionFormatTime(time: Double): String {
    if (time == 0.0) return "기록 없음"
    val date: dynamic = js("new Date(time)")
    return date.toLocaleString("ko-KR") as String
}

fun optionFormatBytes(bytes: Double): String {
    val size = when {
        bytes < 1024 -> js("String")(bytes) as String
        bytes < 1024 * 1024 -> (bytes / 1024).asDynamic().toFixed(1) as String
        else -> (bytes / 1024 / 1024).asDynamic().toFixed(2) as String
    }
    return size + when {
        bytes < 1024 -> "B"
        bytes < 1024 * 1024 -> "KB"
        else -> "MB"
    }
}

fun optionDownloadJson(name: String, data: dynamic) {
    val now: dynamic = js("new Date()")
    val year = now.getFullYear() as Int
    val month = ((now.getMonth() as Int) + 1).toString().padStart(2, '0')
    val day = (now.getDate() as Int).toString().padStart(2, '0')
    val text = js("JSON.stringify")(data)
    val blob = js("new Blob([text], {type: 'application/json'})")
    val urlApi: dynamic = js("URL")
    val url = urlApi.createObjectURL(blob)
    val doc: dynamic = js("document")
    val link = doc.createElement("a")
    link.href = url
    link.download = "$name-$year$month$day.json"
    link.click()
    val timeout: dynamic = js("setTimeout")
    timeout({ urlApi.revokeObjectURL(url) }, 60_000)
}

private class AboutLink(val label: String, val url: String, val icon: dynamic)

fun createAboutTab(services: dynamic): (dynamic) -> dynamic {
    val react = services.react
    val ui = ReactUi(react)
    val promise: dynamic = js("Promise")
    val quota = services.quota as Double
    val storage = services.browser.storage
    val storeUrl = if (services.firefox == true) "https://addons.mozilla.org/ko/firefox/addon/dcrefresher-reborn"
    else "https://chromewebstore.google.com/detail/pmfifcbendahnkeojgpfppklgioemgon"
    val links = listOf(
        AboutLink("위키", "$PROJECT_REPO/wiki", services.BookOpen),
        AboutLink("버그 제보 / 문의", "$PROJECT_REPO/issues", services.Bug),
        AboutLink("업데이트 내역", "$PROJECT_REPO/releases", services.Tag),
        AboutLink("GitHub", PROJECT_REPO, services.Code),
        AboutLink("리프레셔 갤러리", "https://gall.dcinside.com/mini/board/lists/?id=bjwg64", services.Users),
        AboutLink("Discord", "https://discord.gg/SSW6Zuyjz6", services.MessageCircle),
        AboutLink("리뷰 남기기", storeUrl, services.Star),
        AboutLink("후원", "https://www.buymeacoffee.com/green1052", services.Heart),
    )
    return { page ->
        val enables = services.useModulesStore { state: dynamic -> state.enables }
        val blocks = services.useBlocksStore { state: dynamic -> state.entries }
        val memos = services.useMemosStore { state: dynamic -> state.memos }
        val usageState = react.useState(null)
        val usage = usageState[0]
        val setUsage = usageState[1]

        react.useEffect({
            promise.all(arrayOf<dynamic>(storage.local.getBytesInUse(null), storage.sync.getBytesInUse(null))).then(
                { result: dynamic -> setUsage(props("local" to result[0], "sync" to result[1])) },
                { _: dynamic -> setUsage("error") },
            )
            undefinedValue()
        }, emptyArray<dynamic>())

        val blockCount = jsValues(blocks).sumOf { (it.length as Int) }
        val memoCount = jsValues(memos).sumOf { jsValues(it).size }
        val copy: () -> Unit = {
            services.dbMetaStorage().getValue().catch { _: dynamic -> null }.then { db: dynamic ->
                val enabled = (services.features as Array<dynamic>).filter { truthy(enables[it.id]) }
                    .joinToString(", ") { it.name as String }.ifEmpty { "없음" }
                val agent: String = js("navigator.userAgent") as String
                val dbVersion = if (truthy(db?.version)) db.version else "없음"
                val lines = listOf(
                    "DCRefresher Reborn v${page.version}", "브라우저: $agent", "켜진 모듈: $enabled",
                    "IP DB: $dbVersion (마지막 확인 ${optionFormatTime(db?.lastUpdate as Double? ?: 0.0)})",
                    "차단 ${blockCount}개, 메모 ${memoCount}개",
                )
                services.copyText(lines.joinToString("\n"), "진단 정보를 복사했습니다.", "복사하지 못했습니다.")
            }
        }
        val localText = when (usage) {
            "error" -> "알 수 없음"
            null -> "..."
            else -> optionFormatBytes(usage.local as Double)
        }
        val syncText = when (usage) {
            "error" -> "알 수 없음"
            null -> "..."
            else -> {
                val bytes = usage.sync as Double
                val percent = js("Math.round")(bytes / quota * 100) as Int
                "${optionFormatBytes(bytes)} / ${optionFormatBytes(quota)} ($percent%)"
            }
        }

        ui.h("div", null,
            ui.h(services.Section, null,
                ui.div("flex flex-wrap items-center gap-4",
                    ui.h("img", props("src" to page.logo, "alt" to "", "width" to 64, "height" to 64, "className" to "rounded-xl")),
                    ui.div("grow",
                        ui.div("flex items-center gap-2", ui.tag("h2", "text-xl font-bold", "DCRefresher Reborn"),
                            ui.h(services.Badge, props("variant" to "secondary"), "v", page.version)),
                        ui.tag("p", "text-muted-foreground", "디시인사이드 개선 확장 프로그램")),
                    ui.h(services.Button, props("variant" to "secondary", "onClick" to copy),
                        ui.h(services.ClipboardCopy, props("data-icon" to "inline-start")), " 진단 정보 복사"))),
            ui.h(services.Section, props("title" to "바로가기"),
                ui.div("grid grid-cols-1 gap-2 sm:grid-cols-2", links.map { link ->
                    ui.h(services.Button, props("key" to link.label, "variant" to "secondary", "className" to "justify-start",
                        "nativeButton" to false, "render" to ui.h("a", props("href" to link.url, "target" to "_blank", "rel" to "noreferrer"))),
                        ui.h(link.icon, props("data-icon" to "inline-start")), " ", link.label)
                }.toTypedArray())),
            ui.h(services.Section, props("title" to "데이터 현황"),
                ui.tag("dl", "grid grid-cols-[auto_1fr] gap-x-6 gap-y-3",
                    ui.tag("dt", "text-muted-foreground", "차단, 메모"), ui.h("dd", null, "차단 ${blockCount}개, 메모 ${memoCount}개"),
                    ui.tag("dt", "text-muted-foreground", "로컬 저장소"), ui.h("dd", null, localText),
                    ui.tag("dt", "text-muted-foreground", "클라우드 저장소"), ui.h("dd", null, syncText))))
    }
}
