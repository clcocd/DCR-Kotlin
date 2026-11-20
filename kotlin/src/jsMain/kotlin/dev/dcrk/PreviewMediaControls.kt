package dev.dcrk

internal fun createImageViewer(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.store
    val dialog = s.Dialog
    val close: () -> Unit = { store.setState(props("viewer" to null)) }

    return viewer@{ p ->
        val long = react.useState()
        val popup = react.useRef(null)
        val images = p.images as Array<dynamic>
        val index = p.index as Int
        val image = images.getOrNull(index) ?: return@viewer null
        val multiple = images.size > 1
        val onDark = "text-white/85 hover:bg-white/10 hover:text-white"
        val original = s.dcinsideHref(image.pop)
        fun go(dir: Int) {
            popup.current?.scrollTo(props("top" to 0))
            store.setState(props("viewer" to props("images" to images, "index" to (index + dir + images.size) % images.size)))
        }
        val keydown: (dynamic) -> Unit = keys@{ event ->
            s.keepTabInside(event)
            if (!multiple || (event.key != "ArrowLeft" && event.key != "ArrowRight") || s.hasModifier(event) == true) return@keys
            event.preventDefault()
            go(if (event.key == "ArrowLeft") -1 else 1)
        }
        val alt = if (truthy(image.alt)) image.alt else "본문 이미지 ${index + 1}"
        val load: (dynamic) -> Unit = { event ->
            val node = event.currentTarget
            if ((node.naturalHeight as Double) > (node.naturalWidth as Double) * 2.5) long[1](image.src)
        }
        val photo = ui.h("img", props("src" to image.src, "alt" to alt, "onLoad" to load, "className" to
            s.cn("m-auto max-w-full select-none", if (long[0] != image.src) "max-h-full object-contain" else false)))
        val toolbar = ui.div("fixed top-3 right-4 flex items-center gap-4",
            if (multiple) ui.h("span", props("className" to "text-white/85", "aria-live" to "polite"), "${index + 1} / ${images.size}") else null,
            if (truthy(original)) ui.h(s.Button, props("size" to "icon", "variant" to "ghost", "className" to onDark,
                "aria-label" to "원본 보기", "title" to "원본 보기", "nativeButton" to false,
                "render" to ui.h("a", props("href" to original, "target" to "_blank", "rel" to "noopener noreferrer"))), ui.h(s.ExternalLink)) else null,
            ui.h(dialog.Close, props("render" to ui.h(s.Button, props("size" to "icon", "variant" to "ghost",
                "className" to onDark, "aria-label" to "닫기", "title" to "닫기"))), ui.h(s.X))
        )
        fun arrow(dir: Int): dynamic {
            if (!multiple) return null
            val left = dir < 0
            val css = if (left) "fixed top-1/2 left-4 -translate-y-1/2 rounded-full"
                else "fixed top-1/2 right-4 -translate-y-1/2 rounded-full"
            return ui.h(s.Button, props("size" to "icon-lg", "variant" to "secondary", "className" to css,
                "aria-label" to if (left) "이전 이미지" else "다음 이미지", "onClick" to { go(dir) }),
                ui.h(if (left) s.ChevronLeft else s.ChevronRight))
        }
        val outside: (dynamic) -> Unit = { event -> if (event.target === event.currentTarget) close() }
        val openChange: (Boolean) -> Unit = { if (!it) close() }
        ui.h(dialog.Root, props("open" to true, "onOpenChange" to openChange),
            ui.h(dialog.Portal, props("container" to s.overlay.portal),
                ui.h(dialog.Backdrop, props("className" to "fixed inset-0 bg-black/85 duration-150 animate-in fade-in")),
                ui.h(dialog.Popup, props("ref" to popup, "onKeyDown" to keydown, "onClick" to outside,
                    "className" to "refresher-viewer fixed inset-0 flex overflow-y-auto px-18 py-14 outline-none duration-150 animate-in fade-in"),
                    ui.h(dialog.Title, props("className" to "sr-only"), "이미지 크게 보기"),
                    photo, toolbar, arrow(-1), arrow(1))))
    }
}

internal fun createVotes(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    val store = s.store
    val window: dynamic = js("window")
    val assign: dynamic = js("Object.assign")
    return { p ->
        val post = p.post
        val pre = store { state: dynamic -> state.preData }
        val voting = s.react.useState(null)
        val current = voting[0]

        fun vote(target: dynamic, mode: String) = jsPromise {
            val id = store.getState().signalId
            val label = if (mode == "U") "추천" else "비추천"
            val code = if (post.requireCaptcha == true) {
                val response = awaitJs(store.getState().openCaptcha(s.captchaImage(target, "recommend")))
                if (!truthy(response)) return@jsPromise
                response
            } else undefinedValue()

            val request = s.vote(target, post, mode, code)
            val managed = awaitJs(s.notifyManage(request, "${label}했습니다.", "${label}하지 못했습니다. 잠시 후 다시 시도해 주세요."))
            if (!truthy(managed)) return@jsPromise
            val result = awaitJs(request)
            val counts = if (mode == "U") props("upvotes" to (result.counts ?: post.upvotes ?: "X"),
                "fixedUpvotes" to if (truthy(result.fixedCounts)) result.fixedCounts else undefinedValue())
            else props("downvotes" to (result.counts ?: post.downvotes))
            val patch: (dynamic) -> dynamic = { state ->
                if (state.signalId !== id || state.post == null) state
                else props("post" to assign(props(), state.post, counts))
            }
            store.setState(patch)
            val cached = s.getEntry(target)?.post
            if (cached != null) s.setEntry(target, props("post" to assign(props(), cached, counts)))
        }

        fun trigger(mode: String) {
            if (pre != null && current == null) {
                voting[1](mode)
                val done: () -> Unit = { voting[1](null) }
                vote(pre, mode).asDynamic().finally(done)
            }
        }

        val share: () -> Unit = {
            if (pre != null) s.copyText(s.viewUrl(pre.link, pre.gallery, pre.id), "링크를 복사했습니다.", "링크를 복사하지 못했습니다.")
        }
        val up = if (truthy(post.upvotes)) post.upvotes else "X"
        val fixed = if (truthy(post.fixedUpvotes)) post.fixedUpvotes else null
        val down = post.downvotes
        fun button(mode: String, label: String, icon: dynamic, count: dynamic, fixed: dynamic = null): dynamic {
            val busy = current == mode
            return ui.h(s.Button, props("size" to "lg", "variant" to "secondary", "className" to "h-10 px-4",
                "aria-label" to label, "disabled" to (current != null), "onClick" to { trigger(mode) }),
                ui.h(if (busy) s.Spinner else icon, props("aria-hidden" to if (busy) true else undefinedValue(), "data-icon" to "inline-start")),
                count, if (fixed != null) ui.span("text-muted-foreground", "($fixed)") else null)
        }
        ui.div("flex items-center justify-center gap-3 py-6",
            button("U", "추천 $up" + if (fixed == null) "" else " (고정닉 $fixed)", s.ChevronUp, up, fixed),
            if (down !== undefinedValue()) button("D", "비추천 $down", s.ChevronDown, down) else null,
            ui.h(s.TooltipButton, props("tip" to "링크 복사", "size" to "icon-lg", "variant" to "ghost", "onClick" to share), ui.h(s.Link2)),
            ui.h(s.TooltipButton, props("tip" to "새 탭으로 열기", "size" to "icon-lg", "variant" to "ghost", "nativeButton" to false,
                "render" to ui.h("a", props("href" to (pre?.link ?: window.location.href), "target" to "_blank", "rel" to "noreferrer"))),
                ui.h(s.ExternalLink)))
    }
}

internal fun createWheelGesture(react: dynamic): (dynamic, (Int) -> Unit, (Int) -> Boolean, Boolean) -> dynamic {
    fun canScroll(node: dynamic, dir: Int): Boolean = if (dir < 0) (node.scrollTop as Double) > 0
        else (node.scrollTop as Double) + (node.clientHeight as Double) < (node.scrollHeight as Double) - 2
    return { key, go, canGo, enabled ->
        val wheel = react.useRef(props("last" to 0, "armed" to 0, "key" to "", "settling" to false))
        val hint = react.useState(props("dir" to 0, "key" to ""))
        val onWheel: (dynamic) -> Unit = wheelEvent@{ event ->
            if (!enabled || event.deltaY == 0 || event.ctrlKey == true || event.shiftKey == true) return@wheelEvent
            val box = event.currentTarget
            val target = event.target
            if (js("Element.prototype").isPrototypeOf(target) != true || box.contains(target) != true) return@wheelEvent
            val dir = if ((event.deltaY as Double) > 0) 1 else -1
            var atEdge = !canScroll(box, dir)
            var node = target
            val style: dynamic = js("getComputedStyle")
            while (atEdge && node != null && node !== box) {
                if ((node.scrollHeight as Double) > (node.clientHeight as Double) &&
                    Regex("auto|scroll").containsMatchIn(style(node).overflowY as String) && canScroll(node, dir)) atEdge = false
                node = node.parentElement
            }
            val state = wheel.current
            if (state.key !== key) {
                state.key = key
                state.armed = 0
            }
            val newGesture = (event.timeStamp as Double) - (state.last as Double) > 250
            state.last = event.timeStamp
            if (state.settling == true) {
                if (!newGesture) return@wheelEvent
                state.settling = false
            }
            var armed = 0
            if (atEdge && newGesture && state.armed == dir) {
                state.settling = true
                go(dir)
            } else if (atEdge && (state.armed == dir || canGo(dir))) armed = dir
            if (armed != state.armed) hint[1](props("dir" to armed, "key" to key))
            state.armed = armed
        }
        props("hint" to hint[0], "onWheel" to onWheel)
    }
}

internal fun createContentsControls(s: dynamic): dynamic {
    val window: dynamic = js("window")
    fun viewable(image: dynamic): Boolean = !isBrokenMedia(image) &&
        image.closest("a, .written_dccon, [data-block]") == null && s.isBlockedHidden(image) != true && image.checkVisibility() == true
    fun click(event: dynamic, imageViewer: Boolean) {
        if (s.openDcconInfo(event) == true) return
        val raw = event.target
        val target = if (js("Element.prototype").isPrototypeOf(raw) == true) raw else null
        val clicked = target?.closest("img")
        if (clicked != null && imageViewer && viewable(clicked)) {
            val images = jsArray(event.currentTarget.querySelectorAll("img")).filter(::viewable)
            val info = images.map { image ->
                props("src" to if (truthy(image.currentSrc)) image.currentSrc else image.src,
                    "alt" to image.alt, "pop" to s.dcinsideHref(image.dataset.pop))
            }.toTypedArray()
            s.store.setState(props("viewer" to props("images" to info, "index" to images.indexOfFirst { it === clicked })))
            return
        }
        val image = target?.closest("img[data-pop]")
        if (image != null && image.closest("a") == null) {
            if (s.isBlockedHidden(image) == true) return
            val url = s.dcinsideHref(image.dataset.pop)
            if (truthy(url)) window.open(url, "_blank", "noopener")
            return
        }
        val button = target?.closest(".btn_img_block") ?: return
        event.preventDefault()
        s.store.setState(props("imageBlocked" to false))
        val parent = button.parentElement
        if (parent != null) for (media in jsArray(parent.querySelectorAll(":scope > [data-block], :scope > .refresher-imgnum > [data-block]"))) {
            if (js("HTMLImageElement.prototype").isPrototypeOf(media) == true && truthy(media.dataset.original)) media.src = media.dataset.original
            media.removeAttribute("data-block")
        }
        button.remove()
        markPreviewPressable(event.currentTarget, imageViewer)
    }
    fun press(event: dynamic) {
        val target = event.target
        if ((event.key != "Enter" && event.key != " ") || js("HTMLElement.prototype").isPrototypeOf(target) != true ||
            target.getAttribute("role") != "button") return
        event.preventDefault()
        target.click()
    }
    return props("clickContents" to ::click, "pressContents" to ::press)
}
