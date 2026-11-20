import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {type GallogActivity, getGallogActivity} from "@/core/gallog";
import {isFresh, useUiStore} from "@/stores/ui";

export type ActivityState = GallogActivity | undefined | "loading" | "error";

export const useGallogActivity = kotlinBridge.createGallogActivityHook<ActivityState>({
    react, store: useUiStore, isFresh, getGallogActivity
});
