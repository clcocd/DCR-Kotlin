import {kotlinBridge} from "@/kotlin/bridge";
import {dcinsideHref} from "@/core/http/urls";
import {LIST_ROW_SELECTOR} from "@/core/list";
import type {GalleryPreData} from "@/core/preview/types";
import {hasModifier} from "@/utils/event";

import type {Ctx} from "./meta";
import type {createMini} from "./mini";
import {buildPreData} from "./rows";
import {usePreviewStore} from "./ui/previewStore";

interface RowHandlers {
    open(preData: GalleryPreData, commentsOnly: boolean): void;
    /** 우클릭을 누르는 동안 본문을 미리 받는다. */
    prefetch(preData: GalleryPreData): void;
    mini: ReturnType<typeof createMini>;
}

export const bindRows = (ctx: Ctx, handlers: RowHandlers): void => kotlinBridge.bindPreviewRows(ctx, handlers, {
    dcinsideHref, hasModifier, buildPreData, store: usePreviewStore, rowSelector: LIST_ROW_SELECTOR
});
