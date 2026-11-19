// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {afterEach, expect, it} from "vitest";

import fonts from "@/features/fonts/index";
import {runModule, type Running} from "../module";

let running: Running<void> | undefined;
const style = (): HTMLStyleElement | null => document.querySelector("style#refresher-fonts");

afterEach(() => {
    running?.stop();
    running = undefined;
    style()?.remove();
});

it("글꼴 setup, 설정 변경, 종료 처리가 Kotlin 모듈 안에서 동작한다", async () => {
    running = await runModule(fonts, {customFonts: "Gulim", bodyFontSize: 14, changeDCFont: false});
    const first = style();
    expect(first?.textContent).toContain('--refresher-font: "Gulim", sans-serif;');
    expect(first?.textContent).not.toContain(".write_div");

    running.change({customFonts: "Batang", bodyFontSize: 18, changeDCFont: true});
    expect(style()).toBe(first);
    expect(style()?.textContent).toContain('--refresher-font: "Batang", sans-serif;');
    expect(style()?.textContent).toContain(".write_div { font-size: 18px; }");

    running.stop();
    running = undefined;
    expect(style()).toBeNull();
});
