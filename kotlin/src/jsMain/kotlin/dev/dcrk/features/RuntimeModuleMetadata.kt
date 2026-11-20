package dev.dcrk

/** WXT 모듈 메타에서 사용하는 런타임 데이터. 원본 이름·주소·순서를 유지한다. */
internal fun imageSearchEngines(): dynamic = props(
    "saucenao" to props(
        "name" to "SauceNAO",
        "url" to "https://saucenao.com/search.php?url="
    ),
    "googleLens" to props(
        "name" to "Google Lens",
        "url" to "https://lens.google.com/uploadbyurl?url="
    ),
    "yandex" to props(
        "name" to "Yandex",
        "url" to "https://yandex.com/images/search?rpt=imageview&url="
    ),
    "ascii2d" to props(
        "name" to "ascii2d",
        "url" to "https://ascii2d.net/search/url/"
    ),
    "tineye" to props(
        "name" to "TinEye",
        "url" to "https://tineye.com/search?url="
    ),
    "iqdb" to props(
        "name" to "IQDB",
        "url" to "https://iqdb.org/?url="
    ),
    "tracemoe" to props(
        "name" to "trace.moe",
        "url" to "https://trace.moe/?url="
    )
)

internal fun layoutHideOptions(): dynamic = props(
    "hideGalleryView" to props(
        "selector" to ".issue_wrap, #visit_history"
    ),
    "hideUselessView" to props(
        "selector" to "section.right_content article"
    ),
    "hideGalleryImage" to props(
        "selector" to "#zzbang_div"
    ),
    "removeNotice" to props(
        "selector" to "tr:has(em[class*=icon_notice])"
    ),
    "removeDCNotice" to props(
        "selector" to "tr[class*=ub-content]:has(> td[user_name=운영자]), tr.ub-content:has(> .ub-writer[data-nick=운영자][data-uid=\"\"][data-ip=\"\"])"
    ),
    "removeGamemeca" to props(
        "selector" to "tr[data-type=icon_fnews], tr.ub-content:has(> .ub-writer[data-uid=\"gamemeca\"])"
    ),
    "removeAi" to props(
        "selector" to "tr[data-type=icon_ai]"
    )
)

internal fun userInfoRuntimeOptions(): dynamic = props(
    "badgeColors" to props(
        "uid" to "#999999",
        "ratio" to "#999999",
        "ratioAlarm" to "#ff0000",
        "korea" to "#6495ed",
        "japan" to "#e5484d",
        "china" to "#f76b15",
        "foreign" to "#12a594",
        "vpn" to "#8e4ec6"
    ),
    "ipInfoFilters" to props(
        "all" to "전체",
        "foreign" to "해외, VPN만",
        "vpn" to "VPN만",
        "none" to "표시 안 함"
    ),
    "lowActivityActions" to props(
        "none" to "배지 색만",
        "tag" to "[깡계] 표시",
        "blur" to "흐리게",
        "hide" to "숨기기"
    )
)


/** 원본 메타에서 JS Object.fromEntries가 만든 기본값 스키마를 Kotlin에서 생성한다. */
internal fun imageSearchSettings(ids: Array<String>): dynamic {
    val settings: dynamic = props()
    for (id in ids) settings[id] = props("type" to "check", "default" to (id == "saucenao"))
    return settings
}

/** 레이아웃 모듈의 6.2.2 설정 순서·범위를 그대로 생성한다. */
internal fun layoutSettings(ids: Array<String>): dynamic {
    val settings: dynamic = props(
        "activePixel" to props(
            "type" to "range", "default" to 900, "min" to 100, "max" to 3840, "step" to 1, "unit" to "px"
        ),
        "forceCompact" to props("type" to "check", "default" to false),
        "useCompactModeOnView" to props("type" to "check", "default" to true)
    )
    for (id in ids) settings[id] = props("type" to "check", "default" to false)
    settings.pushToRight = props("type" to "check", "default" to false)
    return settings
}

internal fun userInfoColorSettings(colors: dynamic): dynamic {
    val settings: dynamic = props()
    for (entry in entriesOf(colors)) {
        val key = entry[0] as String
        val color = entry[1] as String
        settings["${key}Color"] = props("type" to "color", "default" to color)
    }
    return settings
}

/** 원본 6.2.2 유저 정보 설정을 배지 색 순서까지 보존해 생성한다. */
internal fun userInfoSettings(colors: dynamic, filters: dynamic, actions: dynamic): dynamic {
    val settings: dynamic = props(
        "showFixedNickUID" to props("type" to "check", "default" to true),
        "showHalfFixedNickUID" to props("type" to "check", "default" to true),
        "ipInfoFilter" to props("type" to "option", "default" to "all", "items" to filters),
        "checkRatio" to props("type" to "check", "default" to false),
        "alarmRatio" to props("type" to "range", "default" to 0, "min" to 0, "max" to 5000, "step" to 10, "unit" to "개"),
        "lowActivityAction" to props("type" to "option", "default" to "tag", "items" to actions)
    )
    for (entry in entriesOf(userInfoColorSettings(colors))) settings[entry[0] as String] = entry[1]
    settings.badgeOrder = props(
        "type" to "order",
        "items" to props("UID" to "아이디/IP", "MEMO" to "메모", "RATIO" to "글댓비"),
        "default" to arrayOf("UID", "MEMO", "RATIO")
    )
    return settings
}

internal fun fontExtensionPageVars(settings: dynamic): dynamic =
    props("--refresher-font" to fontFamilyOf(settings.customFonts as String))
