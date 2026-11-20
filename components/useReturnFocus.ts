import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

export const {focusedElement, panelOf, focusPanel} = kotlinBridge;
export const useReturnFocus = kotlinBridge.createReturnFocus(react);
