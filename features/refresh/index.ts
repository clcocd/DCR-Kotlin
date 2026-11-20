import {isViewPage, listUrl, pageGallery, queryString, writerFilter} from "@/core/http/urls";
import {LIST_SELECTOR, PAGING_SELECTOR} from "@/core/list";
import {defineModule} from "@/core/module/define";
import {ownPreviewEntry} from "@/core/preview/history";
import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";
import {whenDomReady} from "@/utils/dom";
import {hasModifier, isTyping} from "@/utils/event";

import {createListLoader} from "./loader";
import meta from "./meta";
import {createUnseenCounter} from "./title";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupRefresh(ctx, {
        createListLoader,
        createUnseenCounter,
        hasModifier,
        isTyping,
        isViewPage,
        listSelector: LIST_SELECTOR,
        listUrl,
        ownPreviewEntry,
        pageGallery,
        pagingSelector: PAGING_SELECTOR,
        queryString,
        showToast: (message) => useUiStore.getState().showToast(message),
        whenDomReady,
        writerFilter
    }),

    shortcuts: {
        refreshLists: (api) => void api.refreshLists(),
        refreshPause: (api) => api.togglePause()
    },

    pageToggles: {
        pause: {
            desc: "이 페이지의 자동 새로고침을 멈춥니다",
            isOn: (api) => api.isPaused(),
            toggle: (api) => api.togglePause()
        }
    }
});
