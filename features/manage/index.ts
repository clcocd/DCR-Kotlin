import {kotlinBridge} from "@/kotlin/bridge";
import {defineModule} from "@/core/module/define";
import {documentUrl, pageGallery, rowPostNo} from "@/core/http/urls";
import {LIST_ROW_SELECTOR, WRITER_ROW_SELECTOR} from "@/core/list";
import {deletePost} from "@/core/preview/request";
import {whenDomReady} from "@/utils/dom";
import {notifyManage} from "@/stores/notify";
import {isGalleryManager} from "@/utils/user";

import meta from "./meta";
import {renderViewTools} from "./view";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupManage(ctx, {
        documentUrl, pageGallery, rowPostNo, listRowSelector: LIST_ROW_SELECTOR, writerRowSelector: WRITER_ROW_SELECTOR,
        deletePost, whenDomReady, notifyManage, isGalleryManager, renderViewTools
    })
});
