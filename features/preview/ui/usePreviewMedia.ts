import * as react from "react";
import {useRunningModuleSettings} from "@/core/module/useModuleSettings";
import {useBlocksStore} from "@/stores/blocks";
import {blockMode, dcconCode, isBlocked} from "@/core/block";
import {kotlinBridge} from "@/kotlin/bridge";

export const {usePreviewMedia, markBlockedDccons, installExternalEmbeds} = kotlinBridge.createPreviewMediaHooks({
    react, useRunningModuleSettings, useBlocksStore, blockMode, dcconCode, isBlocked
});
