import {storage} from "wxt/utils/storage";

import {BLOCK_TYPES, blockListKey} from "@/core/storage/items";
import {kotlinBridge} from "@/kotlin/bridge";

export const fillMissingBlockIds = (): Promise<void> => kotlinBridge.fillBlockIds({storage, blockTypes: BLOCK_TYPES, blockListKey});
