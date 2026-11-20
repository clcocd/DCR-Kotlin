import type {WxtStorageItem} from "wxt/utils/storage";
import * as react from "react";

import {kotlinBridge} from "@/kotlin/bridge";

export const useStorageItem = <T,>(item: WxtStorageItem<T, {}>): T => kotlinBridge.useOptionStorageItem<T>(react, item);
