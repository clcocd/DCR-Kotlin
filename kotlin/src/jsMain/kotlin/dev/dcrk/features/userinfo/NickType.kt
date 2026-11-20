package dev.dcrk

/** 원본 utils/user.ts의 닉콘 파일명 목록. 대소문자와 접미사를 그대로 비교한다. */
private val fixedIcons = setOf(
    "fix_managernik.gif",
    "fix_sub_managernik.gif",
    "nftcon_fix_manager.png",
    "w_app_gonick_manager_16.png",
    "bestcon_fix_manager.png",
    "fix_nik.gif",
    "nftcon_fix.png",
    "dc20th_wgallcon4.png",
    "w_app_gonick_16.png",
    "nftmdcon_fix.png",
    "gnftmdcon_fix.gif",
    "bestcon_fix.png",
    "fix_newnik.gif"
)

private val halfFixedIcons = setOf(
    "managernik.gif",
    "sub_managernik.gif",
    "nik.gif",
    "nftcon.png",
    "dc20th_wgallcon.png",
    "w_app_nogonick_16.png",
    "nftmdcon.png",
    "gnftmdcon.gif",
    "bestcon.png",
    "newnik.gif"
)

/** 등록되지 않은 닉콘은 원본과 같이 UNFIXED로 취급한다. */
fun nickType(iconUrl: String): String {
    val filename = iconUrl.substringAfterLast('/')
    return when {
        filename in fixedIcons -> "FIXED"
        filename in halfFixedIcons -> "HALF_FIXED"
        else -> "UNFIXED"
    }
}
