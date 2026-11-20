package dev.dcrk

import kotlin.math.floor

private val networkFailure = Regex("fetch|network|timed out|시간 초과", RegexOption.IGNORE_CASE)

fun createPreviewError(s: dynamic): (dynamic) -> dynamic {
    val ui = ReactUi(s.react)
    return { p ->
        val pre = s.usePreviewStore { state: dynamic -> state.preData }
        val error = p.error
        val status = error.status as Int?
        val original = error.adult == true || error.secret == true
        val deleted = status == 404
        var showOriginal = original
        val text = when {
            error.adult == true -> "성인 인증이 필요한 글입니다. 원문에서 확인해 주세요."
            error.secret == true -> "비밀글입니다. 원문에서 비밀번호를 입력해 주세요."
            deleted -> "게시글이 삭제되었거나 존재하지 않습니다."
            status == 403 || status == 429 -> "요청이 많아 디시인사이드가 잠시 접속을 막았습니다. 잠시 기다린 뒤 다시 시도해 주세요."
            status != null && status >= 500 -> "디시인사이드 서버가 불안정합니다. 잠시 후 다시 시도해 주세요."
            networkFailure.containsMatchIn(error.detail as String? ?: "") -> "디시인사이드에 연결하지 못했습니다. 인터넷 연결을 확인하고 다시 시도해 주세요."
            else -> {
                showOriginal = true
                "게시글을 읽지 못했습니다. 다시 시도하거나 원문에서 확인해 주세요."
            }
        }
        val reopen: () -> Unit = {
            if (pre != null) {
                val state = s.usePreviewStore.getState()
                state.requestOpen(pre, state.commentsOnly)
            }
        }
        val link = if (!showOriginal) null else ui.h(s.Button, props("size" to "sm", "variant" to "secondary", "nativeButton" to false,
            "render" to ui.h("a", props("href" to (pre?.link ?: js("location.href")), "target" to "_blank", "rel" to "noreferrer"))),
            ui.h(s.ExternalLink, props("data-icon" to "inline-start")), "원문 열기")
        val retry = if (deleted) null else ui.h(s.Button, props("size" to "sm", "variant" to if (original) "secondary" else "destructive", "onClick" to reopen),
            "다시 시도")
        ui.h(s.Alert, props("variant" to if (original) "default" else "destructive", "className" to if (original) "my-4 text-amber-700 dark:text-amber-400" else "my-4"),
            ui.h(s.CircleAlert), ui.h(s.AlertDescription, props("className" to "text-current"), text, ui.div("mt-2 flex gap-2", link, retry)))
    }
}

fun createPreviewClock(s: dynamic): dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val window: dynamic = js("window")
    val doc: dynamic = js("document")
    val options = props("year" to "numeric", "month" to "numeric", "day" to "numeric",
        "hour" to "numeric", "minute" to "numeric", "second" to "numeric")
    val formatter: dynamic = js("new Intl.DateTimeFormat(undefined, options)")
    val times = listOf("년" to 31_536_000_000.0, "주" to 604_800_000.0, "일" to 86_400_000.0,
        "시간" to 3_600_000.0, "분" to 60_000.0, "초" to 1000.0)
    val listeners: dynamic = js("new Set()")
    var timer = 0
    fun absolute(date: dynamic): String = if ((date.getTime() as Double).isNaN()) "" else formatter.format(date) as String
    fun relative(date: dynamic): String {
        val milliseconds = (js("Date.now()") as Double) - (date.getTime() as Double)
        if (milliseconds.isNaN() || milliseconds < -60_000) return absolute(date)
        if (milliseconds < 3_000) return "방금 전"
        for ((label, unit) in times) {
            if (milliseconds >= unit) return "${js("String")(floor(milliseconds / unit))}${label} 전"
        }
        return absolute(date)
    }
    val subscribe: (() -> Unit) -> (() -> Unit) = { listener ->
        listeners.add(listener)
        if (timer == 0) timer = window.setInterval({
            if (doc.hidden == false) listeners.forEach { next: dynamic -> next() }
        }, 5000) as Int
        val dispose: () -> Unit = {
            listeners.delete(listener)
            if (listeners.size == 0) {
                window.clearInterval(timer)
                timer = 0
            }
        }
        dispose
    }
    val tick: (Int) -> Unit = { ms ->
        val state = react.useState(0)
        react.useEffect({
            val id = window.setInterval({ if (doc.hidden == false) state[1] { count: Int -> count + 1 } }, ms)
            val dispose: () -> Unit = { window.clearInterval(id) }
            dispose
        }, arrayOf(ms))
    }
    val timeStamp: (dynamic) -> dynamic = { p ->
        val date = s.parseDate(p.date)
        val state = react.useState(false)
        val since = react.useSyncExternalStore(subscribe, { relative(date) })
        val full = absolute(date)
        val onClick: () -> Unit = { state[1] { value: Boolean -> !value } }
        val label = when {
            (date.getTime() as Double).isNaN() -> "이미 삭제됨"
            state[0] == true -> full
            else -> since
        }
        ui.h("button", props("type" to "button", "title" to full, "onClick" to onClick,
            "className" to s.cn(s.focusRing, "whitespace-nowrap text-muted-foreground", if (p.size == "2") "text-sm" else "text-xs")), label)
    }
    return props("TimeStamp" to timeStamp, "useTick" to tick)
}
