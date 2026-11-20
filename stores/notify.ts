import {kotlinBridge} from "@/kotlin/bridge";

import {useUiStore} from "./ui";

export const {reportFailure, notifyManage} = kotlinBridge.createNotify(useUiStore.getState);
