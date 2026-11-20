// Gradle에서 생성하는 번들. 프로젝트 코드에서는 Kotlin 함수의 타입만 이 파일이 담당한다.
import "../generated/dcr-kotlin.mjs";
import type {KyInstance} from "ky";
import type * as React from "react";
import type {createRoot} from "react-dom/client";
import type {ContentScriptContext} from "wxt/utils/content-script-context";
import type {listUrl, queryString, rowPostNo, writerFilter, WriterFilter} from "@/core/http/urls";
import type {checkboxFiller, highlightSearchResults, writerSelector} from "@/core/list";
import type {BadgeView} from "@/core/badges";
import type {Asn, Range} from "@/scripts/ip-candidates";
import type {CompactIpData, IpCandidate} from "@/core/ipdb";
import type {CommentOrderValue} from "@/features/preview/ui/previewStore";
import type {ListRow} from "@/features/preview/rows";
import type {ProcessedComment} from "@/core/preview/comments";
import type {Ctx as PreviewContext} from "@/features/preview/meta";
import type {Ctx as BlockContext} from "@/features/block/meta";
import type {Ctx as FontsContext} from "@/features/fonts/meta";
import type {createListLoader, RefreshState} from "@/features/refresh/loader";
import type {createUnseenCounter} from "@/features/refresh/title";
import type {BackgroundModule} from "@/core/module/background";
import type {BeforeStart, ModuleLoaders} from "@/core/module/registry";
import type {PageAction, PageState, PageToggleState} from "@/core/messaging/protocol";
import type {StorageItemKey} from "wxt/utils/storage";
import type {ListSyncOptions, StorageSync} from "@/core/storage/sync";
import type {BlockRequestOptions} from "@/stores/blockRequest";
import type {Ratios, SelectedUser} from "@/stores/ui";
import type {smoothScroll, whenDomReady} from "@/utils/dom";
import type {sha256} from "@/utils/hash";
import type {hasModifier, isTyping} from "@/utils/event";
import type {Ctx as RefreshContext} from "@/features/refresh/meta";
import type {Ctx as LayoutContext} from "@/features/layout/meta";
import type {AnyModule, AnyModuleIcons, AnyModuleLabels, ModuleInfo, DefinedModule, ModuleDefinition, ModuleImpl, AnyModuleMeta, ModuleApis, ModuleContext, ModuleSettings, SettingSchema, LabelsOf} from "@/core/module/types";
import type {ManageResult} from "@/core/preview/manage";
import type {CacheEntry} from "@/core/preview/cache";
import type {DcinsideComment, GalleryPreData, PostInfo} from "@/core/preview/types";
import type {ownPreviewEntry, PreviewEntry, SavedHistory} from "@/core/preview/history";
import type {BlockEntry, BlockType, DetectMode, MemoEntry, MemoType, SettingValue} from "@/core/storage/types";

export interface ReplaceListOptions {
    navigated: boolean;
    search: string | undefined;
    searchType: string | null;
    fadeIn: boolean;
    keepDeleted: boolean;
}

interface RefreshListHelpers {
    rowPostNo: typeof rowPostNo;
    checkboxFiller: typeof checkboxFiller;
    highlightSearchResults: typeof highlightSearchResults;
    isViewPage: boolean;
    pagePostNo: string | null;
    noticeIcon: string;
}

interface RefreshModuleHelpers {
    createListLoader: typeof createListLoader;
    createUnseenCounter: typeof createUnseenCounter;
    hasModifier: typeof hasModifier;
    isTyping: typeof isTyping;
    isViewPage: boolean;
    listSelector: string;
    listUrl: typeof listUrl;
    ownPreviewEntry: typeof ownPreviewEntry;
    pageGallery: string | null;
    pagingSelector: string;
    queryString: typeof queryString;
    showToast(message: string): void;
    whenDomReady: typeof whenDomReady;
    writerFilter: typeof writerFilter;
}

interface RefreshLoaderHelpers {
    fetchText(url: string, options: {signal: AbortSignal; totalTimeout: number; retry?: number}): Promise<string>;
    isViewPage: boolean;
    isWholeFirstPage(url: string): boolean;
    listSelector: string;
    notifyListReplaced(gallery: string): void;
    pagingSelector: string;
    previewApi(): {isOpen(): boolean; archiveArticle(): boolean} | undefined;
    queryString: typeof queryString;
    replaceList(oldList: HTMLElement, newList: HTMLElement, options: ReplaceListOptions): {added: HTMLTableRowElement[]; changed: boolean};
    sha256: typeof sha256;
    showToast(message: string, type: "warning" | "error"): void;
    smoothScroll: typeof smoothScroll;
    tableStart: string;
    userinfoApi(): {checkNewPosts(rows: HTMLTableRowElement[]): unknown} | undefined;
    writerFilter: typeof writerFilter;
    writerSelector: typeof writerSelector;
}

type FeatureLabelMap = {
    block: LabelsOf<typeof import("@/features/block/meta").default>;
    fonts: LabelsOf<typeof import("@/features/fonts/meta").default>;
    layout: LabelsOf<typeof import("@/features/layout/meta").default>;
    preview: LabelsOf<typeof import("@/features/preview/meta").default>;
    refresh: LabelsOf<typeof import("@/features/refresh/meta").default>;
    search: LabelsOf<typeof import("@/features/search/meta").default>;
    stealth: LabelsOf<typeof import("@/features/stealth/meta").default>;
    write: LabelsOf<typeof import("@/features/write/meta").default>;
};

type CheckSetting = {type: "check"; default: boolean};
type RangeSetting = {type: "range"; default: number; min: number; max: number; step: number; unit: string};
type TextSetting = {type: "text"; default: string};

type StaticFeatureSettingsMap = {
    block: {
        replyRemove: CheckSetting;
        blur: CheckSetting;
        blurReveal: CheckSetting;
        blurStrength: RangeSetting;
        foldDuplicate: CheckSetting;
        duplicateCount: RangeSetting;
        duplicateMinLength: RangeSetting;
    };
    fonts: {
        customFonts: TextSetting;
        changeDCFont: CheckSetting;
        bodyFontSize: RangeSetting;
    };
    preview: {
        previewWidth: RangeSetting;
        toggleBackgroundBlur: CheckSetting;
        popupBlur: CheckSetting;
        scrollToSkip: CheckSetting;
        reversePreviewKey: CheckSetting;
        longPressDelay: RangeSetting;
        colorPreviewLink: CheckSetting;
        autoRefreshComment: CheckSetting;
        highlightNewComments: CheckSetting;
        commentRefreshInterval: RangeSetting;
        expandRecognizeRange: CheckSetting;
        imageViewer: CheckSetting;
        listKeyboard: CheckSetting;
        disableCache: CheckSetting;
        archiveArticle: CheckSetting;
        blockImage: CheckSetting;
    };
    refresh: {
        refreshRate: RangeSetting;
        fadeIn: CheckSetting;
        useBetterBrowse: CheckSetting;
        noRefreshOnSearch: CheckSetting;
        pauseOnHover: CheckSetting;
        titleCount: CheckSetting;
        readMark: CheckSetting;
        backgroundRefresh: CheckSetting;
        backgroundRefreshRate: RangeSetting;
        doNotColorVisited: CheckSetting;
    };
    search: {
        maxSearches: RangeSetting;
    };
    write: {
        preventExit: CheckSetting;
        webpConvert: CheckSetting;
        webpQuality: RangeSetting;
        webpConcurrency: RangeSetting;
        obfuscateName: CheckSetting;
    };
};

interface KotlinBridge {
    staticFeatureSettings<Id extends keyof StaticFeatureSettingsMap>(id: Id): StaticFeatureSettingsMap[Id];
    imageSearchSettings(ids: readonly string[]): Record<string, {type: "check"; default: boolean}>;
    imageSearchMeta<Key extends string>(ids: readonly Key[]): {
        id: "imagesearch";
        settings: Record<Key, {type: "check"; default: boolean}>;
    };
    layoutSettings<Key extends string>(ids: readonly Key[]): {
        activePixel: {type: "range"; default: number; min: number; max: number; step: number; unit: string};
        forceCompact: {type: "check"; default: boolean};
        useCompactModeOnView: {type: "check"; default: boolean};
        pushToRight: {type: "check"; default: boolean};
    } & Record<Key, {type: "check"; default: boolean}>;
    userInfoSettings<Color extends string>(
        colors: Record<Color, string>,
        filters: Record<"all" | "foreign" | "vpn" | "none", string>,
        actions: Record<"none" | "tag" | "blur" | "hide", string>
    ): {
        showFixedNickUID: CheckSetting;
        showHalfFixedNickUID: CheckSetting;
        ipInfoFilter: {type: "option"; default: "all"; items: Record<"all" | "foreign" | "vpn" | "none", string>};
        checkRatio: CheckSetting;
        alarmRatio: RangeSetting;
        lowActivityAction: {type: "option"; default: "tag"; items: Record<"none" | "tag" | "blur" | "hide", string>};
        badgeOrder: {
            type: "order"; items: Record<"UID" | "MEMO" | "RATIO", string>;
            default: readonly ["UID", "MEMO", "RATIO"]
        };
    } & Record<`${Color}Color`, {type: "color"; default: string}>;
    readonly defaultFonts: string;
    fontExtensionPageVars(settings: {customFonts: string}): {"--refresher-font": string};
    fontsMeta(): {
        id: "fonts";
        settings: StaticFeatureSettingsMap["fonts"];
        extensionPageVars: (settings: {customFonts: string}) => {"--refresher-font": string};
    };
    imageSearchEngines(): Record<"saucenao" | "googleLens" | "yandex" | "ascii2d" | "tineye" | "iqdb" | "tracemoe", {name: string; url: string}>;
    layoutHideOptions(): Record<"hideGalleryView" | "hideUselessView" | "hideGalleryImage" | "removeNotice" | "removeDCNotice" | "removeGamemeca" | "removeAi", {selector: string}>;
    userInfoRuntimeOptions(): {
        badgeColors: Record<"uid" | "ratio" | "ratioAlarm" | "korea" | "japan" | "china" | "foreign" | "vpn", string>;
        ipInfoFilters: Record<"all" | "foreign" | "vpn" | "none", string>;
        lowActivityActions: Record<"none" | "tag" | "blur" | "hide", string>;
    };
    moduleLabels<Id extends keyof FeatureLabelMap>(id: Id): FeatureLabelMap[Id];
    imageSearchLabels(
        engineIds: readonly string[],
        engines: Record<string, {name: string; url: string}>
    ): LabelsOf<typeof import("@/features/imagesearch/meta").default>;
    userInfoLabels(): LabelsOf<typeof import("@/features/userinfo/meta").default>;
    createBlockDefaultsWatcher(
        store: {
            getState(): {defaults: Record<BlockType, DetectMode>};
            setState(value: {defaults: Record<BlockType, DetectMode>}): void;
        },
        normalize: (value: unknown) => Record<BlockType, DetectMode>,
        equal: (a: Record<BlockType, DetectMode>, b: Record<BlockType, DetectMode>) => boolean
    ): (value: unknown) => void;
    currentPageContext(viewPage: RegExp): {
        documentUrl: URL;
        isViewPage: boolean;
        pagePostNo: string | null;
        pageGallery: string | null;
    };
    readonly runtimeConstants: {
        ui: {
            readonly focusRing: "cursor-pointer rounded-sm outline-none focus-visible:ring-3 focus-visible:ring-ring/50";
            readonly modalOverlaySelector: "[data-slot=dialog-overlay], [data-slot=alert-dialog-overlay]";
            readonly saveFailed: "저장하지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.";
        };
        messaging: {
            readonly listReplacedEvent: "refresher:list-replaced";
            readonly turnstileHost: "data-refresher-turnstile";
            readonly turnstileCancelEvent: "refresher-turnstile-cancel";
        };
        storage: {
            readonly blockDefaultsKey: "local:refresher:block:defaults";
            readonly modulesKey: "local:refresher:modules";
            readonly listLocks: {
                readonly blocks: "refresher:blocks";
                readonly memos: "refresher:memos";
                readonly moduleSettings: "refresher:module-settings";
            };
            readonly dbMetaKey: "local:refresher:db:meta";
            readonly dbKeys: {readonly ip: "local:refresher:db:ip"};
        };
        urls: {
            base: string; vote: string; comments: string; comments_submit: string; dccon_comments_submit: string;
            txtcon_submit: string; comment_remove: string;
            dccon: {lists: string; detail: string; buy: string; image: string; shop: string};
            database: {version: string; ip: string};
        };
        writerParams: {readonly uid: "rf_uid"; readonly ip: "rf_ip"};
        list: {list: string; paging: string; writerRow: string; listRow: string};
        block: {blurred: string; hidden: string; rowsHiddenEvent: string; blockedText: string};
        write: {submit: string; editor: string; subject: string; auto: string; leave: string; posted: string};
        refresh: {table: string; notice: string};
        defaultBadgeView: BadgeView;
    };
    readonly dAlphabet: string;
    readonly iconSiteKey: string;
    readonly commentSiteKeys: Record<"new" | "new3", string>;
    readonly blockDays: Record<"1" | "6" | "24" | "168" | "336" | "744", string>;
    readonly blockReasons: readonly (readonly ["0" | "1" | "2" | "3" | "4" | "5" | "6" | "7", string])[];
    readonly txtconBackgrounds: string[];
    readonly txtconColors: string[];
    storageMetadata: {
        typeNames: Record<"NICK" | "ID" | "IP" | "TITLE" | "TEXT" | "COMMENT" | "DCCON" | "TAB", string>;
        detectModeNames: Record<"SAME" | "CONTAIN" | "NOT_SAME" | "NOT_CONTAIN", string>;
        memoTypeNames: Record<"UID" | "NICK" | "IP", string>;
        defaultDetectMode: Record<BlockType, DetectMode>;
    };
    typedObjectKeys(value: object): string[];
    typedObjectEntries(value: object): [string, unknown][];
    typedObjectFromEntries(entries: Iterable<readonly [PropertyKey, unknown]>): Record<string, unknown>;
    typedArrayIncludes(array: readonly unknown[], item: unknown): boolean;
    assembleFeatureInfo(modules: Record<string, AnyModuleMeta>, icons: Record<string, AnyModuleIcons>, labels: Record<string, AnyModuleLabels>,
        fallbackIcon: AnyModuleIcons["module"]): ModuleInfo[];
    featureLoaders(modules: Record<string, () => Promise<AnyModule>>): Record<string, () => Promise<AnyModule>>;
    makeManifestCommands<T>(current: T, metas: AnyModuleMeta[], labels: AnyModuleLabels[]): T;
    featureFolders(root: string, file: string, services: object): string[];
    moduleTypeEntries(metaFolders: string[], moduleFolders: string[]): {path: string; text: string; tsReference?: boolean}[];
    featureStyleEntry(folders: string[]): {path: string; text: string};
    gzipBackup(text: string): Promise<Uint8Array<ArrayBuffer>>;
    gunzipBackup(data: Uint8Array<ArrayBuffer>): Promise<string>;
    createHttpClient(ky: KyInstance): {http: KyInstance; ajax: KyInstance};
    buildIpDatabase(services: object): Promise<void>;
    rawStorageKey<K extends string>(key: `local:${K}`): K;
    blockListKey(type: BlockType): `local:refresher:block:${BlockType}`;
    memoMapKey(type: MemoType): `local:refresher:memo:${MemoType}`;
    moduleSettingsKey(id: string): `local:refresher:module:${string}:settings`;
    moduleDataKey(id: string): `local:refresher:module:${string}:data`;
    moduleKeyModule(key: string): string | undefined;
    settingsKeyModule(key: string): string | undefined;
    isBlockListKey(key: string): boolean;
    createStorageDefinitions<T>(services: object): T;
    ownPreviewEntry(state: unknown, timeOrigin: number): PreviewEntry | null;
    ownPreviewDepth(timeOrigin: number): number;
    createTooltipViews<T>(services: object): T;
    createOverlayPopover(services: object): React.ComponentType;
    createColorInput<P>(react: typeof React): (props: P) => React.ReactNode;
    defineBackgroundFeature<M, Impl, Result>(meta: M, impl: Impl): Result;
    defineFeature<M extends AnyModuleMeta, Api>(meta: M, impl: ModuleImpl<M, Api>): DefinedModule<M["id"], Api>;
    moduleEnables(data: unknown): Record<string, unknown>;
    settingsOf(definition: Pick<ModuleDefinition, "settings">, value: unknown): Record<string, SettingValue>;
    createModuleRead(services: object): (ids: readonly string[]) => Promise<{enables: Record<string, unknown>; settings: Map<string, unknown>}>;
    createModuleHooks<T>(services: object): T;
    formBody(fields: Record<string, string | null | undefined | false>): URLSearchParams;
    csrfBody(fields: Record<string, string | null | undefined | false>): Promise<URLSearchParams>;
    dcBody(link: string, fields: Record<string, string | null | undefined | false>): Promise<URLSearchParams>;
    sha256(data: BufferSource): Promise<string>;
    once<A extends unknown[], T>(action: (...args: A) => Promise<T>): (...args: A) => Promise<T>;
    focusOnMount(element: HTMLElement | null): void;
    canonicalStorageValue(value: unknown): string | undefined;
    storageSizeOf(value: unknown): number | undefined;
    isRecord(value: unknown): value is Record<string, unknown>;
    installPageRuntime(): void;
    whenDomReady(run: () => void, signal?: AbortSignal): void;
    onBfcacheRestore(run: () => Promise<void>, signal?: AbortSignal): void;
    smoothScroll(): ScrollBehavior;
    followSystemAppearance(): void;
    followDcAppearance(root: HTMLElement): () => void;
    isGalleryManager(): boolean;
    loggedInUserId(): string | undefined;
    badgeViewOf(settings: ModuleSettings["userinfo"]): BadgeView;
    showsUid(view: BadgeView, icon?: string): boolean;
    loadedDocumentUrl(): URL;
    writerFilter(href: string | undefined, fields: Record<WriterFilter["kind"], string>): WriterFilter | null;
    queryString(name: string): string | null;
    rowPostNo(row: HTMLElement, gallery: string | null): string | undefined;
    buildIpCandidates(asns: Range<Asn>[], countries: Range<string>[], vpns: Range<true>[], kisa: Map<number, string>): Map<number, IpCandidate[]>;
    shortenOrg(name: string): string;
    createUiSlider<P>(services: object): (props: P) => React.ReactNode;
    createInputGroupAddon<P>(services: object): (props: P) => React.ReactNode;
    createFieldError<P>(services: object): (props: P) => React.ReactNode;
    createUiVariants<T>(name: string, cva: unknown): T;
    createUiAlert<T>(services: object): T;
    createUiAlertDialog<T>(services: object): T;
    createUiBadge<T>(services: object): T;
    createUiButton<T>(services: object): T;
    createUiCard<T>(services: object): T;
    createUiCheckbox<T>(services: object): T;
    createUiCollapsible<T>(services: object): T;
    createUiDialog<T>(services: object): T;
    createUiEmpty<T>(services: object): T;
    createUiField<T>(services: object): T;
    createUiInput<T>(services: object): T;
    createUiInputGroup<T>(services: object): T;
    createUiKbd<T>(services: object): T;
    createUiLabel<T>(services: object): T;
    createUiPopover<T>(services: object): T;
    createUiRadioGroup<T>(services: object): T;
    createUiSelect<T>(services: object): T;
    createUiSeparator<T>(services: object): T;
    createUiSkeleton<T>(services: object): T;
    createUiSpinner<T>(services: object): T;
    createUiSwitch<T>(services: object): T;
    createUiTable<T>(services: object): T;
    createUiTabs<T>(services: object): T;
    createUiTextarea<T>(services: object): T;
    createUiToggle<T>(services: object): T;
    createUiToggleGroup<T>(services: object): T;
    createUiTooltip<T>(services: object): T;
    listenPreviewBackground(services: object): void;
    listenListBridge(services: object): void;
    hasSenderTab(sender: Browser.runtime.MessageSender): boolean;
    tabTarget(sender: Browser.runtime.MessageSender & {tab: {id: number}}): Browser.scripting.InjectionTarget;
    createNotify(getState: () => object): {
        reportFailure(error: unknown, label: string, message: string): void;
        notifyManage(request: Promise<ManageResult>, done: string, fallback: string): Promise<boolean>;
    };
    createErrorUtilities(services: object): {
        isTimeoutError(error: unknown): boolean;
        isAbortError(error: unknown): boolean;
        messageOf(error: unknown): string;
        friendlyMessage(error: unknown): string;
        saveOrReload(write: Promise<void>, reload: () => Promise<void>, label: string): Promise<void>;
    };
    originalEventTarget(event: Event): EventTarget | null;
    createTyping(overlay: {portal?: HTMLElement}, selector: string): (event: Event) => boolean;
    fitPolls(root: HTMLElement): () => void;
    fitTxtconNearView(box: HTMLElement): () => void;
    watchGifVideos(root: HTMLElement): () => void;
    createScrollLock(react: typeof React): (active: boolean) => void;
    createCountDown(services: object): () => React.ReactNode;
    createPreviewMediaHooks(services: object): {
        usePreviewMedia(ref: React.RefObject<HTMLElement | null>, html: string | undefined, gallery: string | undefined, hideOnly?: boolean): void;
        markBlockedDccons(root: HTMLElement, gallery: string | undefined, block: ModuleSettings["block"] | undefined, hideOnly: boolean): void;
    };
    keepTabInside(event: React.KeyboardEvent<HTMLDivElement>): void;
    createDialogs<T>(services: object): T;
    focusedElement(): HTMLElement | null;
    panelOf(from?: Element | null): HTMLElement | null | undefined;
    focusPanel(from?: Element | null): void;
    createReturnFocus(react: typeof React): (popup: React.RefObject<HTMLElement | null>) => {
        keyboard: boolean;
        finalFocus(): HTMLElement | true;
        restoreFocus(): void;
        returnFocus(): void;
    };
    createToastHost(services: object): () => React.ReactNode;
    savedNonmember(): {nick: string; pw: string};
    saveNonmember(nick: string, password: string): void;
    fcmToken(): Promise<string | undefined>;
    createCommentSubmission<T>(services: object): T;
    createCommentSender<T>(services: object): T;
    createSettingsState<T>(services: object): T;
    startAutoBackup(services: object): void;
    fillBlockIds(services: object): Promise<void>;
    startDatabaseUpdates(services: object): (force?: boolean) => Promise<void>;
    createDcconPicker<P>(services: object): (props: P) => React.ReactNode;
    createDcconInfoView<T>(services: object): T;
    createSanitizer(factory: () => object): (html: string, options?: {stripMedia?: boolean}) => string;
    createPreviewPopups<T>(services: object): T;
    createUserBubble<T>(services: object): T;
    createMemoDialog(services: object): () => React.ReactNode;
    createBlockRequest(services: object): (options: BlockRequestOptions, selected: SelectedUser) => Promise<void>;
    createDcconRequests<T>(services: object): T;
    createManageRequests<T>(services: object): T;
    txtconGraphemes(text: string): string[];
    wrapTxtcon(text: string): string;
    normalizeTxtcon(text: string): string;
    createTxtconSubmission<T>(services: object): T;
    startContentScript(ctx: ContentScriptContext, services: object): Promise<void>;
    createBackgroundStartup(services: object): () => void;
    startBackgroundModules(modules: BackgroundModule[], services: object): () => Promise<void>;
    createWriteCommentView(services: object): () => React.ReactNode;
    extractCommentIcon(html: string | undefined): string | undefined;
    extractCommentIp(html: string | undefined): string | undefined;
    createCommentView<T>(services: object): T;
    threadParents(comments: ProcessedComment[], order?: CommentOrderValue): ProcessedComment[];
    commentSummary(comments: ProcessedComment[]): {counts: string; hidden: string};
    groupCommentReplies(replies: ProcessedComment[], prev?: ReadonlyMap<string, ProcessedComment[]>): Map<string, ProcessedComment[]>;
    mentionComment(comments: ProcessedComment[], from: string, mention: string): ProcessedComment | undefined;
    createCommentList<T>(services: object): T;
    readonly numberedImageSource: RegExp;
    readonly adultPost: string;
    readonly secretPost: string;
    parsePostInfo(html: string): PostInfo | undefined;
    createPreviewRequests<T>(services: object): T;
    ipCandidateKey(candidate: IpCandidate): string;
    encodeIpData(prefixes: ReadonlyMap<number, readonly IpCandidate[]>): CompactIpData;
    parseIpData(text: string): CompactIpData | null;
    parseStoredIpData(text: string): CompactIpData;
    createIpLookup(data: CompactIpData): (ip: string) => IpCandidate[] | undefined;
    createBlockRules<T>(services: object): T;
    createListDom<T>(services: object): T;
    createUiStateService<T>(services: object): T;
    createGallog<T>(services: object): T;
    createGallogBackground(services: object): () => void;
    createWriterCard<P>(services: object): (props: P) => React.ReactNode;
    createGallogActivityHook<T>(services: object): (uid: string | undefined) => T;
    createPreviewRows(services: object): {
        buildPreData(element: HTMLElement): GalleryPreData | null;
        isBlurHidden(element: Element): boolean;
        listRows(): ListRow[];
        adjacentPreData(from: GalleryPreData, dir: number): GalleryPreData | null;
        isTextPost(pre: GalleryPreData): boolean;
    };
    bindPreviewRows(ctx: PreviewContext, handlers: object, services: object): void;
    bindPreviewListKeys(ctx: PreviewContext, open: (preData: GalleryPreData) => void, services: object): void;
    createImageViewer<P>(services: object): (props: P) => React.ReactNode;
    createVotes<P>(services: object): (props: P) => React.ReactNode;
    createWheelGesture(react: typeof React): (key: string, goToAdjacent: (dir: number) => void,
        canGo: (dir: number) => boolean, enabled: boolean) => {onWheel(ev: React.WheelEvent<HTMLDivElement>): void; hint: {dir: number; key: string}};
    createContentsControls(services: object): {
        clickContents(ev: React.MouseEvent<HTMLElement>, viewer: boolean): void;
        pressContents(ev: React.KeyboardEvent<HTMLElement>): void;
    };
    createDatabase<T>(services: object): T;
    backupTargetKeys(
        modulesKey: string,
        defaultsKey: string,
        blockTypes: readonly BlockType[],
        memoTypes: readonly MemoType[],
        moduleIds: readonly string[]
    ): string[];
    createCloudBackup<T>(services: object): T;
    createCommentsPull<T>(ctx: PreviewContext, signal: () => AbortSignal | undefined, services: object): T;
    createCommentProcessing(services: object): {
        prepareComments(raw: DcinsideComment[], preData: GalleryPreData, archive: boolean, truncated?: boolean): DcinsideComment[];
        processComments(raw: DcinsideComment[], preData: GalleryPreData,
            block: ModuleSettings["block"] | undefined, ratios?: Ratios | null): Promise<ProcessedComment[]>;
    };
    setupBlock(ctx: BlockContext, services: object): {
        isRevealed(): boolean;
        hiddenCount(): number;
        toggleReveal(): void;
    };
    createBlockControls(): {
        shortcuts: {blockReveal: (api: ReturnType<KotlinBridge["setupBlock"]>) => void};
        pageToggles: {reveal: {
            desc: (api: ReturnType<KotlinBridge["setupBlock"]>) => string;
            isOn: (api: ReturnType<KotlinBridge["setupBlock"]>) => boolean;
            toggle: (api: ReturnType<KotlinBridge["setupBlock"]>) => void;
        }};
    };
    createPreviewStateService<T>(services: object): T;
    createPreviewPostService(services: object): {
        textBlockOf(preData: GalleryPreData, post: PostInfo): PostInfo["textBlocked"];
        cachedPost(preData: GalleryPreData): {post: PostInfo; age: number} | undefined;
        createPostLoader(ctx: PreviewContext): {
            processContents(preData: GalleryPreData, post: PostInfo, stripMedia?: boolean): Promise<PostInfo>;
            requestPost(preData: GalleryPreData): Promise<PostInfo>;
            getPost(preData: GalleryPreData): Promise<{post: PostInfo; fresh: boolean; archived?: true}>;
            hasPending(): boolean;
            abortPending(): void;
        };
    };
    createPreviewHistoryController(ctx: PreviewContext, services: object): {
        restore(fromHistory: boolean): void;
        remember(back: SavedHistory): void;
        setBack(back: SavedHistory | null): void;
        replaceEntry(state: unknown, url?: string): boolean;
        back(): SavedHistory | null;
        reopenFromEntry(open: (preData: GalleryPreData) => void): void;
    };
    createSettingsTransfer(services: Record<string, unknown>): {
        write(data: Record<string, unknown>, mode: "replace" | "merge"): Promise<number | undefined>;
        merge(current: Record<string, unknown>, backup: Record<string, unknown>): Record<string, unknown>;
        parse(input: string): Record<string, unknown>;
    };
    pinDefaultMode(list: BlockEntry[], from: DetectMode, local: DetectMode): BlockEntry[];
    createAboutTab(services: Record<string, unknown>): (props: {logo: string; version: string}) => React.ReactNode;
    optionFormatTime(time: number): string;
    optionFormatBytes(bytes: number): string;
    optionDownloadJson(name: string, data: unknown): void;
    createImportDialog(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createRefresherSelect(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createOptionWidgets(services: Record<string, unknown>): Record<"Section" | "SearchInput" | "EmptyList", (props: unknown) => React.ReactNode>;
    useOptionStorageItem<T>(react: typeof React, item: object): T;
    optionStoreState(set: (next: Partial<{rain: number; notice: string | null; startRain: () => void; endRain: () => void}>) => void):
        {rain: number; notice: string | null; startRain: () => void; endRain: () => void};
    notifyOptionDone(showToast: (...args: never[]) => void, done: string | {message: string; undo: () => void}): void;
    createDcconRain(react: typeof React, getState: () => object, packs: readonly unknown[]): () => React.ReactNode;
    needOverlayWhen<S>(store: {getState(): S; subscribe(listener: (state: S) => void): () => void}, needed: (state: S) => boolean): void;
    overlayNeeded(): boolean;
    watchOverlayDemands(listener: () => void): () => void;
    mountOverlayWhenNeeded(ctx: ContentScriptContext, services: Record<string, unknown>): void;
    createContentRoot(services: Record<string, unknown>): () => React.ReactNode;
    createPreviewHost(react: typeof React, frame: React.ComponentType,
        popups: React.ComponentType): () => React.ReactNode;
    setupUserInfo(ctx: {signal: AbortSignal; settings: ModuleSettings["userinfo"]}, services: Record<string, unknown>): Promise<{checkNewPosts(elements: HTMLElement[]): void}>;
    setupPreview(ctx: Pick<ModuleContext, "signal" | "addCleanup"> & {settings: ModuleSettings["preview"]},
        services: Record<string, unknown>): {archiveArticle(): boolean; isOpen(): boolean};
    createModuleRegistry(services: Record<string, unknown>): {
        getModuleApi<K extends keyof ModuleApis>(id: K): ModuleApis[K] | undefined;
        runShortcut(command: string): void;
        pageToggleStates(): PageToggleState[];
        pageState(): PageState;
        settledPageState(): Promise<PageState>;
        runPageToggle(action: PageAction): void;
        stopAll(): void;
        loadAll(metas: AnyModuleMeta[], loaders: ModuleLoaders, signal: AbortSignal, beforeStart?: BeforeStart): Promise<void>;
    };
    blockLabel(entry: BlockEntry): string;
    emptyBlockEntries(): Record<BlockType, BlockEntry[]>;
    createBlockState<S>(set: (change: (state: S) => Partial<S>) => void, get: () => S, services: Record<string, unknown>): S;
    createMemoState<S>(lists: () => object): S;
    randomMemoColor(): string;
    normalizeMemoMap(value: unknown): Record<string, MemoEntry>;
    ownMemo(map: Record<string, MemoEntry>, key: string): MemoEntry | undefined;
    lookupMemo(memos: Record<MemoType, Record<string, MemoEntry>>, user: {uid?: string; ip?: string; nick?: string}, gallery?: string | null): MemoEntry | undefined;
    createStorageSync(services: Record<string, unknown>): {
        watch<T>(key: StorageItemKey, callback: (next: T | null, previous: T | null) => void, signal?: AbortSignal): () => void;
        read(keys: readonly StorageItemKey[]): Promise<Map<StorageItemKey, unknown>>;
        sync<K extends StorageItemKey>(keys: readonly K[], onChange: (key: K, value: unknown) => void): StorageSync;
        lists<T extends string, V>(options: ListSyncOptions<T, V>): StorageSync & {update(type: T, change: (value: V) => V): Promise<void>};
    };
    writerSearchUrl(filter: WriterFilter | null, nick?: string): string;
    keepWriterFilter(element: Element, filter: WriterFilter): void;
    addWriterTypes(filter: WriterFilter | null, search: (filter: WriterFilter) => void, signal: AbortSignal): void;
    removeWriterTypes(): void;
    listenSearchBackground(services: Record<string, unknown>): void;
    searchMeta(urls: RegExp[]): {
        id: "search";
        urls: RegExp[];
        defaultEnable: false;
        settings: StaticFeatureSettingsMap["search"];
    };
    setupSearch(ctx: Pick<ModuleContext, "signal" | "onRevoke"> & {settings: ModuleSettings["search"]},
        services: Record<string, unknown>): {searchUserPosts(user: SelectedUser, by: "nick" | "identity"): void};
    previewPostKey(preData: Pick<GalleryPreData, "gallery" | "id" | "link">): string;
    createPreviewCache(create: (maxSize: number, maxAge: number) => object): {
        get(preData: GalleryPreData): CacheEntry | undefined;
        set(preData: GalleryPreData, patch: CacheEntry): void;
        restoreArchive(preData: GalleryPreData, list: DcinsideComment[], truncated?: boolean): DcinsideComment[];
    };
    readonly uploadOptionsKey: string;
    hookUploads(limit: (concurrency: number) => object): void;
    writeMeta(urls: RegExp[]): {
        id: "write";
        urls: RegExp[];
        defaultEnable: false;
        settings: StaticFeatureSettingsMap["write"];
    };
    setupWrite(ctx: Pick<ModuleContext, "signal" | "onRevoke"> & {settings: ModuleSettings["write"]}, services: Record<string, unknown>): void;
    listenWriteBackground(services: Record<string, unknown>): void;
    createImageSearch(services: Record<string, unknown>): {listen(): void; apply(state: {enabled: boolean; settings: object}): Promise<void>};
    createPreviewError(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createPreviewClock(services: Record<string, unknown>): {
        TimeStamp(props: {date: string; size?: "1" | "2"}): React.ReactNode;
        useTick(ms: number): void;
    };
    createPreviewFrame(services: Record<string, unknown>): () => React.ReactNode;
    createPreviewDialogRoot(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    restoreOptionList<I>(current: readonly I[], removed: readonly {index: number; item: I}[], key: (item: I) => string): I[];
    createOptionListRow(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createOptionListTabs(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createSettingItem(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createMemoTab(services: Record<string, unknown>): () => React.ReactNode;
    parseAppMemos(text: string): {memos: Record<"UID" | "IP", Record<string, string>>; skipped: number};
    formatAppMemos(memos: Record<MemoType, Record<string, MemoEntry>>): {text: string; count: number; skipped: number};
    createBlockDialog(services: Record<string, unknown>): (props: unknown) => React.ReactNode;
    createBlockTab(services: Record<string, unknown>): () => React.ReactNode;
    optionDcconImage(entry: BlockEntry): string;
    createDataTab(services: Record<string, unknown>): () => React.ReactNode;
    createGeneralTab(services: Record<string, unknown>): () => React.ReactNode;
    createShortcutTab(services: Record<string, unknown>): () => React.ReactNode;
    matchOptionSettings(feature: object, needle: string): unknown;
    createOptionsPage(services: Record<string, unknown>): () => React.ReactNode;
    createPopup(services: Record<string, unknown>): () => React.ReactNode;
    mountExtensionPage(react: typeof React, rootFactory: typeof createRoot,
        app: React.ComponentType, provider?: React.ComponentType<{children?: React.ReactNode}>): void;
    readonly filterSkipAttr: string;
    addDomFilter(scope: string, callback: (element: HTMLElement) => void): () => void;
    moduleEnabled(id: string, defaultEnable: boolean | undefined, enables: Record<string, unknown>): boolean;
    defaultSetting(schema: SettingSchema): SettingValue;
    normalizeSetting(schema: SettingSchema, value: unknown): SettingValue;
    scanBlocks(
        entries: BlockEntry[],
        defaultMode: DetectMode,
        type: BlockType,
        content: string,
        gallery: string | undefined,
        onEntry?: (entry: BlockEntry, kind: "hit" | "allow" | "miss") => void
    ): boolean;
    previewEntry(state: unknown): PreviewEntry | null;
    createRefreshLoader(
        ctx: RefreshContext, state: RefreshState, unseen: ReturnType<typeof createUnseenCounter>, gallery: string, helpers: RefreshLoaderHelpers
    ): {
        load(url?: string, force?: boolean): Promise<boolean>;
        failures(): number;
        currentUrl(): string;
        abort(): void;
    };
    setupRefresh(ctx: RefreshContext, helpers: RefreshModuleHelpers): {
        refreshLists(): Promise<void>;
        togglePause(): void;
        isPaused(): boolean;
        reload(): Promise<void>;
        catchUp(): void;
    };
    refreshMeta<S extends Record<string, unknown>>(urls: readonly RegExp[], settings: S): {
        id: "refresh";
        urls: RegExp[];
        settings: S;
        toggles: readonly ["pause"];
        commands: {
            refreshLists: {key: "Alt+R"};
            refreshPause: {key: "Alt+S"};
        };
    };
    createRefreshControls(): {
        shortcuts: {
            refreshLists: (api: ReturnType<KotlinBridge["setupRefresh"]>) => void;
            refreshPause: (api: ReturnType<KotlinBridge["setupRefresh"]>) => void;
        };
        pageToggles: {pause: {
            desc: string;
            isOn: (api: ReturnType<KotlinBridge["setupRefresh"]>) => boolean;
            toggle: (api: ReturnType<KotlinBridge["setupRefresh"]>) => void;
        }};
    };
    readonly refreshCountCells: string;
    readonly refreshCountText: RegExp;
    clearNewPost(row: HTMLElement): void;
    syncRefreshPaging(document: Document, selector: string): void;
    isWholeFirstPage(url: string, lookupWriter: typeof writerFilter): boolean;
    replaceRefreshList(oldList: HTMLElement, newList: HTMLElement, options: ReplaceListOptions, helpers: RefreshListHelpers): {
        added: HTMLTableRowElement[];
        changed: boolean;
    };
    dcinsideHref(url: string): string | null;
    dcinsideHrefInput(url: string | URL | null | undefined): string | null;
    galleryKind(url: string): "normal" | "minor" | "mini" | "person";
    galleryPath(url: string): string;
    galltypeOf(url: string): string;
    managerAjaxUrl(url: string, action: string): string;
    listUrl(url: string): string;
    mergeParamURL(origin: string, from: string): string;
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
    setupFonts(ctx: FontsContext): void;
    setupLayout(ctx: LayoutContext, options: Record<string, {selector: string}>, viewPage: boolean, queryString: (name: string) => string | null): void;
    stealthMeta(): {
        id: "stealth";
        defaultEnable: false;
        toggles: readonly ["reveal"];
        commands: {stealthPause: {key: "Alt+P"}};
    };
    setupStealth(ctx: ModuleContext<{}>): {isRevealed: () => boolean; toggle: () => void};
    createStealthControls(showToast: (message: string) => void): {
        shortcuts: {stealthPause: (api: ReturnType<KotlinBridge["setupStealth"]>) => void};
        pageToggles: {reveal: {
            desc: string;
            isOn: (api: ReturnType<KotlinBridge["setupStealth"]>) => boolean;
            toggle: (api: ReturnType<KotlinBridge["setupStealth"]>) => void;
        }};
    };
    setTitleCount(count: number): void;
    createUnseenCounter(ctx: RefreshContext, selector: string, noticeIcon: string, blurredRows: string, readPostNo: typeof rowPostNo): {
        count(rows: HTMLElement[]): void;
        clearTitle(): void;
        clearMark(): void;
        showMark(): void;
    };
    textWithoutScripts(element: Element | null | undefined): string;
    writeStyle(id: string, css: string): () => void;
    setAppearance(element: HTMLElement, dark: boolean): void;
    isBrokenMedia(image: HTMLImageElement): boolean;
    markPreviewMedia(media: HTMLElement, imageViewer: boolean): void;
    markPreviewPressable(root: HTMLElement, imageViewer: boolean): void;
}

export const kotlinBridge = (globalThis as typeof globalThis & {__DCR_KOTLIN__: KotlinBridge}).__DCR_KOTLIN__;
