package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.undefined
import kotlin.math.max
import kotlin.math.min

private const val MINI_WIDTH = 720
private const val MINI_HEIGHT = 560
private val previewHostWindow: dynamic get() = js("window")

internal fun createPreviewStateService(services: dynamic): dynamic {
    val noReply = props("commentNo" to null, "replyNo" to null)
    val noFresh = js("new Set()")
    val noop: () -> dynamic = { undefined }
    val noHooks = props("requestOpen" to noop, "requestClose" to noop, "requestManage" to noop,
        "requestRefresh" to { Promise.resolve(undefined) }, "requestReload" to { Promise.resolve(undefined) },
        "requestBlock" to { Promise.resolve(false) })
    var signalSeq = 0
    var fadeTimer: dynamic = 0
    var miniCloseTimer: dynamic = 0
    var miniHovered = false
    var store: dynamic = null

    fun freshPost(): dynamic = props(
        "error" to undefined, "post" to undefined, "archived" to false,
        "comments" to undefined, "commentsFailed" to false, "allowReply" to true, "collapsed" to js("new Set()"),
        "commentsRevealed" to false, "freshComments" to noFresh, "reply" to noReply, "commentsOnly" to false,
        "imageBlocked" to false, "adminVisible" to false, "blockPopup" to false, "selectedComments" to noFresh,
        "deletePrompt" to null, "deletingComments" to noFresh, "viewer" to null
    )

    fun miniPosition(x: Double, y: Double): dynamic = props(
        "x" to max(0.0, min(x + 16, (previewHostWindow.innerWidth as Double) - MINI_WIDTH - 20)),
        "y" to max(0.0, min(y + 16, (previewHostWindow.innerHeight as Double) - MINI_HEIGHT - 20))
    )

    fun closeMiniSoon() {
        previewHostWindow.clearTimeout(miniCloseTimer)
        if (!miniHovered) miniCloseTimer = previewHostWindow.setTimeout({ store.setState(props("mini" to null)) }, 150)
    }

    fun keepMini() {
        previewHostWindow.clearTimeout(miniCloseTimer)
        miniCloseTimer = 0
    }

    fun hoverMini(hovered: dynamic) {
        miniHovered = hovered == true
        if (hovered == true) keepMini()
        else if (hovered == false) closeMiniSoon()
    }

    fun toggled(set: dynamic, no: String, on: dynamic): dynamic {
        val next = js("new Set(set)")
        if ((on ?: (set.has(no) != true)) == true) next.add(no)
        else next.delete(no)
        return next
    }

    fun init(set: dynamic, get: dynamic): dynamic {
        fun resolvePrompts() {
            get().captcha?.resolve("")
            get().turnstile?.resolve(undefined)
        }
        fun open(pre: dynamic, patch: dynamic) {
            resolvePrompts()
            set(js("Object").assign(freshPost(), patch, props(
                "visible" to true, "fading" to false, "preData" to pre, "signalId" to ++signalSeq,
                "mini" to null, "captcha" to null, "turnstile" to null, "dcconInfo" to null
            )))
        }
        fun close() {
            if (get().visible != true) return
            resolvePrompts()
            set(props("visible" to false, "fading" to true, "comments" to undefined, "blockPopup" to false,
                "captcha" to null, "turnstile" to null, "reply" to noReply, "signalId" to ++signalSeq,
                "dcconInfo" to null, "viewer" to null))
            previewHostWindow.clearTimeout(fadeTimer)
            fadeTimer = previewHostWindow.setTimeout({ set(props("fading" to false)) }, 200)
        }
        fun toggle(key: String, no: String) {
            val update: (dynamic) -> dynamic = { state -> props(key to toggled(state[key], no, undefined)) }
            set(update)
        }
        fun openCaptcha(url: String): Promise<String> = Promise { resolve, _ ->
            set(props("captcha" to props("url" to url, "resolve" to resolve)))
        }
        fun openTurnstile(config: dynamic): Promise<dynamic> = Promise { resolve, _ ->
            set(props("turnstile" to props("config" to config, "resolve" to resolve)))
        }
        fun moveMini(x: Double, y: Double) {
            val update: (dynamic) -> dynamic = { state ->
                if (state.mini == null) state
                else props("mini" to js("Object").assign(props(), state.mini, miniPosition(x, y)))
            }
            set(update)
        }
        val collapse: (String) -> Unit = { toggle("collapsed", it) }
        val select: (String) -> Unit = { toggle("selectedComments", it) }
        return js("Object").assign(freshPost(), noHooks, props(
            "visible" to false, "warm" to false, "fading" to false, "preData" to null, "signalId" to 0,
            "captcha" to null, "turnstile" to null, "mini" to null, "dcconInfo" to null, "commentOrder" to "oldest",
            "open" to ::open, "close" to ::close, "toggleCollapse" to collapse, "toggleSelected" to select,
            "openCaptcha" to ::openCaptcha, "openTurnstile" to ::openTurnstile, "moveMini" to ::moveMini
        ))
    }

    fun previewGallery(): dynamic {
        val state = store.getState()
        return if (state.visible == true) state.preData?.gallery ?: undefined else undefined
    }

    fun refreshComments(pre: dynamic) {
        services.setEntry(pre, props("commentsAt" to 0))
        val current = store.getState()
        if (current.preData != null && services.postKey(current.preData) == services.postKey(pre)) current.requestRefresh()
    }

    fun attach(nextStore: dynamic) {
        store = nextStore
        val needed: (dynamic) -> Boolean = { state ->
            state.visible == true || state.warm == true || state.mini != null ||
                state.captcha != null || state.turnstile != null || state.blockPopup != false || state.dcconInfo != null
        }
        services.needOverlayWhen(store, needed)
    }

    fun parseDate(value: String): Date {
        val year = if (value.take(4).contains('.')) "${Date(Date.now() + 9 * 3_600_000).getUTCFullYear()}-" else ""
        return Date(year + value.replace('.', '-').replaceFirst(' ', 'T') + "+09:00")
    }

    fun postTitle(post: dynamic): String {
        val title = (post.title ?: "") as String
        val header = post.header as String?
        return if (header.isNullOrEmpty()) title else "[$header] $title"
    }

    return props("attach" to ::attach, "init" to ::init, "previewGallery" to ::previewGallery,
        "refreshCommentsOf" to ::refreshComments, "parseDate" to ::parseDate, "postTitle" to ::postTitle,
        "noHooks" to noHooks, "noReply" to noReply, "noFresh" to noFresh, "miniWidth" to MINI_WIDTH,
        "miniHeight" to MINI_HEIGHT, "miniPosition" to ::miniPosition, "toggled" to ::toggled,
        "closeMiniSoon" to ::closeMiniSoon, "keepMini" to ::keepMini, "hoverMini" to ::hoverMini)
}
