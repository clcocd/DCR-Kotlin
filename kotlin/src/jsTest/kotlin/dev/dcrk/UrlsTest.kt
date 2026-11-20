package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UrlsTest {
    private val view = "https://gall.dcinside.com/mgallery/board/view/?id=test&no=1"

    @Test
    fun galleryAndManagerMapping() {
        assertEquals("minor", galleryKind(view))
        assertEquals("mgallery/", galleryPath(view))
        assertEquals("M", galltypeOf(view))
        assertEquals("MI", galltypeOf("https://gall.dcinside.com/mini/board/lists"))
        assertEquals("PR", galltypeOf("https://gall.dcinside.com/person/board/lists"))
        assertEquals("G", galltypeOf("https://gall.dcinside.com/board/lists"))
        assertEquals("https://gall.dcinside.com/ajax/minor_manager_board_ajax/delete_list",
            managerAjaxUrl(view, "delete_list"))
        assertEquals("normal", galleryKind("https://gall.dcinside.com/board/lists?s_keyword=/mgallery/"))
    }

    @Test
    fun listAndMergeQueries() {
        assertEquals("https://gall.dcinside.com/board/lists?id=test&s_type=search_all",
            listUrl("https://gall.dcinside.com/board/view/?no=5&t=cv&page=1&id=test&s_type=search_all"))
        assertEquals("https://gall.dcinside.com/board/lists?id=test&page=2",
            listUrl("https://gall.dcinside.com/board/lists/?page=2&id=test"))
        assertEquals("?id=a&no=5&page=2",
            mergeParamURL("https://gall.dcinside.com/board/view/?id=a&no=5&page=1",
                "https://gall.dcinside.com/board/lists/?id=a&page=2"))
    }

    @Test
    fun safeLinks() {
        assertEquals("https://gall.dcinside.com/", dcinsideHref("https://gall.dcinside.com/"))
        assertNull(dcinsideHref("https://dcinside.com.evil.com/"))
        assertNull(dcinsideHref("javascript:alert(1)"))
    }

    @Test
    fun hashReferenceVectors() {
        assertEquals(3338908027751811.0, hash53(""))
        assertEquals(5059922895146125.0, hash53("abc"))
        assertEquals(6336466587208215.0, hash53("한글"))
        assertEquals(4725715722941614.0, hash53("😀"))
    }
}
