package dev.dcrk

/** 원본 6.2.2 스텔스 기능 메타데이터. 단축키·토글 ID를 유지한다. */
internal fun stealthMeta(): dynamic = props(
    "id" to "stealth",
    "defaultEnable" to false,
    "toggles" to arrayOf("reveal"),
    "commands" to props("stealthPause" to props("key" to "Alt+P"))
)

private const val STEALTH_CLASS = "stlth"
private const val CONTROL_BUTTON = ".stealth_control_button"

private fun icon(name: String, paths: String): String =
    """<svg data-icon="$name" xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">$paths</svg>"""

private val eyeIcon = icon(
    "eye",
    """<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>"""
)

private val eyeOffIcon = icon(
    "eye-off",
    """<path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49"/><path d="M14.084 14.158a3 3 0 0 1-4.242-4.242"/><path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143"/><path d="m2 2 20 20"/>"""
)

/** 표시 상태의 단일 출처는 HTML의 stlth 클래스다. */
fun stealthIsRevealed(): Boolean {
    val doc: dynamic = js("document")
    return doc.documentElement.classList.contains(STEALTH_CLASS) as Boolean
}

private fun renderStealthButton(button: dynamic) {
    val doc: dynamic = js("document")
    val shown = stealthIsRevealed()
    val label = doc.createElement("span")
    label.textContent = if (shown) "이미지 숨기기" else "이미지 보이기"
    button.replaceChildren(label)
    button.insertAdjacentHTML("beforeend", if (shown) eyeOffIcon else eyeIcon)
}

fun stealthToggle() {
    val doc: dynamic = js("document")
    doc.documentElement.classList.toggle(STEALTH_CLASS)
    val button = doc.querySelector("$CONTROL_BUTTON > #tempview")
    if (button != null) renderStealthButton(button)
}

private fun removeButtons() {
    val doc: dynamic = js("document")
    val buttons: dynamic = doc.querySelectorAll(CONTROL_BUTTON)
    var i = 0
    while (i < (buttons.length as Int)) {
        buttons[i].remove()
        i++
    }
}

/** Firefox 재주입 시 이전 인스턴스의 버튼도 반드시 교체한다. */
fun stealthCreateButton() {
    val doc: dynamic = js("document")
    removeButtons()
    val frame = doc.createElement("div")
    frame.className = "stealth_control_button"
    frame.dataset.refresherUi = ""
    val button = doc.createElement("button")
    button.type = "button"
    button.className = "button"
    button.id = "tempview"
    renderStealthButton(button)
    button.addEventListener("click", { _: dynamic -> stealthToggle() })
    frame.append(button)
    doc.body.append(frame)
}

fun stealthEnable() {
    val doc: dynamic = js("document")
    doc.documentElement.classList.add("refresherStealth")
}

/** 모듈을 끌 때만 페이지 상태를 원복한다. */
fun stealthDispose() {
    val doc: dynamic = js("document")
    doc.documentElement.classList.remove("refresherStealth", STEALTH_CLASS)
    removeButtons()
}

fun setupStealth(ctx: dynamic): dynamic {
    val doc: dynamic = js("document")
    ctx.onRevoke(::stealthDispose)
    stealthEnable()
    if (doc.body != null) {
        stealthCreateButton()
    } else {
        val options: dynamic = js("({once: true})")
        options.signal = ctx.signal
        doc.addEventListener("DOMContentLoaded", { _: dynamic -> stealthCreateButton() }, options)
    }

    val api: dynamic = js("({})")
    api.isRevealed = ::stealthIsRevealed
    api.toggle = ::stealthToggle
    return api
}

fun stealthShortcut(toggle: () -> Unit, isRevealed: () -> Boolean, showToast: (String) -> Unit) {
    toggle()
    showToast(if (isRevealed()) "이미지를 보이게 했습니다." else "이미지를 숨겼습니다.")
}

/** 스텔스 단축키와 페이지 토글의 실제 동작도 Kotlin에서 정의한다. */
fun createStealthControls(showToast: (String) -> Unit): dynamic {
    val pause: (dynamic) -> Unit = { api ->
        stealthShortcut({ api.toggle() }, { api.isRevealed() as Boolean }, showToast)
    }
    val isOn: (dynamic) -> Boolean = { api -> api.isRevealed() as Boolean }
    val toggle: (dynamic) -> Unit = { api -> api.toggle() }
    return props(
        "shortcuts" to props("stealthPause" to pause),
        "pageToggles" to props("reveal" to props(
            "desc" to "스텔스로 숨긴 이미지를 보입니다",
            "isOn" to isOn,
            "toggle" to toggle
        ))
    )
}
