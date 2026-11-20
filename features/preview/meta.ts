import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pages";

// "v5와 같은 키"는 v5에서 옮긴 값을 그대로 쓰므로 이름을 바꾸지 않는다.

export const settings = kotlinBridge.staticFeatureSettings("preview") satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "preview",
    urls: [BOARD_PAGE],
    settings
});
