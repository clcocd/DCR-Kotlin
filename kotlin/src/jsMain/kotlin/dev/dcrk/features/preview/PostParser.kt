package dev.dcrk

import kotlin.js.jsTypeOf

internal const val ADULT_POST = "adult"
internal const val SECRET_POST = "secret"
internal val numberedImageSource: dynamic = js("/dcimg\\d\\.dcinside\\.(com|co\\.kr)\\/viewimage\\.php/")
private val numberingCookie = Regex("""(?:^|; )img_numbering=([^;]*)""")
private val popupImage = Regex("""imgPop\('([^']+)'""")
private val commentId = Regex("""\$\(document\)\.data\('comment_id',\s+'([^']+)'\);""")
private val commentNo = Regex("""\$\(document\)\.data\('comment_no',\s+'([^']+)'\);""")
private val turnstileConfig = Regex("""var _TS_CMT_\s*=\s*(\{[^;]*\});""")
private val dValue = Regex("""_d\('([^']*)'\)""")
private val headerBrackets = Regex("""^\[|\]$""")
private val onlyDigits = Regex("""^\d+$""")

private fun optionalText(value: dynamic): dynamic = if (truthy(value)) value else undefinedValue()
private fun firstMatch(raw: dynamic, pattern: Regex): String? =
    (raw as String?)?.let { pattern.find(it)?.groupValues?.get(1) }

private fun findInScripts(doc: dynamic, pattern: Regex): dynamic {
    for (script in jsArray(doc.scripts)) {
        val result = firstMatch(script.textContent, pattern)
        if (result != null) return result
    }
    return undefinedValue()
}

private fun strip(value: dynamic, vararg parts: String): dynamic {
    var result = value as String? ?: return undefinedValue()
    for (part in parts) result = result.replaceFirst(part, "")
    return optionalText(result.trim())
}

private fun restoreMedia(doc: dynamic) {
    for (image in jsArray(doc.querySelectorAll("img[data-original]:not([data-block])"))) {
        if (truthy(image.dataset.original)) image.src = image.dataset.original
    }
    for (image in jsArray(doc.querySelectorAll("img[onclick*='imgPop']"))) {
        val url = firstMatch(image.getAttribute("onclick"), popupImage)
        if (!url.isNullOrEmpty()) image.dataset.pop = url
    }
    for (node in jsArray(doc.querySelectorAll(".writing_view_box #zzbang_div, .writing_view_box #ad_nv_slot, .writing_view_box #tip_video, .writing_view_box .tip_box2.tip_video, .writing_view_box #spoiler_warning_tip"))) node.remove()

    val cookie = firstMatch(js("document.cookie"), numberingCookie)
    if (!cookie.isNullOrEmpty() && cookie != "1") return
    val media = jsArray(doc.querySelectorAll(".writing_view_box :is(img, video):not(.og-img)")).filter { el ->
        val src = arrayOf<dynamic>(el.getAttribute("src"), el.dataset.original, el.dataset.src).firstOrNull(::truthy) ?: ""
        numberedImageSource.test(src) == true
    }
    if (media.size < 3) return
    for ((i, el) in media.withIndex()) {
        val wrap = doc.createElement("span")
        wrap.className = "refresher-imgnum"
        wrap.dataset.num = (i + 1).toString()
        el.replaceWith(wrap)
        wrap.append(el)
    }
}

private fun parseWriter(doc: dynamic): dynamic {
    val writer = doc.querySelector(".gallview_head > .gall_writer") ?: return undefinedValue()
    return props("nick" to optionalText(writer.dataset.nick), "id" to optionalText(writer.dataset.uid), "ip" to optionalText(writer.dataset.ip),
        "image" to (writer.querySelector("img")?.src ?: undefinedValue()))
}

private fun parseTurnstile(doc: dynamic): dynamic {
    val raw = findInScripts(doc, turnstileConfig)
    if (!truthy(raw)) return undefinedValue()
    val data = try { js("JSON.parse(raw)") } catch (_: Throwable) { null } ?: return undefinedValue()
    val mode = js("Number(data.mode)")
    if (!truthy(mode) || jsTypeOf(data.sitekey) != "string") return undefinedValue()
    val wait = js("Number.parseInt(data.wait, 10)")
    return props("mode" to mode, "sitekey" to data.sitekey, "wait" to if (truthy(wait)) wait else 0,
        "appearance" to if (jsTypeOf(data.appearance) == "string") data.appearance else undefinedValue(), "test" to truthy(data.test))
}

private fun parseCommentForm(doc: dynamic): dynamic {
    val checks = props()
    for (name in arrayOf("check_6", "check_7", "check_8")) checks[name] = doc.querySelector("#$name")?.value ?: ""
    val fields = jsArray(doc.querySelectorAll("#focus_cmt > input")).map { input ->
        val name = if (truthy(input.name)) input.name else if (truthy(input.id)) input.id else ""
        arrayOf<dynamic>(name, input.value)
    }.toTypedArray()
    val gallNick = if (doc.querySelector("#use_gall_nick") != null)
        doc.querySelector("#focus_cmt input[name=gall_nick_name]")?.value ?: "" else undefinedValue()
    return props("fields" to fields, "serviceCode" to (doc.querySelector("input[name=service_code]")?.value ?: ""),
        "dValue" to (firstMatch(doc.querySelector("#reply-setting-tmpl + script")?.textContent, dValue) ?: undefinedValue()),
        "checks" to checks, "gallNickName" to gallNick, "turnstile" to parseTurnstile(doc))
}

internal fun parsePostInfo(html: String): dynamic {
    val parser = js("new DOMParser()")
    val doc = parser.parseFromString(html, "text/html")
    if (doc.querySelector(".gallview_head, .writing_view_box, .title_subject") == null) {
        if (html.contains("/error/adult") || doc.querySelector(".adult_certify") != null) throw Error(ADULT_POST)
        return undefinedValue()
    }
    if (doc.querySelector(".writing_view_box .mini_pwcheck") != null) throw Error(SECRET_POST)
    restoreMedia(doc)
    val subject = doc.querySelector(".title_subject")
    val device = doc.querySelector(".title_device")
    if (subject != null) for (script in jsArray(subject.querySelectorAll("script"))) script.remove()
    val body = doc.querySelector(".writing_view_box")
    val contents = body?.innerHTML ?: undefinedValue()
    val writeText = (doc.querySelector(".write_div")?.textContent as String?)?.trim()
    body?.remove()
    fun text(selector: String): dynamic = doc.querySelector(selector)?.textContent
    fun value(selector: String): dynamic = doc.querySelector(selector)?.value ?: undefinedValue()
    val header = (text(".title_headtext") as String?)?.replace(headerBrackets, "")
    val count = strip((text(".gall_comment") as String?)?.trim()?.split(' ')?.getOrNull(1)) as String?
    val date = doc.querySelector(".gallview_head .gall_date")
    val random = doc.querySelector("#adult_article + input")
    val deleteFields = jsArray(doc.querySelectorAll("#_cmt_del_form_ > input")).map { arrayOf<dynamic>(it.name, it.value) }.toTypedArray()
    val result = props(
        "header" to strip(header), "title" to strip(subject?.textContent),
        "expire" to strip(text(".view_content_wrap div.fl > span.mini_autodeltime > div.pop_tipbox > div"), " 자동 삭제"),
        "date" to strip(if (truthy(date?.title)) date.title else date?.textContent), "user" to parseWriter(doc),
        "views" to strip(text(".fr > .gall_count"), "조회"), "upvotes" to strip(text(".fr > .gall_reply_num"), "추천"),
        "fixedUpvotes" to strip(text(".sup_num > .smallnum")), "downvotes" to strip(text(".btn_recommend_box .down_num")),
        "contents" to contents, "commentId" to findInScripts(doc, commentId), "commentNo" to findInScripts(doc, commentNo),
        "commentCount" to if (count != null && onlyDigits.matches(count)) js("Number(count)") else undefinedValue(),
        "requireCaptcha" to (doc.querySelector(".recommend_kapcode") != null),
        "requireCommentCaptcha" to (doc.querySelector(".cmt_write_box input[name=comment_code]") != null),
        "v_cur_t" to value("input[name=v_cur_t]"),
        "randomParam" to if (truthy(random?.name)) props("name" to random.name, "value" to random.value) else undefinedValue(),
        "esno" to value("#e_s_n_o"), "recommendCode" to value("input[name=code_recommend]"), "writeText" to (writeText ?: undefinedValue()),
        "commentForm" to parseCommentForm(doc), "commentDeleteFields" to deleteFields
    )
    for (media in jsArray(doc.querySelectorAll("video, audio"))) media.remove()
    return result
}
