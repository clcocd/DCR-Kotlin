import QuickLRU from "quick-lru";

import {http} from "@/core/http/client";
import {documentUrl, pageGallery, queryString, writerFilter} from "@/core/http/urls";
import {checkboxFiller, highlightSearchResults, LIST_SELECTOR, notifyListReplaced, PAGING_SELECTOR, writerSelector} from "@/core/list";
import {sendMessage} from "@/core/messaging/protocol";
import {defineModule} from "@/core/module/define";
import {LIST_PAGE} from "@/core/pagePatterns";
import {ownPreviewEntry} from "@/core/preview/history";
import {kotlinBridge} from "@/kotlin/bridge";
import {reportFailure} from "@/stores/notify";
import {useUiStore} from "@/stores/ui";
import {whenDomReady} from "@/utils/dom";

import meta from "./meta";


export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupSearch(ctx, {
        documentUrl, pageGallery, queryString, writerFilter, writerSelector, checkboxFiller, highlightSearchResults,
        listPage: LIST_PAGE, listSelector: LIST_SELECTOR, pagingSelector: PAGING_SELECTOR, notifyListReplaced,
        createPageCache: (maxSize: number) => new QuickLRU({maxSize}), whenDomReady, ownPreviewEntry,
        reportFailure, gallogPage: (id: string) => sendMessage("refresher:gallogPage", id),
        showToast: (message: string, tone: "warning" | "error") => useUiStore.getState().showToast(message, tone),
        fetchText: (url: string, signal: AbortSignal) => http.get(url, {signal}).text()
    })
});
