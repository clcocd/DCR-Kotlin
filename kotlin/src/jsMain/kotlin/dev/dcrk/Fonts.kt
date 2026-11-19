package dev.dcrk

private const val DEFAULT_FONTS = "Noto Sans CJK KR, NanumGothic"

private val genericFamilies = setOf(
    "serif", "sans-serif", "monospace", "cursive", "fantasy", "system-ui",
    "math", "emoji", "fangsong", "ui-serif", "ui-sans-serif",
    "ui-monospace", "ui-rounded"
)

private val controlChars = Regex("[\\u0000-\\u001f\\u007f-\\u009f]")

/** 입력한 글꼴 이름을 CSS 문자열로 감싸고, 제어 문자 및 따옴표를 안전하게 처리한다. */
fun fontFamilyOf(customFonts: String): String {
    val input = customFonts.trim().ifEmpty { DEFAULT_FONTS }
    val fonts = input.replace(controlChars, "")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { font ->
            val quoted = font.length >= 2 &&
                (font.first() == '"' || font.first() == '\'') &&
                font.first() == font.last()
            val plain = if (quoted) font.substring(1, font.length - 1) else font
            if (!quoted && font.lowercase() in genericFamilies) {
                font
            } else {
                "\"" + plain.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
            }
        }.toMutableList()
    if (fonts.lastOrNull()?.lowercase() !in genericFamilies) fonts.add("sans-serif")
    return fonts.joinToString(", ")
}

private val dcFontTargets = listOf(
    "body", "button", "input", "textarea", "select", ".gall_list",
    ".view_content_wrap", ".view_comment div", ".btn_cmt_open", ".btn_cmt_close"
).joinToString(", ") { ":root $it" }

private fun cssNumber(value: Double): String =
    if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()

/** fonts 모듈에서 실제 사용하는 CSS. UI shadow DOM용 변수는 항상 남긴다. */
fun buildFontCss(customFonts: String, size: Double, changeDCFont: Boolean): String {
    val fonts = fontFamilyOf(customFonts)
    val css = mutableListOf(
        ":root { --refresher-font: $fonts; --refresher-preview-font-size: ${cssNumber(size + 2.0)}px; }"
    )
    if (changeDCFont) {
        css.add("$dcFontTargets { font-family: $fonts; }")
        css.add(":root .write_div { font-size: ${cssNumber(size)}px; }")
    }
    return css.joinToString("\n")
}
