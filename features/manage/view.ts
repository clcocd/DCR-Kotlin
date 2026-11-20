import {pageGallery, pagePostNo, postSearchUrl} from "@/core/http/urls";
import {sendMessage} from "@/core/messaging/protocol";
import {NUMBERED_SOURCE} from "@/core/preview/parser";
import {useUiStore} from "@/stores/ui";
import {kotlinBridge} from "@/kotlin/bridge";

/** 이미지 출처와 같은 제목 검색 UI는 Kotlin/JS가 구성한다. */
const view = kotlinBridge.manageViewImpl(
    (src) => NUMBERED_SOURCE.test(src),
    (title) => sendMessage("refresher:searchPosts", title),
    postSearchUrl,
    (text, type, duration, action) => useUiStore.getState().showToast(text, type, duration, action),
    pageGallery,
    pagePostNo
);

export const removeViewTools = view.remove;
export const renderViewTools = view.render;
