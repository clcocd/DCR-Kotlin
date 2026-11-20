package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class StaticFeatureSettingsTest {
    @Test
    fun originalSchemaDefaultsAndOrderArePreserved() {
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("""{"replyRemove":{"type":"check","default":false},"blur":{"type":"check","default":false},"blurReveal":{"type":"check","default":true},"blurStrength":{"type":"range","default":5,"min":1,"max":20,"step":1,"unit":"px"},"foldDuplicate":{"type":"check","default":false},"duplicateCount":{"type":"range","default":3,"min":2,"max":10,"step":1,"unit":"번"},"duplicateMinLength":{"type":"range","default":5,"min":1,"max":50,"step":1,"unit":"자"}}""", stringify(staticFeatureSettings("block")) as String)
        assertEquals("""{"customFonts":{"type":"text","default":"Noto Sans CJK KR, NanumGothic"},"changeDCFont":{"type":"check","default":true},"bodyFontSize":{"type":"range","default":13,"min":5,"max":30,"step":1,"unit":"px"}}""", stringify(staticFeatureSettings("fonts")) as String)
        assertEquals("""{"previewWidth":{"type":"range","default":1200,"min":700,"max":1600,"step":50,"unit":"px"},"toggleBackgroundBlur":{"type":"check","default":true},"popupBlur":{"type":"check","default":true},"scrollToSkip":{"type":"check","default":true},"reversePreviewKey":{"type":"check","default":false},"longPressDelay":{"type":"range","default":300,"min":200,"max":2000,"step":50,"unit":"ms"},"colorPreviewLink":{"type":"check","default":true},"autoRefreshComment":{"type":"check","default":false},"highlightNewComments":{"type":"check","default":true},"commentRefreshInterval":{"type":"range","default":10000,"min":3000,"max":20000,"step":100,"unit":"ms"},"expandRecognizeRange":{"type":"check","default":false},"imageViewer":{"type":"check","default":true},"listKeyboard":{"type":"check","default":true},"disableCache":{"type":"check","default":false},"archiveArticle":{"type":"check","default":false},"blockImage":{"type":"check","default":false}}""", stringify(staticFeatureSettings("preview")) as String)
        assertEquals("""{"refreshRate":{"type":"range","default":5000,"min":3000,"max":20000,"step":100,"unit":"ms"},"fadeIn":{"type":"check","default":true},"useBetterBrowse":{"type":"check","default":true},"noRefreshOnSearch":{"type":"check","default":true},"pauseOnHover":{"type":"check","default":false},"titleCount":{"type":"check","default":true},"readMark":{"type":"check","default":true},"backgroundRefresh":{"type":"check","default":false},"backgroundRefreshRate":{"type":"range","default":30000,"min":10000,"max":120000,"step":1000,"unit":"ms"},"doNotColorVisited":{"type":"check","default":false}}""", stringify(staticFeatureSettings("refresh")) as String)
        assertEquals("""{"maxSearches":{"type":"range","default":10,"min":1,"max":30,"step":1,"unit":"회"}}""", stringify(staticFeatureSettings("search")) as String)
        assertEquals("""{"preventExit":{"type":"check","default":true},"webpConvert":{"type":"check","default":false},"webpQuality":{"type":"range","default":80,"min":10,"max":100,"step":5,"unit":"%"},"webpConcurrency":{"type":"range","default":2,"min":1,"max":8,"step":1,"unit":"장"},"obfuscateName":{"type":"check","default":false}}""", stringify(staticFeatureSettings("write")) as String)
    }
}
