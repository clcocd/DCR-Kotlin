import QuickLRU from "quick-lru";
import {afterEach, expect, it, vi} from "vitest";

import {dcconPackages} from "@/core/preview/dccon";
import {kotlinBridge} from "@/kotlin/bridge";

afterEach(() => {
    dcconPackages.clear();
    vi.useRealTimers();
});

it("Kotlin 디시콘 패키지 캐시 설정이 10분 뒤 실제로 만료된다", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-01-01T00:00:00Z"));
    const cache = kotlinBridge.createDcconPackageCache<string>((options) => new QuickLRU(options));
    cache.set("sample", "package");
    expect(cache.get("sample")).toBe("package");
    vi.advanceTimersByTime(600_001);
    expect(cache.get("sample")).toBeUndefined();
});

it("디시콘 패키지 캐시는 기존 QuickLRU 인스턴스를 사용한다", () => {
    expect(dcconPackages).toBeInstanceOf(QuickLRU);
});
