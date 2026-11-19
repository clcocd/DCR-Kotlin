import {defineModule} from "@/core/module/define";
import {writeStyle} from "@/utils/dom";

import meta, {type Ctx} from "./meta";
import {kotlinBridge} from "@/kotlin/bridge";

const STYLE_ID = "refresher-fonts";

const apply = (ctx: Ctx): (() => void) => writeStyle(STYLE_ID, buildCss(ctx));

export default defineModule(meta, {
    setup(ctx) {
        ctx.onRevoke(apply(ctx));

        ctx.onSettingsChanged(() => apply(ctx));
    }
});
