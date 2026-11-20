import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import packs from "./dccon-rain.json";
import {useOptionsStore} from "./optionsStore";

export const DcconRain = kotlinBridge.createDcconRain(react, useOptionsStore.getState, packs);
