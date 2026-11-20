import {isViewPage, queryString} from "@/core/http/urls";
import {defineModule} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";

import meta, {HIDE_OPTIONS} from "./meta";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupLayout(ctx, HIDE_OPTIONS, isViewPage, queryString)
});
