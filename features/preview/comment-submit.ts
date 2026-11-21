import {kotlinBridge} from "@/kotlin/bridge";
import {sendMessage, type TurnstileResult} from "@/core/messaging/protocol";
import {type ManageResult, resultMessage, submitComment, type SubmitResult, submitTxtcon, userDeleteComment} from "@/core/preview/request";
import type {DcinsideDccon, GalleryPreData, PostInfo, TurnstileConfig} from "@/core/preview/types";

import type {DoubleTxtcon} from "@/core/preview/txtcon";

/** 텍스트와 함께 글자콘 또는 선택한 디시콘의 전송 데이터를 전달한다. */
interface CommentContent {
    text: string;
    dccons: DcinsideDccon[];
    bigDccon: boolean;
    txtcon?: { bg: string; txt: string };
    txtconDouble?: DoubleTxtcon;
}

/** captcha: 원문 페이지에서만 풀 수 있는 자동입력 방지 확인을 요구받았다. */
type CommentOutcome = { ok: true } | { ok: false; captcha: true } | { ok: false; captcha: false; message: string };


interface CommentSender {
    isCommentPosted(result: SubmitResult): boolean;
    prefetchTurnstile(config: TurnstileConfig): void;
    turnstileFields(config: TurnstileConfig, focusMs: () => number, ask: (config: TurnstileConfig) => Promise<TurnstileResult | undefined>): Promise<Record<string, string> | undefined>;
    postComment(preData: GalleryPreData, post: PostInfo, user: {name: string; pw?: string}, content: CommentContent,
        reply: {commentNo: string | null; replyNo: string | null}, code?: string, turnstile?: Record<string, string>): Promise<CommentOutcome>;
    deleteOwnComment(preData: GalleryPreData, post: PostInfo, commentNo: string, password: string): Promise<ManageResult>;
}

export const {isCommentPosted, prefetchTurnstile, turnstileFields, postComment, deleteOwnComment} =
    kotlinBridge.createCommentSender<CommentSender>({sendMessage, resultMessage, submitComment, submitTxtcon, userDeleteComment});
