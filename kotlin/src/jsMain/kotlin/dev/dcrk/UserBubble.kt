package dev.dcrk

internal fun createUserBubble(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.uiStore
    val blocks = s.blockStore
    val assign: dynamic = js("Object.assign")
    val close: () -> Unit = { store.getState().closeBubble() }
    val readSearchApi: () -> dynamic = { s.getModuleApi("search") }

    fun menu(options: dynamic, vararg content: dynamic): dynamic {
        val fields = assign(props("variant" to "ghost", "size" to "sm"), options)
        fields.className = s.cn("w-full cursor-pointer justify-start font-normal",
            if (options?.danger == true) "text-destructive hover:bg-destructive/10 hover:text-destructive" else false, options?.className)
        js("Reflect").deleteProperty(fields, "danger")
        return ui.h(s.Button, fields, *content)
    }
    fun blockEntry(type: String, entry: dynamic) = jsPromise {
        val toast = store.getState().showToast
        val failed: () -> Unit = { toast(s.saveFailed, "error") }
        try {
            awaitJs(blocks.getState().removeEntry(type, entry.id))
        } catch (_: Throwable) {
            failed()
            return@jsPromise
        }
        val fields = assign(props(), entry)
        js("Reflect").deleteProperty(fields, "id")
        val suffix = if (entry.isRegex == true) " 같은 규칙에 걸린 다른 대상도 풀렸습니다." else ""
        val undo: () -> Unit = { blocks.getState().addEntry(type, fields).catch(failed) }
        toast("차단을 해제했습니다.$suffix", "info", 5000, props("label" to "되돌리기", "run" to undo))
    }
    fun ruleList(rules: Array<dynamic>, defaults: dynamic): dynamic {
        val hits = mutableListOf<dynamic>()
        val allows = mutableMapOf<String, Int>()
        for (rule in rules) {
            val type = rule.type as String
            val entry = rule.entry
            val name = s.blockLabel(entry)
            val typeName = s.typeNames[type]
            if (((entry.mode ?: defaults[type]) as String).startsWith("NOT_")) {
                allows[type] = (allows[type] ?: 0) + 1
                continue
            }
            val regex = entry.isRegex == true
            hits.add(ui.h("div", props("key" to entry.id, "className" to "flex items-center justify-between gap-2"),
                ui.h("span", props("className" to "truncate text-xs", "title" to if (regex)
                    "정규식 - 풀면 이 규칙에 걸린 다른 대상도 함께 풀립니다." else undefinedValue()),
                    ui.span("text-muted-foreground", typeName), " $name",
                    if (regex) ui.span("text-muted-foreground", " (정규식)") else null,
                    if (truthy(entry.gallery)) ui.span("text-muted-foreground", " (이 갤러리만)") else null),
                ui.h(s.Button, props("size" to "xs", "variant" to "ghost", "className" to "shrink-0 text-destructive",
                    "aria-label" to "$typeName $name 차단 해제", "onClick" to { blockEntry(type, entry) }), "해제")))
        }
        for ((type, count) in allows) hits.add(ui.h("span", props("key" to type, "className" to "text-xs"),
            ui.span("text-muted-foreground", s.typeNames[type]), " 허용 목록(${count}개)에 없음"))
        return ui.h(react.Fragment, null, ui.h(s.Separator), ui.tag("p", "text-xs text-muted-foreground", "걸린 차단 규칙"),
            ui.div("flex flex-col gap-1", hits.toTypedArray()))
    }
    val dcconSummary: (dynamic) -> dynamic = { p ->
        val code = p.code
        val initial: () -> dynamic = { s.dcconPackages.get(code) ?: null }
        val item = react.useState(initial)
        val failed = react.useState(false)
        val load: () -> dynamic = {
            if (item[0] != null) undefinedValue()
            else {
                val controller = js("new AbortController()")
                val done: (dynamic) -> Unit = {
                    s.dcconPackages.set(code, it)
                    if (controller.signal.aborted != true) item[1](it)
                }
                val error: () -> Unit = { if (controller.signal.aborted != true) failed[1](true) }
                s.fetchDcconPackage(code, controller.signal).then(done, error)
                val abort: () -> Unit = { controller.abort() }
                abort
            }
        }
        react.useEffect(load, arrayOf<dynamic>(code))
        val info = item[0]?.info
        val text = if (info != null) ui.h(react.Fragment, null,
            ui.h("img", props("src" to "${s.imageBase}${info.main_img_path}", "alt" to "", "className" to "size-10 flex-none rounded-sm object-contain")),
            ui.span("flex min-w-0 flex-col items-start gap-0.5", ui.h("strong", null, info.title),
                if (truthy(info.description)) ui.span("line-clamp-2 text-left text-xs text-muted-foreground", info.description) else null))
        else if (failed[0] == true) ui.h("span", null, "디시콘 정보")
        else ui.h(react.Fragment, null,
            ui.h(s.Skeleton, props("className" to "size-10 flex-none")),
            ui.span("flex flex-col gap-1", ui.h(s.Skeleton, props("className" to "h-4 w-24")),
                ui.h(s.Skeleton, props("className" to "h-3 w-40"))))
        menu(props("title" to "디시콘 정보 보기", "className" to "h-auto items-start gap-2 py-1.5 whitespace-normal", "onClick" to p.onOpen), text)
    }
    val packageDialog: (dynamic) -> dynamic = { p ->
        fun choose(mode: String) {
            p.onClose()
            s.handleBlockRequest(props("target" to "dccon", "dcconPackage" to mode), p.target)
        }
        ui.h(s.ModalDialog, props("onClose" to p.onClose, "className" to "sm:max-w-[400px]"),
            ui.h(s.DialogHeader, null, ui.h(s.DialogTitle, null, "디시콘 패키지를 어떻게 차단할까요?"),
                ui.h(s.DialogDescription, null, "묶어서 차단하면 차단 목록에 한 항목으로 들어갑니다. 하나씩 차단하면 디시콘마다 항목이 생겨 따로 풀 수 있습니다.")),
            ui.h(s.DialogActions, null, ui.h(s.Button, props("variant" to "secondary", "onClick" to { choose("each") }), "하나씩 차단"),
                ui.h(s.Button, props("onClick" to { choose("bundle") }), "묶어서 차단")))
    }

    val bubble: (dynamic) -> dynamic = { p ->
        val selected = p.selected
        val position = p.bubble
        val dccon = truthy(selected.dccon)
        val popup = react.useRef(null)
        val focus = s.useReturnFocus(popup)
        val gallery = s.selectedGallery(selected.gallery)
        val activity = s.useGallogActivity(if (dccon) undefinedValue() else selected.uid)
        val memo = s.useUserMemo(selected, gallery)
        val entries = blocks { state: dynamic -> state.entries }
        val defaults = blocks { state: dynamic -> state.defaults }
        val values = if (dccon) props("DCCON" to selected.dccon) else props("NICK" to selected.nick, "ID" to selected.uid, "IP" to selected.ip)
        val rules = s.blockingEntries(values, gallery ?: undefinedValue(), props("entries" to entries, "defaults" to defaults)) as Array<dynamic>
        val version = react.useSyncExternalStore(if (dccon) s.onDatabaseChange else s.subscribeDatabase, s.databaseVersion) as Int
        val ip = if (version > 0 && truthy(selected.ip)) s.ipInfoOf(selected.ip) else null
        val userinfo = s.useRunningModuleSettings("userinfo")
        val color = if (ip != null && userinfo != null) userinfo["${ip.category}Color"] else null
        val ratio = when (activity) {
            "loading" -> "불러오는 중..."
            "error" -> "불러오지 못함"
            null, undefinedValue() -> null
            else -> "${activity.article.toLocaleString()} / ${activity.comment.toLocaleString()}"
        }

        val watchScroll: () -> dynamic = {
            val controller = js("new AbortController()")
            val options = props("capture" to true, "signal" to controller.signal)
            js("window").addEventListener("scroll", close, options)
            s.overlay.portal?.getRootNode()?.addEventListener("scroll", close, options)
            val dispose: () -> Unit = { controller.abort() }
            dispose
        }
        react.useEffect(watchScroll, emptyArray<dynamic>())
        val searchApi = react.useSyncExternalStore(s.subscribeModuleApis, readSearchApi)
        fun searchBy(by: String): dynamic {
            if (searchApi == null) return null
            val search: () -> Unit = { close(); searchApi.searchUserPosts(selected, by) }
            return search
        }
        fun copyRow(label: String, value: dynamic, search: dynamic = null, css: dynamic = null, color: dynamic = null): dynamic {
            if (!truthy(value)) return null
            val copy: () -> Unit = { close(); s.copyText(value, "복사했습니다.", "복사하지 못했습니다.") }
            val searchLabel = "${label}(으)로 글 검색"
            return ui.div("flex items-center",
                menu(props("className" to "h-auto min-h-8 min-w-0 flex-1 py-1.5 whitespace-normal",
                    "title" to "클릭하면 복사됩니다.", "onClick" to copy),
                    ui.span("w-18 shrink-0 text-left text-muted-foreground", label), " ",
                    ui.h("strong", props("className" to s.cn("min-w-0 text-left break-keep wrap-anywhere", css),
                        "style" to props("color" to color)), value)),
                if (search != null) ui.h(s.Button, props("variant" to "ghost", "size" to "icon-sm",
                    "className" to "shrink-0 cursor-pointer text-muted-foreground", "aria-label" to searchLabel,
                    "title" to searchLabel, "onClick" to search), ui.h(s.Search)) else null)
        }
        fun block(options: dynamic) { s.handleBlockRequest(options, selected); close() }
        val uid = selected.uid
        val address = selected.ip
        val identity = if (truthy(uid) && truthy(address)) "$uid ($address)" else if (truthy(uid)) uid else address
        val user = if (truthy(selected.nick)) selected.nick else if (truthy(identity)) identity else "사용자"
        val iconMenu = if (dccon) ui.h(react.Fragment, null,
            ui.h(dcconSummary, props("key" to selected.dccon, "code" to selected.dccon,
                "onOpen" to { focus.returnFocus(); close(); p.onDcconInfo(selected.dccon) })),
            ui.h(s.Separator),
            ui.div("flex flex-col",
                menu(props("danger" to true, "onClick" to { block(props("target" to "dccon")) }), ui.h(s.Ban), " 이 디시콘 차단"),
                menu(props("danger" to true, "title" to "이 디시콘이 든 패키지를 모두 가립니다.",
                    "onClick" to { focus.returnFocus(); close(); p.onBlockPackage(selected) }), ui.h(s.Ban), " 전체 차단")))
        else ui.h(react.Fragment, null,
            ui.div("flex flex-col", copyRow("닉네임", selected.nick, searchBy("nick")), copyRow("아이디/IP", identity, searchBy("identity")),
                copyRow("IP 정보", ip?.label, css = if (truthy(color)) null else "text-blue-600 dark:text-blue-400", color = color),
                copyRow("글/댓글", ratio), copyRow("메모", memo?.text, color = memo?.color)),
            ui.h(s.Separator),
            ui.div("flex flex-col",
                menu(props("danger" to true, "onClick" to { block(props("target" to "user")) }), ui.h(s.Ban), " 유저 차단"),
                if (truthy(gallery)) menu(props("danger" to true, "title" to "$gallery 갤러리에서만 가립니다.",
                    "onClick" to { block(props("target" to "user", "gallery" to gallery)) }), ui.h(s.Ban), " 이 갤러리에서만 차단") else null),
            ui.h(s.Separator),
            ui.div("flex flex-col",
                menu(props("onClick" to { focus.returnFocus(); store.getState().openMemo(selected) }), ui.h(s.NotebookPen), " 메모"),
                if (truthy(uid)) menu(props("nativeButton" to false, "render" to ui.h("a", props("href" to "https://gallog.dcinside.com/$uid",
                    "target" to "_blank", "rel" to "noreferrer", "onClick" to close))), ui.h(s.ExternalLink), " 갤로그") else null))
        val bounds: () -> dynamic = { js("DOMRect").fromRect(props("x" to position.x, "y" to position.y, "width" to 0, "height" to 0)) }
        val onOpen: (Boolean, dynamic) -> Unit = { open, details ->
            if (!open) {
                if (store.getState().bubble !== position) {
                    details.cancel()
                    details.allowPropagation()
                } else close()
            }
        }
        ui.h(s.Popover, props("open" to true, "onOpenChange" to onOpen),
            ui.h(s.OverlayPopoverContent, props("ref" to popup, "side" to "bottom", "align" to "start", "sideOffset" to 4,
                "className" to "w-auto min-w-[200px] max-w-[320px] gap-2 p-2",
                "anchor" to props("getBoundingClientRect" to bounds), "initialFocus" to focus.keyboard,
                "finalFocus" to focus.finalFocus, "aria-label" to if (dccon) "디시콘 메뉴" else "$user 메뉴"),
                iconMenu, if (rules.isNotEmpty()) ruleList(rules, defaults) else null))
    }
    val host: (dynamic) -> dynamic = { actions ->
        val position = store { state: dynamic -> state.bubble }
        val selected = store { state: dynamic -> state.selected }
        if (position != null && selected != null) ui.h(bubble, assign(props("bubble" to position, "selected" to selected), actions)) else null
    }
    return props("BubbleHost" to host, "DcconPackageDialog" to packageDialog)
}
