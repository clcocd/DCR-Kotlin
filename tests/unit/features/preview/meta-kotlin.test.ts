// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {expect, it} from "vitest";

import {BOARD_PAGE} from "@/core/pages";
import meta, {settings} from "@/features/preview/meta";

it("Kotlin 미리보기 메타에서 v5 호환 설정 키와 기본값을 그대로 유지한다", () => {
    expect(meta.id).toBe("preview");
    expect(meta.urls?.[0]).toBe(BOARD_PAGE);
    expect(meta.settings).toBe(settings);
    expect(Object.keys(settings)).toEqual([
        "previewWidth", "toggleBackgroundBlur", "popupBlur", "scrollToSkip",
        "reversePreviewKey", "longPressDelay", "colorPreviewLink",
        "autoRefreshComment", "highlightNewComments", "commentRefreshInterval",
        "expandRecognizeRange", "imageViewer", "listKeyboard", "disableCache",
        "archiveArticle", "blockImage"
    ]);
    expect(settings.previewWidth).toEqual({
        type: "range", default: 1200, min: 700, max: 1600, step: 50, unit: "px"
    });
    expect(settings.longPressDelay).toEqual({
        type: "range", default: 300, min: 200, max: 2000, step: 50, unit: "ms"
    });
    expect(settings.commentRefreshInterval).toEqual({
        type: "range", default: 10000, min: 3000, max: 20000, step: 100, unit: "ms"
    });
    expect(settings.autoRefreshComment).toEqual({type: "check", default: false});
    expect(settings.archiveArticle).toEqual({type: "check", default: false});
});
