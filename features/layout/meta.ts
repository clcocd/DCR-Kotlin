import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingSchema, SettingsSchema} from "@/core/module/types";
import {objectKeys} from "@/utils/typed";
import {kotlinBridge} from "@/kotlin/bridge";

/** 체크하면 숨기는 영역. 설정과 <style> 규칙을 모두 여기서 만들므로 새 항목은 여기 한 줄과 labels.ts의 이름, 설명만 추가하면 된다. */
export const HIDE_OPTIONS = kotlinBridge.layoutHideOptions() satisfies Record<string, {selector: string}>;

export const HIDE_KEYS = objectKeys(HIDE_OPTIONS);

export const settings = {
    activePixel: {
        type: "range",
        default: 900,
        min: 100,
        // 모니터마다 다른 screen.width로 두면 큰 모니터에서 저장한 값이 작은 모니터에서 잘린다.
        max: 3840,
        step: 1,
        unit: "px"
    },
    forceCompact: {type: "check", default: false},
    useCompactModeOnView: {type: "check", default: true},
    ...(Object.fromEntries(HIDE_KEYS.map((key) => [key, {type: "check", default: false}])) as
        Record<(typeof HIDE_KEYS)[number], Extract<SettingSchema, { type: "check" }>>),
    pushToRight: {type: "check", default: false}
} satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "layout",
    settings
});
