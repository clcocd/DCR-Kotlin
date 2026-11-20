// 디시 글 목록 DOM 도우미. 목록을 갈아끼우거나 이어 붙이는 refresh, search 모듈과 행을 찾는 모듈이 같이 쓴다.
import {kotlinBridge} from "@/kotlin/bridge";
import type {WriterFilter} from "@/core/http/urls";
import {LIST_REPLACED_EVENT, sendMessage} from "@/core/messaging/protocol";

/** 목록과 작성자 행 선택자는 원본 문자열을 Kotlin에서 보관한다. */
export const {list: LIST_SELECTOR, paging: PAGING_SELECTOR, writerRow: WRITER_ROW_SELECTOR, listRow: LIST_ROW_SELECTOR} =
    kotlinBridge.runtimeConstants.list;

interface ListDom {
    writerSelector(filter: WriterFilter): string;
    notifyListReplaced(gallery: string): void;
    checkboxFiller(list: HTMLElement, commentSearch: boolean): (row: HTMLTableRowElement) => void;
    highlightSearchResults(list: HTMLElement, searchValue: string, commentSearch: boolean): void;
}

export const {writerSelector, notifyListReplaced, checkboxFiller, highlightSearchResults} =
    kotlinBridge.createListDom<ListDom>({listReplacedEvent: () => LIST_REPLACED_EVENT, sendMessage});
