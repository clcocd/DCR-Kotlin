import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

export const useWheelGesture = kotlinBridge.createWheelGesture(react);
