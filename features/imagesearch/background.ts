import {defineBackgroundModule} from "@/core/module/background";
import {IMAGE_URL_PATTERNS} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

import meta, {ENGINE_IDS, ENGINES} from "./meta";

export default defineBackgroundModule(meta, kotlinBridge.createImageSearch({
    browser, engineIds: ENGINE_IDS, engines: ENGINES, patterns: IMAGE_URL_PATTERNS
}));
