import QuickLRU from "quick-lru";
import {kotlinBridge} from "@/kotlin/bridge";

import {blockMode, duplicateOf, groupDuplicates, isAnyBlocked, isBlocked} from "@/core/block";
import type {ModuleSettings} from "@/core/module/types";
import {lowActivityOf, type Ratios} from "@/stores/ui";
import {htmlToText, sanitizeHtml} from "@/utils/sanitize";

import {restoreArchive} from "./cache";
import type {DcinsideComment, GalleryPreData} from "./types";

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

const GALLOG_DCCON = /dcimg5\.dcinside\.com\/dccon\.php\?no=(\w*)/g;

// 정화는 입력의 순수 함수라 기억해 둔다. HTML 정화 자체는 기존 DOMPurify를 유지한다.
const cleaned = new QuickLRU<string, string>({maxSize: 2000});
const sanitizeMemo = (memo: string): string => sanitizeHtml(kotlinBridge.preprocessCommentMemo(memo));

// 평문도 입력의 순수 함수라 함께 기억한다.
const plainTexts = new QuickLRU<string, string>({maxSize: 2000});

const remember = (cache: QuickLRU<string, string>, key: string, compute: (key: string) => string): string => {
    const cached = cache.get(key);
    if (cached !== undefined) return cached;
    const value = compute(key);
    cache.set(key, value);
    return value;
};

const plainTextOf = (html: string): string => remember(plainTexts, html, (text) => htmlToText(text).trim());

const extractVoice = (memo: string): {memo: string; voice?: ProcessedComment["voice"]} | undefined =>
    kotlinBridge.extractVoice(memo) ?? undefined;

/** 목록을 정리하고 삭제 보존을 적용한다. 보존은 받을 때마다 한 번만. */
export const prepareComments = (raw: DcinsideComment[], preData: GalleryPreData, archive: boolean, truncated = false): DcinsideComment[] => {
    // 댓글돌이는 보존 기록에 안 들어가게 먼저 빼고, 삭제 판별은 is_delete "1"로 맞춘다.
    const filtered = raw
        .filter((comment) => comment.nicktype !== "COMMENT_BOY")
        .map((comment) => ({...comment, is_delete: comment.is_delete !== "0" || comment.del_yn === "Y" ? "1" : "0"}));

    return archive ? restoreArchive(preData, filtered, truncated) : filtered;
};

/** 댓글 필드별 비교는 Kotlin/JS에서 수행한다. */
export const sameComments = <T extends object>(a: T[], b: T[]): boolean => kotlinBridge.sameComments(a, b);

/** 차단하지 않은 깡계 댓글을 가린다. */
const markLowActivity = (list: ProcessedComment[], ratios: Ratios | null): ProcessedComment[] => {
    for (const comment of list) {
        const action = comment.blocked ? undefined : lowActivityOf(ratios, comment.user_id);
        if (action) Object.assign(comment, {blocked: action, lowActivity: true});
    }
    return list;
};

/** 정화 → 차단 → 깡계 → 묶기. 같은 원본으로 다시 부르니 복사본을 가공한다. */
export const processComments = async (
    source: DcinsideComment[], preData: GalleryPreData, block: ModuleSettings["block"] | undefined, ratios: Ratios | null = null
): Promise<ProcessedComment[]> => {
    const list: ProcessedComment[] = source.map((comment) => ({...comment, memo: comment.memo ?? ""}));
    // 큰 목록만 나눠 양보한다 - 작은 목록은 기다림이 느려진다. scheduler.yield는 크롬 129, 파이어폭스 142부터다.
    const chunked = list.length > 200;

    // 음성 URL은 정화(재직렬화)하면 &가 &amp;로 바뀌므로 정화 전에 떼어 낸다.
    for (const [i, comment] of list.entries()) {
        const voice = extractVoice(comment.memo);
        // 늘 덮어쓴다. 디시 응답의 voice 필드를 덮어야 한다.
        comment.voice = voice?.voice;
        comment.memo = remember(cleaned, voice?.memo ?? comment.memo, sanitizeMemo);
        if (chunked && i % 50 === 49) await scheduler.yield();
    }

    // 차단 모듈이 꺼져 있으면 block이 없고 깡계만 가린다.
    if (!block) return markLowActivity(list, ratios);
    // 페이지 쪽 검사처럼 앞뒤 공백을 뗀다. 디시콘만 있는 댓글이 " "로 남아 빈 글과 달라지지 않게.
    const texts = new Map(list.map((comment) => [comment, plainTextOf(comment.memo)]));

    for (const [i, comment] of list.entries()) {
        // 삭제 표시된 댓글도 검사한다. 보존으로 되살린 댓글은 원문을 담고 있어 건너뛰면 차단된 내용이 보인다.
        const plain = texts.get(comment);
        // 디시콘 2개짜리 댓글은 두 번째도 검사한다.
        const dcconNos = Array.from(comment.memo.matchAll(GALLOG_DCCON), (match) => match[1] ?? "");

        const blocked =
            isAnyBlocked(
                {
                    NICK: comment.name || null,
                    ID: comment.user_id || null,
                    IP: comment.ip || null,
                    COMMENT: plain || null
                },
                preData.gallery
            ) || dcconNos.some((no) => isBlocked("DCCON", no, preData.gallery));

        if (blocked) comment.blocked = blockMode(block);
        if (chunked && i % 50 === 49) await scheduler.yield();
    }

    if (block.replyRemove) {
        // 답글의 c_no는 스레드 첫 댓글 번호다.
        const blockedThreads = new Set(list.filter((comment) => comment.depth === 0 && comment.blocked).map((comment) => comment.no));
        for (const comment of list) if (comment.depth === 1 && blockedThreads.has(comment.c_no)) comment.blocked ??= blockMode(block);
    }

    // 페이지처럼 깡계 댓글의 답글은 가리지 않는다.
    markLowActivity(list, ratios);

    const duplicate = duplicateOf(block);
    if (duplicate) {
        // 페이지처럼 가린 댓글(차단, 깡계)은 세지 않는다.
        const candidates = list.filter((comment) => !comment.blocked && comment.is_delete !== "1");
        for (const [comment, repeats] of groupDuplicates(candidates, (comment) => texts.get(comment) ?? "", duplicate)) comment.duplicates = repeats;
    }

    return list;
};

