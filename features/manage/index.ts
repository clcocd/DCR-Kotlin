import {defineModule} from "@/core/module/define";
import type {ModuleImpl} from "@/core/module/types";
import {documentUrl, pageGallery, rowPostNo} from "@/core/http/urls";
import {LIST_ROW_SELECTOR, WRITER_ROW_SELECTOR} from "@/core/list";
import {deletePost} from "@/core/preview/request";
import {whenDomReady} from "@/utils/dom";
import {notifyManage} from "@/stores/notify";
import {isGalleryManager} from "@/utils/user";
import {kotlinBridge} from "@/kotlin/bridge";

import meta from "./meta";
import {removeViewTools, renderViewTools} from "./view";

const CHECKBOX_ROW = `${WRITER_ROW_SELECTOR}, .cmt_nickbox`;

export default defineModule(meta, kotlinBridge.manageModuleImpl(
    {checkboxRow: CHECKBOX_ROW, gifVideo: ".gallview_contents video", listRow: LIST_ROW_SELECTOR},
    whenDomReady,
    renderViewTools,
    removeViewTools,
    isGalleryManager,
    rowPostNo,
    (id, gallery, link) => notifyManage(
        deletePost({gallery, id, link}),
        "게시글을 삭제했습니다.",
        "게시글을 삭제하지 못했습니다. 잠시 후 다시 시도해 주세요."
    ),
    pageGallery ?? "",
    documentUrl.href
) as ModuleImpl<typeof meta, void>);
