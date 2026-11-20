package dev.dcrk

/** 확장의 런타임 URL·선택자·문구. 빌드용 WXT match/manifest 값은 별도 유지한다. */
internal fun runtimeConstants(): dynamic {
    val blurred = ".refresherBlur, .refresherLowActivityBlur"
    return props(
        "urls" to props(
            "base" to "https://gall.dcinside.com/",
            "vote" to "https://gall.dcinside.com/board/recommend/vote",
            "comments" to "https://gall.dcinside.com/board/comment/",
            "comments_submit" to "https://gall.dcinside.com/board/forms/comment_submit",
            "dccon_comments_submit" to "https://gall.dcinside.com/dccon/insert_icon",
            "txtcon_submit" to "https://gall.dcinside.com/txtcon/insert",
            "comment_remove" to "https://gall.dcinside.com/board/comment/comment_delete_submit",
            "dccon" to props(
                "lists" to "https://gall.dcinside.com/dccon/lists",
                "detail" to "https://gall.dcinside.com/dccon/package_detail",
                "buy" to "https://gall.dcinside.com/dccon/buy",
                "image" to "https://dcimg5.dcinside.com/dccon.php?no=",
                "shop" to "https://dccon.dcinside.com/hot/1"
            ),
            "database" to props(
                "version" to "https://dcrefresher.green1052.com/version",
                "ip" to "https://dcrefresher.green1052.com/ip.json"
            )
        ),
        "writerParams" to props("uid" to "rf_uid", "ip" to "rf_ip"),
        "list" to props(
            "list" to ".gall_list:not([id]) tbody",
            "paging" to ".left_content article:has(.gall_listwrap) .bottom_paging_box",
            "writerRow" to ".ub-content, .search_comment",
            "listRow" to ".gall_list .ub-content"
        ),
        "block" to props(
            "blurred" to blurred,
            "hidden" to ".refresherBlocked, .refresherLowActivityHide, $blurred",
            "rowsHiddenEvent" to "refresher:rowsHidden",
            "blockedText" to "게시글 내용이 차단되었습니다."
        ),
        "write" to props(
            "submit" to "button.write",
            "editor" to ".note-editable",
            "subject" to "input#subject",
            "auto" to "#auto_zzal_img_div, .wrt_guide_preview_inn",
            "leave" to "#leave_confirm_box .btn_blue",
            "posted" to "#clickbutton"
        ),
        "refresh" to props(
            "table" to "<table class=\"gall_list",
            "notice" to "em[class*=icon_notice]"
        ),
        "ui" to props(
            "focusRing" to "cursor-pointer rounded-sm outline-none focus-visible:ring-3 focus-visible:ring-ring/50",
            "modalOverlaySelector" to "[data-slot=dialog-overlay], [data-slot=alert-dialog-overlay]",
            "saveFailed" to "저장하지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요."
        ),
        "messaging" to props(
            "listReplacedEvent" to "refresher:list-replaced",
            "turnstileHost" to "data-refresher-turnstile",
            "turnstileCancelEvent" to "refresher-turnstile-cancel"
        ),
        "storage" to props(
            "blockDefaultsKey" to "local:refresher:block:defaults",
            "modulesKey" to "local:refresher:modules",
            "listLocks" to props(
                "blocks" to "refresher:blocks",
                "memos" to "refresher:memos",
                "moduleSettings" to "refresher:module-settings"
            ),
            "dbMetaKey" to "local:refresher:db:meta",
            "dbKeys" to props("ip" to "local:refresher:db:ip")
        ),
        "defaultBadgeView" to props(
            "order" to arrayOf("UID", "MEMO", "RATIO"),
            "fixedUid" to true, "halfFixedUid" to true, "ipFilter" to "none"
        )
    )
}
