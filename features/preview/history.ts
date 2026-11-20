import {historyDoc, ownPreviewDepth, previewEntry} from "@/core/preview/history";
import {kotlinBridge} from "@/kotlin/bridge";
import {whenDomReady} from "@/utils/dom";
import type {Ctx} from "./meta";

export const createPreviewHistory = (ctx: Ctx) =>
    kotlinBridge.createPreviewHistoryController(ctx, {historyDoc, ownPreviewDepth, previewEntry, whenDomReady});
