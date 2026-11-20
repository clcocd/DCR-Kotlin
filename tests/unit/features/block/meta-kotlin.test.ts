// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {expect, it} from "vitest";

import {BOARD_PAGE} from "@/core/pages";
import meta, {settings} from "@/features/block/meta";

it("Kotlin 차단 모듈이 원본 URL·단축키·팝업 토글을 유지한다", () => {
    expect(meta.id).toBe("block");
    expect(meta.urls?.[0]).toBe(BOARD_PAGE);
    expect(meta.toggles).toEqual(["reveal"]);
    expect(meta.commands).toEqual({blockReveal: {}});
    expect(meta.settings).toBe(settings);
    expect(Object.keys(settings)).toEqual([
        "replyRemove", "blur", "blurReveal", "blurStrength",
        "foldDuplicate", "duplicateCount", "duplicateMinLength"
    ]);
    expect(settings.replyRemove).toEqual({type: "check", default: false});
    expect(settings.blurReveal).toEqual({type: "check", default: true});
    expect(settings.blurStrength).toEqual({type: "range", default: 5, min: 1, max: 20, step: 1, unit: "px"});
    expect(settings.duplicateCount).toEqual({type: "range", default: 3, min: 2, max: 10, step: 1, unit: "번"});
});
