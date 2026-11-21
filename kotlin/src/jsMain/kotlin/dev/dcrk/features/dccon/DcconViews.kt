package dev.dcrk

import kotlin.math.abs

internal fun createDcconPicker(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    return { p ->
        val cached = s.cachedDcconList()
        val initial: () -> dynamic = { cached ?: emptyArray<dynamic>() }
        val packages = react.useState(initial)
        val activeId = react.useState()
        val double = react.useState(false)
        val big = react.useState(false)
        val selection = react.useState(emptyArray<dynamic>())
        val unavailable = react.useState(false)
        val pickingTxtcon = p.onTxtcon != null
        val available = packages[0] as Array<dynamic>
        val chosen = selection[0] as Array<dynamic>
        val loading = available.isEmpty() && unavailable[0] != true
        val active = available.firstOrNull { it.package_idx === activeId[0] } ?: available.firstOrNull()
        val fetch: () -> dynamic = {
            if (!loading) undefinedValue()
            else {
                val ctl = js("new AbortController()")
                val signal = ctl.signal
                val ok: (dynamic) -> Unit = complete@{ value ->
                    if (signal.aborted == true) return@complete
                    if (kotlin.js.jsTypeOf(value) == "string" || value.length == 0) {
                        if (pickingTxtcon) {
                            unavailable[1](true)
                            return@complete
                        }
                        val notLogin = value == "not_login"
                        s.uiStore.getState().showToast(if (notLogin) "디시콘은 로그인한 뒤에 쓸 수 있습니다." else "사용 가능한 디시콘이 없습니다.",
                            if (notLogin) "warning" else "error")
                        p.onClose()
                    } else {
                        s.setCachedDcconList(value)
                        packages[1](value)
                    }
                }
                val fail: () -> Unit = {
                    if (signal.aborted != true) {
                        if (pickingTxtcon) unavailable[1](true)
                        else {
                            s.uiStore.getState().showToast("디시콘을 불러오지 못했습니다.", "error")
                            p.onClose()
                        }
                    }
                }
                s.fetchDcconList(signal).then(ok, fail)
                val dispose: () -> Unit = { ctl.abort() }
                dispose
            }
        }
        react.useEffect(fetch, emptyArray<dynamic>())
        val id = react.useId() as String
        val wheelTarget = react.useRef(null)
        val strip = react.useRef(null)
        val bind: () -> dynamic = {
            val el = strip.current
            if (el == null) undefinedValue()
            else {
                val scroll: (dynamic) -> Unit = scroll@{ event ->
                    val dy = event.deltaY as Double
                    val dx = event.deltaX as Double
                    val width = (el.scrollWidth as Double) - (el.clientWidth as Double)
                    if (event.shiftKey == true || event.ctrlKey == true || abs(dy) <= abs(dx) || width <= 0) return@scroll
                    event.preventDefault()
                    val left = ((wheelTarget.current ?: el.scrollLeft) as Double) + dy
                    wheelTarget.current = left.coerceIn(0.0, width)
                    el.scrollTo(props("left" to wheelTarget.current, "behavior" to s.smoothScroll()))
                }
                el.addEventListener("wheel", scroll, props("passive" to false))
                val dispose: () -> Unit = { el.removeEventListener("wheel", scroll) }
                dispose
            }
        }
        react.useEffect(bind, emptyArray<dynamic>())
        fun choice(dccon: dynamic) {
            val next = if (double[0] == true) chosen + arrayOf<dynamic>(dccon) else arrayOf<dynamic>(dccon)
            if (double[0] != true || next.size == 2) p.onSelect(next, big[0]) else selection[1](next)
        }
        fun toggle(key: String, title: String, state: dynamic, change: dynamic): dynamic =
            ui.h(s.Field, props("orientation" to "horizontal"),
                ui.h(s.FieldLabel, props("htmlFor" to "$id-$key", "className" to "font-normal"), title),
                ui.h(s.Switch, props("id" to "$id-$key", "size" to "sm", "checked" to state[0], "onCheckedChange" to change)))
        val changeDouble: (Boolean) -> Unit = { double[1](it); selection[1](emptyArray<dynamic>()) }
        val textChoice = if (pickingTxtcon && !loading)
            ui.h(s.Button, props("size" to "sm", "variant" to "secondary", "onClick" to p.onTxtcon), "글자콘")
        else null
        val header = ui.div("flex items-center justify-between", ui.h(s.DialogTitle, null, "디시콘"),
            ui.div("flex items-center gap-4",
                if (pickingTxtcon) (if (available.isNotEmpty()) textChoice else null) else toggle("double", "더블콘", double, changeDouble),
                if (pickingTxtcon) null else toggle("big", "대왕콘", big, big[1]), ui.h(s.DialogCloseButton)))
        val reminder = if (double[0] == true && chosen.isNotEmpty())
            ui.div("flex items-center gap-2", ui.h("img", props("src" to chosen[0].list_img, "alt" to chosen[0].title, "width" to 32, "height" to 32)),
                ui.span("flex-1 text-muted-foreground", "더블콘 ${chosen.size}/2 - 하나만 더 선택"),
                ui.h(s.Button, props("size" to "xs", "variant" to "secondary", "onClick" to { selection[1](emptyArray<dynamic>()) }), "초기화"))
        else null
        fun placeholder(total: Int, css: String) = (0 until total).map { ui.h(s.Skeleton, props("key" to it, "className" to css)) }.toTypedArray()
        val packOptions = if (loading) placeholder(12, "size-12 flex-none") else {
            val change: (Array<dynamic>) -> Unit = { if (it.isNotEmpty()) activeId[1](it[0]) }
            val scrollTo: (dynamic) -> Unit = { it.currentTarget.scrollIntoView(
                props("behavior" to s.smoothScroll(), "block" to "nearest", "inline" to "center")) }
            ui.h(s.ToggleGroup, props("aria-label" to "디시콘 패키지", "spacing" to 1, "onValueChange" to change,
                "value" to if (active != null) arrayOf<dynamic>(active.package_idx) else emptyArray<dynamic>()),
                available.map { pack -> ui.h(s.ToggleGroupItem,
                    props("key" to pack.package_idx, "value" to pack.package_idx, "title" to pack.title, "onClick" to scrollTo,
                        "className" to "cursor-pointer rounded-lg p-0.5 [&>img]:size-full [&>img]:rounded-md [&>img]:object-contain size-12 flex-none border-2 border-transparent aria-pressed:border-primary aria-pressed:bg-transparent"),
                    ui.h("img", props("src" to pack.main_img_url, "alt" to pack.title, "loading" to "lazy", "decoding" to "async"))) }.toTypedArray())
        }
        val icons = when {
            loading -> placeholder(36, "aspect-square w-full")
            pickingTxtcon && unavailable[0] == true -> arrayOf<dynamic>(
                ui.div("col-span-full flex h-80 flex-col items-center justify-center gap-3 text-center text-sm text-muted-foreground",
                    ui.h("span", null, "사용 가능한 디시콘이 없습니다."), textChoice))
            else -> ((active?.detail as Array<dynamic>?) ?: emptyArray<dynamic>()).map { item ->
                ui.h("button", props("type" to "button", "key" to item.detail_idx, "title" to item.title,
                    "className" to s.cn(s.focusRing, "aspect-square rounded-lg p-0.5 hover:bg-muted [&>img]:size-full [&>img]:rounded-md [&>img]:object-contain"),
                    "onClick" to { choice(item) }),
                    ui.h("img", props("src" to item.list_img, "alt" to item.title, "loading" to "lazy", "decoding" to "async")))
            }.toTypedArray()
        }
        ui.h(s.ModalDialog, props("onClose" to p.onClose, "className" to "sm:max-w-140", "focusOnOpen" to "keyboard"),
            header, reminder, ui.h("div", props("ref" to strip, "className" to "flex gap-1 overflow-x-auto scrollbar-none aria-busy:overflow-hidden",
                "aria-busy" to loading, "onScrollEnd" to { wheelTarget.current = null }), packOptions),
            ui.h("div", props("className" to "grid h-80 grid-cols-[repeat(auto-fill,minmax(72px,1fr))] content-start gap-1 overflow-y-auto scrollbar-none aria-busy:overflow-hidden",
                "aria-busy" to loading), icons))
    }
}

internal fun createDcconInfoView(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val close: () -> Unit = { store.setState(props("dcconInfo" to null)) }
    fun open(event: dynamic): Boolean {
        val target = event.target
        val dccon = if (js("Element.prototype").isPrototypeOf(target) == true) target.closest(".written_dccon") else null
        if (dccon == null || s.isBlockedHidden(dccon) == true) return false
        val code = s.dcconCode(dccon)
        if (truthy(code)) store.setState(props("dcconInfo" to code))
        return truthy(code)
    }
    val context: (dynamic) -> Unit = context@{ event ->
        val target = event.target
        if (event.shiftKey == true || js("HTMLImageElement.prototype").isPrototypeOf(target) != true) return@context
        val code = s.dcconCode(target)
        if (!truthy(code)) return@context
        event.preventDefault()
        s.uiStore.getState().openBubble(props("dccon" to code, "gallery" to s.previewGallery()), event.clientX, event.clientY)
    }
    fun label(text: String) = ui.h(s.Badge, props("variant" to "outline", "className" to "rounded-sm text-muted-foreground"), text)
    fun link(path: String, text: String, key: dynamic = null): dynamic {
        val escaped = js("encodeURIComponent(text)")
        return ui.h("a", props("key" to key, "href" to "${s.shop}/$path/$escaped", "target" to "_blank", "rel" to "noopener noreferrer",
            "className" to "text-link underline-offset-4 hover:underline"), text)
    }
    fun skeleton(css: String, key: dynamic = null) = ui.h(s.Skeleton, props("key" to key, "className" to css))
    val infoPopup: (dynamic) -> dynamic = { p ->
        val code = p.code
        val initial: () -> dynamic = { s.dcconPackages.get(code) }
        val state = react.useState(initial)
        val sending = react.useState(false)
        val added = react.useState(false)
        val confirming = react.useState(false)
        val bubble = s.uiStore { item: dynamic -> item.bubble }
        val dccon = state[0]
        fun add(id: dynamic) = jsPromise {
            confirming[1](false)
            if (sending[0] == true) return@jsPromise
            sending[1](true)
            val fail: (dynamic) -> String = {
                console.error("Dccon package add failed:", it)
                "fail"
            }
            val result = awaitJs(s.addDcconPackage(id).catch(fail))
            val toast = s.uiStore.getState().showToast
            when (result) {
                "ok" -> {
                    s.clearDcconCaches()
                    added[1](true)
                    toast("디시콘을 추가했습니다.")
                }
                "not_login" -> toast("디시콘은 로그인한 뒤에 추가할 수 있습니다.", "warning")
                else -> toast("디시콘을 추가하지 못했습니다.", "error")
            }
            sending[1](false)
        }
        val load: () -> dynamic = load@{
            if (dccon != null) return@load undefinedValue()
            val ctl = js("new AbortController()")
            val ok: (dynamic) -> Unit = { value ->
                s.dcconPackages.set(code, value)
                if (ctl.signal.aborted != true) state[1](value)
            }
            val fail: () -> Unit = {
                if (ctl.signal.aborted != true) {
                    s.uiStore.getState().showToast("디시콘 정보를 불러오지 못했습니다.", "error")
                    close()
                }
            }
            s.fetchDcconPackage(code, ctl.signal).then(ok, fail)
            val dispose: () -> Unit = { ctl.abort() }
            dispose
        }
        react.useEffect(load, arrayOf<dynamic>(code))
        val info = dccon?.info
        val tags = (dccon?.tags as Array<dynamic>?) ?: emptyArray<dynamic>()
        val title = if (info == null) ui.h(react.Fragment, null, skeleton("h-6 w-40"), skeleton("h-[18px] w-70"), skeleton("h-[18px] w-50"))
        else ui.h(react.Fragment, null,
            ui.div("flex items-start justify-between gap-3", ui.span("text-lg font-bold", info.title),
                if (!truthy(info.register) && !truthy(info.residual) && added[0] != true)
                    ui.h(s.Button, props("className" to "shrink-0", "disabled" to sending[0], "onClick" to { confirming[1](true) }),
                        if (sending[0] == true) ui.h(s.Spinner, props("aria-hidden" to true, "data-icon" to "inline-start")) else null, "사용") else null),
            if (truthy(info.description)) ui.h("p", null, info.description) else null,
            ui.div("flex flex-wrap items-center gap-2", label("제작"), link("nick_name", info.seller_name as String),
                ui.span("text-muted-foreground", info.reg_date_short)),
            if (tags.isNotEmpty()) ui.div("flex flex-wrap items-center gap-2", label("태그"),
                tags.map { link("tags", it.tag as String, it.tag) }.toTypedArray()) else null)
        val detail = if (dccon != null) (dccon.detail as Array<dynamic>).map { item -> ui.h("img", props("key" to item.idx,
            "src" to "${s.imageBase}${item.path}", "alt" to item.title, "title" to item.title, "loading" to "lazy", "decoding" to "async",
            "className" to "aspect-square w-full cursor-context-menu object-contain")) }.toTypedArray()
        else (0 until 30).map { skeleton("aspect-square w-full", it) }.toTypedArray()
        ui.h(s.ModalDialog, props("onClose" to close, "className" to "sm:max-w-[600px]", "focusOnOpen" to "keyboard", "disablePointerDismissal" to (bubble != null)),
            ui.div("flex items-center justify-between", ui.h(s.DialogTitle, null, "디시콘 정보"), ui.h(s.DialogCloseButton)),
            ui.div("flex gap-4 rounded-lg bg-muted/50 p-4",
                if (info != null) ui.h("img", props("src" to "${s.imageBase}${info.main_img_path}", "alt" to info.title, "className" to "size-30 flex-none object-contain"))
                else skeleton("size-30 flex-none"), ui.div("flex min-w-0 grow flex-col gap-2", title)),
            ui.h("div", props("className" to "grid max-h-[420px] grid-cols-[repeat(auto-fill,minmax(96px,1fr))] content-start gap-2 overflow-y-auto aria-busy:h-[420px] aria-busy:overflow-hidden",
                "aria-busy" to (dccon == null), "onContextMenu" to context), detail),
            if (confirming[0] == true && info != null) ui.h(s.ConfirmDialog, props("title" to "디시콘을 추가할까요?",
                "confirmLabel" to "추가", "onConfirm" to { add(info.package_idx) }, "onClose" to { confirming[1](false) })) else null)
    }
    return props("DcconInfoPopup" to infoPopup, "openDcconInfo" to ::open)
}
