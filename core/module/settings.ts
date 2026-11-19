import {MODULES_KEY, moduleSettingsKey} from "@/core/storage/items";
import {readItems} from "@/core/storage/sync";
import type {SettingValue} from "@/core/storage/types";
import {kotlinBridge} from "@/kotlin/bridge";
import type {ModuleDefinition, SettingSchema} from "./types";

/** on/off 판단은 콘텐츠, 배경, 옵션에서 같은 Kotlin 구현을 사용한다. */
export const isModuleEnabled = (
    def: Pick<ModuleDefinition, "id" | "defaultEnable">, enables: Record<string, unknown>
): boolean => kotlinBridge.isModuleEnabled(def, enables);

/** order 기본값의 복사 규칙을 포함한 Kotlin 기본값. */
export const defaultValue = (schema: SettingSchema): SettingValue => kotlinBridge.defaultSettingValue(schema);

/** 저장된 설정값을 원본의 check/text/option/key/color/range/order 규칙으로 정규화한다. */
export const normalizeSetting = (schema: SettingSchema, value: unknown): SettingValue =>
    kotlinBridge.normalizeModuleSetting(schema, value);

/** 저장소 모양이 잘못되면 빈 on/off 객체를 돌려준다. */
export const enablesOf = (stored: unknown): Record<string, unknown> => kotlinBridge.moduleEnablesOf(stored);

/** 모듈 스키마에 정의된 설정 키만 정규화한다. */
export const settingsOf = (def: Pick<ModuleDefinition, "settings">, stored: unknown): Record<string, SettingValue> =>
    kotlinBridge.moduleSettingsOf(def, stored);

/**
 * 모듈 on/off와 주어진 모듈들의 설정을 한 번의 storage.local.get으로 읽는다. 모듈마다 따로 읽으면 왕복이 모듈 수만큼 쌓인다.
 * 항목(defineItem)은 만드는 순간 키마다 한 번 더 읽으므로 키로 읽는다 (core/storage/items.ts). 설정은 저장소 값 그대로다 (settingsOf로 맞춘다).
 * 콘텐츠 레지스트리, 배경 모듈이 같이 쓴다.
 */
export const readModuleStorage = async (ids: readonly string[]): Promise<{ enables: Record<string, unknown>; settings: Map<string, unknown> }> => {
    const items = await readItems([MODULES_KEY, ...ids.map(moduleSettingsKey)]);
    return {enables: enablesOf(items.get(MODULES_KEY)), settings: new Map(ids.map((id) => [id, items.get(moduleSettingsKey(id)) ?? null]))};
};
