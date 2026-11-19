import {describe, expect, it, vi} from "vitest";

import {defineModule, defineModuleMeta} from "@/core/module/define";
import {defineBackgroundModule} from "@/core/module/background";

describe("Kotlin module definition compatibility", () => {
    it("preserves metadata, setup and shortcut identities", () => {
        const meta = defineModuleMeta({id: "demo", defaultEnable: false, toggles: ["reveal"] as const});
        const setup = () => ({revealed: false});
        const toggle = vi.fn();
        const module = defineModule(meta, {
            setup,
            pageToggles: {
                reveal: {
                    desc: "이미지 보이기",
                    isOn: (api) => api.revealed,
                    toggle
                }
            }
        });

        expect(module.id).toBe("demo");
        expect(module.defaultEnable).toBe(false);
        expect(module.setup).toBe(setup);
        expect(module.pageToggles).toHaveLength(1);
        expect(module.pageToggles![0]).toMatchObject({id: "reveal", desc: "이미지 보이기"});
        expect(module.pageToggles![0]!.toggle).toBe(toggle);
    });

    it("does not add phantom page toggles", () => {
        const meta = defineModuleMeta({id: "test", toggles: [] as const});
        const module = defineModule(meta, {setup: () => undefined});
        expect(module.pageToggles).toEqual([]);
        expect(defineModule(defineModuleMeta({id: "bare"}), {setup: () => undefined}).pageToggles).toBeUndefined();
    });

    it("background module only copies id, enable, settings and implementation", () => {
        const meta = defineModuleMeta({id: "background", defaultEnable: true, settings: {flag: {type: "check", default: false} as const}});
        const listen = vi.fn();
        const apply = vi.fn();
        const module = defineBackgroundModule(meta, {listen, apply});
        expect(module).toEqual({id: "background", defaultEnable: true, settings: meta.settings, listen, apply});
    });
});
