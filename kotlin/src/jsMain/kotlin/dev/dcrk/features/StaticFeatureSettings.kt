package dev.dcrk

/** 원본 6.2.2 기능 설정 기본값. 메타데이터 타입과 WXT 페이지 일치 규칙은 TS 경계에서 유지한다. */
internal fun staticFeatureSettings(id: String): dynamic = when (id) {
    "block" -> props(
        "replyRemove" to props(
            "type" to "check",
            "default" to false
        ),
        "blur" to props(
            "type" to "check",
            "default" to false
        ),
        "blurReveal" to props(
            "type" to "check",
            "default" to true
        ),
        "blurStrength" to props(
            "type" to "range",
            "default" to 5,
            "min" to 1,
            "max" to 20,
            "step" to 1,
            "unit" to "px"
        ),
        "foldDuplicate" to props(
            "type" to "check",
            "default" to false
        ),
        "duplicateCount" to props(
            "type" to "range",
            "default" to 3,
            "min" to 2,
            "max" to 10,
            "step" to 1,
            "unit" to "번"
        ),
        "duplicateMinLength" to props(
            "type" to "range",
            "default" to 5,
            "min" to 1,
            "max" to 50,
            "step" to 1,
            "unit" to "자"
        )
    )
    "fonts" -> props(
        "customFonts" to props(
            "type" to "text",
            "default" to DEFAULT_FONTS
        ),
        "changeDCFont" to props(
            "type" to "check",
            "default" to true
        ),
        "bodyFontSize" to props(
            "type" to "range",
            "default" to 13,
            "min" to 5,
            "max" to 30,
            "step" to 1,
            "unit" to "px"
        )
    )
    "preview" -> props(
        "previewWidth" to props(
            "type" to "range",
            "default" to 1200,
            "min" to 700,
            "max" to 1600,
            "step" to 50,
            "unit" to "px"
        ),
        "toggleBackgroundBlur" to props(
            "type" to "check",
            "default" to true
        ),
        "popupBlur" to props(
            "type" to "check",
            "default" to true
        ),
        "scrollToSkip" to props(
            "type" to "check",
            "default" to true
        ),
        "reversePreviewKey" to props(
            "type" to "check",
            "default" to false
        ),
        "longPressDelay" to props(
            "type" to "range",
            "default" to 300,
            "min" to 200,
            "max" to 2000,
            "step" to 50,
            "unit" to "ms"
        ),
        "colorPreviewLink" to props(
            "type" to "check",
            "default" to true
        ),
        "autoRefreshComment" to props(
            "type" to "check",
            "default" to false
        ),
        "highlightNewComments" to props(
            "type" to "check",
            "default" to true
        ),
        "commentRefreshInterval" to props(
            "type" to "range",
            "default" to 10000,
            "min" to 3000,
            "max" to 20000,
            "step" to 100,
            "unit" to "ms"
        ),
        "expandRecognizeRange" to props(
            "type" to "check",
            "default" to false
        ),
        "imageViewer" to props(
            "type" to "check",
            "default" to true
        ),
        "listKeyboard" to props(
            "type" to "check",
            "default" to true
        ),
        "disableCache" to props(
            "type" to "check",
            "default" to false
        ),
        "archiveArticle" to props(
            "type" to "check",
            "default" to false
        ),
        "blockImage" to props(
            "type" to "check",
            "default" to false
        )
    )
    "refresh" -> props(
        "refreshRate" to props(
            "type" to "range",
            "default" to 5000,
            "min" to 3000,
            "max" to 20000,
            "step" to 100,
            "unit" to "ms"
        ),
        "fadeIn" to props(
            "type" to "check",
            "default" to true
        ),
        "useBetterBrowse" to props(
            "type" to "check",
            "default" to true
        ),
        "noRefreshOnSearch" to props(
            "type" to "check",
            "default" to true
        ),
        "pauseOnHover" to props(
            "type" to "check",
            "default" to false
        ),
        "titleCount" to props(
            "type" to "check",
            "default" to true
        ),
        "readMark" to props(
            "type" to "check",
            "default" to true
        ),
        "backgroundRefresh" to props(
            "type" to "check",
            "default" to false
        ),
        "backgroundRefreshRate" to props(
            "type" to "range",
            "default" to 30000,
            "min" to 10000,
            "max" to 120000,
            "step" to 1000,
            "unit" to "ms"
        ),
        "doNotColorVisited" to props(
            "type" to "check",
            "default" to false
        )
    )
    "search" -> props(
        "maxSearches" to props(
            "type" to "range",
            "default" to 10,
            "min" to 1,
            "max" to 30,
            "step" to 1,
            "unit" to "회"
        )
    )
    "write" -> props(
        "preventExit" to props(
            "type" to "check",
            "default" to true
        ),
        "webpConvert" to props(
            "type" to "check",
            "default" to false
        ),
        "webpQuality" to props(
            "type" to "range",
            "default" to 80,
            "min" to 10,
            "max" to 100,
            "step" to 5,
            "unit" to "%"
        ),
        "webpConcurrency" to props(
            "type" to "range",
            "default" to 2,
            "min" to 1,
            "max" to 8,
            "step" to 1,
            "unit" to "장"
        ),
        "obfuscateName" to props(
            "type" to "check",
            "default" to false
        )
    )
    else -> error("지원하지 않는 기능 설정: $id")
}
