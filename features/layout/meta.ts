import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {objectKeys} from "@/utils/typed";
import {kotlinBridge} from "@/kotlin/bridge";

/** 체크하면 숨기는 영역. 설정과 <style> 규칙을 모두 여기서 만들므로 새 항목은 여기 한 줄과 labels.ts의 이름, 설명만 추가하면 된다. */
export const HIDE_OPTIONS = kotlinBridge.layoutHideOptions() satisfies Record<string, {selector: string}>;

export const HIDE_KEYS = objectKeys(HIDE_OPTIONS);

export const settings = kotlinBridge.layoutSettings(HIDE_KEYS) satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "layout",
    settings
});
