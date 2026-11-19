// Gradle에서 생성하는 번들. 프로젝트 코드에서는 Kotlin 함수의 타입만 이 파일이 담당한다.
import "../generated/kotlin-bridge.js";
import type {BlockEntry, BlockType, DetectMode} from "@/core/storage/types";

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
    nickType(iconUrl: string): "FIXED" | "HALF_FIXED" | "UNFIXED";
    submitResult(response: string): {result: string; message?: string; detail?: string};
    resultMessage(message?: string, detail?: string): string | null;
    htmlToText(html: string): string;
    decodeD(value: string): string;
    composeExtra(isRegex: boolean, gallery?: string, mode?: string): string;
    blockKey(content: string, gallery?: string): string;
    normalizeBlockList(value: unknown, known: BlockEntry[]): BlockEntry[];
    normalizeDefaults(value: unknown): Record<BlockType, DetectMode>;
    sameComments(a: object[], b: object[]): boolean;
    preprocessCommentMemo(memo: string): string;
    extractVoice(memo: string): {memo: string; voice?: {src: string; iframe: boolean}} | null;
    hasModifier(ctrl: boolean, alt: boolean, meta: boolean, shift: boolean): boolean;
    pressedKey(code: string, key: string): string;
}

export const kotlinBridge = (globalThis as typeof globalThis & {__DCR_KOTLIN__: KotlinBridge}).__DCR_KOTLIN__;
