import {create, type StateCreator, type StoreApi} from "zustand";
import {kotlinBridge} from "@/kotlin/bridge";

import {needOverlayWhen} from "@/components/overlay/demands";
import type {TurnstileResult} from "@/core/messaging/protocol";
import {postKey, setEntry} from "@/core/preview/cache";
import type {ProcessedComment} from "@/core/preview/comments";
import type {BlockOptions} from "@/core/preview/request";
import type {GalleryPreData, PostInfo, TurnstileConfig} from "@/core/preview/types";

export interface ErrorState {
    detail: string;
    /** HTTP 상태 코드 (HTTPError일 때). */
    status?: number;
    /** 성인 인증이 필요한 글 (비로그인, 미인증이면 본문 대신 인증 안내가 온다). */
    adult?: boolean;
    /** 미니 갤러리 비밀글 (비밀번호는 원문에서만 넣을 수 있다). */
    secret?: boolean;
}

export type CommentOrderValue = "oldest" | "newest" | "replies";

type Reply = { commentNo: string | null; replyNo: string | null };

/** 크게 보기(ImageViewer)의 이미지. pop은 dcinsideHref로 거른 디시 원본 보기 주소 (core/preview/parser.ts가 옮겨 둔 data-pop). */
export interface ViewerImage {
    src: string;
    alt: string;
    pop?: string;
}

/** 게시글을 새로 열 때마다 초기화되는 상태. */
interface PostState {
    error: ErrorState | undefined;
    post: PostInfo | undefined;
    /** 받지 못해 보존해 둔 본문을 보이는 중 ('삭제된 글과 댓글 보존'). */
    archived: boolean;

    comments: ProcessedComment[] | undefined;
    /** 댓글을 받지 못했다. comments가 없는 동안 받는 중 표시 대신 '다시 시도' 안내를 보인다. */
    commentsFailed: boolean;
    /** 댓글, 답글 쓰기 허용. 멤버만 댓글을 쓸 수 있는 갤러리면 댓글 응답의 allow_reply가 0이다. */
    allowReply: boolean;
    collapsed: Set<string>;
    /** 이 글에서만 가린 댓글(숨김 차단, 접힌 같은 댓글) 보기. 댓글 머리의 요약에서 켜고 끈다. 페이지의 '가린 내용 보기'와 따로다. */
    commentsRevealed: boolean;
    /** 댓글 새로고침으로 새로 들어온 댓글 번호. 잠깐 강조한다 (Comment.tsx의 animate-fresh-comment). */
    freshComments: ReadonlySet<string>;
    reply: Reply;
    /** 댓글만 보기 (목록의 댓글 수 링크로 열었을 때). */
    commentsOnly: boolean;
    /** 본문 이미지 차단 (blockImage). */
    imageBlocked: boolean;

    /** 차단 창. post는 글쓴이, comments는 고른 댓글(selectedComments)의 작성자를 차단한다. */
    blockPopup: false | "post" | "comments";
    /** 관리자가 체크한 댓글 번호. 디시 댓글 목록의 체크박스처럼 골라 한 번에 지우거나 차단한다. */
    selectedComments: ReadonlySet<string>;
    /** 댓글 삭제 확인 창(비밀번호 삭제는 비밀번호 창)을 띄운 댓글. 창은 CommentList가 한 번만 그린다 (Comment.tsx의 CommentDeletePrompt). */
    deletePrompt: { no: string; kind: "confirm" | "password" } | null;
    /** 삭제 요청 중인 댓글 번호. 그동안 그 댓글의 X를 막는다. */
    deletingComments: ReadonlySet<string>;
    /** 본문 이미지 크게 보기. 글을 넘기거나 닫으면 닫힌다. */
    viewer: { images: ViewerImage[]; index: number } | null;
}

/** 컨트롤러(index.ts)가 UI에 넘기는 동작. 모듈이 켜질 때 setState로 넣고, 정리할 때 NO_HOOKS로 되돌린다. */
interface Hooks {
    requestOpen: (preData: GalleryPreData, commentsOnly?: boolean, dir?: number) => void;
    requestClose: () => void;
    /** 댓글만 다시 받는다. report: 실패를 알린다. */
    requestRefresh: (report?: boolean) => Promise<void>;
    /** 본문을 캐시 없이 다시 받고 댓글도 다시 받는다. */
    requestReload: () => Promise<void>;
    /** 차단하고 성공 여부를 돌려준다. comments가 있으면 그 댓글 작성자를 차단한다. 글도 지웠으면 창을 닫는다. */
    requestBlock: (preData: GalleryPreData, options: BlockOptions, comments?: string[]) => Promise<boolean>;
}

interface PreviewState extends PostState, Hooks {
    visible: boolean;
    /** 우클릭을 누른 순간 오버레이를 미리 띄운다 (윈도우는 떼야 contextmenu가 온다). */
    warm: boolean;
    /** 닫힌 뒤 페이드아웃 중 (200ms). */
    fading: boolean;
    preData: GalleryPreData | null;
    /** 열고 닫을 때마다 오른다. 늦게 도착한 이전 글의 응답을 버리는 데 쓴다. */
    signalId: number;

    captcha: { url: string; resolve: (code: string) => void } | null;
    turnstile: TurnstilePrompt | null;

    /** 정보 창(DcconInfoPopup)을 띄운 디시콘 코드. null이면 닫혀 있다. */
    dcconInfo: string | null;
    /** 댓글 스레드 순서. 글을 넘겨도 그대로 두고 저장하지 않는다. */
    commentOrder: CommentOrderValue;

    /** 글 상태를 비우고 patch를 얹어 한 번의 setState로 연다. */
    open: (preData: GalleryPreData, patch?: Partial<PostState>) => void;
    close: () => void;
    toggleCollapse: (no: string) => void;
    toggleSelected: (no: string) => void;
    openCaptcha: (url: string) => Promise<string>;
    openTurnstile: (config: TurnstileConfig) => Promise<TurnstileResult | undefined>;
}

/** 확인을 눌러야 하는 Turnstile 창 (TurnstilePopup). 닫으면 undefined로 끝낸다. */
export interface TurnstilePrompt {
    config: TurnstileConfig;
    resolve: (result: TurnstileResult | undefined) => void;
}

interface PreviewStateService {
    init: StateCreator<PreviewState>;
    attach(store: StoreApi<PreviewState>): void;
    noReply: Reply;
    noFresh: ReadonlySet<string>;
    noHooks: Hooks;
    toggled(set: ReadonlySet<string>, no: string, on?: boolean): Set<string>;
    parseDate(value: string): Date;
    postTitle(post: PostInfo): string;
    previewGallery(): string | undefined;
    refreshCommentsOf(preData: GalleryPreData): void;
}

const state = kotlinBridge.createPreviewStateService<PreviewStateService>({needOverlayWhen, postKey, setEntry});
export const {
    noReply: NO_REPLY, noFresh: NO_FRESH, noHooks: NO_HOOKS,
    toggled, parseDate, postTitle, previewGallery, refreshCommentsOf
} = state;

export const usePreviewStore = create<PreviewState>(state.init);
state.attach(usePreviewStore);
