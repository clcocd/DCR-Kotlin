import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

export const useScrollLock = kotlinBridge.createScrollLock(react);
