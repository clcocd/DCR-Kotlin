import {ChevronDown, ChevronUp, GripVertical, Undo2} from "lucide-react";
import * as react from "react";
import {shallow} from "zustand/shallow";

import {ColorInput} from "@/components/ColorInput";
import {WithTooltip} from "@/components/WithTooltip";
import {Slider as SliderPrimitive} from "@base-ui/react/slider";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Kbd} from "@/components/ui/kbd";
import {Switch} from "@/components/ui/switch";
import {kotlinBridge} from "@/kotlin/bridge";
import type {LabeledSetting} from "@/core/module/types";
import type {SettingValue} from "@/core/storage/types";
import {pressedKey} from "@/utils/event";
import {cn} from "cn";

import {RefresherSelect} from "../shared/RefresherSelect";

interface SettingItemProps {
    schema: LabeledSetting;
    value: SettingValue;
    /** 묶음용 - 설명을 숨기고 이름과 컨트롤만 한 줄에 둔다. */
    compact?: boolean;
    /** key 설정 전용. 같은 모듈의 다른 key 설정이 쓰는 키라 고를 수 없다. */
    takenKeys?: string[];
    onChange: (value: SettingValue) => void;
}

export const SettingItem = kotlinBridge.createSettingItem({
    react, shallow, pressedKey, cn, Slider: SliderPrimitive, ColorInput, WithTooltip,
    Button, Input, Kbd, Switch, RefresherSelect, ChevronDown, ChevronUp, GripVertical, Undo2
}) as (props: SettingItemProps) => import("react").ReactNode;
