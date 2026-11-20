import {defineModuleMeta} from "@/core/module/define";
import type {SettingSchema} from "@/core/module/types";
import {objectKeys} from "@/utils/typed";
import {kotlinBridge} from "@/kotlin/bridge";

/** 이미지 주소를 붙이면 바로 검색되는 GET 주소 (url 뒤에 encodeURIComponent(이미지 주소)). */
export const ENGINES = kotlinBridge.imageSearchEngines() satisfies Record<string, {name: string; url: string}>;

export const ENGINE_IDS = objectKeys(ENGINES);

export default defineModuleMeta({
    id: "imagesearch",
    // 메뉴는 background.ts가 만든다. 페이지에 할 일이 없어 index.ts가 없다.
    settings: kotlinBridge.imageSearchSettings(ENGINE_IDS) as
        Record<(typeof ENGINE_IDS)[number], Extract<SettingSchema, { type: "check" }>>
});
