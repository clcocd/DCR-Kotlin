import {ajax, formBody} from "@/core/http/client";
import {csrfBody} from "@/core/http/cookie";
import {urls} from "@/core/http/urls";
import type {CommentRcKey} from "@/core/messaging/protocol";

import {kotlinBridge} from "@/kotlin/bridge";
import {fcmToken} from "./fcm";
import {type SubmitResult, submitResult} from "./response";
import type {DcinsideDccon, GalleryPreData, PostInfo} from "./types";

interface CommentSubmitFields {
    user: { name: string; pw?: string };
    memo: string | DcinsideDccon[];
    commentNo: string | null;
    replyNo: string | null;
    bigDccon: boolean;
    captcha?: string;
    rcKey?: CommentRcKey;
    grecaptchaToken?: string;
    /** 디시 ts_cmt_fields처럼 끝에 붙이는 Turnstile 칸. */
    turnstile?: Record<string, string>;
}

export const submitComment = kotlinBridge.createCommentSubmission<
    (preData: GalleryPreData, postInfo: PostInfo, fields: CommentSubmitFields) => Promise<SubmitResult>
>({ajax, formBody, csrfBody, urls, fcmToken, submitResult});
