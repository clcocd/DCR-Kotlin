import {Ban, Trash2} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {ConfirmDialog} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {Checkbox} from "@/components/ui/checkbox";
import {Collapsible, CollapsibleContent} from "@/components/ui/collapsible";
import {Field, FieldLabel} from "@/components/ui/field";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import type {ProcessedComment} from "@/core/preview/comments";
import {adminDeleteComments} from "@/core/preview/request";
import {useUiStore} from "@/stores/ui";
import {eventTarget, isTyping} from "@/utils/event";
import {isGalleryManager} from "@/utils/user";

import {Comment, CommentDeletePrompt, notifyDeleted} from "./Comment";
import {NO_FRESH, usePreviewStore} from "./previewStore";

export const {threadParents, commentSummary: summaryOf, groupCommentReplies: groupReplies, mentionComment: mentionTarget} = kotlinBridge;

export const {CommentList, CommentOrder, CommentSummary} = kotlinBridge.createCommentList<{
    CommentList: (props: {comments: ProcessedComment[]}) => react.ReactNode;
    CommentSummary: (props: {comments: ProcessedComment[]}) => react.ReactNode;
    CommentOrder: () => react.ReactNode;
}>({
    react, store: usePreviewStore, useUiStore, noFresh: NO_FRESH, ConfirmDialog, Button, Checkbox,
    Collapsible, CollapsibleContent, Field, FieldLabel, ToggleGroup, ToggleGroupItem, Comment, CommentDeletePrompt,
    notifyDeleted, adminDeleteComments, eventTarget, isTyping, isGalleryManager, Ban, Trash2
});
