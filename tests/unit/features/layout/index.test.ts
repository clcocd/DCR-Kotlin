// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {afterEach, expect, it, vi} from "vitest";

import layout from "@/features/layout/index";
import meta from "@/features/layout/meta";
import {HIDE_KEYS, HIDE_OPTIONS, settings} from "@/features/layout/meta";

import {type Running, runModule} from "../module";

const html = document.documentElement;
const hideCss = (): string => document.querySelector("style#refresher-layout-hide")?.textContent ?? "";
const rule = (key: keyof typeof HIDE_OPTIONS): string => `${HIDE_OPTIONS[key].selector} { display: none !important; }`;

let running: Running<void> | undefined;

afterEach(() => {
    running?.stop();
    running = undefined;
    history.replaceState(null, "", "/board/lists/?id=test");
});

it("공지 모아보기에서는 공지 숨김 규칙만 빼고 나머지는 쓴다", async () => {
    const patch = {removeNotice: true, removeDCNotice: true, removeAi: true};
    running = await runModule(layout, patch);
    expect(hideCss()).toContain(rule("removeNotice"));
    expect(hideCss()).toContain(rule("removeDCNotice"));
    expect(hideCss()).toContain(rule("removeAi"));
    running.stop();

    history.replaceState(null, "", "/board/lists/?id=test&exception_mode=notice");
    running = await runModule(layout, patch);
    expect(hideCss()).toBe(rule("removeAi"));
});

it("본문 확장은 잡다 링크 숨기기가 켜졌을 때만 붙는다", async () => {
    running = await runModule(layout, {pushToRight: true});
    expect(html.classList.contains("refresherPushToRight")).toBe(false);
    running.change({hideUselessView: true});
    expect(html.classList.contains("refresherPushToRight")).toBe(true);
});

it("설정을 바꾸면 키에 맞는 쪽을 다시 적용하고, 멈추면 모두 걷어 낸다", async () => {
    running = await runModule(layout);
    expect(html.classList.contains("refresherCompact")).toBe(false);
    running.change({forceCompact: true});
    expect(html.classList.contains("refresherCompact")).toBe(true);
    running.change({removeAi: true});
    expect(hideCss()).toBe(rule("removeAi"));
    running.change({pushToRight: true, hideUselessView: true});
    expect(html.classList.contains("refresherPushToRight")).toBe(true);

    running.stop();
    running = undefined;
    expect(document.querySelector("style#refresher-layout-hide")).toBeNull();
    expect(html.classList.contains("refresherCompact")).toBe(false);
    expect(html.classList.contains("refresherPushToRight")).toBe(false);
});

it("글 보기에서는 게시글 보기 컴팩트 모드를 켰을 때만 컴팩트로 바꾼다", async () => {
    // isViewPage는 불러올 때 정해지므로 글 주소로 옮긴 뒤 모듈을 새로 불러온다.
    history.replaceState(null, "", "/board/view/?id=test&no=1");
    vi.resetModules();
    const {default: viewLayout} = await import("@/features/layout/index");
    running = await runModule(viewLayout, {forceCompact: true, useCompactModeOnView: false});
    expect(html.classList.contains("refresherCompact")).toBe(false);
    running.change({useCompactModeOnView: true});
    expect(html.classList.contains("refresherCompact")).toBe(true);
});

it("6.2.2 레이아웃 설정 스키마·순서를 Kotlin에서 그대로 만든다", () => {
    expect(Object.keys(settings)).toEqual([
        "activePixel", "forceCompact", "useCompactModeOnView", ...HIDE_KEYS, "pushToRight"
    ]);
    expect(settings.activePixel).toEqual({
        type: "range", default: 900, min: 100, max: 3840, step: 1, unit: "px"
    });
    expect(settings.forceCompact).toEqual({type: "check", default: false});
    expect(settings.useCompactModeOnView).toEqual({type: "check", default: true});
    expect(settings.pushToRight).toEqual({type: "check", default: false});
    for (const key of HIDE_KEYS) {
        expect(settings[key]).toEqual({type: "check", default: false});
    }
});

it("Kotlin layout 메타는 설정 스키마의 같은 객체를 내보낸다", () => {
    expect(meta.id).toBe("layout");
    expect(meta.settings).toBe(settings);
    expect(Object.keys(meta.settings ?? {})).toEqual(Object.keys(settings));
});
