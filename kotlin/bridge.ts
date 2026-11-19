// Gradle에서 생성하는 번들. 프로젝트 코드에서는 Kotlin 함수의 타입만 이 파일이 담당한다.
import "../generated/kotlin-bridge.js";

interface KotlinBridge {
    dcinsideHref(url: string): string | null;
    galleryKind(url: string): "normal" | "minor" | "mini" | "person";
    galleryPath(url: string): string;
    galltypeOf(url: string): string;
    managerAjaxUrl(url: string, action: string): string;
    listUrl(url: string): string;
    mergeParamURL(origin: string, from: string): string;
    postSearchUrl(query: string): string;
    hash53(text: string): number;
}

export const kotlinBridge = (globalThis as typeof globalThis & {__DCR_KOTLIN__: KotlinBridge}).__DCR_KOTLIN__;
