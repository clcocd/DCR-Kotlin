import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pages";

const metadata = kotlinBridge.blockMeta([BOARD_PAGE]);
export const settings = metadata.settings satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta(metadata);
