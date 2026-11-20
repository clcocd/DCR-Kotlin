// 디시 글 목록 DOM 도우미. 목록을 갈아끼우거나 이어 붙이는 refresh, search 모듈과 행을 찾는 모듈이 같이 쓴다.
import {kotlinBridge} from "@/kotlin/bridge";
import type {WriterFilter} from "@/core/http/urls";
import {LIST_REPLACED_EVENT, sendMessage} from "@/core/messaging/protocol";

/** 갈아끼울 글 목록 tbody. 검색 페이지 아래쪽 통합검색 목록(#kakao_seach_list)은 id가 있어 :not([id])로 빠진다. */
export const LIST_SELECTOR = ".gall_list:not([id]) tbody";
export const PAGING_SELECTOR = ".left_content article:has(.gall_listwrap) .bottom_paging_box";

/**
 * 작성자 칸(.ub-writer)이 든 행. 글 목록 행, 글 보기 머리, 댓글은 .ub-content이고,
 * 댓글 검색 결과의 댓글 행은 ub-content가 아니라 .search_comment다.
 */
export const WRITER_ROW_SELECTOR = ".ub-content, .search_comment";

/** 글 목록의 글 행. 글 보기 머리, 댓글과 댓글 검색 결과의 댓글 행(.search_comment)은 들지 않는다. */
export const LIST_ROW_SELECTOR = ".gall_list .ub-content";

interface ListDom {
    writerSelector(filter: WriterFilter): string;
    notifyListReplaced(gallery: string): void;
    checkboxFiller(list: HTMLElement, commentSearch: boolean): (row: HTMLTableRowElement) => void;
    highlightSearchResults(list: HTMLElement, searchValue: string, commentSearch: boolean): void;
}

export const {writerSelector, notifyListReplaced, checkboxFiller, highlightSearchResults} =
    kotlinBridge.createListDom<ListDom>({listReplacedEvent: () => LIST_REPLACED_EVENT, sendMessage});
