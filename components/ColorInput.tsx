import * as react from "react";
import type {ComponentProps} from "react";
import {kotlinBridge} from "@/kotlin/bridge";

export const ColorInput = kotlinBridge.createColorInput<ComponentProps<"input"> & {width?: number; height?: number}>(react);
