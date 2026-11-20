package dev.dcrk

internal fun createWriterCard(s: dynamic): (dynamic) -> dynamic {
    val react = s.react
    val ui = ReactUi(react)
    val store = s.uiStore
    val hasOwn: dynamic = js("Object.hasOwn")

    return { p ->
        val user = p.user
        val settings = s.useRunningModuleSettings("userinfo")
        val view = if (settings != null) s.badgeViewOf(settings) else s.defaultBadgeView
        val dbVersion = react.useSyncExternalStore(s.onDatabaseChange, s.databaseVersion) as Int
        val ipInfo = if (dbVersion > 0 && !truthy(user.id) && truthy(user.ip)) s.ipInfoOf(user.ip) else null
        val ipColor = if (ipInfo != null) if (settings != null) settings["${ipInfo.category}Color"] else null else null
        val uidColor = settings?.uidColor
        val gallery = s.previewStore { state: dynamic -> state.preData?.gallery ?: undefinedValue() }
        val memo = s.useUserMemo(props("uid" to user.id, "ip" to user.ip, "nick" to user.nick), gallery)
        val showsRatio = store { state: dynamic -> state.ratios != null } == true
        val alarm = store { state: dynamic -> state.ratios?.alarm ?: 0 }
        val action = store { state: dynamic -> state.ratios?.action }
        val cached = store(s.useShallow { state: dynamic ->
            val cache = state.ratios?.cache
            if (truthy(user.id) && cache != null && hasOwn(cache, user.id) == true) cache[user.id] else undefinedValue()
        })
        val fetched = s.useGallogActivity(if (p.fetchRatio == true && showsRatio && s.isFresh(cached) != true) user.id else undefinedValue())
        val ratio = if (fetched != null && kotlin.js.jsTypeOf(fetched) == "object") fetched else cached
        val lowActivity = ratio != null && s.isLowActivity(ratio, alarm) == true
        val ratioColor = if (lowActivity) settings?.ratioAlarmColor else settings?.ratioColor
        val expanded = store { state: dynamic ->
            val selected = state.selected
            state.bubble != null && selected?.nick === user.nick && selected?.uid === user.id && selected?.ip === user.ip
        }
        fun openBubble(x: dynamic, y: dynamic) {
            store.getState().openBubble(props("nick" to user.nick, "uid" to user.id, "ip" to user.ip, "gallery" to s.previewGallery()), x, y)
        }
        val menu: (dynamic) -> Unit = { event ->
            if (event.shiftKey != true) {
                event.preventDefault()
                openBubble(event.clientX, event.clientY)
            }
        }
        val click: (dynamic) -> Unit = { event ->
            val rect = event.currentTarget.getBoundingClientRect()
            openBubble(rect.left, rect.bottom)
        }
        val identityClass = s.cn("truncate text-xs", if (truthy(uidColor)) false else "text-muted-foreground")
        fun badge(value: String, color: dynamic, title: dynamic = undefinedValue(), css: dynamic = "truncate text-xs") =
            ui.h("span", props("className" to css, "style" to props("color" to color), "title" to title), value)
        val identity = if (truthy(user.id)) {
            if (s.showsUid(view, user.image) == true) badge("(${user.id})", uidColor, css = identityClass) else null
        } else if (ipInfo != null && s.passesIpFilter(ipInfo, view.ipFilter) == true) {
            badge("[${ipInfo.label}]", ipColor, ipInfo.title, s.cn("max-w-[300px] truncate text-xs",
                if (truthy(ipColor)) false else "text-blue-600 dark:text-blue-400"))
        } else null

        val badges = props(
            "UID" to identity,
            "MEMO" to if (memo != null) badge("[${memo.text}]", if (truthy(memo.color)) memo.color else undefinedValue(), memo.text) else null,
            "RATIO" to if (ratio != null) badge("[${ratio.article}/${ratio.comment}]", ratioColor, "글/댓글") else null
        )
        val order = (view.order as Array<String>).map { ui.h(react.Fragment, props("key" to it), badges[it]) }.toTypedArray()
        ui.h("div", props("className" to s.cn("refresher-user flex min-w-0 cursor-context-menu items-center gap-1",
            if (p.op == true) "rounded-md bg-primary/15 px-1.5 py-px text-link" else false), "onContextMenu" to menu),
            ui.h("button", props("type" to "button", "aria-haspopup" to "dialog", "aria-expanded" to expanded,
                "className" to s.cn(s.focusRing, "truncate text-sm font-bold"), "onClick" to click), user.nick ?: user.id ?: user.ip),
            if (truthy(user.image)) ui.h("img", props("src" to user.image, "alt" to "", "height" to 12)) else null,
            if (truthy(user.ip)) badge("(${user.ip})", uidColor, css = identityClass) else null,
            order,
            if (lowActivity && action == "tag") badge("[깡계]", settings?.ratioAlarmColor, "글댓합 ${alarm}개 이하") else null
        )
    }
}

internal fun createGallogActivityHook(s: dynamic): (dynamic) -> dynamic {
    fun cached(uid: String): dynamic {
        val cache = s.store.getState().ratios?.cache ?: return undefinedValue()
        val entry = if (js("Object.hasOwn(cache, uid)") == true) cache[uid] else undefinedValue()
        return if (s.isFresh(entry) == true) entry else undefinedValue()
    }
    return { input ->
        val uid = input as String?
        val initial: () -> dynamic = { if (uid.isNullOrEmpty()) undefinedValue() else cached(uid) ?: "loading" }
        val state = s.react.useState(initial)
        val effect: () -> dynamic = activity@{
            if (uid.isNullOrEmpty()) {
                state[1](undefinedValue())
                return@activity undefinedValue()
            }
            val hit = cached(uid)
            if (hit != null) {
                state[1](hit)
                return@activity undefinedValue()
            }
            var alive = true
            state[1]("loading")
            val apply: (dynamic) -> Unit = { if (alive) state[1](it ?: "error") }
            s.getGallogActivity(uid).then(apply)
            val dispose: () -> Unit = { alive = false }
            dispose
        }
        s.react.useEffect(effect, arrayOf<dynamic>(input))
        state[0]
    }
}
