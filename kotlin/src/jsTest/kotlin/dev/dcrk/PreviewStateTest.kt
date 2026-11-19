package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreviewStateTest {
    @Test
    fun validatesHistoryStateAndPreData() {
        val saved: dynamic = js("""({
            refresher:1,doc:1,
            preData:{
                gallery:"test",id:"1",title:"글",
                link:"https://gall.dcinside.com/board/view/?id=test&no=1",
                notice:false,recommend:false,type:"G",commentCount:2
            },
            back:{title:"목록",url:"/list",state:null},depth:2,reopen:true
        })""")
        val valid = previewEntry(saved)
        assertEquals(1, valid.refresher as Int)
        assertEquals(2, valid.depth as Int)
        assertEquals("test", valid.preData.gallery as String)
        assertTrue(valid.reopen as Boolean)
        assertNull(previewEntry(js("({from:'dc'})")))
        assertNull(previewEntry(js("({refresher:1,doc:'1'})")))

        saved.preData.link = "javascript:alert(1)"
        assertEquals(null, validPreDataPlaceholder(previewEntry(saved).preData))
        saved.preData.link = "https://gall.dcinside.com/board/view/?id=test&no=1"
        saved.preData.commentCount = js("NaN")
        assertEquals(null, validPreDataPlaceholder(previewEntry(saved).preData))
    }

    /** Kotlin/JS의 undefined를 테스트용 null과 일관되게 비교한다. */
    private fun validPreDataPlaceholder(value: dynamic): String? =
        if (value == null) null else "present"

    @Test
    fun stripsBotCommentsAndNormalizesDeletedFlag() {
        val raw: dynamic = js("""([
            {no:"1",nicktype:"ANON",is_delete:"0",del_yn:"N"},
            {no:"2",nicktype:"COMMENT_BOY",is_delete:"0"},
            {no:"3",nicktype:"ANON",is_delete:"2"},
            {no:"4",nicktype:"ANON",is_delete:"0",del_yn:"Y"}
        ])""")
        val list = normalizeIncomingComments(raw)
        assertEquals(3, list.length as Int)
        assertEquals("1", list[0].no as String)
        assertEquals("0", list[0].is_delete as String)
        assertEquals("1", list[1].is_delete as String)
        assertEquals("1", list[2].is_delete as String)
        assertEquals("0", raw[0].is_delete as String)
    }
}
