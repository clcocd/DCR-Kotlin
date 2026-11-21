package dev.dcrk

/** 정화된 본문에서 사용자 지정 글자색·배경색을 표시 변수로 보존한다. */
internal fun adaptPreviewColors(html: String): String {
    if (!html.contains("color", true)) return html
    val parser: dynamic = js("new DOMParser()")
    val doc = parser.parseFromString("<div id='refresher-colors-root'>" + html + "</div>", "text/html")
    val root = doc.getElementById("refresher-colors-root") ?: return html
    val neutrals = setOf("black", "white", "#000", "#000000", "#fff", "#ffffff",
        "rgb(0, 0, 0)", "rgb(255, 255, 255)")
    for (node in jsArray(root.querySelectorAll("[style], font[color]"))) {
        val style = node.style
        val color = ((style?.color as String?)?.trim()).orEmpty().ifEmpty {
            (node.getAttribute("color") as String?)?.trim().orEmpty()
        }
        if (color.isNotEmpty()) {
            node.style.setProperty("--refresher-user-text-color", color)
            node.setAttribute("data-refresher-custom-color", "")
            if (color.lowercase() in neutrals) node.setAttribute("data-refresher-neutral-color", "")
        }
        val background = ((style?.backgroundColor as String?)?.trim()).orEmpty()
        if (background.isNotEmpty()) {
            node.style.setProperty("--refresher-user-background-color", background)
            node.setAttribute("data-refresher-custom-background", "")
            if (background.lowercase() in neutrals) node.setAttribute("data-refresher-neutral-background", "")
        }
    }
    return root.innerHTML as String
}
