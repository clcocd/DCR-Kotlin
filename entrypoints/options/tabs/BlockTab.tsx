import * as react from "react";

import {Info} from "lucide-react";

import {TooltipButton} from "@/components/WithTooltip";
import {kotlinBridge} from "@/kotlin/bridge";
import {BLOCK_TYPES, DETECT_MODE_NAMES, TYPE_NAMES} from "@/core/storage/items";
import {blockLabel, useBlocksStore} from "@/stores/blocks";
import {SAVE_FAILED} from "@/utils/error";

import {BlockDialog, DETECT_MODE_DESCRIPTIONS} from "./BlockDialog";

import {ListRow, ListTabs} from "../lists/ListTabs";
import {notify} from "../optionsStore";
import {RefresherSelect} from "../shared/RefresherSelect";

export const dcconImage = kotlinBridge.optionDcconImage;

export const BlockTab = kotlinBridge.createBlockTab({
    react, useBlocksStore, types: BLOCK_TYPES, typeNames: TYPE_NAMES, modeNames: DETECT_MODE_NAMES,
    modeDescriptions: DETECT_MODE_DESCRIPTIONS, blockLabel, notify, saveFailed: SAVE_FAILED,
    TooltipButton, BlockDialog, Info, ListRow, ListTabs, RefresherSelect
});
