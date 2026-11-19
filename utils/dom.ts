import {kotlinBridge} from "@/kotlin/bridge";

/** DOMContentLoaded에 run을 부른다. signal이 abort되면 부르지 않는다. */
export const whenDomReady = (run: () => void, signal?: AbortSignal): void => {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", run, {once: true, signal});
    else run();
};

/** 직접 자식 script를 뺀 텍스트는 Kotlin/JS가 계산한다. */
export const textWithoutScripts = kotlinBridge.textWithoutScripts;

/** bfcache 복귀 탭은 저장소 변경을 못 받았으니 돌아올 때 다시 읽는다. */
export const onBfcacheRestore = (run: () => Promise<void>, signal?: AbortSignal): void => {
    window.addEventListener("pageshow", (ev) => {
        if (ev.persisted) void run().catch(console.error);
    }, {signal});
};

/** 부드러운 스크롤(맨 위로, 댓글로, 페이지 넘김 등). 동작 줄이기(prefers-reduced-motion)를 켰으면 바로 옮긴다. */
export const smoothScroll = (): ScrollBehavior => (matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth");

/** style 재사용·정리 함수는 Kotlin/JS로 구현한다. */
export const writeStyle = kotlinBridge.writeStyle;
