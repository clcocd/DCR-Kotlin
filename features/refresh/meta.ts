import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pagePatterns";

export const settings = kotlinBridge.staticFeatureSettings("refresh") satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta(kotlinBridge.refreshMeta([BOARD_PAGE], settings));
