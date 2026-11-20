import {defineModuleMeta} from "@/core/module/define";
import {objectKeys} from "@/utils/typed";
import {kotlinBridge} from "@/kotlin/bridge";

/** 이미지 주소를 붙이면 바로 검색되는 GET 주소 (url 뒤에 encodeURIComponent(이미지 주소)). */
export const ENGINES = kotlinBridge.imageSearchEngines() satisfies Record<string, {name: string; url: string}>;

export const ENGINE_IDS = objectKeys(ENGINES);

// 메뉴 등록과 실행은 Kotlin, WXT 메타 타입 연결만 TypeScript에 남긴다.
export default defineModuleMeta(kotlinBridge.imageSearchMeta(ENGINE_IDS));
