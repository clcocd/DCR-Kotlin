import {isViewPage, pagePostNo, rowPostNo, writerFilter} from "@/core/http/urls";
import {checkboxFiller, highlightSearchResults, PAGING_SELECTOR} from "@/core/list";
import {kotlinBridge, type ReplaceListOptions} from "@/kotlin/bridge";

import {NOTICE_ICON} from "./markup";

export const clearNewPost = kotlinBridge.clearNewPost;

export const syncPaging = (dom: Document): void => kotlinBridge.syncRefreshPaging(dom, PAGING_SELECTOR);

export const isWholeFirstPage = (url: string): boolean => kotlinBridge.isWholeFirstPage(url, writerFilter);

export const replaceList = (oldList: HTMLElement, newList: HTMLElement, options: ReplaceListOptions): {added: HTMLTableRowElement[]; changed: boolean} =>
    kotlinBridge.replaceRefreshList(oldList, newList, options, {
        rowPostNo,
        checkboxFiller,
        highlightSearchResults,
        isViewPage,
        pagePostNo,
        noticeIcon: NOTICE_ICON
    });
