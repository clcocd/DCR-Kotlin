import {describe, expect, it} from "vitest";

import {BOARD_PAGE} from "@/core/pages";
import meta, {settings} from "@/features/block/meta";

describe("Kotlin block module metadata", () => {
    it("preserves the 6.2.2 block command and page toggle contract", () => {
        expect(meta.id).toBe("block");
        expect(meta.urls).toEqual([BOARD_PAGE]);
        expect(meta.settings).toBe(settings);
        expect(meta.toggles).toEqual(["reveal"]);
        expect(meta.commands).toEqual({blockReveal: {}});
    });
});
