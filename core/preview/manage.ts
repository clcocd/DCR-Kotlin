import {ajax} from "@/core/http/client";
import {galltypeOf, managerAjaxUrl, urls} from "@/core/http/urls";
import type {CommentRcKey} from "@/core/messaging/protocol";
import {kotlinBridge} from "@/kotlin/bridge";

import {dcBody, resultMessage, type SubmitResult, submitResult} from "./response";
import type {BlockDay, BlockReason, GalleryPreData, PostInfo} from "./types";

/** 관리 요청 결과. 디시 관리 API는 {"result": "success" | "fail", "msg": "..."}를 돌려준다. */
export interface ManageResult {
    success: boolean;
    /** 디시가 준 안내 문구 (없을 수 있음). */
    message?: string;
}

export interface BlockOptions {
    avoidHour: BlockDay;
    avoidReason: BlockReason;
    avoidReasonTxt: string;
    /** 선택한 글도 삭제. */
    delChk: boolean;
    /** 식별 코드 차단 시 IP 동시 차단. */
    userTypeChk: boolean;
}

interface ManageRequests {
    bump(preData: GalleryPreData): Promise<ManageResult>;
    deletePost(target: Pick<GalleryPreData, "gallery" | "id" | "link">): Promise<ManageResult>;
    blockUser(preData: GalleryPreData, options: BlockOptions): Promise<ManageResult>;
    blockCommenters(preData: GalleryPreData, commentIds: string[], options: BlockOptions): Promise<ManageResult>;
    setNotice(preData: GalleryPreData, notice: boolean): Promise<ManageResult>;
    setRecommend(preData: GalleryPreData, recommend: boolean): Promise<ManageResult>;
    captchaImage(preData: GalleryPreData, type: "comment" | "recommend"): string;
    adminDeleteComments(preData: GalleryPreData, commentIds: string[]): Promise<ManageResult>;
    userDeleteComment(preData: GalleryPreData, postInfo: Pick<PostInfo, "commentId" | "v_cur_t" | "commentDeleteFields">,
        commentId: string, password: string, rcKey: CommentRcKey, grecaptchaToken?: string): Promise<SubmitResult>;
}

export const {bump, deletePost, blockUser, blockCommenters, setNotice, setRecommend, captchaImage, adminDeleteComments, userDeleteComment} =
    kotlinBridge.createManageRequests<ManageRequests>({ajax, urls, galltypeOf, managerAjaxUrl, dcBody, resultMessage, submitResult});
