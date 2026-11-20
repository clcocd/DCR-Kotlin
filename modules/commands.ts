import {kotlinBridge} from "../kotlin/bridge";
import {resolve} from "node:path";

import {defineWxtModule} from "wxt/modules";

import type {AnyModuleLabels, AnyModuleMeta} from "../core/module/types";
import {featureFolders} from "./lib/features";

/** 모듈 메타의 commands를 모아 manifest commands로 넣는다. 설명은 labels.ts의 "모듈 이름: 명령 설명". */
export default defineWxtModule((wxt) => {
    wxt.hook("build:manifestGenerated", async (wxt, manifest) => {
        const folders = featureFolders(wxt.config.root, "meta.ts");
        const files = (file: string) => folders.map((folder) => resolve(wxt.config.root, "features", folder, file));

        const loaded = await wxt.builder.importEntrypoints([...files("meta.ts"), ...files("labels.ts")]);
        const metas = loaded.slice(0, folders.length) as unknown as AnyModuleMeta[];
        const labels = loaded.slice(folders.length) as unknown as AnyModuleLabels[];
        const commands = kotlinBridge.makeManifestCommands(manifest.commands, metas, labels);
        if (commands) manifest.commands = commands;
    });
});
