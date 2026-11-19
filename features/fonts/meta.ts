import {kotlinBridge} from "@/kotlin/bridge";
import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";

const DEFAULT_FONTS = "Noto Sans CJK KR, NanumGothic";

/** 옵션·팝업과 페이지가 공유하는 글꼴 구성. Kotlin/JS가 생성한다. */
export const fontFamilyOf = kotlinBridge.fontFamilyOf;

export const settings = {
    customFonts: {type: "text", default: DEFAULT_FONTS},
    changeDCFont: {type: "check", default: true},
    bodyFontSize: {type: "range", default: 13, min: 5, max: 30, step: 1, unit: "px"}
} satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "fonts",
    settings,

    // 옵션, 팝업도 같은 폰트로 (assets/styles/tailwind.css의 --font-sans가 --refresher-font를 쓴다).
    extensionPageVars: (settings) => ({"--refresher-font": fontFamilyOf(settings.customFonts)})
});
