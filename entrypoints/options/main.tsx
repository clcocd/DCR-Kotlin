import "@/assets/styles/pages.css";
import * as react from "react";
import {createRoot} from "react-dom/client";

import {TooltipProvider} from "@/components/ui/tooltip";
import {kotlinBridge} from "@/kotlin/bridge";
import {followSystemAppearance} from "@/utils/appearance";

import {App} from "./App";

followSystemAppearance();

kotlinBridge.mountExtensionPage(react, createRoot, App, TooltipProvider);
