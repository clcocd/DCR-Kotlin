import * as react from "react";

import {Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Collapsible, CollapsibleContent} from "@/components/ui/collapsible";
import {Separator} from "@/components/ui/separator";
import {Switch} from "@/components/ui/switch";
import {kotlinBridge} from "@/kotlin/bridge";
import type {LabeledSetting, ModuleInfo} from "@/core/module/types";
import features from "@/features/meta";
import {useModulesStore} from "@/stores/modules";
import {SAVE_FAILED} from "@/utils/error";

import {EmptyList, SearchInput} from "../shared/widgets";
import {notify} from "../optionsStore";
import {SettingItem} from "./SettingItem";

type SettingEntry = [key: string, schema: LabeledSetting];

export const matchSettings = (feature: Pick<ModuleInfo, "name" | "description" | "settings">, needle: string): SettingEntry[] | undefined =>
    kotlinBridge.matchOptionSettings(feature, needle) as SettingEntry[] | undefined;

export const GeneralTab = kotlinBridge.createGeneralTab({
    react, features, useModulesStore, notify, saveFailed: SAVE_FAILED,
    Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle,
    Collapsible, CollapsibleContent, Separator, Switch, EmptyList, SearchInput, SettingItem
});
