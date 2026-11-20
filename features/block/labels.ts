import {kotlinBridge} from "@/kotlin/bridge";
import type {LabelsOf} from "@/core/module/types";
import type meta from "./meta";

/** 원본 설정 문구는 Kotlin에서 생성한다. 타입·메타 검사는 유지한다. */
export default kotlinBridge.moduleLabels("block") satisfies LabelsOf<typeof meta>;
