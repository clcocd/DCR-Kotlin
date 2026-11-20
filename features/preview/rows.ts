import {kotlinBridge} from "@/kotlin/bridge";
import {BLURRED_ROW_SELECTOR} from "@/core/block";
import {dcinsideHref} from "@/core/http/urls";
import {LIST_ROW_SELECTOR} from "@/core/list";
import {postKey} from "@/core/preview/cache";
import type {GalleryPreData} from "@/core/preview/types";
import {useUiStore} from "@/stores/ui";

export interface ListRow {
    row: HTMLElement;
    pre: GalleryPreData;
}

export const {buildPreData, isBlurHidden, listRows, adjacentPreData, isTextPost} = kotlinBridge.createPreviewRows({
    postKey, dcinsideHref, uiStore: useUiStore, rowSelector: LIST_ROW_SELECTOR, blurredRowSelector: BLURRED_ROW_SELECTOR
});
