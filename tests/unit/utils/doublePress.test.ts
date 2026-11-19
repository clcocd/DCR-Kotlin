import {afterEach, describe, expect, it, vi} from "vitest";
import {createDoublePress} from "@/utils/doublePress";

describe("createDoublePress Kotlin", () => {
    afterEach(() => vi.useRealTimers());

    it("같은 키를 제한 시간 안에 두 번 누르면 성공하고 초기화한다", () => {
        vi.useFakeTimers();
        vi.setSystemTime(1000);
        const press = createDoublePress(500);
        expect(press("delete")).toBe(false);
        vi.setSystemTime(1499);
        expect(press("delete")).toBe(true);
        expect(press("delete")).toBe(false);
    });

    it("정확히 제한 시간이면 실패한다", () => {
        vi.useFakeTimers();
        vi.setSystemTime(1000);
        const press = createDoublePress(500);
        expect(press("a")).toBe(false);
        vi.setSystemTime(1500);
        expect(press("a")).toBe(false);
        vi.setSystemTime(1501);
        expect(press("a")).toBe(true);
    });

    it("다른 키는 이전 키를 해제한다", () => {
        vi.useFakeTimers();
        vi.setSystemTime(1000);
        const press = createDoublePress(500);
        expect(press("a")).toBe(false);
        vi.setSystemTime(1100);
        expect(press("b")).toBe(false);
        vi.setSystemTime(1200);
        expect(press("a")).toBe(false);
        expect(press("a")).toBe(true);
    });

    it("독립된 인스턴스끼리는 상태를 공유하지 않는다", () => {
        vi.useFakeTimers();
        vi.setSystemTime(1000);
        const a = createDoublePress(500);
        const b = createDoublePress(500);
        expect(a("x")).toBe(false);
        expect(b("x")).toBe(false);
        expect(a("x")).toBe(true);
        expect(b("x")).toBe(true);
    });
});
