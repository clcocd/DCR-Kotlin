package dev.dcrk

private var segmenter: dynamic = null
private val lineBreaks = Regex("\\r\\n?")
private val fourDots = Regex("\\.{4,}")
private fun textPattern(source: String, flags: String): dynamic = js("new RegExp(source, flags)")
private val ineligibleAstral by lazy { textPattern("""[[\u{10000}-\u{10FFFF}]--[\u{1F000}-\u{1FAFF}]]""", "gv") }
private val bmpEmoji by lazy { textPattern("""[\p{Emoji_Presentation}--[\u{10000}-\u{10FFFF}]]""", "gv") }

internal fun txtconGraphemes(text: String): Array<String> {
    if (segmenter == null) segmenter = js("new Intl.Segmenter()")
    return jsArray(segmenter.segment(text)).map { it.segment as String }.toTypedArray()
}

internal fun wrapTxtcon(text: String): String =
    text.replace(lineBreaks, "\n").split('\n').joinToString("\n") { line ->
        txtconGraphemes(line).asList().chunked(5).joinToString("\n") { it.joinToString("") }
    }

internal fun normalizeTxtcon(text: String): String {
    val arabicForms: dynamic = js("/[\\uFB50-\\uFDFF\\uFE70-\\uFEFE]/g")
    val spaces: dynamic = js("/[\\u00A0\\u1680\\u2000-\\u200A\\u202F\\u205F\\u3000]/g")
    val fillers: dynamic = js("/[\\u2800\\u115F\\u1160\\u3164\\uFFA0\\u034F\\u17B4\\u17B5]/g")
    val disallowed: dynamic = js("/[^\\p{L}\\p{N}\\p{P}\\p{S}\\p{Zs}\\p{M}\\u{1F000}-\\u{1FAFF}\\u200D\\uFE00-\\uFE0F\\n]/gu")
    val braces: dynamic = js("/[{}]/g")
    val repeatedMarks: dynamic = js("/\\p{M}{3,}/gu")
    val shortMarks: (String) -> String = { jsArray(it).take(2).joinToString("") { mark -> mark as String } }
    val sanitized = text.replace(lineBreaks, "\n").asDynamic().replace(ineligibleAstral, "+")
        .replace(arabicForms, "+").replace(spaces, " ").replace(fillers, "").replace(disallowed, "").replace(braces, "")
        .replace(repeatedMarks, shortMarks) as String
    val limitedLines = sanitized.replace(fourDots, "...").split('\n').take(4).joinToString("\n")
    var current = txtconGraphemes(limitedLines).take(24).joinToString("")
    while (wrapTxtcon(current).count { it == '\n' } >= 4) {
        current = jsArray(current).dropLast(1).joinToString("") { it as String }
    }
    var count = 0
    return buildString {
        for (part in jsArray(current)) {
            val char = part as String
            val width = if (char == "\n") 0 else char.length + ((part.match(bmpEmoji)?.length ?: 0) as Int)
            if (count + width > 20) break
            append(char)
            count += width
        }
    }
}

internal fun createTxtconSubmission(s: dynamic): (dynamic, dynamic, dynamic) -> kotlin.js.Promise<dynamic> =
    { pre, post, entry -> jsPromise {
        val form = post.commentForm
        val data = props("id" to (post.commentId ?: pre.gallery), "no" to (post.commentNo ?: pre.id),
            "c_no" to optionalTxtconField(entry.commentNo), "reply_no" to optionalTxtconField(entry.replyNo),
            "name" to optionalTxtconField(entry.user.name), "password" to optionalTxtconField(entry.user.pw),
            "code" to optionalTxtconField(entry.captcha))
        val slots = entry.double as Array<dynamic>?
        if (slots == null) {
            data.txtcon_text = entry.text
            data.txtcon_bg = entry.colors.bg
            data.txtcon_color = entry.colors.txt
        } else {
            for ((index, slot) in slots.withIndex()) {
                val number = index + 1
                data["slot${number}_type"] = slot.type
                if (slot.type == "dccon") {
                    data["package_idx_$number"] = slot.package_idx
                    data["detail_idx_$number"] = slot.detail_idx
                } else {
                    data["txtcon_text_$number"] = slot.text
                    data["txtcon_bg_$number"] = slot.colors.bg
                    data["txtcon_color_$number"] = slot.colors.txt
                }
            }
        }
        val assign: dynamic = js("Object.assign")
        assign(data, form.checks)
        if (form.gallNickName !== undefinedValue()) {
            data.gall_nick_name = form.gallNickName
            data.use_gall_nick = "N"
        }
        assign(data, props("g-recaptcha-response" to "", "g-recaptcha-token" to optionalTxtconField(entry.grecaptchaToken)), entry.turnstile)
        val body = awaitJs(s.dcBody(pre.link, data))
        val token = awaitJs(s.fcmToken())
        val endpoint = if (slots == null) s.urls.txtcon_submit else s.urls.txtcon_double_submit
        val result = awaitJs(s.ajax.post(endpoint, props("body" to body, "headers" to props("fcmToken" to token))).text())
        s.submitResult(result)
    } }

private fun optionalTxtconField(value: dynamic): dynamic = if (truthy(value)) value else undefinedValue()
