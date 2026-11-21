import type {IpCategory} from "@/core/database";
import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

/** 배지 색 기본값. 키마다 `${key}Color` 설정이 생긴다 (IP는 분류가 키). */
const runtimeOptions = kotlinBridge.userInfoRuntimeOptions();
export const BADGE_COLORS = runtimeOptions.badgeColors satisfies Record<IpCategory | "uid" | "ratio" | "ratioAlarm", string>;

export type BadgeColor = keyof typeof BADGE_COLORS;

export const settings = kotlinBridge.userInfoSettings(
    BADGE_COLORS, runtimeOptions.ipInfoFilters, runtimeOptions.lowActivityActions
) satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta(kotlinBridge.userInfoMeta([BOARD_PAGE], settings));
