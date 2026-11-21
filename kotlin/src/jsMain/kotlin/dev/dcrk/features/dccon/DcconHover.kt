package dev.dcrk

/** 미리보기 안 디시콘의 설명. 원본 콘은 클릭 상세창을 유지한다. */
internal fun installDcconHover(root: dynamic, portal: dynamic, blocked: dynamic, openInfo: dynamic): () -> Unit {
    val doc: dynamic = js("document")
    val win: dynamic = js("window")
    val element: dynamic = js("Element.prototype")
    val popup = doc.createElement("div")
    popup.className = "refresher-dccon-infoview"
    popup.style.display = "none"
    val description = doc.createElement("span")
    val button = doc.createElement("button")
    button.type = "button"
    button.textContent = "디시콘 보기"
    popup.appendChild(description)
    popup.appendChild(button)
    (portal ?: root.getRootNode()).appendChild(popup)

    var current: dynamic = null
    var timer: dynamic = null
    fun cancelHide() {
        if (timer != null) win.clearTimeout(timer)
        timer = null
    }
    fun hide() {
        cancelHide()
        popup.style.display = "none"
        current = null
    }
    fun scheduleHide() {
        cancelHide()
        timer = win.setTimeout({ hide() }, 180)
    }
    fun position(target: dynamic) {
        popup.style.display = "flex"
        val rect = target.getBoundingClientRect()
        val view = popup.getBoundingClientRect()
        val maxLeft = ((win.innerWidth as Double) - (view.width as Double) - 8.0).coerceAtLeast(8.0)
        val left = ((rect.left as Double) + (rect.width as Double) / 2.0 - (view.width as Double) / 2.0)
            .coerceIn(8.0, maxLeft)
        val bottom = (rect.bottom as Double) + 8.0
        val top = if (bottom + (view.height as Double) <= (win.innerHeight as Double) - 8.0) bottom
            else (rect.top as Double) - (view.height as Double) - 8.0
        popup.style.left = "${left}px"
        popup.style.top = "${top.coerceAtLeast(8.0)}px"
    }
    val enter: (dynamic) -> Unit = enter@{ event ->
        val target = event.target
        if (element.isPrototypeOf(target) != true) return@enter
        val dccon = target.closest(".written_dccon")
        if (dccon == null || root.contains(dccon) != true || blocked(dccon) == true) return@enter
        cancelHide()
        if (dccon !== current) {
            current = dccon
            val title = dccon.getAttribute("conalt") ?: dccon.getAttribute("alt") ?: dccon.getAttribute("title") ?: ""
            description.textContent = (title as String).ifBlank { "디시콘" }
        }
        position(dccon)
    }
    val leave: (dynamic) -> Unit = leave@{ event ->
        val dccon = current ?: return@leave
        val next = event.relatedTarget
        if (next != null && (dccon.contains(next) == true || popup.contains(next) == true)) return@leave
        scheduleHide()
    }
    val cancel: (dynamic) -> Unit = { cancelHide() }
    val viewClick: (dynamic) -> Unit = { event ->
        val dccon = current
        if (dccon != null && blocked(dccon) != true) {
            event.preventDefault()
            hide()
            openInfo(props("target" to dccon))
        }
    }
    val scrolled: (dynamic) -> Unit = { hide() }
    root.addEventListener("pointerover", enter)
    root.addEventListener("pointerout", leave)
    root.addEventListener("scroll", scrolled, true)
    win.addEventListener("scroll", scrolled, true)
    popup.addEventListener("pointerenter", cancel)
    popup.addEventListener("pointerleave", leave)
    button.addEventListener("click", viewClick)
    return {
        hide()
        root.removeEventListener("pointerover", enter)
        root.removeEventListener("pointerout", leave)
        root.removeEventListener("scroll", scrolled, true)
        win.removeEventListener("scroll", scrolled, true)
        popup.remove()
    }
}
