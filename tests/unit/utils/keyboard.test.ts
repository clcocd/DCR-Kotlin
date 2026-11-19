import {describe, expect, it} from "vitest";
import {hasModifier, pressedKey} from "@/utils/event";

describe("Kotlin keyboard helpers", () => {
    it("modifier detection keeps the original semantics", () => {
        expect(hasModifier({ctrlKey: false, altKey: false, metaKey: false, shiftKey: false})).toBe(false);
        expect(hasModifier({ctrlKey: true, altKey: false, metaKey: false, shiftKey: false})).toBe(true);
        expect(hasModifier({ctrlKey: false, altKey: false, metaKey: true, shiftKey: false})).toBe(true);
    });

    it("code overrides IME key only when the original regex matches", () => {
        expect(pressedKey({code: "KeyA", key: "ㅇ"})).toBe("a");
        expect(pressedKey({code: "Digit7", key: "ㅊ"})).toBe("7");
        expect(pressedKey({code: "PageDown", key: "PageDown"})).toBe("pagedown");
        expect(pressedKey({code: "Keya", key: "ㅇ"})).toBe("ㅇ");
        expect(pressedKey({code: "KeyAB", key: "B"})).toBe("b");
    });
});
