import {BLURRED_ROW_SELECTOR} from "@/core/block";
import {rowPostNo} from "@/core/http/urls";
import {LIST_SELECTOR} from "@/core/list";
import {kotlinBridge} from "@/kotlin/bridge";

import {NOTICE_ICON} from "./markup";
import type {Ctx} from "./meta";

export const setTitleCount = kotlinBridge.setTitleCount;

export const createUnseenCounter = (ctx: Ctx) =>
    kotlinBridge.createUnseenCounter(ctx, LIST_SELECTOR, NOTICE_ICON, BLURRED_ROW_SELECTOR, rowPostNo);
