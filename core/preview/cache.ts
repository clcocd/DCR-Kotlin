import QuickLRU from "quick-lru";

import {kotlinBridge} from "@/kotlin/bridge";

import type {CommentListResponse, DcinsideComment, PostInfo} from "./types";

export interface CacheEntry {
    post?: PostInfo;
    /** post를 받은 시각(ms). 댓글 보존이 항목을 다시 저장하면 수명이 늘어나므로 본문이 얼마나 낡았는지는 이것으로 본다. */
    fetchedAt?: number;
    /** 삭제 댓글 보존용. 지금까지 받은 댓글 전부이며, 서버 목록에서 빠진 댓글은 삭제된 것으로 되살린다. */
    seen?: Record<string, DcinsideComment>;
    /** 마지막으로 그린 댓글 목록. 캐시로 다시 열면 새로 받는 동안 이것을 먼저 보인다. */
    comments?: CommentListResponse;
    /** comments를 디시에서 받은 시각(ms). 방금 받은 목록이면 다시 열어도 다시 받지 않는다. */
    commentsAt?: number;
}

const cache = kotlinBridge.createPreviewCache((maxSize, maxAge) => new QuickLRU<string, CacheEntry>({maxSize, maxAge}));

export const postKey = kotlinBridge.previewPostKey;
export const getEntry = cache.get;
export const setEntry = cache.set;
export const restoreArchive = cache.restoreArchive;
