import {create, type StateCreator, type StoreApi} from "zustand";

import {kotlinBridge} from "@/kotlin/bridge";
import {needOverlayWhen} from "@/components/overlay/demands";
import type {GallogActivity, RatioInfo} from "@/core/gallog";
import {pageGallery} from "@/core/http/urls";
import type {ModuleSettings} from "@/core/module/types";
import type {MemoType} from "@/core/storage/types";
import {eventTarget} from "@/utils/event";

type ToastLevel = "info" | "error" | "warning";

export interface ToastData {
    content: string;
    type: ToastLevel;
    autoClose: number;
    /** 토스트에 붙는 버튼 (되돌리기 등). 누르면 토스트를 닫고 run을 부른다. */
    action?: { label: string; run: () => void };
}

export interface SelectedUser {
    nick?: string;
    uid?: string;
    ip?: string;
    /** 우클릭한 디시콘 코드 (dccon.php?no= 값). */
    dccon?: string;
    /** 갤러리 한정 차단, 메모에 쓰는 갤러리. 미리보기 글에서 연 버블은 그 글의 갤러리를 넘긴다 (selectedGallery). */
    gallery?: string;
}

export interface MemoTargetState {
    targets: Partial<Record<MemoType, string>>;
    initialType: MemoType;
    /** 버블에서 넘어온 갤러리 (SelectedUser.gallery). */
    gallery?: string;
}

/** 미리보기도 쓰는 userinfo의 글댓비 캐시와 깡계 설정. action은 깡계 처리(태그, 흐리게, 숨기기)다. */
export interface Ratios {
    cache: Record<string, RatioInfo>;
    alarm: number;
    action: ModuleSettings["userinfo"]["lowActivityAction"];
}

interface UiState {
    /** 오버레이가 아직 받지 않은 토스트. 마지막이 가장 최근 것이다. 띄우고 닫는 것은 ToastHost(Base UI Toast)가 맡는다. */
    toasts: ToastData[];
    selected: SelectedUser | null;
    bubble: { x: number; y: number } | null;
    memo: MemoTargetState | null;
    /** 글댓비 캐시와 깡계 기준, 처리 (userinfo). 글댓비 표시를 끄거나 모듈이 꺼져 있으면 null. 지난 값도 있으니 isFresh로 가려 읽는다. */
    ratios: Ratios | null;
    /** 차단 모듈의 '가린 내용 보기' (이 페이지에서만, 저장하지 않는다). 미리보기도 따른다. 모듈이 꺼져 있으면 false다. */
    blockRevealed: boolean;

    showToast: (content: string, type?: ToastLevel, autoClose?: number, action?: ToastData["action"]) => void;
    /** user를 고르고 (x, y)에 유저 버블을 연다. 버블의 메모, 차단은 고른 대상에 건다. */
    openBubble: (user: SelectedUser, x: number, y: number) => void;
    closeBubble: () => void;
    /** 버블에서 고른 대상의 메모 다이얼로그를 연다. */
    openMemo: (user: SelectedUser) => void;
    closeMemo: () => void;
}

interface UiStateService {
    init: StateCreator<UiState>;
    attach(store: StoreApi<UiState>): void;
    selectedGallery(gallery: string | undefined): string | null;
    isLowActivity(ratio: GallogActivity, alarm: number): boolean;
    isFresh<T extends {date: number}>(info?: T): info is T;
    lowActivityOf(ratios: Ratios | null, uid: string | undefined): "blur" | "hide" | undefined;
    copyText(text: string, done: string, failed: string): Promise<void>;
    openWriterBubble(event: MouseEvent): void;
}

const state = kotlinBridge.createUiStateService<UiStateService>({pageGallery, eventTarget, needOverlayWhen});
export const {selectedGallery, isLowActivity, isFresh, lowActivityOf, copyText, openWriterBubble} = state;
export const useUiStore = create<UiState>(state.init);
state.attach(useUiStore);
