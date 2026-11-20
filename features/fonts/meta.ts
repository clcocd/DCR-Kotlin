import {kotlinBridge} from "@/kotlin/bridge";
import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";

/** 옵션·팝업과 페이지가 공유하는 글꼴 구성. Kotlin/JS가 생성한다. */
export const fontFamilyOf = kotlinBridge.fontFamilyOf;

const metadata = kotlinBridge.fontsMeta();
export const settings = metadata.settings satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta(metadata);
