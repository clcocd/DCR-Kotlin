import {kotlinBridge} from "@/kotlin/bridge";
import {onMessage} from "@/core/messaging/protocol";
import {defineBackgroundModule, hasTab, runFileInPage} from "@/core/module/background";

import meta from "./meta";

export default defineBackgroundModule(meta, {
    listen: () => kotlinBridge.listenWriteBackground({onMessage, hasTab, runFileInPage})
});
