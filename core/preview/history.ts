import type {GalleryPreData} from "@/core/preview/types";
import {kotlinBridge} from "@/kotlin/bridge";

/** 미리보기를 열기 전 위치. 닫을 때 여기로 돌아간다. */
export interface SavedHistory {
    title: string;
    url: string;
    state: unknown;
}

/** 미리보기가 글을 열며 쌓은 기록 항목 (history.state). */
export interface PreviewEntry {
    refresher: 1;
    /** 쌓은 문서의 performance.timeOrigin. 새로고침한 페이지에 남은 예전 항목과 가린다. */
    doc: number;
    preData?: GalleryPreData;
    back?: SavedHistory;
    /** 미리보기를 열기 전 기록에서 몇 칸 위인지. */
    depth?: number;
    /** 목록 문서를 다시 불러온 뒤 이 글 미리보기를 다시 열지. */
    reopen?: boolean;
}

/** 이 문서를 가리는 값. 미리보기가 쌓는 항목의 doc에 넣는다. */
export const historyDoc = performance.timeOrigin;

/** history.state에서 미리보기 항목의 유효한 필드만 읽는다. */
export const previewEntry = kotlinBridge.previewEntry;

export const ownPreviewEntry = (state: unknown): PreviewEntry | null => kotlinBridge.ownPreviewEntry(state, historyDoc);
export const ownPreviewDepth = (): number => kotlinBridge.ownPreviewDepth(historyDoc);
