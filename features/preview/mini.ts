import {kotlinBridge} from "@/kotlin/bridge";
import {BLOCKED_TEXT} from "@/core/block";
import type {GalleryPreData, PostInfo} from "@/core/preview/types";
import {useUiStore} from "@/stores/ui";

import type {Ctx} from "./meta";
import {buildPreData, isBlurHidden, isTextPost} from "./rows";
import {closeMiniSoon, hoverMini, keepMini, MINI_WIDTH, miniPosition, postTitle, usePreviewStore} from "./ui/previewStore";

export const createMini = (
    ctx: Ctx,
    getPost: (preData: GalleryPreData) => Promise<{post: PostInfo}>,
    processContents: (preData: GalleryPreData, post: PostInfo, stripMedia?: boolean) => Promise<PostInfo>
) => kotlinBridge.createMiniController(ctx, getPost, processContents, {
    buildPreData, isBlurHidden, isTextPost, closeMiniSoon, hoverMini, keepMini,
    miniWidth: MINI_WIDTH, miniPosition, postTitle, usePreviewStore, useUiStore, blockedText: BLOCKED_TEXT
});
