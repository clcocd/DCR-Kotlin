package dev.dcrk

import kotlin.math.min

private const val LIST_PAGE_SIZE = 200

fun restoreOptionList(current: Array<dynamic>, removed: Array<dynamic>, key: (dynamic) -> String): Array<dynamic> {
    val kept = current.map(key).toSet()
    val result = current.toMutableList()
    for (entry in removed) {
        if (key(entry.item) in kept) continue
        val index = entry.index as Int
        val position = if (index < 0) (result.size + index).coerceAtLeast(0) else index.coerceAtMost(result.size)
        result.add(position, entry.item)
    }
    return result.toTypedArray()
}

fun createOptionListRow(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    return { row ->
        val state = react.useState(null)
        val opener = state[0]
        val open: (dynamic) -> Unit = { event -> event.stopPropagation(); state[1](event.currentTarget) }
        val close: () -> Unit = { state[1](null) }
        val remove: () -> Unit = { s.focusPanel(opener); row.onRemove(); close() }
        ui.h(react.Fragment, null,
            ui.h(s.TableRow, props("className" to "cursor-pointer [content-visibility:auto] [contain-intrinsic-size:auto_45px]", "onClick" to row.onEdit),
                ui.h(s.TableHead, props("scope" to "row", "className" to "font-normal"),
                    ui.h("button", props("type" to "button", "title" to "수정", "className" to s.cn(s.focusRing, "block w-full text-left")), row.head)),
                ui.h(s.TableCell, props("className" to "whitespace-normal"), row.info),
                ui.h(s.TableCell, props("className" to "text-right"),
                    ui.h("button", props("type" to "button", "aria-label" to "${row.label} 삭제", "title" to "삭제",
                        "className" to s.cn(s.focusRing, "grid size-8 place-items-center rounded-[min(var(--radius-md),10px)] text-destructive hover:bg-accent"), "onClick" to open),
                        ui.h(s.Trash2)))),
            if (opener == null) null else ui.h(s.ConfirmDialog, props("title" to "\"${row.label}\" 삭제할까요?", "confirmLabel" to "삭제",
                "danger" to true, "onConfirm" to remove, "onClose" to close)))
    }
}

private class OptionListSnapshot(val total: Int, val shown: List<dynamic>)

fun createOptionListTabs(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    return { p ->
        val types = p.types as Array<String>
        val label = p.label as String
        val targetLabel = label + if (((label.lastOrNull()?.code ?: 0) - 0xac00) % 28 > 0) "을" else "를"
        val search = react.useState("")
        val query = search[0] as String
        val needle = query.trim().lowercase()
        val filter = react.useState("all")
        val limits = react.useState(props())
        val confirm = react.useState(null)
        val importDialog = react.useState(false)
        val filterRow = react.useRef(null)
        val keys: (dynamic) -> String = p.itemKey
        fun items(type: String): Array<dynamic> = p.items(type) as Array<dynamic>
        fun reset() { limits[1](props()) }

        val galleries = react.useMemo({
            types.flatMap { type -> items(type).mapNotNull { p.galleryOf(it) as String? } }
                .filter { it.isNotEmpty() }.distinct().sorted()
        }, arrayOf<dynamic>(types, p.items, p.galleryOf)) as List<String>
        val galleryOptions = props("all" to "모든 항목", "common" to "갤러리 공통")
        for (id in galleries) galleryOptions["g:$id"] = id
        val selectedGallery = filter[0] as String
        val galleryFilter = if (galleryOptions[selectedGallery] != null) selectedGallery else "all"
        val filtering = needle.isNotEmpty() || galleryFilter != "all"
        fun visible(item: dynamic): Boolean {
            if (needle.isNotEmpty() && (p.searchText(item) as Array<dynamic>).none {
                    (it as String?)?.lowercase()?.contains(needle) == true
                }) return false
            val wanted = if (galleryFilter == "common") "" else galleryFilter.drop(2)
            return galleryFilter == "all" || (p.galleryOf(item) ?: "") == wanted
        }
        val shown = react.useMemo({
            types.associateWith { type ->
                val all = items(type)
                OptionListSnapshot(all.size, all.filter(::visible).asReversed())
            }
        }, arrayOf<dynamic>(types, p.items, needle, galleryFilter, p.searchText, p.galleryOf)) as Map<String, OptionListSnapshot>

        fun removeItems(type: String, targets: Array<dynamic>, run: () -> dynamic) {
            val picked = targets.map(keys).toSet()
            val removed = items(type).mapIndexedNotNull { index, item ->
                if (keys(item) in picked) props("index" to index, "item" to item) else null
            }.toTypedArray()
            run().then(
                { _: dynamic ->
                    val undo: () -> Unit = {
                        p.onRestore(type, { current: Array<dynamic> -> restoreOptionList(current, removed, keys) })
                            .catch { _: dynamic -> s.notify(s.saveFailed) }
                    }
                    s.notifyDone(props("message" to "${p.names[type]} $targetLabel ${removed.size}개 삭제했습니다.", "undo" to undo))
                },
                { _: dynamic -> s.notify("$targetLabel 삭제하지 못했습니다.") },
            )
        }

        val submitImport: (String) -> dynamic = { text -> jsPromise import@{
            val data = try { js("JSON.parse")(text) } catch (error: Throwable) {
                s.notify("$targetLabel 가져오지 못했습니다. ${s.friendlyMessage(error)}")
                return@import undefinedValue()
            }
            try {
                val record = s.isRecord(data) as Boolean
                if (record && (awaitJs(p.importData(data)) as Int) > 0) return@import "$targetLabel 가져왔습니다."
                val all = record && (js("Object.keys")(data) as Array<String>).any { it.startsWith("refresher:") }
                s.notify(if (all) "전체 데이터는 데이터 탭에서 가져와 주세요."
                    else "${label} 데이터가 아닙니다. 이 탭에서 내보낸 JSON인지 확인해 주세요.")
            } catch (_: Throwable) { s.notify(s.saveFailed) }
            undefinedValue()
        } }

        fun tool(title: String, icon: dynamic, variant: String?, disabled: Boolean = false, run: () -> Unit): dynamic =
            ui.h(s.TooltipButton, props("tip" to title, "variant" to (variant ?: undefinedValue()), "size" to "icon",
                "disabled" to disabled, "onClick" to run), ui.h(icon))

        val nav = types.map { type ->
            val snapshot = shown.getValue(type)
            ui.h(s.TabsTrigger, props("key" to type, "value" to type, "className" to "flex-none"), p.names[type],
                if (snapshot.total == 0) null else ui.h(s.Badge, props("variant" to if (filtering) "default" else "secondary"),
                    if (filtering) "${snapshot.shown.size}/${snapshot.total}" else snapshot.total))
        }.toTypedArray()

        val panels = types.map { type ->
            val snapshot = shown.getValue(type)
            val list = snapshot.shown
            val limit = limits[0][type] as Int? ?: LIST_PAGE_SIZE
            val body = when {
                snapshot.total == 0 -> ui.h(s.EmptyList, null, p.emptyText(type))
                list.isEmpty() -> ui.h(s.EmptyList, null, if (needle.isNotEmpty()) "\"${query.trim()}\" 검색 결과 없음" else "조건에 맞는 항목 없음")
                else -> {
                    val rows = list.take(limit).map { item ->
                        p.row(type, item, { removeItems(type, arrayOf<dynamic>(item), { p.onRemoveMany(type, arrayOf<dynamic>(item)) }) })
                    }.toTypedArray()
                    val more: (dynamic) -> Unit = { event ->
                        if (list.size <= limit + LIST_PAGE_SIZE) s.focusPanel(event.currentTarget)
                        limits[1](js("Object.assign")(props(), limits[0], props(type to limit + LIST_PAGE_SIZE)))
                    }
                    ui.h(react.Fragment, null,
                        ui.div("overflow-hidden rounded-lg border", ui.h(s.Table, null,
                            ui.h(s.TableHeader, props("className" to "bg-muted/50"),
                                ui.h(s.TableRow, null, ui.h(s.TableHead, null, p.columns[0]), ui.h(s.TableHead, null, p.columns[1]),
                                    ui.h(s.TableHead, props("className" to "w-12")))),
                            ui.h(s.TableBody, null, rows))),
                        if (list.size <= limit) null else ui.div("flex justify-center pt-4",
                            ui.h(s.Button, props("variant" to "outline", "onClick" to more), "더 보기 (+${min(LIST_PAGE_SIZE, list.size - limit)})")))
                }
            }
            val galleryChange: (String) -> Unit = { next -> filter[1](next); reset() }
            val searchChange: (dynamic) -> Unit = { event -> search[1](event.target.value); reset() }
            ui.h(s.TabsContent, props("key" to type, "value" to type, "className" to "tab-enter"),
                ui.h("div", props("ref" to filterRow, "className" to "flex flex-wrap items-center justify-between gap-3 py-4"),
                    p.toolbar(type),
                    ui.div("ml-auto flex flex-wrap items-center gap-2",
                        if (galleries.isEmpty()) null else ui.h(s.RefresherSelect, props("value" to galleryFilter, "options" to galleryOptions,
                            "aria-label" to "갤러리", "onChange" to galleryChange)),
                        ui.h(s.SearchInput, props("className" to "w-[180px]", "placeholder" to "검색", "aria-label" to "$label 검색",
                            "value" to query, "onChange" to searchChange)),
                        tool("전체 삭제", s.Trash2, "destructive", snapshot.total == 0) { confirm[1](type) },
                        tool("추가", s.Plus, null) { search[1](""); filter[1]("all"); reset(); p.onAdd(type) })),
                body)
        }.toTypedArray()
        val confirmed = confirm[0] as String?
        ui.h(s.Card, null,
            ui.h(s.CardContent, null, ui.h(s.Tabs, props("defaultValue" to types[0]),
                ui.div("flex items-end gap-3", ui.h(s.TabsList, props("variant" to "line", "className" to "h-auto flex-1 flex-wrap justify-start"), nav),
                    ui.div("flex gap-1 pb-1",
                        tool("클립보드로 내보내기", s.ClipboardCopy, "ghost") {
                            s.copyText(js("JSON.stringify")(p.exportData()), "$targetLabel 클립보드로 내보냈습니다.", "$targetLabel 내보내지 못했습니다.")
                        },
                        tool("파일로 내보내기", s.FileDown, "ghost") {
                            optionDownloadJson(p.fileName as String, p.exportData())
                            s.notifyDone("$targetLabel 파일로 내보냈습니다.")
                        },
                        tool("가져오기", s.Upload, "ghost") { importDialog[1](true) })),
                panels)),
            if (confirmed == null) null else ui.h(s.ConfirmDialog, props(
                "title" to "${p.names[confirmed]} $targetLabel 모두(${items(confirmed).size}개) 삭제할까요?", "confirmLabel" to "삭제", "danger" to true,
                "onConfirm" to {
                    s.focusPanel(filterRow.current)
                    removeItems(confirmed, items(confirmed), { p.onClear(confirmed) })
                    confirm[1](null)
                }, "onClose" to { confirm[1](null) })),
            if (importDialog[0] == true) ui.h(s.ImportDialog, props("title" to "$label 가져오기",
                "onClose" to { importDialog[1](false) }, "onSubmit" to submitImport)) else null)
    }
}
