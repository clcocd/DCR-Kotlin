import type {IpCategory, IpInfoFilter} from "@/core/database";
import {defineModuleMeta} from "@/core/module/define";
import type {ModuleContext, SettingSchema, SettingsSchema} from "@/core/module/types";
import {BOARD_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

/** 배지 색 기본값. 키마다 `${key}Color` 설정이 생긴다 (IP는 분류가 키). */
const runtimeOptions = kotlinBridge.userInfoRuntimeOptions();
export const BADGE_COLORS = runtimeOptions.badgeColors satisfies Record<IpCategory | "uid" | "ratio" | "ratioAlarm", string>;

export type BadgeColor = keyof typeof BADGE_COLORS;

const IP_INFO_FILTERS: Record<IpInfoFilter, string> = runtimeOptions.ipInfoFilters;
const LOW_ACTIVITY_ACTIONS = runtimeOptions.lowActivityActions;

export const settings = {
    showFixedNickUID: {type: "check", default: true},
    showHalfFixedNickUID: {type: "check", default: true},
    ipInfoFilter: {type: "option", default: "all", items: IP_INFO_FILTERS},
    checkRatio: {type: "check", default: false},
    alarmRatio: {type: "range", default: 0, min: 0, max: 5000, step: 10, unit: "개"},
    lowActivityAction: {type: "option", default: "tag", items: LOW_ACTIVITY_ACTIONS},
    ...(Object.fromEntries(Object.entries(BADGE_COLORS).map(([key, color]) => [`${key}Color`, {type: "color", default: color}])) as
        Record<`${BadgeColor}Color`, Extract<SettingSchema, { type: "color" }>>),
    badgeOrder: {
        type: "order",
        items: {UID: "아이디/IP", MEMO: "메모", RATIO: "글댓비"},
        default: ["UID", "MEMO", "RATIO"]
    }
} satisfies SettingsSchema;

export type Ctx = ModuleContext<typeof settings>;

export default defineModuleMeta({
    id: "userinfo",
    urls: [BOARD_PAGE],
    settings
});
