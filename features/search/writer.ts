import {WRITER_PARAMS, type WriterFilter} from "@/core/http/urls";
import {kotlinBridge} from "@/kotlin/bridge";

/** 글쓴이 검색 주소·페이징 필터·검색 종류 선택은 Kotlin/JS가 처리한다. */
export const writerSearchUrl = (filter: WriterFilter | null, nick?: string): string =>
    kotlinBridge.writerSearchUrl(filter, nick, WRITER_PARAMS);

export const keepWriterFilter = (paging: Element, filter: WriterFilter): void =>
    kotlinBridge.keepWriterFilter(paging, filter, WRITER_PARAMS);

export const addWriterTypes = (filter: WriterFilter | null, search: (filter: WriterFilter) => void, signal: AbortSignal): void =>
    kotlinBridge.addWriterTypes(filter, search, signal);

export const removeWriterTypes = kotlinBridge.removeWriterTypes;
