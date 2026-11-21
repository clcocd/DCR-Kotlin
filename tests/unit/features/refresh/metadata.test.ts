import {describe, expect, it} from "vitest";

import {BOARD_PAGE} from "@/core/pagePatterns";
import meta, {settings} from "@/features/refresh/meta";

describe("Kotlin refresh module metadata", () => {
    it("keeps the 6.2.2 feature identity, URL filter and command bindings", () => {
        expect(meta.id).toBe("refresh");
        expect(meta.urls).toEqual([BOARD_PAGE]);
        expect(meta.settings).toBe(settings);
        expect(meta.toggles).toEqual(["pause"]);
        expect(meta.commands).toEqual({
            refreshLists: {key: "Alt+R"},
            refreshPause: {key: "Alt+S"}
        });
    });
});
