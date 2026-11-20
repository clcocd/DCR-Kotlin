import {kotlinBridge} from "@/kotlin/bridge";
import {ajax} from "@/core/http/client";
import {urls} from "@/core/http/urls";

import {fcmToken} from "./fcm";
import {dcBody, type SubmitResult, submitResult} from "./response";
import type {GalleryPreData, PostInfo} from "./types";

export const TXTCON_BACKGROUNDS = kotlinBridge.txtconBackgrounds;
export const TXTCON_COLORS = kotlinBridge.txtconColors;

export const {txtconGraphemes: graphemes, wrapTxtcon, normalizeTxtcon} = kotlinBridge;

interface TxtconSubmitFields {
    user: { name: string; pw?: string };
    text: string;
    colors: { bg: string; txt: string };
    commentNo: string | null;
    replyNo: string | null;
    captcha?: string;
    grecaptchaToken?: string;
    /** 디시 ts_cmt_fields처럼 끝에 붙이는 Turnstile 칸. */
    turnstile?: Record<string, string>;
}

export const submitTxtcon = kotlinBridge.createTxtconSubmission<
    (preData: GalleryPreData, postInfo: PostInfo, fields: TxtconSubmitFields) => Promise<SubmitResult>
>({ajax, urls, fcmToken, dcBody, submitResult});
