import {kotlinBridge} from "@/kotlin/bridge";
import {Puzzle} from "lucide-react";

import type {AnyModuleIcons, AnyModuleLabels, AnyModuleMeta} from "@/core/module/types";

/** 모듈 목록 (옵션, 팝업용). meta.ts만 모아 setup 코드가 번들에 들어가지 않게 하고, icons·labels는 여기서 합친다. */
const modules = import.meta.glob<AnyModuleMeta>("./*/meta.ts", {eager: true, import: "default"});
const icons = import.meta.glob<AnyModuleIcons>("./*/icons.ts", {eager: true, import: "default"});
const labels = import.meta.glob<AnyModuleLabels>("./*/labels.ts", {eager: true, import: "default"});

export default kotlinBridge.assembleFeatureInfo(modules, icons, labels, Puzzle);
