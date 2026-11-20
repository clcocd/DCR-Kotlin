import {useLayoutEffect} from "react";
import {storage} from "wxt/utils/storage";
import {create, type StateCreator, type StoreApi} from "zustand";

import {enablesOf, isModuleEnabled, normalizeSetting, readModuleStorage, settingsOf} from "@/core/module/settings";
import {LIST_LOCKS, MODULES_KEY, moduleDataKey, moduleKeyModule, moduleSettingsKey, moduleSettingsStorage, modulesStorage} from "@/core/storage/items";
import {storageSync} from "@/core/storage/sync";
import type {SettingValue} from "@/core/storage/types";
import features from "@/features/meta";
import {kotlinBridge} from "@/kotlin/bridge";

type Values = Record<string, SettingValue>;

interface ModulesState {
    /** 모듈 on/off (저장값 없으면 defaultEnable). */
    enables: Record<string, boolean>;
    /** 모듈별 설정값 (스키마에 맞춘 값). */
    values: Record<string, Values>;
    toggle: (id: string, value: boolean) => Promise<void>;
    changeSetting: (id: string, key: string, value: SettingValue) => Promise<void>;
}

interface SettingsStateService {
    init: StateCreator<ModulesState>;
    attach(store: StoreApi<ModulesState>): void;
    initModulesStore(): Promise<void>;
    ExtensionPageVars(): null;
}

const state = kotlinBridge.createSettingsState<SettingsStateService>({
    features, useLayoutEffect, storage, storageSync, listKeys: () => browser.storage.local.getKeys(),
    enablesOf, isModuleEnabled, normalizeSetting, readModuleStorage, settingsOf, lockName: LIST_LOCKS.moduleSettings,
    modulesKey: MODULES_KEY, moduleDataKey, moduleKeyModule, moduleSettingsKey, moduleSettingsStorage, modulesStorage
});
export const {initModulesStore, ExtensionPageVars} = state;
export const useModulesStore = create<ModulesState>(state.init);
state.attach(useModulesStore);
