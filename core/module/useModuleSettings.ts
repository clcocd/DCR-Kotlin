import {kotlinBridge} from "@/kotlin/bridge";
import {useStore} from "zustand";

import {moduleSettings, moduleSettingsStore, runningModulesStore} from "./registry";
import type {ModuleSettings} from "./types";

export const {useModuleSettings, useContentModuleSettings, useRunningModuleSettings} = kotlinBridge.createModuleHooks<{
    useModuleSettings<Id extends keyof ModuleSettings>(id: Id): ModuleSettings[Id] | undefined;
    useContentModuleSettings<Id extends keyof ModuleSettings>(id: Id): ModuleSettings[Id];
    useRunningModuleSettings<Id extends keyof ModuleSettings>(id: Id): ModuleSettings[Id] | undefined;
}>({useStore, moduleSettings, moduleSettingsStore, runningModulesStore});
