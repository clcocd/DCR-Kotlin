package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun createManageRequests(s: dynamic): dynamic {
    fun success(result: dynamic) = result === "success" || result === "true" || result === true
    val parseJson: dynamic = js("(text) => { try { return JSON.parse(text); } catch (_) { return null; } }")
    fun manage(target: dynamic, action: String, fields: dynamic): Promise<dynamic> = jsPromise {
        val body = awaitJs(s.dcBody(target.link, props()))
        for (entry in entriesOf(fields)) {
            val value = entry[1]
            if (js("Array.isArray")(value) == true) {
                for (item in value as Array<dynamic>) body.append(entry[0], item)
            } else body.append(entry[0], value)
        }
        val text = (awaitJs(s.ajax.post(s.managerAjaxUrl(target.link, action), props("body" to body)).text()) as String).trim()
        val parsed = parseJson(text)
        if (record(parsed)) {
            val msg = parsed.msg
            return@jsPromise props("success" to success(parsed.result),
                "message" to if (jsTypeOf(msg) == "string" && truthy(msg)) msg else undefinedValue())
        }
        val result = s.submitResult(text)
        props("success" to success(result.result), "message" to s.resultMessage(result))
    }
    fun avoid(pre: dynamic, ids: dynamic, parent: dynamic, options: dynamic) = manage(pre, "update_avoid_list",
        props("id" to pre.gallery, "nos[]" to ids, "parent" to parent, "avoid_hour" to options.avoidHour,
            "avoid_reason" to options.avoidReason, "avoid_reason_txt" to options.avoidReasonTxt,
            "del_chk" to if (options.delChk == true) "1" else "0",
            "avoid_type_chk" to if (options.userTypeChk == true) "1" else "0"))
    fun image(pre: dynamic, type: String): String {
        val now = Date.now().asDynamic().toString()
        return "${s.urls.base}kcaptcha/image_v3/?gall_id=${pre.gallery}&kcaptcha_type=$type&time=$now&_GALLTYPE_=${s.galltypeOf(pre.link)}"
    }
    fun deleteAsAdmin(pre: dynamic, ids: dynamic) = manage(pre, "delete_comment",
        props("id" to pre.gallery, "pno" to pre.id, "cmt_nos[]" to ids))
    fun deleteAsUser(pre: dynamic, post: dynamic, id: String, password: String, key: String, token: dynamic) = jsPromise {
        val hasPassword = password.isNotEmpty()
        fun nonblank(value: dynamic): dynamic = if (truthy(value)) value else undefinedValue()
        val body = awaitJs(s.dcBody(pre.link, props("id" to (post.commentId ?: pre.gallery), "no" to pre.id, "re_no" to id,
            "mode" to "del", "re_password" to nonblank(password),
            "v_cur_t" to if (hasPassword) nonblank(post.v_cur_t) else undefinedValue(),
            "g-recaptcha-response" to "", "g-recaptcha-token" to nonblank(token), "rc_key" to key, "c_k_v" to "dzc")))
        if (hasPassword && post.commentDeleteFields != null) for (field in post.commentDeleteFields as Array<dynamic>) {
            body.append(field[0], field[1])
        }
        val response = awaitJs(s.ajax.post(s.urls.comment_remove, props("body" to body)).text())
        s.submitResult(response)
    }
    val blockCommenters: (dynamic, dynamic, dynamic) -> Promise<dynamic> = { pre, ids, options -> avoid(pre, ids, pre.id, options) }
    return props("blockCommenters" to blockCommenters, "captchaImage" to ::image,
        "adminDeleteComments" to ::deleteAsAdmin, "userDeleteComment" to ::deleteAsUser)
}
