package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

private const val WRITER_SELECTOR = ".ub-writer:not([user_name])"
private const val BADGES_CLASS = "refresher-user-badges"
private const val LOW_BLUR = "refresherLowActivityBlur"
private const val LOW_HIDE = "refresherLowActivityHide"
private const val LOW_SELECTOR = ".$LOW_BLUR,.$LOW_HIDE"
private var userRowsQueued = false

private class UserInfoFeature(private val ctx: dynamic, private val s: dynamic) {
    private val doc: dynamic = js("document")
    private val console: dynamic = js("console")
    private val promise: dynamic = js("Promise")
    private val signal = ctx.signal
    private val failedRatios = s.createFailedCache()
    private val pending = mutableSetOf<String>()
    private var colors: dynamic = props()
    private var view: dynamic = null
    private var ratios: dynamic = props()

    private fun notifyRowsHidden() {
        if (userRowsQueued) return
        userRowsQueued = true
        val queue: dynamic = js("queueMicrotask")
        val type = s.rowsHiddenEvent as String
        queue {
            userRowsQueued = false
            doc.dispatchEvent(js("new Event(type)"))
        }
    }

    private fun clearLowActivity(): Boolean {
        val marked = jsArray(doc.querySelectorAll(LOW_SELECTOR))
        for (element in marked) element.classList.remove(LOW_BLUR, LOW_HIDE)
        return marked.isNotEmpty()
    }

    private fun badge(text: String, color: dynamic, title: dynamic, css: String = "refresherUserData"): dynamic {
        val span = doc.createElement("span")
        span.className = css
        span.textContent = text
        if (truthy(color)) span.style.color = color
        if (truthy(title)) span.title = title
        return span
    }

    private fun process(element: dynamic) {
        element.querySelector(".$BADGES_CLASS")?.remove()
        val data = element.dataset
        val uid = data.uid as String?
        val badges = mutableListOf<dynamic>()
        val settings = ctx.settings
        var low = false
        for (key in view.order as Array<String>) {
            when (key) {
                "UID" -> if (!uid.isNullOrEmpty()) {
                    if (s.showsUid(view, element.querySelector("img")?.src) == true) {
                        badges.add(badge("($uid)", colors.uid, uid, "ip refresherUserData"))
                    }
                } else if (truthy(data.ip)) {
                    val info = s.ipInfoOf(data.ip)
                    if (info != null && s.passesIpFilter(info, view.ipFilter) == true) {
                        badges.add(badge("[${info.label}]", colors[info.category], info.title, "refresherUserData refresherIpInfo"))
                    }
                }
                "MEMO" -> {
                    val memo = s.findMemo(props("uid" to uid, "ip" to data.ip, "nick" to data.nick), s.pageGallery)
                    if (memo != null) badges.add(badge("[${memo.text}]", memo.color, memo.text))
                }
                "RATIO" -> if (!uid.isNullOrEmpty() && settings.checkRatio == true && js("Object.hasOwn")(ratios, uid) == true) {
                    val cached = ratios[uid]
                    if (cached != null) {
                        val label = "${cached.article}/${cached.comment}"
                        low = s.isLowActivity(cached, settings.alarmRatio) == true
                        badges.add(badge("[$label]", if (low) colors.ratioAlarm else colors.ratio, label, "ip refresherUserData"))
                    }
                }
                "PERMBAN" -> if (!uid.isNullOrEmpty() && settings.checkPermBan == true) {
                    val reasons = s.banReasonsOf(uid)
                    if (truthy(reasons)) badges.add(badge("[$reasons]", colors.permBan, reasons, "ip refresherUserData"))
                }
            }
        }
        val action = settings.lowActivityAction as String
        if (low && action == "tag") badges.add(badge("[깡계]", colors.ratioAlarm, "글댓합 ${settings.alarmRatio}개 이하"))
        if (low && (action == "blur" || action == "hide") && element.closest(".gallview_head") == null) {
            val row = element.closest(s.writerRowSelector) ?: element
            row.classList.add(if (action == "blur") LOW_BLUR else LOW_HIDE)
            notifyRowsHidden()
        }
        if (badges.isEmpty()) return
        val group = doc.createElement("span")
        group.className = BADGES_CLASS
        group.setAttribute(s.filterSkipAttr, "")
        for (item in badges) group.append(item)
        val container = element.querySelector(".addbox") ?: element.querySelector(".fl > span") ?: element
        val anchor = container.querySelector(".writer_nikcon, .ip")
        if (anchor != null) anchor.after(group) else container.append(group)
    }

    private fun readSettings() {
        colors = fromEntries(ownKeys(s.badgeColors).map { key -> arrayOf<dynamic>(key, ctx.settings["${key}Color"]) }.toTypedArray())
        view = s.badgeViewOf(ctx.settings)
    }

    private fun publishRatios() {
        val settings = ctx.settings
        s.uiStore.setState(props("ratios" to if (settings.checkRatio == true)
            props("cache" to ratios, "alarm" to settings.alarmRatio, "action" to settings.lowActivityAction) else null))
    }

    private fun rebuildAll() {
        val changed = clearLowActivity()
        for (writer in jsArray(doc.querySelectorAll(WRITER_SELECTOR))) process(writer)
        if (changed) notifyRowsHidden()
    }

    private fun rebuildUsers(changed: Set<String>) {
        var cleared = false
        for (writer in jsArray(doc.querySelectorAll(WRITER_SELECTOR))) {
            if ((writer.dataset.uid as String? ?: "") !in changed) continue
            val row = writer.closest(s.writerRowSelector) ?: writer
            if (row.matches(LOW_SELECTOR) == true) cleared = true
            row.classList.remove(LOW_BLUR, LOW_HIDE)
            process(writer)
        }
        if (cleared) notifyRowsHidden()
    }

    private fun needsDatabase(): Boolean = view.ipFilter != "none" || ctx.settings.checkPermBan == true

    private fun applyStored(next: dynamic) {
        val before = ratios
        ratios = s.trimRatios(js("Object.assign")(props(), next?.ratio))
        val changed = (ownKeys(before).toSet() + ownKeys(ratios)).filterTo(mutableSetOf()) { uid ->
            val old = before[uid]
            val recent = ratios[uid]
            old?.date !== recent?.date || old?.article !== recent?.article || old?.comment !== recent?.comment
        }
        if (changed.isNotEmpty()) {
            publishRatios()
            rebuildUsers(changed)
        }
    }

    private fun storeRatios(entries: List<Pair<String, dynamic>>) {
        val now = js("Date.now()")
        val received = fromEntries(entries.map { (uid, activity) -> arrayOf<dynamic>(uid, setField(activity, "date", now)) }.toTypedArray())
        ratios = s.trimRatios(js("Object.assign")(props(), ratios, received))
        publishRatios()
        rebuildUsers(ownKeys(received).toSet())
    }

    private fun checkNewPosts(elements: Array<dynamic>) {
        if (ctx.settings.checkRatio != true) return
        val stale = elements.take(10).mapNotNull { (it.querySelector(".ub-writer")?.dataset?.uid as String?)?.takeIf { id -> id.isNotEmpty() } }
            .distinct().filter { s.isFresh(ratios[it]) != true && failedRatios.has(it) != true && it !in pending }
        if (stale.isEmpty()) return
        pending.addAll(stale)
        val jobs = stale.map { uid -> jsPromise { awaitJs(s.getUnlessBusy(uid)) } }.toTypedArray()
        promise.all(jobs).then { results: dynamic ->
            val updates = mutableListOf<Pair<String, dynamic>>()
            for ((index, uid) in stale.withIndex()) {
                val info = results[index]
                if (jsTypeOf(info) == "undefined") failedRatios.set(uid, true)
                else if (truthy(info) && s.isFresh(ratios[uid]) != true) updates.add(uid to info)
            }
            if (updates.isNotEmpty() && signal.aborted != true) storeRatios(updates)
        }.catch(console.error).finally { pending.removeAll(stale.toSet()) }
    }

    fun start(): Promise<dynamic> = jsPromise install@{
        doc.documentElement.classList.add("refresherUserInfo")
        ctx.onRevoke {
            doc.documentElement.classList.remove("refresherUserInfo")
            s.uiStore.setState(props("ratios" to null))
            if (clearLowActivity()) notifyRowsHidden()
            for (tag in jsArray(doc.querySelectorAll(".$BADGES_CLASS"))) tag.remove()
        }
        readSettings()
        ctx.onSettingsChanged { _: dynamic ->
            readSettings()
            publishRatios()
            rebuildAll()
            if (needsDatabase()) s.initDatabase().catch(console.error)
        }
        val ratioKey = s.ratioKey
        doc.addEventListener("contextmenu", s.openWriterBubble, props("capture" to true, "signal" to signal))
        var loading = true
        var early: dynamic = undefinedValue()
        val onStored: (dynamic) -> Unit = { next -> if (loading) early = next else applyStored(next) }
        s.watchStorage(ratioKey, onStored, signal)
        s.onBfcacheRestore({ jsPromise {
            val next = awaitJs(s.storage.getItem(ratioKey))
            applyStored(next)
        } }, signal)
        val loaded = awaitJs(promise.all(arrayOf<dynamic>(s.storage.getItem(ratioKey), if (needsDatabase()) s.initDatabase() else null)))
        if (signal.aborted == true) return@install undefinedValue()
        loading = false
        val data = if (jsTypeOf(early) == "undefined") loaded[0] else early
        ratios = data?.ratio ?: props()
        publishRatios()
        ctx.addFilter(WRITER_SELECTOR, ::process)
        val onMemo: (dynamic, dynamic) -> Unit = { next, previous -> if (next.memos !== previous.memos) rebuildAll() }
        ctx.addCleanup(s.memoStore.subscribe(onMemo))
        ctx.addCleanup(s.onDatabaseChange(::rebuildAll))

        var batch = mutableListOf<Pair<String, dynamic>>()
        val incoming: (String, dynamic) -> Unit = add@{ uid, result ->
            if (ctx.settings.checkRatio != true) return@add
            batch.add(uid to result)
            if (batch.size > 1) return@add
            val queue: dynamic = js("queueMicrotask")
            queue {
                val updates = batch
                batch = mutableListOf()
                if (signal.aborted != true) storeRatios(updates)
            }
        }
        ctx.addCleanup(s.onGallogActivity(incoming))
        props("checkNewPosts" to ::checkNewPosts)
    }
}

fun setupUserInfo(ctx: dynamic, services: dynamic): Promise<dynamic> = UserInfoFeature(ctx, services).start()
