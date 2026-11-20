package dev.dcrk

/** 차단·메모 설정의 키와 순서를 원본 6.2.2 그대로 유지한다. */
internal fun storageMetadata(): dynamic = props(
    "typeNames" to props(
        "NICK" to "닉네임", "ID" to "아이디", "IP" to "IP",
        "TITLE" to "제목", "TEXT" to "내용", "COMMENT" to "댓글",
        "DCCON" to "디시콘", "TAB" to "말머리"
    ),
    "detectModeNames" to props(
        "SAME" to "일치", "CONTAIN" to "포함",
        "NOT_SAME" to "불일치", "NOT_CONTAIN" to "불포함"
    ),
    "memoTypeNames" to props("UID" to "아이디", "NICK" to "닉네임", "IP" to "IP"),
    "defaultDetectMode" to props(
        "NICK" to "SAME", "ID" to "SAME", "IP" to "SAME",
        "TITLE" to "CONTAIN", "TEXT" to "CONTAIN", "COMMENT" to "CONTAIN",
        "DCCON" to "SAME", "TAB" to "SAME"
    )
)
