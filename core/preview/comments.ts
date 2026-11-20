import QuickLRU from "quick-lru";
import {kotlinBridge} from "@/kotlin/bridge";

import {blockMode, duplicateOf, groupDuplicates, isAnyBlocked, isBlocked} from "@/core/block";
import {lowActivityOf} from "@/stores/ui";
import {htmlToText, sanitizeHtml} from "@/utils/sanitize";

import {restoreArchive} from "./cache";
import type {DcinsideComment} from "./types";

export interface ProcessedComment extends DcinsideComment {
    memo: string;
    /** 음성댓글. iframe이면 src가 음성 파일이 아니라 플레이어 페이지라 <audio>로 틀 수 없다. */
    voice?: { src: string; iframe: boolean };
    /** 가리는 방식 (blur 흐리게, hide 숨김). */
    blocked?: "blur" | "hide";
    /** 깡계라서 가렸다. */
    lowActivity?: true;
    /** 같은 댓글 묶음. 첫 댓글은 반복 수, 나머지는 0이며 접힌다. */
    duplicates?: number;
}

export const {prepareComments, processComments} = kotlinBridge.createCommentProcessing({
    createCache: () => new QuickLRU<string, string>({maxSize: 2000}),
    blockMode, duplicateOf, groupDuplicates, isAnyBlocked, isBlocked, lowActivityOf, htmlToText, sanitizeHtml, restoreArchive
});

export const sameComments = kotlinBridge.sameComments;
