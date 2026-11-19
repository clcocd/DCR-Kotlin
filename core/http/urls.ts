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
        ip: "https://dcrefresher.green1052.com/ip.json",
        ban: "https://dcrefresher.green1052.com/ban.json"
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
export const postSearchUrl = kotlinBridge.postSearchUrl;

/** 작성자 거르기가 목록 주소에 얹는 쿼리 이름 - 디시 글쓴이 검색은 닉네임만 찾는다. */
export const WRITER_PARAMS = {uid: "rf_uid", ip: "rf_ip"} as const;

export type WriterKind = keyof typeof WRITER_PARAMS;

export interface WriterFilter {
    kind: WriterKind;
    value: string;
}

/** 문서를 불러온 주소 - pushState로 주소창이 바뀌어도 문서가 보여 주는 페이지는 그대로다. */
const loadedDocumentUrl = (): URL => {
    // vitest가 performance를 통째로 바꿀 수 있고, Node 평가 시엔 location이 없어 선택 호출로 한다.
    const name = performance.getEntriesByType?.("navigation")[0]?.name ?? "";
    return URL.parse(name) ?? new URL(globalThis.location?.href ?? "https://gall.dcinside.com/");
};

/** 주소의 작성자 거르기. 기본은 문서를 불러온 주소다 (미리보기가 글 주소를 쌓아도). */
export const writerFilter = (href = loadedDocumentUrl().href): WriterFilter | null => {
    const params = new URL(href).searchParams;
    for (const kind of ["uid", "ip"] as const) {
        const value = params.get(WRITER_PARAMS[kind]);
        if (value) return {kind, value};
    }
    return null;
};

/** 이 문서를 불러온 주소의 쿼리 값 (미리보기가 주소를 바꿔도 그대로). */
export const queryString = (name: string): string | null => loadedDocumentUrl().searchParams.get(name);

/** 문서를 불러온 주소(내비게이션 항목). 주소창이 바뀌어도 문서의 페이지를 가린다. */
export const documentUrl = loadedDocumentUrl();
export const isViewPage = VIEW_PAGE.test(documentUrl.pathname);
export const pagePostNo = isViewPage ? documentUrl.searchParams.get("no") : null;
/** 이 문서의 갤러리 id. 미리보기가 주소를 바꿔도 그대로다. */
export const pageGallery = documentUrl.searchParams.get("id");

/** 목록 행의 글 번호. 글 보기 아래 목록의 행엔 data-no가 없어 제목 링크의 no를 쓴다. */
export const rowPostNo = (row: HTMLElement): string | undefined => {
    if (row.dataset.no) return row.dataset.no;

    const href = row.querySelector(".gall_tit > a")?.getAttribute("href");
    const params = href ? URL.parse(href, location.href)?.searchParams : undefined;
    return (params?.get("id") === pageGallery && params?.get("no")) || undefined;
};
