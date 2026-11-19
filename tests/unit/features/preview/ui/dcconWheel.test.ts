import {describe, expect, it} from "vitest";
import {kotlinBridge} from "@/kotlin/bridge";

describe("DCCon wheel capture", () => {
    it("세로 휠을 직접 scrollLeft에 더하고 기본 스크롤을 막는다", () => {
        const strip = document.createElement("div");
        strip.scrollLeft = 11;
        const dispose = kotlinBridge.bindDcconPackageWheel(strip);

        const down = new WheelEvent("wheel", {deltaY: 75, bubbles: true, cancelable: true});
        strip.dispatchEvent(down);
        expect(down.defaultPrevented).toBe(true);
        expect(strip.scrollLeft).toBe(86);

        const up = new WheelEvent("wheel", {deltaY: -20, bubbles: true, cancelable: true});
        strip.dispatchEvent(up);
        expect(strip.scrollLeft).toBe(66);

        // 버블링이 대상에서 중단돼도 컨테이너의 캡처 리스너가 먼저 처리한다.
        const inner = document.createElement("button");
        strip.append(inner);
        inner.addEventListener("wheel", (event) => event.stopPropagation());
        const captured = new WheelEvent("wheel", {deltaY: 5, bubbles: true, cancelable: true});
        inner.dispatchEvent(captured);
        expect(captured.defaultPrevented).toBe(true);
        expect(strip.scrollLeft).toBe(71);

        const horizontalOnly = new WheelEvent("wheel", {deltaX: 12, deltaY: 0, cancelable: true});
        strip.dispatchEvent(horizontalOnly);
        expect(horizontalOnly.defaultPrevented).toBe(false);
        expect(strip.scrollLeft).toBe(71);

        dispose();
        const after = new WheelEvent("wheel", {deltaY: 20, cancelable: true});
        strip.dispatchEvent(after);
        expect(after.defaultPrevented).toBe(false);
        expect(strip.scrollLeft).toBe(71);
    });
});
