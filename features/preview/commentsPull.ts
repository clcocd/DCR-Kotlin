import {kotlinBridge} from "@/kotlin/bridge";
import {shallow} from "zustand/shallow";

import {duplicateOf} from "@/core/block";
import {runningModuleSettings, moduleSettingsStore, runningModulesStore} from "@/core/module/registry";
import {prepareComments, processComments, sameComments} from "@/core/preview/comments";
import {setEntry} from "@/core/preview/cache";
import {fetchComments} from "@/core/preview/request";
import type {CommentListResponse, GalleryPreData, PostInfo} from "@/core/preview/types";
import {useBlocksStore} from "@/stores/blocks";
import {lowActivityOf, useUiStore} from "@/stores/ui";
import {isAbortError} from "@/utils/error";

import {textBlockOf} from "./postLoader";
import type {Ctx} from "./meta";
import {NO_FRESH, NO_REPLY, usePreviewStore} from "./ui/previewStore";

interface CommentsPull {
    pullComments(preData: GalleryPreData, post: PostInfo, signal: number, skip?: boolean,
        given?: CommentListResponse | Promise<CommentListResponse | undefined>, cached?: boolean): Promise<void>;
    refreshComments(report?: boolean): Promise<boolean>;
    scheduleReapplyBlocks(): void;
    isPulling(): boolean;
    forgetShown(): void;
    hasAllowReply(gallery: string): boolean;
}

export const createCommentsPull = (ctx: Ctx, signal: () => AbortSignal | undefined): CommentsPull =>
    kotlinBridge.createCommentsPull(ctx, signal, {
        store: usePreviewStore, useUiStore, useBlocksStore, runningModuleSettings, moduleSettingsStore, runningModulesStore,
        prepareComments, processComments, sameComments, setEntry, fetchComments, lowActivityOf, isAbortError, textBlockOf,
        duplicateOf, shallow, noFresh: NO_FRESH, noReply: NO_REPLY
    });
