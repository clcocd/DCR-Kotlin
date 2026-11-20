import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pages";

// "v5와 같은 키"는 v5에서 옮긴 값을 그대로 쓰므로 이름을 바꾸지 않는다.

export const settings = {
    previewWidth: {type: "range", default: 1200, min: 700, max: 1600, step: 50, unit: "px"},
    // v5와 같은 키.
    toggleBackgroundBlur: {type: "check", default: true},
    popupBlur: {type: "check", default: true},
    // v5와 같은 키.
    scrollToSkip: {type: "check", default: true},
    reversePreviewKey: {type: "check", default: false},
    longPressDelay: {type: "range", default: 300, min: 200, max: 2000, step: 50, unit: "ms"},
    colorPreviewLink: {type: "check", default: true},
    autoRefreshComment: {type: "check", default: false},
    highlightNewComments: {type: "check", default: true},
    commentRefreshInterval: {type: "range", default: 10000, min: 3000, max: 20000, step: 100, unit: "ms"},
    expandRecognizeRange: {type: "check", default: false},
    imageViewer: {type: "check", default: true},
    listKeyboard: {type: "check", default: true},
    disableCache: {type: "check", default: false},
    archiveArticle: {type: "check", default: false},
    blockImage: {type: "check", default: false}
} satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "preview",
    urls: [BOARD_PAGE],
    settings
});
