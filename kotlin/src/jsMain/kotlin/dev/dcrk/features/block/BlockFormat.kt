package dev.dcrk.block

import dev.dcrk.*

import kotlin.js.JSON

/** 원본 composeExtra의 표시 순서와 모드 이름을 유지한다. */
fun composeExtra(isRegex: Boolean, gallery: String?, mode: String?): String {
    val parts = mutableListOf<String>()
    if (isRegex) parts.add("[정규식]")
    if (!gallery.isNullOrEmpty()) parts.add("[갤러리: $gallery]")
    if (!mode.isNullOrEmpty()) {
        val label = when (mode) {
            "SAME" -> "일치"
            "CONTAIN" -> "포함"
            "NOT_SAME" -> "불일치"
            "NOT_CONTAIN" -> "불포함"
            else -> "undefined"
        }
        parts.add("[$label]")
    }
    return parts.joinToString(" ")
}

/** content와 gallery의 2항목 JSON 튜플. isRegex와 관계없이 같은 키를 공유한다. */
fun blockKey(content: String, gallery: String?): String =
    JSON.stringify(arrayOf(content, gallery ?: ""))
