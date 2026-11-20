import {isHTTPError} from "ky";

import {http} from "@/core/http/client";
import {kotlinBridge} from "@/kotlin/bridge";
import {onMessage} from "@/core/messaging/protocol";
import {defineBackgroundModule} from "@/core/module/background";

import meta from "./meta";

export default defineBackgroundModule(meta, {
    listen: () => kotlinBridge.listenSearchBackground({
        onMessage, isHTTPError, fetchText: (url: string) => http.get(url).text()
    })
});
