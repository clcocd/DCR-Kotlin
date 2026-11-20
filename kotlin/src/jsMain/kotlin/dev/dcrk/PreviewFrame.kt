package dev.dcrk

private const val PREVIEW_FRAME_CLASS = "refresher-frame m-auto flex min-h-[min(800px,90vh)] flex-col overflow-clip rounded-2xl bg-background text-sm shadow-2xl ring-1 ring-foreground/10"

fun createPreviewFrame(s: dynamic): () -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val window: dynamic = js("window")
    val doc: dynamic = js("document")
    val store = s.usePreviewStore

    fun goToAdjacent(direction: Int) {
        val state = store.getState()
        val pre = state.preData
        val next = if (pre == null) null else s.adjacentPreData(pre, direction)
        if (next != null) state.requestOpen(next, false, direction)
    }

    fun hasAdjacent(direction: Int): Boolean {
        val pre = store.getState().preData
        return pre != null && s.adjacentPreData(pre, direction) != null
    }
    val go: (Int) -> Unit = ::goToAdjacent
    val has: (Int) -> Boolean = ::hasAdjacent

    val refreshButton: (dynamic) -> dynamic = { p ->
        val state = react.useState(false)
        val busy = state[0] as Boolean
        val run: () -> Unit = { state[1](true); p.run().finally { state[1](false) } }
        ui.h(s.TooltipButton, props("tip" to p.label, "size" to "icon-xs", "variant" to "ghost", "disabled" to busy, "onClick" to run),
            if (busy) ui.h(s.Spinner, props("aria-hidden" to true)) else ui.h(s.RotateCw))
    }

    fun skipHint(direction: Int): dynamic {
        val forward = direction < 0
        val placement = if (forward) "top-0 animate-skip-hint-top bg-linear-to-b from-[rgb(12_23_53/0.7)] via-[rgb(32_42_72/0.3)] to-transparent"
        else "bottom-0 animate-skip-hint-bottom bg-linear-to-t from-[rgb(12_23_53/0.7)] via-[rgb(32_42_72/0.3)] to-transparent"
        return ui.div(s.cn("refresher-skip-hint pointer-events-none fixed inset-x-0 flex h-[40%] items-center justify-center text-white motion-reduce:animate-none", placement) as String,
            ui.tag("p", s.cn("relative text-2xl font-bold tracking-[-1.66px]", if (forward) "-top-[20%]" else "top-[20%]") as String,
                "한 번 더 스크롤하면 ", if (forward) "다음" else "이전", " 게시글로 넘어갑니다."))
    }

    val contents: () -> dynamic = {
        val post = store { state: dynamic -> state.post }
        val blockImage = store { state: dynamic -> state.imageBlocked } == true
        val gallery = store { state: dynamic -> state.preData?.gallery }
        val viewer = s.useContentModuleSettings("preview").imageViewer
        val revealed = s.useUiStore { state: dynamic -> state.blockRevealed } == true
        val root = react.useRef(null)
        val hideText = post?.textBlocked == "hide" && !revealed
        val html = if (hideText) s.blockedText else post?.contents ?: ""
        s.usePreviewMedia(root, html, gallery)
        react.useEffect({
            if (root.current != null) s.markPressable(root.current, viewer)
            undefinedValue()
        }, arrayOf<dynamic>(html, viewer))

        ui.h("div", props("className" to "grow", "data-blocked" to if (hideText) undefinedValue() else post?.textBlocked),
            ui.h("div", props("ref" to root,
                "className" to s.cn("refresher-html refresher-preview-contents", if (blockImage) "refresher-preview-block-media" else false),
                "onClick" to { event: dynamic -> s.clickContents(event, viewer) }, "onKeyDown" to s.pressContents,
                "onErrorCapture" to { event: dynamic ->
                    val image = event.target
                    if (js("image instanceof HTMLImageElement") as Boolean) s.markMedia(image, viewer)
                }, "dangerouslySetInnerHTML" to props("__html" to html))))
    }

    fun spinner(label: String? = null): dynamic = ui.div("flex justify-center py-8",
        ui.h(s.Spinner, props("className" to "size-6", "aria-label" to (label ?: undefinedValue()))))

    return frame@{
        val visible = store { state: dynamic -> state.visible } as Boolean
        val fading = store { state: dynamic -> state.fading } as Boolean
        val post = store { state: dynamic -> state.post }
        val error = store { state: dynamic -> state.error }
        val archived = store { state: dynamic -> state.archived } == true
        val comments = store { state: dynamic -> state.comments }
        val commentsFailed = store { state: dynamic -> state.commentsFailed } == true
        val allowReply = store { state: dynamic -> state.allowReply } == true
        val commentsOnly = store { state: dynamic -> state.commentsOnly } == true
        val settings = s.useContentModuleSettings("preview")
        val block = s.useRunningModuleSettings("block")
        val revealed = s.useUiStore { state: dynamic -> state.blockRevealed } == true
        val postKey = store { state: dynamic -> if (state.preData == null) "" else s.keyOfPost(state.preData) }
        val listTitle = store { state: dynamic -> state.preData?.title }
        val scroller = react.useRef(null)
        val commentsSection = react.useRef(null)
        val focusOptions = props("preventScroll" to true)
        s.useScrollLock(visible)

        react.useEffect({
            if (visible) scroller.current?.focus(focusOptions)
            undefinedValue()
        }, arrayOf<dynamic>(visible, postKey))
        react.useLayoutEffect({
            if (scroller.current != null) scroller.current.scrollTop = 0
            undefinedValue()
        }, arrayOf<dynamic>(postKey))
        react.useLayoutEffect(effect@{
            if (!visible) return@effect undefinedValue()
            val onKey: (dynamic) -> Unit = key@{ event ->
                if (s.hasModifier(event) == true || s.isTyping(event) == true) return@key
                val state = store.getState()
                if (truthy(state.dcconInfo) || truthy(state.viewer)) return@key
                if (event.key != "Tab" && s.focusedElement() === doc.body) scroller.current?.focus(focusOptions)
                if (event.key == "PageUp" || event.key == "PageDown") {
                    event.preventDefault()
                    if (event.repeat != true) goToAdjacent(if (event.key == "PageUp") -1 else 1)
                }
            }
            window.addEventListener("keydown", onKey)
            val dispose: () -> Unit = { window.removeEventListener("keydown", onKey) }
            dispose
        }, arrayOf<dynamic>(visible))

        val signal = store { state: dynamic -> state.signalId }
        val gestureKey = js("String")(signal)
        val gesture = s.useWheelGesture(gestureKey, go, has, settings.scrollToSkip)
        val hint = if (visible && !fading && gesture.hint.key == gestureKey) gesture.hint.dir as Int else 0
        if (!visible && !fading) return@frame null

        val busy = error == null && post == null
        val hasComments = comments != null || (post != null && visible)
        val reload: () -> dynamic = { store.getState().requestReload() }
        val refreshComments: () -> dynamic = { store.getState().requestRefresh(true) }
        val title = if (post == null) listTitle ?: "" else s.postTitle(post)
        val postHeader = ui.div("px-8 pt-6 pb-3",
            ui.h(s.DialogTitle, props("render" to ui.tag("h2", "text-2xl font-bold")), title),
            if (post == null) null else ui.div("mt-3 flex flex-wrap items-center justify-between gap-3",
                ui.h(s.UserCard, props("user" to (post.user ?: props()), "fetchRatio" to true)),
                ui.div("flex items-center gap-3", ui.h(s.CountDown),
                    if (truthy(post.date)) ui.h(s.TimeStamp, props("date" to post.date, "size" to "2")) else null,
                    ui.span("flex items-center gap-1 text-sm text-muted-foreground", ui.h(s.Eye, props("className" to "size-3.5")), post.views),
                    ui.h(refreshButton, props("label" to "새로고침", "run" to reload)))))

        val body = when {
            error != null -> ui.h(s.ErrorBlock, props("error" to error))
            commentsOnly -> ui.h(s.Button, props("variant" to "secondary", "className" to "mb-6 w-full",
                "onClick" to { store.setState(props("commentsOnly" to false)) }), "댓글만 표시 중입니다. 눌러서 본문 보기")
            else -> ui.h(react.Fragment, null, ui.h(contents), if (post == null) null else ui.h(s.Votes, props("post" to post)))
        }
        val archiveWarning = if (!archived) null else ui.h(s.Alert, props("className" to "mb-4 text-amber-700 dark:text-amber-400"),
            ui.h(s.Archive), ui.h(s.AlertDescription, props("className" to "text-current"), "불러오지 못해 저장해 둔 내용을 보여 줍니다."))
        val postBody = ui.div("flex grow flex-col px-8 pt-6", archiveWarning, body, if (busy) spinner() else null)

        val commentsBody = if (!hasComments) null else {
            val list = when {
                comments != null && comments.length == 0 -> ui.tag("p", "py-8 text-center text-muted-foreground", "댓글이 없습니다.")
                comments != null -> ui.h(s.CommentList, props("comments" to comments))
                !commentsFailed -> spinner("댓글을 불러오는 중")
                else -> ui.h(s.Alert, props("variant" to "destructive", "className" to "mx-8 my-4 w-auto"), ui.h(s.CircleAlert),
                    ui.h(s.AlertDescription, props("className" to "text-current"), "댓글을 불러오지 못했습니다.",
                        ui.div("mt-2", ui.h(s.Button, props("size" to "sm", "variant" to "destructive",
                            "onClick" to { store.getState().requestRefresh() }), "다시 시도"))))
            }
            ui.h("div", props("ref" to commentsSection), ui.h(s.Separator),
                ui.div("flex items-center justify-between gap-3 px-8 pt-3",
                    if (comments == null) null else ui.h(s.CommentSummary, props("comments" to comments)),
                    ui.div("flex items-center gap-2",
                        if (comments != null && (s.threadParents(comments).length as Int) > 1) ui.h(s.CommentOrder) else null,
                        ui.h(refreshButton, props("label" to "댓글 새로고침", "run" to refreshComments)))),
                list)
        }

        fun jump(tip: String, icon: dynamic, click: () -> Unit): dynamic =
            ui.h(s.TooltipButton, props("tip" to tip, "side" to "left", "variant" to "secondary", "size" to "icon",
                "className" to "rounded-full", "onClick" to click), ui.h(icon))
        val jumps = ui.div("sticky bottom-0 h-0", ui.div("absolute right-4 bottom-4 flex flex-col gap-2",
            jump("맨 위로", s.ArrowUp) { scroller.current?.scrollTo(props("top" to 0, "behavior" to s.smoothScroll())) },
            if (hasComments) jump("댓글로", s.MessageSquare) {
                commentsSection.current?.scrollIntoView(props("behavior" to s.smoothScroll(), "block" to "start"))
            } else null))
        val writeComment = if (post == null || archived) null else if (allowReply) ui.h(s.WriteComment)
        else ui.tag("p", "px-8 pt-3 pb-6 text-muted-foreground", "멤버만 댓글을 쓸 수 있습니다.")
        val width = "min(${settings.previewWidth}px, 90vw)"
        ui.h(s.PreviewDialogRoot, props("fading" to fading, "busy" to busy, "blur" to settings.toggleBackgroundBlur,
            "onWheel" to gesture.onWheel, "scroller" to scroller, "after" to if (hint != 0) skipHint(hint) else undefinedValue()),
            ui.h("div", props("className" to PREVIEW_FRAME_CLASS,
                "data-blur-reveal" to if (block?.blurReveal == true) true else undefinedValue(),
                "data-block-revealed" to if (revealed) true else undefinedValue(), "style" to props("width" to width)),
                ui.h(react.Fragment, props("key" to postKey), postHeader, ui.h(s.Separator), postBody, commentsBody, writeComment), jumps))
    }
}
