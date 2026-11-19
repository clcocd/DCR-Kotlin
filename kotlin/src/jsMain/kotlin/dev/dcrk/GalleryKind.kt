@file:OptIn(ExperimentalJsExport::class)

package dev.dcrk

/**
 * 원본 core/http/urls.ts의 galleryKind가 반환하는 이름을
 * URL pathname에 대해 동일하게 판정하는 첫 Kotlin 포팅 단위.
 */
@JsExport
fun galleryKindFromPath(pathname: String): String = when {
    pathname.startsWith("/mgallery/") -> "minor"
    pathname.startsWith("/mini/") -> "mini"
    pathname.startsWith("/person/") -> "person"
    else -> "normal"
}

fun main() {
    // Kotlin/JS 빌드의 실사용 연결 전에 실행 컨텍스트만 검증한다.
    galleryKindFromPath("/")
}
