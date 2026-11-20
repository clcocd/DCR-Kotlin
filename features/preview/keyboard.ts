import {kotlinBridge} from "@/kotlin/bridge";
import {LIST_ROW_SELECTOR} from "@/core/list";
import {postKey} from "@/core/preview/cache";
import type {GalleryPreData} from "@/core/preview/types";
import {smoothScroll} from "@/utils/dom";
import {isTyping} from "@/utils/event";

import type {Ctx} from "./meta";
import {buildPreData, isBlurHidden, listRows} from "./rows";
import {usePreviewStore} from "./ui/previewStore";

export const bindListKeys = (ctx: Ctx, open: (preData: GalleryPreData) => void): void =>
    kotlinBridge.bindPreviewListKeys(ctx, open, {store: usePreviewStore, rowSelector: LIST_ROW_SELECTOR,
        postKey, buildPreData, isBlurHidden, listRows, smoothScroll, isTyping});
