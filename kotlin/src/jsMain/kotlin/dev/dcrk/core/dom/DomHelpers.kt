package dev.dcrk

/** DOM의 바로 아래 script 노드를 제외한 나머지 텍스트. */
fun textWithoutScripts(element: dynamic): String {
    if (element == null) return ""
    val nodes = element.childNodes
    val result = StringBuilder()
    var i = 0
    while (i < (nodes.length as Int)) {
        val node = nodes[i]
        if (node.nodeName != "SCRIPT") {
            val text: dynamic = node.textContent
            if (text != null) result.append(text as String)
        }
        i++
    }
    return result.toString().trim()
}

/** document_start에서도 documentElement에 style을 쓰고 이미 있으면 재사용한다. */
fun writeStyle(id: String, css: String): () -> Unit {
    val doc: dynamic = js("document")
    var style: dynamic = doc.querySelector("style#$id")
    if (style == null) {
        style = doc.createElement("style")
        style.id = id
        doc.documentElement.appendChild(style)
    }
    style.textContent = css
    return { style.remove() }
}

/** Tailwind dark/light class와 color-scheme을 원본처럼 함께 갱신한다. */
fun setAppearance(root: dynamic, dark: Boolean) {
    root.classList.toggle("dark", dark)
    root.classList.toggle("light", !dark)
    root.style.colorScheme = if (dark) "dark" else "light"
}
