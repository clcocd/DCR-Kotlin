import {defineModule} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";

import meta from "./meta";

export default defineModule(meta, {
    setup: kotlinBridge.setupFonts
});
