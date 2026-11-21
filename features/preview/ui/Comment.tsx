import {Check, ChevronDown, Reply as ReplyIcon, X} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {ConfirmDialog, DialogActions, ModalDialog, SubmitForm} from "@/components/dialogs";
import {Button, buttonVariants} from "@/components/ui/button";
import {Checkbox} from "@/components/ui/checkbox";
import {DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import type {ProcessedComment} from "@/core/preview/comments";
import type {CommentForm, GalleryPreData} from "@/core/preview/types";
import {adminDeleteComments, type ManageResult} from "@/core/preview/request";
import {notifyManage} from "@/stores/notify";
import {cn} from "cn";

import {deleteOwnComment} from "../comment-submit";
import {savedNonmember} from "../nonmember";
import {markPressable, pressContents} from "./contentsClick";
import {openDcconInfo} from "./DcconInfoPopup";
import {fitTxtconNearView} from "./fitTxtcon";
import {watchGifVideos} from "./gifVideos";
import {NO_REPLY, parseDate, refreshCommentsOf, toggled, usePreviewStore} from "./previewStore";
import {TimeStamp} from "./TimeStamp";
import {UserCard} from "./UserCard";
import {WriteComment} from "./WriteComment";

interface CommentProps {
    comment: ProcessedComment;
    depth: number;
    replyCount: number;
    /** 답글이 펼쳐진 부모. 아래로 트리 선을 긋는다. */
    threadOpen?: boolean;
    /** 스레드의 마지막 답글. 트리 선이 여기서 끝난다. */
    lastReply?: boolean;
    /** 갤러리 관리 권한. 문서를 훑어 재므로 목록(CommentList)에서 한 번만 재서 넘긴다. */
    isAdmin: boolean;
}

export const {extractCommentIcon: extractIcon, extractCommentIp: extractIp} = kotlinBridge;
export const {Comment, CommentDeletePrompt, notifyDeleted, noticeDeleteWarning} = kotlinBridge.createCommentView<{
    Comment: (props: CommentProps) => react.ReactNode;
    CommentDeletePrompt: (props: {comments: ProcessedComment[]; isAdmin: boolean}) => react.ReactNode;
    notifyDeleted(preData: GalleryPreData, request: Promise<ManageResult>): Promise<boolean>;
    noticeDeleteWarning(fields: CommentForm["fields"], date: string): string | undefined;
}>({
    react, store: usePreviewStore, noReply: NO_REPLY, toggled, parseDate, refreshCommentsOf, ConfirmDialog,
    DialogActions, ModalDialog, SubmitForm, Button, buttonVariants, Checkbox, DialogHeader, DialogTitle, Input,
    adminDeleteComments, notifyManage, deleteOwnComment, savedNonmember, markPressable, pressContents,
    openDcconInfo, fitTxtconNearView, watchGifVideos, TimeStamp, UserCard, WriteComment, Check, ChevronDown, ReplyIcon, X, cn
});
