import {VIEW_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

export const urls = kotlinBridge.runtimeConstants.urls;

/** 원본의 URL/갤러리 판정 로직은 Kotlin/JS에서 실행한다. */
export const dcinsideHref = (url: string | URL | null | undefined): string | undefined => {
    if (url == null) return undefined;
    return kotlinBridge.dcinsideHref(typeof url === "string" ? url : url.href) ?? undefined;
};

export type GalleryKind = "normal" | "minor" | "mini" | "person";
export const galleryKind = kotlinBridge.galleryKind;
export const galleryPath = kotlinBridge.galleryPath;
export const galltypeOf = kotlinBridge.galltypeOf;
export const managerAjaxUrl = kotlinBridge.managerAjaxUrl;
export const listUrl = kotlinBridge.listUrl;
export const mergeParamURL = kotlinBridge.mergeParamURL;

/** 작성자 거르기가 목록 주소에 얹는 쿼리 이름 - 디시 글쓴이 검색은 닉네임만 찾는다. */
export const WRITER_PARAMS = kotlinBridge.runtimeConstants.writerParams;

export type WriterKind = keyof typeof WRITER_PARAMS;

export interface WriterFilter {
    kind: WriterKind;
    value: string;
}

export const writerFilter = (href?: string): WriterFilter | null => kotlinBridge.writerFilter(href, WRITER_PARAMS);
export const queryString = kotlinBridge.queryString;

export const documentUrl = kotlinBridge.loadedDocumentUrl();
export const isViewPage = VIEW_PAGE.test(documentUrl.pathname);
export const pagePostNo = isViewPage ? documentUrl.searchParams.get("no") : null;
export const pageGallery = documentUrl.searchParams.get("id");

export const rowPostNo = (row: HTMLElement): string | undefined => kotlinBridge.rowPostNo(row, pageGallery);
