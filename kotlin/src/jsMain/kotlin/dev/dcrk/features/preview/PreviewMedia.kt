package dev.dcrk

fun isBrokenMedia(image: dynamic): Boolean =
    (image.complete as Boolean) && (image.naturalWidth as Int) == 0

/** 본문 이미지와 디시콘의 키보드 버튼 역할·접근성 문구를 결정한다. */
fun previewMediaLabel(
    isDccon: Boolean, inLink: Boolean, broken: Boolean, imageViewer: Boolean, hasPop: Boolean
): String? = when {
    isDccon -> "디시콘 정보"
    inLink -> null
    imageViewer && !broken -> "이미지 크게 보기"
    hasPop -> "원본 보기"
    else -> null
}

fun markPreviewMedia(media: dynamic, imageViewer: Boolean) {
    val isDccon = media.matches(".written_dccon") as Boolean
    val inLink = media.closest("a") != null
    val isImage = media.tagName == "IMG"
    val broken = isImage && isBrokenMedia(media)
    val pop: dynamic = media.dataset.pop
    val hasPop = pop != null && pop != ""
    val label = previewMediaLabel(isDccon, inLink, broken, imageViewer, hasPop)
    if (label != null) {
        media.tabIndex = 0
        media.setAttribute("role", "button")
        val alt = (media.getAttribute("alt") as String?)?.trim()
        media.setAttribute("aria-label", if (alt.isNullOrEmpty()) label else "$alt ($label)")
    } else {
        for (name in listOf("tabindex", "role", "aria-label")) media.removeAttribute(name)
    }
}

fun markPreviewPressable(root: dynamic, imageViewer: Boolean) {
    val media = root.querySelectorAll(".written_dccon, img")
    var index = 0
    while (index < (media.length as Int)) {
        markPreviewMedia(media[index], imageViewer)
        index++
    }
}
