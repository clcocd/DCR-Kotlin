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

        const horizontalOnly = new WheelEvent("wheel", {deltaX: 12, deltaY: 0, cancelable: true});
        strip.dispatchEvent(horizontalOnly);
        expect(horizontalOnly.defaultPrevented).toBe(false);
        expect(strip.scrollLeft).toBe(66);

        dispose();
        const after = new WheelEvent("wheel", {deltaY: 20, cancelable: true});
        strip.dispatchEvent(after);
        expect(after.defaultPrevented).toBe(false);
        expect(strip.scrollLeft).toBe(66);
    });
});
