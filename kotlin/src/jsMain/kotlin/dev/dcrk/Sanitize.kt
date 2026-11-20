package dev.dcrk

private val keptStyle = Regex("""^(?:color|background-color|font-.+|text-(?:align|indent|decoration|wrap).*|white-space.*|line-height|letter-spacing|word-spacing|vertical-align|border-(?!image).+|padding.*|margin.*|width|height|table-layout)$""")
private val iframeAllow = setOf("autoplay", "encrypted-media", "fullscreen", "picture-in-picture")
private val embed: dynamic = js("""/<embed\s[^>]*?src="(https:\/\/www\.youtube(?:-nocookie)?\.com\/embed\/[^"]+)"[^>]*>/gi""")

internal fun createSanitizer(factory: dynamic): (String, dynamic) -> String {
    val forbidden = arrayOf("style", "form", "input", "textarea", "select", "feimage")
    val disabled = forbidden + arrayOf("img", "video", "iframe", "audio", "embed", "source", "picture", "image")
    var regular: dynamic = null
    var mediaFree: dynamic = null
    var inert: dynamic = null

    fun style(node: dynamic) {
        val attributes = node.style
        val name = node.nodeName
        val source = (node.getAttribute("src") ?: "") as String
        val autoHeight = name == "IMG" || name == "VIDEO" || (name == "IFRAME" && source.contains("youtube"))
        val parseNumber: dynamic = js("Number.parseFloat")
        for (key in jsArray(attributes)) {
            val field = key as String
            val value = attributes.getPropertyValue(field) as String
            val dimensions = field == "width" || field == "height"
            val drop = !keptStyle.matches(field) || (field == "height" && autoHeight) ||
                (dimensions && name == "IFRAME" && parseNumber(value) == 0) ||
                (field.startsWith("margin") && value.contains("-"))
            if (drop) attributes.removeProperty(field)
        }
        if (attributes.length == 0) node.removeAttribute("style")
    }

    fun attributes(node: dynamic) {
        if (node.hasAttribute("style") == true) {
            if (js("node instanceof HTMLElement || node instanceof SVGElement") == true) style(node)
            else node.removeAttribute("style")
        }
        val name = node.nodeName
        if (name == "VIDEO") {
            val gif = node.hasAttribute("data-src") == true || node.classList.contains("written_dccon") == true ||
                ((node.getAttribute("src") ?: "") as String).contains("dccon.php")
            if (gif) for (attribute in arrayOf("autoplay", "loop", "muted", "playsinline")) node.setAttribute(attribute, "")
            else node.setAttribute("controls", "")
        } else if (name == "IMG" || name == "IFRAME") {
            node.setAttribute("loading", "lazy")
            val allow = node.getAttribute("allow") as String?
            if (allow != null) {
                val kept = allow.split(';').map { it.trim() }.filter {
                    val token = it.asDynamic().split(js("/\\s/"))[0] as String
                    token.lowercase() in iframeAllow
                }
                if (kept.isEmpty()) node.removeAttribute("allow") else node.setAttribute("allow", kept.joinToString("; "))
            }
        } else if (node.localName == "a" || node.localName == "area") {
            val href = (node.getAttribute("href") ?: node.getAttribute("xlink:href")) as String? ?: return
            if (href.startsWith('#')) {
                node.removeAttribute("href")
                node.removeAttribute("xlink:href")
            } else {
                node.setAttribute("target", "_blank")
                node.setAttribute("rel", "noopener noreferrer")
            }
        }
    }

    fun make(tags: Array<String>): dynamic {
        val sanitizer = factory()
        sanitizer.setConfig(props(
            "FORBID_ATTR" to arrayOf("background", "popover", "popovertarget", "popovertargetaction", "commandfor", "command",
                "data-slot", "data-blocked", "data-block-revealed", "data-comments-revealed", "data-blur-reveal", "data-duplicate", "data-low-activity"),
            "FORBID_TAGS" to tags, "ADD_TAGS" to arrayOf("iframe"), "ADD_ATTR" to arrayOf("allowfullscreen", "frameborder", "allow", "scrolling"),
            "IN_PLACE" to true))
        sanitizer.addHook("afterSanitizeAttributes", ::attributes)
        return sanitizer
    }

    return { html, options ->
        val strip = options?.stripMedia == true
        val sanitizer = if (strip) {
            if (mediaFree == null) mediaFree = make(disabled)
            mediaFree
        } else {
            if (regular == null) regular = make(forbidden)
            regular
        }
        if (inert == null) inert = js("document").createElement("template").content.ownerDocument
        val root = inert.createElement("div")
        root.innerHTML = if (!strip && html.contains("<embed"))
            html.asDynamic().replace(embed, "<iframe src=\"$1\" width=\"560\" height=\"315\" allowfullscreen></iframe>") else html
        sanitizer.sanitize(root)
        root.innerHTML as String
    }
}
