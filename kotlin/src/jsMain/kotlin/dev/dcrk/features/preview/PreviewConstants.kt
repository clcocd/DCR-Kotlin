package dev.dcrk

/** 원본 6.2.2의 디시 토큰 사이트 키. 개인 패치를 적용하지 않는다. */
internal const val ICON_SITE_KEY = "6Lc-Fr0UAAAAAOdqLYqPy53MxlRMIXpNXFvBliwI"

internal fun commentSiteKeys(): dynamic = props(
    "new" to "6LfrAuMtAAAAABXdEPC0ZSItQjLjlt2lC4jo3VNX",
    "new3" to "6LcjA-MtAAAAAIdgo8TKvID6HS34Ac9AkzRjuJfY"
)

/** 정수 모양 키의 JS 열거 순서와 원본 라벨을 유지한다. */
internal fun blockDays(): dynamic = props(
    "1" to "1시간", "6" to "6시간", "24" to "1일",
    "168" to "7일", "336" to "14일", "744" to "31일"
)

/** 마지막의 "0"은 사용자 직접 입력 사유다. */
internal fun blockReasons(): Array<Array<String>> = arrayOf(
    arrayOf("1", "음란성"), arrayOf("2", "광고"),
    arrayOf("3", "욕설"), arrayOf("4", "도배"),
    arrayOf("5", "혐오 콘텐츠"), arrayOf("6", "저작권 침해"),
    arrayOf("7", "명예훼손"), arrayOf("0", "직접 입력")
)

internal val txtconBackgrounds = arrayOf("3b4890", "b4b4e1", "f5e1f0", "d2f0e6", "ffffff", "333333")
internal val txtconColors = arrayOf("ffffff", "333333")
