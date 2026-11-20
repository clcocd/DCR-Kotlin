import {kotlinBridge} from "@/kotlin/bridge";
import QuickLRU from "quick-lru";

import type {GallogActivity, RatioData} from "@/core/gallog";
import {onMessage} from "@/core/messaging/protocol";
import {defineBackgroundModule} from "@/core/module/background";
import {moduleDataStorage} from "@/core/storage/items";

import meta from "./meta";

export default defineBackgroundModule(meta, {
    listen: kotlinBridge.createGallogBackground({
        onMessage, ratioStorage: moduleDataStorage<RatioData>("userinfo", {}),
        cache: new QuickLRU<string, GallogActivity>({maxSize: 500, maxAge: 3_600_000})
    })
});
