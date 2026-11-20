import {pageGallery, pagePostNo} from "@/core/http/urls";
import {sendMessage} from "@/core/messaging/protocol";
import {NUMBERED_SOURCE} from "@/core/preview/parser";
import {useUiStore} from "@/stores/ui";
import {kotlinBridge} from "@/kotlin/bridge";

export const removeViewTools = kotlinBridge.removeManageViewTools;

export const renderViewTools = (show: {imageOrigin: boolean; titleSearch: boolean}): void =>
    kotlinBridge.renderManageViewTools(show, {
        pageGallery, pagePostNo, numberedSource: NUMBERED_SOURCE, showToast: () => useUiStore.getState().showToast,
        searchPosts: (title: string) => sendMessage("refresher:searchPosts", title)
    });
