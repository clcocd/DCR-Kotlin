package dev.dcrk

import kotlin.js.Date

internal fun createUiStateService(s: dynamic): dynamic {
    var store: dynamic = null
    fun low(ratio: dynamic, alarm: Double): Boolean =
        alarm > 0 && (ratio.article as Double) + (ratio.comment as Double) <= alarm
    fun fresh(info: dynamic): Boolean =
        info != null && Date.now() - (info.date as Double) <= 3_600_000
    fun lowActivityOf(ratios: dynamic, uid: dynamic): dynamic {
        if (ratios == null || !truthy(uid) || (ratios.action != "blur" && ratios.action != "hide")) return undefinedValue()
        val cache = ratios.cache
        if (js("Object.hasOwn(cache, uid)") != true) return undefinedValue()
        val ratio = cache[uid]
        return if (ratio != null && low(ratio, ratios.alarm as Double)) ratios.action else undefinedValue()
    }
    fun selectedGallery(gallery: dynamic): dynamic = gallery ?: s.pageGallery

    fun init(set: dynamic, get: dynamic): dynamic {
        fun showToast(content: String, type: dynamic, autoClose: dynamic, action: dynamic) {
            val toast = props("content" to content,
                "type" to (if (type === undefinedValue()) "info" else type),
                "autoClose" to (if (autoClose === undefinedValue()) 5000 else autoClose), "action" to action)
            val all = get().toasts as Array<dynamic>
            set(props("toasts" to (all.asList() + listOf(toast)).toTypedArray()))
        }
        fun openBubble(selected: dynamic, x: Double, y: Double) {
            set(props("selected" to selected, "bubble" to props("x" to x, "y" to y)))
        }
        fun openMemo(selected: dynamic) {
            val targets = props()
            if (truthy(selected.nick)) targets.NICK = selected.nick
            if (truthy(selected.uid)) targets.UID = selected.uid
            if (truthy(selected.ip)) targets.IP = selected.ip
            val type = when {
                truthy(selected.uid) -> "UID"
                truthy(selected.ip) -> "IP"
                else -> "NICK"
            }
            set(props("bubble" to null, "memo" to props("targets" to targets, "initialType" to type, "gallery" to selected.gallery)))
        }
        return props("toasts" to emptyArray<dynamic>(), "selected" to null, "bubble" to null, "memo" to null,
            "ratios" to null, "blockRevealed" to false, "showToast" to ::showToast, "openBubble" to ::openBubble,
            "openMemo" to ::openMemo, "closeBubble" to { set(props("bubble" to null)) },
            "closeMemo" to { set(props("memo" to null)) })
    }
    fun attach(next: dynamic) {
        store = next
        val needed: (dynamic) -> Boolean = { (it.toasts.length as Int) > 0 || it.bubble != null || it.memo != null }
        s.needOverlayWhen(store, needed)
    }
    fun copy(text: String, done: String, failed: String): dynamic {
        val toast = store.getState().showToast
        val clipboard: dynamic = js("navigator.clipboard")
        val good: () -> Unit = { toast(done) }
        val bad: () -> Unit = { toast(failed, "error") }
        return clipboard.writeText(text).then(good, bad)
    }
    fun openWriter(event: dynamic) {
        if (event.defaultPrevented == true || event.shiftKey == true) return
        val target = s.eventTarget(event)
        if (js("Element.prototype").isPrototypeOf(target) != true) return
        val writer = target.closest(".ub-writer") ?: return
        val data = writer.dataset
        if (!truthy(data.nick) && !truthy(data.uid) && !truthy(data.ip)) return
        event.preventDefault()
        store.getState().openBubble(props("nick" to data.nick, "uid" to data.uid, "ip" to data.ip), event.clientX, event.clientY)
    }
    return props("init" to ::init, "attach" to ::attach, "isLowActivity" to ::low, "isFresh" to ::fresh, "lowActivityOf" to ::lowActivityOf,
        "copyText" to ::copy, "openWriterBubble" to ::openWriter, "selectedGallery" to ::selectedGallery)
}
