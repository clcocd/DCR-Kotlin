/** 게시글, 댓글 받기와 추천. 나머지 디시 요청은 각 파일(manage, submit, txtcon, dccon)로 나뉘어 있고 여기서 다시 내보낸다. */
import {ajax, http} from "@/core/http/client";
import {galleryPath, urls} from "@/core/http/urls";
import {kotlinBridge} from "@/kotlin/bridge";

import {parsePostInfo} from "./parser";
import {dcBody, resultMessage, submitResult} from "./response";
import type {CommentListResponse, GalleryPreData, PostInfo} from "./types";

export {type BlockOptions, type ManageResult, adminDeleteComments, blockCommenters, blockUser, bump, captchaImage, deletePost, setNotice, setRecommend, userDeleteComment} from "./manage";
export {submitComment} from "./submit";
export {TXTCON_BACKGROUNDS, TXTCON_COLORS, normalizeTxtcon, submitTxtcon} from "./txtcon";
export {addDcconPackage, dcconPackages, fetchDcconList, fetchDcconPackage, getDcconPackage} from "./dccon";
export {type SubmitResult, resultMessage} from "./response";

interface VoteResult {
    success: boolean;
    counts?: string;
    fixedCounts?: string;
    /** 실패 이유 (디시가 준 문구). */
    message?: string;
}

interface PreviewRequests {
    viewUrl(link: string, gallery: string, id: string): string;
    fetchPost(preData: GalleryPreData, signal: AbortSignal): Promise<PostInfo>;
    fetchComments(preData: GalleryPreData, postInfo: Pick<PostInfo, "commentId" | "commentNo" | "esno">, signal: AbortSignal): Promise<CommentListResponse>;
    fetchComments(preData: GalleryPreData, postInfo: Pick<PostInfo, "commentId" | "commentNo" | "esno">,
        signal: AbortSignal, skipIfUnchangedFrom: string): Promise<CommentListResponse | undefined>;
    vote(preData: GalleryPreData, post: PostInfo, mode: "U" | "D", code?: string): Promise<VoteResult>;
}

export const {viewUrl, fetchPost, fetchComments, vote} = kotlinBridge.createPreviewRequests<PreviewRequests>({
    ajax, http, urls, galleryPath, dcBody, parsePostInfo, resultMessage, submitResult
});
