import {kotlinBridge} from "../kotlin/bridge";
import {defineWxtModule} from "wxt/modules";

import {featureFolders} from "./lib/features";

/** prepare 때 기능 모듈 타입(.wxt/types/modules.d.ts)과 id 목록(.wxt/module-ids.ts)을 만든다. */
export default defineWxtModule((wxt) => {
    wxt.hook("prepare:types", (_, entries) => {
        const metas = featureFolders(wxt.config.root, "meta.ts");
        const modules = featureFolders(wxt.config.root, "index.ts");
        entries.push(...kotlinBridge.moduleTypeEntries(metas, modules));
    });
});
