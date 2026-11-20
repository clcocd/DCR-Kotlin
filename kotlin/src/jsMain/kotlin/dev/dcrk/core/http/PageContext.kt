package dev.dcrk

/** 문서가 최초로 로드된 URL을 기준으로 페이지 상태를 한 번만 결정한다. */
internal fun pageContextOf(url: dynamic, viewPage: dynamic): dynamic {
    val isView = viewPage.test(url.pathname) as Boolean
    return props(
        "documentUrl" to url,
        "isViewPage" to isView,
        "pagePostNo" to if (isView) url.searchParams.get("no") else null,
        "pageGallery" to url.searchParams.get("id")
    )
}

internal fun currentPageContext(viewPage: dynamic): dynamic =
    pageContextOf(loadedDocumentUrl(), viewPage)
