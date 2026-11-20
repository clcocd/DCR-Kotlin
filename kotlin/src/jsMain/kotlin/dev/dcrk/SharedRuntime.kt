package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

private fun errorNamed(value: dynamic, name: String): Boolean =
    value != null && jsTypeOf(value) == "object" && value.name === name

internal fun errorMessage(value: dynamic): String =
    if (value != null && jsTypeOf(value) == "object" && jsTypeOf(value.message) == "string") value.message as String
    else js("String(value)") as String

internal fun createErrorUtilities(s: dynamic): dynamic {
    fun friendly(failure: dynamic): String {
        val message = errorMessage(failure)
        if (s.isHTTPError(failure) == true) {
            val status = failure.response.status as Int
            if (status == 403 || status == 429) return "요청이 많아 잠시 막혔습니다. 잠시 후 다시 시도해 주세요."
            return if (status >= 500) "서버가 불안정합니다. 잠시 후 다시 시도해 주세요." else "요청이 거절되었습니다. (HTTP $status)"
        }
        if (errorNamed(failure, "TimeoutError")) return "응답이 없습니다. 잠시 후 다시 시도해 주세요."
        if (s.isNetworkError(failure) == true || Regex("failed to fetch|networkerror", RegexOption.IGNORE_CASE).containsMatchIn(message)) {
            return "서버에 연결하지 못했습니다. 인터넷 연결을 확인하고 다시 시도해 주세요."
        }
        if (js("failure instanceof SyntaxError") == true) return "JSON 형식이 올바르지 않습니다."
        if (message.contains("MAX_WRITE_OPERATIONS")) return "짧은 시간에 너무 자주 저장했습니다. 잠시 후 다시 시도해 주세요."
        if (message.contains("quota", ignoreCase = true)) return "저장 공간이 부족합니다."
        return if (Regex("[가-힣]").containsMatchIn(message)) message else "잠시 후 다시 시도해 주세요."
    }
    fun save(write: dynamic, reload: () -> dynamic, label: String): Promise<dynamic> {
        val failure: (dynamic) -> dynamic = { error ->
            console.error(label, error)
            val log: (dynamic) -> Unit = { console.error(it) }
            val rethrow: () -> dynamic = { throw error }
            reload().catch(log).then(rethrow)
        }
        return write.catch(failure) as Promise<dynamic>
    }
    val timeout: (dynamic) -> Boolean = { errorNamed(it, "TimeoutError") }
    val abort: (dynamic) -> Boolean = { errorNamed(it, "AbortError") }
    return props("isTimeoutError" to timeout, "isAbortError" to abort, "messageOf" to ::errorMessage,
        "saveOrReload" to ::save, "friendlyMessage" to ::friendly)
}

internal fun createNotify(get: () -> dynamic): dynamic {
    fun report(error: dynamic, label: String, message: String) {
        console.error(label, error)
        get().showToast(message, "error")
    }
    fun notify(request: dynamic, done: String, fallback: String): dynamic {
        val success: (dynamic) -> dynamic = { result ->
            val ok = truthy(result.success)
            val message = result.message ?: if (ok) done else "처리하지 못했습니다. 로그인과 권한을 확인한 뒤 다시 시도해 주세요."
            get().showToast(message, if (ok) "info" else "error")
            result.success
        }
        val fail: (dynamic) -> Boolean = { error ->
            report(error, "Manage request failed:", fallback)
            false
        }
        return request.then(success).catch(fail)
    }
    return props("reportFailure" to ::report, "notifyManage" to ::notify)
}

internal fun originalEventTarget(event: dynamic): dynamic = event.composedPath()[0] ?: event.target

internal fun createTyping(overlay: dynamic, modalSelector: String): (dynamic) -> Boolean =  typing@{ event ->
    if (truthy(overlay.portal?.querySelector(modalSelector))) return@typing true
    val target = originalEventTarget(event)
    js("HTMLElement.prototype").isPrototypeOf(target) == true &&
        (target.tagName == "INPUT" || target.tagName == "TEXTAREA" || target.isContentEditable == true)
}
