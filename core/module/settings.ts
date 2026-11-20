import {MODULES_KEY, moduleSettingsKey} from "@/core/storage/items";
import {readItems} from "@/core/storage/sync";
import {kotlinBridge} from "@/kotlin/bridge";

import type {ModuleDefinition} from "./types";

/** 모듈 on/off. 저장값이 boolean이 아니면 defaultEnable을 따른다. */
export const isModuleEnabled = (def: Pick<ModuleDefinition, "id" | "defaultEnable">, enables: Record<string, unknown>): boolean =>
    kotlinBridge.moduleEnabled(def.id, def.defaultEnable, enables);

/** order 기본값은 원본 스키마와 별개인 배열이다. */
export const defaultValue = kotlinBridge.defaultSetting;
export const normalizeSetting = kotlinBridge.normalizeSetting;

export const {moduleEnables: enablesOf, settingsOf} = kotlinBridge;
export const readModuleStorage = kotlinBridge.createModuleRead({readItems, modulesKey: MODULES_KEY, moduleSettingsKey});
