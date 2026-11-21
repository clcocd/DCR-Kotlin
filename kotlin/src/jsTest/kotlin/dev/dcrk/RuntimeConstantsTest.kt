package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeConstantsTest {
    @Test
    fun endpointsAndSelectorsStayOriginal() {
        val all = runtimeConstants()
        val urls = all.urls
        assertEquals("https://gall.dcinside.com/", urls.base as String)
        assertEquals("https://gall.dcinside.com/board/recommend/vote", urls.vote as String)
        assertEquals("https://gall.dcinside.com/board/comment/", urls.comments as String)
        assertEquals("https://gall.dcinside.com/board/forms/comment_submit", urls.comments_submit as String)
        assertEquals("https://gall.dcinside.com/dccon/insert_icon", urls.dccon_comments_submit as String)
        assertEquals("https://gall.dcinside.com/txtcon/insert", urls.txtcon_submit as String)
        assertEquals("https://gall.dcinside.com/board/comment/comment_delete_submit", urls.comment_remove as String)
        assertEquals("https://gall.dcinside.com/dccon/lists", urls.dccon.lists as String)
        assertEquals("https://gall.dcinside.com/dccon/package_detail", urls.dccon.detail as String)
        assertEquals("https://gall.dcinside.com/dccon/buy", urls.dccon.buy as String)
        assertEquals("https://dcimg5.dcinside.com/dccon.php?no=", urls.dccon.image as String)
        assertEquals("https://dccon.dcinside.com/hot/1", urls.dccon.shop as String)
        assertEquals("https://dcrefresher.green1052.com/version", urls.database.version as String)
        assertEquals("https://dcrefresher.green1052.com/ip.json", urls.database.ip as String)
        assertEquals("rf_uid", all.writerParams.uid as String)
        assertEquals("rf_ip", all.writerParams.ip as String)
        assertEquals(".gall_list:not([id]) tbody", all.list.list as String)
        assertEquals(".left_content article:has(.gall_listwrap) .bottom_paging_box", all.list.paging as String)
        assertEquals(".ub-content, .search_comment", all.list.writerRow as String)
        assertEquals(".gall_list .ub-content", all.list.listRow as String)
    }

    @Test
    fun imageSearchAndIpFormatStayOriginal() {
        val all = runtimeConstants()
        assertEquals(
            """["*://*.dcinside.co.kr/viewimage.php*","*://*.dcinside.com/viewimage.php*"]""",
            js("JSON.stringify")(all.imageSearch.patterns) as String
        )
        assertEquals(2, (all.ip.format as Number).toInt())
    }

    @Test
    fun pagePatternsStayOriginal() {
        val p = runtimeConstants().pagePatterns
        assertEquals(true, p.board.test("/board/lists/?id=a") as Boolean)
        assertEquals(true, p.board.test("/board/view/?id=a") as Boolean)
        assertEquals(false, p.board.test("/board/write/?id=a") as Boolean)
        assertEquals(true, p.view.test("/board/view/?id=a") as Boolean)
        assertEquals(true, p.write.test("/board/modify/?id=a") as Boolean)
        assertEquals(true, p.list.test("/board/lists/?id=a") as Boolean)
    }

    @Test
    fun originalUiContractsArePreserved() {
        val all = runtimeConstants()
        assertEquals(".refresherBlur, .refresherLowActivityBlur", all.block.blurred as String)
        assertEquals(".refresherBlocked, .refresherLowActivityHide, .refresherBlur, .refresherLowActivityBlur", all.block.hidden as String)
        assertEquals("refresher:rowsHidden", all.block.rowsHiddenEvent as String)
        assertEquals("게시글 내용이 차단되었습니다.", all.block.blockedText as String)
        assertEquals("button.write", all.write.submit as String)
        assertEquals(".note-editable", all.write.editor as String)
        assertEquals("input#subject", all.write.subject as String)
        assertEquals("#auto_zzal_img_div, .wrt_guide_preview_inn", all.write.auto as String)
        assertEquals("#leave_confirm_box .btn_blue", all.write.leave as String)
        assertEquals("#clickbutton", all.write.posted as String)
        assertEquals("<table class=\"gall_list", all.refresh.table as String)
        assertEquals("em[class*=icon_notice]", all.refresh.notice as String)
        assertEquals("""["UID","MEMO","RATIO"]""", js("JSON.stringify")(all.defaultBadgeView.order) as String)
        assertEquals(true, all.defaultBadgeView.fixedUid as Boolean)
        assertEquals(true, all.defaultBadgeView.halfFixedUid as Boolean)
        assertEquals("none", all.defaultBadgeView.ipFilter as String)
    }
}
