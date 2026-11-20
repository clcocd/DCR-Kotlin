package dev.dcrk.http

import dev.dcrk.*

/** 디시 요청 본문의 갤러리 구분과 CSRF 필드를 원본과 같은 순서로 합친다. */
internal fun dcBody(link: String, fields: dynamic): dynamic {
    val body = js("Object.assign")(props("_GALLTYPE_" to galltypeOf(link)), fields)
    return csrfBody(body)
}
