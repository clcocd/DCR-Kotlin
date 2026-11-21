package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun createCommentSubmission(s: dynamic): (dynamic, dynamic, dynamic) -> Promise<dynamic> {
    val excluded = setOf("service_code", "gallery_no", "clickbutton")
    val assign: dynamic = js("Object.assign")
    fun codeOf(form: dynamic): dynamic {
        return try {
            if (!truthy(form.dValue)) return null
            val decoded = decodeD(form.dValue as String)
            if (!truthy(decoded)) return null
            val text = decoded as String
            val first = text.firstOrNull()?.digitToIntOrNull() ?: return null
            val adjusted = if (first > 5) first - 5 else first + 4
            val num: dynamic = js("Number")
            val values = (adjusted.toString() + text.drop(1)).split(',').map { num(it) as Double }
            if (values.any { it.isNaN() }) return null
            val jsString: dynamic = js("String")
            val replacement = values.mapIndexed { index, value ->
                jsString.fromCharCode(2 * (value - index - 1) / (13 - index - 1)) as String
            }.joinToString("")
            val tail: dynamic = js("/(.{10})$/")
            form.serviceCode.replace(tail, replacement)
        } catch (_: Throwable) {
            null
        }
    }
    return { pre, post, data -> jsPromise {
        val form = post.commentForm
        val code = codeOf(form)
        if (!truthy(code)) return@jsPromise props("result" to "false", "message" to "댓글 폼을 읽지 못했습니다. 원문에서 작성해 주세요.")
        val gallery = post.commentId ?: pre.gallery
        val id = post.commentNo ?: pre.id
        val user = data.user
        val memo = data.memo
        val text = jsTypeOf(memo) == "string"
        fun optional(value: dynamic): dynamic = if (truthy(value)) value else undefinedValue()
        val initial = (form.fields as Array<dynamic>).filterNot { it[0] in excluded }.toTypedArray()
        val fields = assign(props("t_vch2" to "", "t_vch2_chk" to ""), fromEntries(initial), props(
            "service_code" to code, "c_gall_id" to gallery, "c_gall_no" to id, "id" to gallery, "no" to id,
            "c_no" to optional(data.commentNo), "reply_no" to optional(data.replyNo), "name" to user.name,
            "password" to optional(user.pw), "use_gall_nick" to "N", "code" to optional(data.captcha),
            "g-recaptcha-response" to "", "g-recaptcha-token" to optional(data.grecaptchaToken),
            "bigdccon" to if (data.bigDccon == true) "1" else false
        ))
        if (text) {
            fields.memo = memo
            fields.rc_key = data.rcKey
        } else {
            val icons = memo as Array<dynamic>
            fields.input_type = "comment"
            fields.double_con_chk = if (icons.size > 1) "1" else false
            val packageIds = icons.map { it.package_idx }.toTypedArray()
            val itemIds = icons.map { it.detail_idx }.toTypedArray()
            fields.package_idx = packageIds.asDynamic().join(",")
            fields.detail_idx = itemIds.asDynamic().join(",")
        }
        assign(fields, data.turnstile)
        val body = awaitJs(if (text) s.formBody(fields) else s.csrfBody(fields))
        val token = awaitJs(s.fcmToken())
        val endpoint = if (text) s.urls.comments_submit else s.urls.dccon_comments_submit
        val result = awaitJs(s.ajax.post(endpoint, props("body" to body, "headers" to props("fcmToken" to token))).text())
        s.submitResult(result)
    } }
}

internal fun createCommentSender(s: dynamic): dynamic {
    val failures = mapOf("code_fail" to "자동입력 방지 코드가 일치하지 않습니다.",
        "fail1" to "닉네임과 비밀번호를 정확하게 입력해 주세요.", "form_error" to "닉네임과 비밀번호를 정확하게 입력해 주세요.")
    val iconFailures = mapOf("not_buy" to "구매내역이 존재하지 않는 디시콘입니다.", "expired" to "사용기간이 만료된 디시콘입니다.",
        "unuseable" to "해당 디시콘은 현재 사용 불가능합니다.", "not_exists" to "잘못된 파일 경로 입니다.", "fail" to "디시콘 입력에 실패하였습니다.")
    val ignore: () -> dynamic = { undefinedValue() }
    var prefetched: dynamic = null
    fun isPosted(response: dynamic): Boolean {
        val result = response.result as String
        return result != "false" && result.isNotEmpty() && !result.trimStart().startsWith("<") && result !in failures && result !in iconFailures
    }
    fun randomKey(): String = if ((js("Math.random()") as Double) < 0.5) "new" else "new3"
    suspend fun sendWithChallenge(send: (dynamic) -> dynamic, challenge: dynamic): dynamic {
        val response = awaitJs(send(undefinedValue()))
        if (response.message != "captcha" || response.detail != "v3") return response
        val token = awaitJs(s.sendMessage("refresher:grecaptchaToken", challenge).catch(ignore))
        return if (truthy(token)) awaitJs(send(token)) else response
    }
    fun fresh(config: dynamic): dynamic {
        val entry = prefetched
        return if (entry != null && entry.sitekey === config.sitekey && Date.now() - (entry.at as Double) < 240_000) entry else null
    }
    fun hidden(config: dynamic): dynamic =
        s.sendMessage("refresher:turnstileToken", props("config" to config, "visible" to false)).catch(ignore)
    fun prefetch(config: dynamic) {
        if (fresh(config) == null) prefetched = props("sitekey" to config.sitekey, "at" to Date.now(), "result" to hidden(config))
    }
    fun makeFields(config: dynamic, focusMs: () -> Double, ask: (dynamic) -> dynamic): Promise<dynamic> = jsPromise {
        val reuse = fresh(config)
        prefetched = null
        var result = awaitJs(reuse?.result)
        if (!truthy(result?.token) && result?.state != "interactive") result = awaitJs(hidden(config))
        if (result != null && !truthy(result.token) && result.state == "interactive" && (config.mode !== 1 || truthy(config.appearance))) {
            result = awaitJs(ask(config))
            if (result == null) return@jsPromise undefinedValue()
        }
        val jsString: dynamic = js("String")
        val fields = props("cf-turnstile-response" to (result?.token ?: ""),
            "ts_state" to if (truthy(result?.token)) "ok" else result?.state ?: "error",
            "ts_focus_ms" to jsString(focusMs()), "ts_token_ms" to jsString(result?.tokenMs ?: 0))
        if (truthy(config.test)) fields.ts_test = "1"
        fields
    }
    fun post(pre: dynamic, post: dynamic, user: dynamic, content: dynamic, reply: dynamic, captcha: dynamic, turnstile: dynamic) = jsPromise {
        val icon = (content.dccons.length as Int) > 0
        val doubleTxtcon = truthy(content.txtconDouble)
        val txtcon = truthy(content.txtcon)
        val iconComment = icon || txtcon || doubleTxtcon
        val rcKey = randomKey()
        val send: (dynamic) -> dynamic = { token ->
            val fields = js("Object").assign(props("user" to user), reply,
                props("captcha" to captcha, "grecaptchaToken" to token, "turnstile" to turnstile))
            if (doubleTxtcon) s.submitTxtcon(pre, post, js("Object").assign(fields, props("double" to content.txtconDouble)))
            else if (txtcon) s.submitTxtcon(pre, post, js("Object").assign(fields, props("text" to content.text, "colors" to content.txtcon)))
            else s.submitComment(pre, post, js("Object").assign(fields, props("memo" to if (icon) content.dccons else content.text,
                "bigDccon" to (icon && content.bigDccon == true), "rcKey" to rcKey)))
        }
        val challenge = if (iconComment) props("action" to "insert_icon") else props("action" to "comment_submit", "rcKey" to rcKey)
        val response = sendWithChallenge(send, challenge)
        val result = response.result as String
        if (if (iconComment) result == "ok" else isPosted(response)) return@jsPromise props("ok" to true)
        if (response.message == "captcha") return@jsPromise props("ok" to false, "captcha" to true)
        val message = if (result == "false") s.resultMessage(response) else failures[result] ?: (if (icon || doubleTxtcon) iconFailures[result] else null)
        props("ok" to false, "captcha" to false, "message" to if (truthy(message)) message else "댓글을 작성하지 못했습니다.")
    }
    fun delete(pre: dynamic, post: dynamic, id: String, password: String) = jsPromise {
        val rc = randomKey()
        val request: (dynamic) -> dynamic = { token -> s.userDeleteComment(pre, post, id, password, rc, token) }
        val result = sendWithChallenge(request, props("action" to "comment_delete_submit", "rcKey" to rc))
        if (result.result == "true") props("success" to true) else props("success" to false,
            "message" to if (result.message == "captcha") "자동입력 방지 확인이 필요합니다. 원문에서 삭제해 주세요."
            else s.resultMessage(result))
    }
    return props("isCommentPosted" to ::isPosted, "prefetchTurnstile" to ::prefetch, "turnstileFields" to ::makeFields,
        "postComment" to ::post, "deleteOwnComment" to ::delete)
}
