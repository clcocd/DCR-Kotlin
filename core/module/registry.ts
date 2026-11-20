import {addFilter} from "@/core/filtering";
import {documentUrl} from "@/core/http/urls";
import {kotlinBridge} from "@/kotlin/bridge";
import {storage} from "wxt/utils/storage";
import {shallow} from "zustand/vanilla/shallow";
import {createStore} from "zustand/vanilla";

import {MODULES_KEY, moduleSettingsKey} from "@/core/storage/items";
import {watchStorage} from "@/core/storage/sync";
import type {SettingValue} from "@/core/storage/types";
import {onBfcacheRestore} from "@/utils/dom";

import {enablesOf, readModuleStorage, settingsOf} from "./settings";
import type {AnyModule, ModuleSettings} from "./types";

export type ModuleLoaders = Record<string, () => Promise<AnyModule>>;
export type BeforeStart = () => Promise<void>;

export const moduleSettingsStore = createStore<Record<string, Readonly<Record<string, SettingValue>>>>(() => ({}));
export const runningModulesStore = createStore<Record<string, boolean>>(() => ({}));
const moduleApisStore = createStore<Record<string, unknown>>(() => ({}));
export const subscribeModuleApis = moduleApisStore.subscribe;

export const moduleSettings = <Id extends keyof ModuleSettings>(id: Id, state = moduleSettingsStore.getState()): ModuleSettings[Id] | undefined =>
    state[id] as ModuleSettings[Id] | undefined;

export const runningModuleSettings = <Id extends keyof ModuleSettings>(id: Id): ModuleSettings[Id] | undefined =>
    runningModulesStore.getState()[id] ? moduleSettings(id) : undefined;

const registry = kotlinBridge.createModuleRegistry({
    addFilter, documentUrl, moduleSettingsStore, runningModulesStore, moduleApisStore, shallow, watchStorage, onBfcacheRestore,
    readModuleStorage, settingsOf, enablesOf, storage, modulesKey: MODULES_KEY, settingsKey: moduleSettingsKey
});

export const {getModuleApi, runShortcut, pageToggleStates, pageState, settledPageState, runPageToggle, stopAll, loadAll} = registry;
