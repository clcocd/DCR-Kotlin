// Gradle에서 생성하는 번들. 프로젝트 코드에서는 Kotlin 함수의 타입만 이 파일이 담당한다.
import "../generated/kotlin-bridge.js";
import type {BlockEntry, BlockType, DetectMode, SettingValue} from "@/core/storage/types";
import type {SettingSchema} from "@/core/module/types";

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
    createDoublePress(windowMs: number): (key: string) => boolean;
    fontFamilyOf(customFonts: string): string;
    buildFontCss(customFonts: string, size: number, changeDCFont: boolean): string;
    stealthIsRevealed(): boolean;
    stealthToggle(): void;
    stealthCreateButton(): void;
    stealthEnable(): void;
    stealthDispose(): void;
    layoutCompact(narrow: boolean, forceCompact: boolean, isDcconShop: boolean, isViewPage: boolean, useCompactModeOnView: boolean): boolean;
    layoutHideCss(options: Record<string, {selector: string}>, settings: Record<string, unknown>, noticePage: boolean): string;
    setTitleCount(count: number): void;
    textWithoutScripts(element: Element | null | undefined): string;
    writeStyle(id: string, css: string): () => void;
    setAppearance(element: HTMLElement, dark: boolean): void;
    isBrokenMedia(image: HTMLImageElement): boolean;
    markPreviewMedia(media: HTMLElement, imageViewer: boolean): void;
    markPreviewPressable(root: HTMLElement, imageViewer: boolean): void;
    isModuleEnabled(def: {id: string; defaultEnable?: boolean}, enables: Record<string, unknown>): boolean;
    defaultSettingValue(schema: SettingSchema): SettingValue;
    normalizeModuleSetting(schema: SettingSchema, value: unknown): SettingValue;
    moduleEnablesOf(stored: unknown): Record<string, unknown>;
    moduleSettingsOf(def: {settings?: Record<string, SettingSchema>}, stored: unknown): Record<string, SettingValue>;
    isJsRecord(value: unknown): boolean;
    canonicalStorageValue(value: unknown): string | undefined;
    storageSizeOf(value: unknown): number | undefined;
    defineContentModule(meta: object, impl: object): unknown;
    defineBackgroundModule(meta: object, impl: object): unknown;
    fontsModuleImpl(): unknown;
    stealthModuleImpl(showToast: (message: string) => void): unknown;
    layoutModuleImpl(
        hideOptions: Record<string, {selector: string}>, isViewPage: boolean, isDcconShop: boolean, isNoticePage: () => boolean
    ): unknown;
    writeModuleImpl(
        selectors: {uploadOptionsKey: string; submit: string; leave: string; posted: string; subject: string; editor: string; auto: string},
        isWritePage: () => boolean, hookUploads: () => Promise<unknown>
    ): unknown;
    manageModuleImpl(
        selectors: {checkboxRow: string; gifVideo: string; listRow: string},
        onDomReady: (callback: () => void, signal: AbortSignal) => void,
        renderViewTools: (settings: {imageOrigin: boolean; titleSearch: boolean}) => void,
        removeViewTools: () => void,
        isManager: () => boolean,
        postNo: (row: HTMLElement) => string | undefined,
        deleteAndNotify: (id: string, gallery: string, link: string) => Promise<boolean>,
        gallery: string, documentHref: string
    ): unknown;
    manageViewImpl(
        numberedSource: (source: string) => boolean,
        searchPosts: (title: string) => Promise<string>,
        postSearchUrl: (title: string) => string,
        showToast: (text: string, type: "info" | "warning" | "error", duration?: number,
                    action?: {label: string; run: () => void}) => void,
        gallery: string | null, postNo: string | null
    ): {remove(): void; render(show: {imageOrigin: boolean; titleSearch: boolean}): void};
    writerSearchUrl(filter: {kind: "uid" | "ip"; value: string} | null, nick: string | undefined,
                    paramNames: Record<"uid" | "ip", string>): string;
    keepWriterFilter(paging: Element, filter: {kind: "uid" | "ip"; value: string},
                     paramNames: Record<"uid" | "ip", string>): void;
    addWriterTypes(filter: {kind: "uid" | "ip"; value: string} | null,
                   search: (filter: {kind: "uid" | "ip"; value: string}) => void, signal: AbortSignal): void;
    removeWriterTypes(): void;
    imageSearchBackgroundImpl(
        engineIds: readonly string[],
        engines: Record<string, {name: string; url: string}>,
        urlPatterns: string[],
        registerClicked: (listener: (
            info: {menuItemId: string | number; srcUrl?: string},
            tab?: {id?: number; index: number; windowId: number}
        ) => Promise<unknown>) => void,
        removeMenu: (id: string) => Promise<void>,
        createMenu: (menu: {id: string; title: string; contexts: ["image"]; targetUrlPatterns: string[]}) => unknown,
        createTab: (details: {url: string; index?: number; openerTabId?: number; windowId?: number}) => Promise<unknown>
    ): unknown;
}

export const kotlinBridge = (globalThis as typeof globalThis & {__DCR_KOTLIN__: KotlinBridge}).__DCR_KOTLIN__;
