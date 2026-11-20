import {kotlinBridge} from "@/kotlin/bridge";
import {onMessage, sendMessage} from "@/core/messaging/protocol";
import {type BackgroundModule, startBackgroundModules} from "@/core/module/background";
import {CONTENT_EXCLUDE_MATCHES, CONTENT_MATCHES} from "@/core/pages";
import {dbMetaStorage} from "@/core/storage/items";
import {friendlyMessage} from "@/utils/error";

import {startAutoBackup} from "./backup";
import {fillMissingBlockIds} from "./blocks";
import {startDatabaseUpdates} from "./database";
import {listenPageMessages} from "./page";

/** 모듈별 배경 코드(features/<id>/background.ts). 필요한 모듈만 이 파일을 둔다. */
const backgroundModules = Object.values(import.meta.glob<{ default: BackgroundModule }>("../../features/*/background.ts", {eager: true}))
    .map((module) => module.default);

export default defineBackground({
    type: "module",
    main: kotlinBridge.createBackgroundStartup({
        browser, onMessage, sendMessage, modules: backgroundModules, startBackgroundModules,
        contentMatches: CONTENT_MATCHES, contentExcludeMatches: CONTENT_EXCLUDE_MATCHES,
        firefox: import.meta.env.BROWSER === "firefox", production: import.meta.env.PROD,
        dbMetaStorage, friendlyMessage, startAutoBackup, fillMissingBlockIds, startDatabaseUpdates, listenPageMessages
    })
});
