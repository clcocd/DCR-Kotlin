import {describe, expect, it} from "vitest";

import {kotlinBridge} from "@/kotlin/bridge";
import {isRecord} from "@/utils/record";

const originalCanonical = (value: unknown): string | undefined =>
    JSON.stringify(value, (_key, item: unknown) =>
        isRecord(item) ? Object.fromEntries(Object.keys(item).sort().map((key) => [key, item[key]])) : item
    );

describe("Kotlin 저장소 비교와 원본 JSON.stringify 동등성", () => {
    it.each([
        [{b: 1, a: 2}],
        [[{text: "hello", color: "red"}, {array: [{z: true, a: false}]}]],
        [null],
        [1],
        [undefined],
        [{date: "2026-11-20", nan: Number.NaN, flag: false}],
        [[null, undefined, 3]],
        [{outer: {third: 3, first: 1, second: 2}}]
    ])("원본 직렬화와 동일하다: %j", (value) => {
        expect(kotlinBridge.canonicalStorageValue(value)).toBe(originalCanonical(value));
    });

    it("배열·객체만 항목 수를 반환한다", () => {
        expect(kotlinBridge.storageSizeOf(["a", "b"])).toBe(2);
        expect(kotlinBridge.storageSizeOf({b: 2, a: 1})).toBe(2);
        for (const value of [null, "hi", 0, false]) {
            expect(kotlinBridge.storageSizeOf(value)).toBeUndefined();
        }
    });

    it("레코드 판정도 원본 정의와 동일하다", () => {
        for (const value of [null, undefined, [], {}, Object.create(null), new Date(), 1, "a"]) {
            expect(isRecord(value)).toBe(typeof value === "object" && value !== null && !Array.isArray(value));
        }
    });
});
