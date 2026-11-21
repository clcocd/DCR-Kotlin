// @vitest-environment-options {"url": "https://gall.dcinside.com/board/write/?id=test"}
import {afterEach, expect, it} from "vitest";

import write from "@/features/write/index";
import meta from "@/features/write/meta";
import {WRITE_PAGE} from "@/core/pagePatterns";
import {runModule} from "../module";

afterEach(() => {
    document.body.innerHTML = "";
    delete document.documentElement.dataset.refresherUpload;
});

it("글자가 없어도 직접 첨부한 동영상·iframe·embed는 작성 중인 글로 취급한다", async () => {
    history.replaceState(null, "", "/board/write/?id=test");
    document.body.innerHTML = '<input id="subject"><div class="note-editable"></div>';
    const running = await runModule(write);
    try {
        const editor = document.querySelector(".note-editable")!;
        for (const html of ['<video src="v.mp4"></video>', '<iframe src="about:blank"></iframe>', '<embed src="a.bin">']) {
            editor.innerHTML = html;
            const ev = new Event("beforeunload", {cancelable: true});
            window.dispatchEvent(ev);
            expect(ev.defaultPrevented).toBe(true);
        }

        editor.innerHTML = '<div class="wrt_guide_preview_inn"><video src="a.mp4"></video></div>';
        const automatic = new Event("beforeunload", {cancelable: true});
        window.dispatchEvent(automatic);
        expect(automatic.defaultPrevented).toBe(false);
    } finally {
        running.stop();
    }
});

it("Kotlin 글쓰기 메타가 페이지 범위·설정 기본값을 유지한다", () => {
    expect(meta.id).toBe("write");
    expect(meta.defaultEnable).toBe(false);
    expect(meta.urls?.[0]).toBe(WRITE_PAGE);
    expect(meta.urls?.[1]?.test("/upload/image")).toBe(true);
    expect(meta.urls?.[1]?.test("/board/lists")).toBe(false);
    expect(meta.settings).toEqual({
        preventExit: {type: "check", default: true},
        webpConvert: {type: "check", default: false},
        webpQuality: {type: "range", default: 80, min: 10, max: 100, step: 5, unit: "%"},
        webpConcurrency: {type: "range", default: 2, min: 1, max: 8, step: 1, unit: "장"},
        obfuscateName: {type: "check", default: false}
    });
});
