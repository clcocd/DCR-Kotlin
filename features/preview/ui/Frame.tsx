import {Dialog} from "@base-ui/react/dialog";
import {Archive, ArrowUp, CircleAlert, Eye, MessageSquare, RotateCw} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {Alert, AlertDescription} from "@/components/ui/alert";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {Spinner} from "@/components/ui/spinner";
import {TooltipButton} from "@/components/WithTooltip";
import {focusedElement} from "@/components/useReturnFocus";
import {BLOCKED_TEXT} from "@/core/block";
import {useContentModuleSettings, useRunningModuleSettings} from "@/core/module/useModuleSettings";
import {postKey as keyOfPost} from "@/core/preview/cache";
import {useUiStore} from "@/stores/ui";
import {smoothScroll} from "@/utils/dom";
import {hasModifier, isTyping} from "@/utils/event";
import {cn} from "cn";

import {adjacentPreData} from "../rows";
import {TimeStamp} from "./TimeStamp";
import {UserCard} from "./UserCard";
import {CommentList, CommentOrder, CommentSummary, threadParents} from "./CommentList";
import {clickContents, markMedia, markPressable, pressContents} from "./contentsClick";
import {CountDown} from "./CountDown";
import {ErrorBlock} from "./ErrorBlock";
import {postTitle, usePreviewStore} from "./previewStore";
import {PreviewDialogRoot} from "./PreviewDialogRoot";
import {usePreviewMedia} from "./usePreviewMedia";
import {useScrollLock} from "./useScrollLock";
import {useWheelGesture} from "./useWheelGesture";
import {Votes} from "./Votes";
import {WriteComment} from "./WriteComment";

export const Frame = kotlinBridge.createPreviewFrame({
    react, cn, DialogTitle: Dialog.Title, Archive, ArrowUp, CircleAlert, Eye, MessageSquare, RotateCw,
    Alert, AlertDescription, Button, Separator, Spinner, TooltipButton, focusedElement, blockedText: BLOCKED_TEXT,
    useContentModuleSettings, useRunningModuleSettings, useUiStore, smoothScroll, hasModifier, isTyping,
    keyOfPost, adjacentPreData, TimeStamp, UserCard, CommentList, CommentOrder, CommentSummary,
    threadParents, clickContents, markMedia, markPressable, pressContents, CountDown, ErrorBlock,
    postTitle, usePreviewStore, PreviewDialogRoot, usePreviewMedia, useScrollLock,
    useWheelGesture, Votes, WriteComment
});
