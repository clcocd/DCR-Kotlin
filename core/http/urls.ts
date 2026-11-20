import {VIEW_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

export const urls = {
    base: "https://gall.dcinside.com/",
    vote: "https://gall.dcinside.com/board/recommend/vote",
    comments: "https://gall.dcinside.com/board/comment/",
    comments_submit: "https://gall.dcinside.com/board/forms/comment_submit",
    dccon_comments_submit: "https://gall.dcinside.com/dccon/insert_icon",
    txtcon_submit: "https://gall.dcinside.com/txtcon/insert",
    comment_remove: "https://gall.dcinside.com/board/comment/comment_delete_submit",
    dccon: {
        lists: "https://gall.dcinside.com/dccon/lists",
        detail: "https://gall.dcinside.com/dccon/package_detail",
        buy: "https://gall.dcinside.com/dccon/buy",
        /** 디시콘 이미지. package_detail이 주는 경로(main_img_path, path)를 뒤에 붙인다. */
        image: "https://dcimg5.dcinside.com/dccon.php?no=",
        /** 디시콘 상점. 뒤에 /nick_name/{제작자}, /tags/{태그}를 붙인다. */
        shop: "https://dccon.dcinside.com/hot/1"
    },
    database: {
        version: "https://dcrefresher.green1052.com/version",
        ip: "https://dcrefresher.green1052.com/ip.json"
    }
};

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
export const WRITER_PARAMS = {uid: "rf_uid", ip: "rf_ip"} as const;

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
