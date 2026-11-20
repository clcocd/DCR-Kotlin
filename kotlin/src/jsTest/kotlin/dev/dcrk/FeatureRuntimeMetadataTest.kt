package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class FeatureRuntimeMetadataTest {
    @Test
    fun originalEngineSelectorsAndUserInfoChoicesRemainUnchanged() {
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("""{"saucenao":{"name":"SauceNAO","url":"https://saucenao.com/search.php?url="},"googleLens":{"name":"Google Lens","url":"https://lens.google.com/uploadbyurl?url="},"yandex":{"name":"Yandex","url":"https://yandex.com/images/search?rpt=imageview&url="},"ascii2d":{"name":"ascii2d","url":"https://ascii2d.net/search/url/"},"tineye":{"name":"TinEye","url":"https://tineye.com/search?url="},"iqdb":{"name":"IQDB","url":"https://iqdb.org/?url="},"tracemoe":{"name":"trace.moe","url":"https://trace.moe/?url="}}""", stringify(imageSearchEngines()) as String)
        assertEquals("""{"hideGalleryView":{"selector":".issue_wrap, #visit_history"},"hideUselessView":{"selector":"section.right_content article"},"hideGalleryImage":{"selector":"#zzbang_div"},"removeNotice":{"selector":"tr:has(em[class*=icon_notice])"},"removeDCNotice":{"selector":"tr[class*=ub-content]:has(> td[user_name=운영자]), tr.ub-content:has(> .ub-writer[data-nick=운영자][data-uid=\"\"][data-ip=\"\"])"},"removeGamemeca":{"selector":"tr[data-type=icon_fnews], tr.ub-content:has(> .ub-writer[data-uid=\"gamemeca\"])"},"removeAi":{"selector":"tr[data-type=icon_ai]"}}""", stringify(layoutHideOptions()) as String)
        assertEquals("""{"badgeColors":{"uid":"#999999","ratio":"#999999","ratioAlarm":"#ff0000","korea":"#6495ed","japan":"#e5484d","china":"#f76b15","foreign":"#12a594","vpn":"#8e4ec6"},"ipInfoFilters":{"all":"전체","foreign":"해외, VPN만","vpn":"VPN만","none":"표시 안 함"},"lowActivityActions":{"none":"배지 색만","tag":"[깡계] 표시","blur":"흐리게","hide":"숨기기"}}""", stringify(userInfoRuntimeOptions()) as String)
    }
}
