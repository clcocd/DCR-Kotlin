// @vitest-environment-options {"url": "https://gall.dcinside.com/board/write/?id=test"}
import {afterEach, expect, it} from "vitest";

import write from "@/features/write/index";
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
