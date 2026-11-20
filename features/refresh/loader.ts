import {http} from "@/core/http/client";
import {isViewPage, queryString, writerFilter} from "@/core/http/urls";
import {LIST_SELECTOR, notifyListReplaced, PAGING_SELECTOR, writerSelector} from "@/core/list";
import {getModuleApi} from "@/core/module/registry";
import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";
import {smoothScroll} from "@/utils/dom";
import {sha256} from "@/utils/hash";

import {isWholeFirstPage, replaceList} from "./list";
import {LIST_TABLE} from "./markup";
import {type Ctx} from "./meta";
import {createUnseenCounter} from "./title";

/** 스케줄러(index.ts)와 로더가 함께 읽고 쓰는 상태. */
export interface RefreshState {
    paused: boolean;
    lastRefresh: number;
    /** 새 글 없이 끝난 자동 새로고침 수. 주기를 배수까지 늘리고 사용자 활동이 0으로 되돌린다. */
    quiet: number;
    /** 페이지를 넘긴 주소. 그 목록으로 갈아끼운 직후 목록 위로 스크롤한다. */
    scrollAfter: string | null;
    /** 글 보기 페이지에서 목록이 화면 밖인지. */
    offscreen: boolean;
    /** 목록이 화면 밖이라 건너뛴 자동 새로고침이 있는지. 다시 보이면 곧바로 받는다. */
    missed: boolean;
}

type UnseenCounter = ReturnType<typeof createUnseenCounter>;

export const createListLoader = (ctx: Ctx, state: RefreshState, unseen: UnseenCounter, gallery: string) =>
    kotlinBridge.createRefreshLoader(ctx, state, unseen, gallery, {
        fetchText: (url, options) => http.get(url, options).text(),
        isViewPage,
        isWholeFirstPage,
        listSelector: LIST_SELECTOR,
        notifyListReplaced,
        pagingSelector: PAGING_SELECTOR,
        previewApi: () => getModuleApi("preview"),
        queryString,
        replaceList,
        sha256,
        showToast: (message, type) => useUiStore.getState().showToast(message, type),
        smoothScroll,
        tableStart: LIST_TABLE,
        userinfoApi: () => getModuleApi("userinfo"),
        writerFilter,
        writerSelector
    });
