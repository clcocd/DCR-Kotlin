import {kotlinBridge} from "@/kotlin/bridge";
import type {LabelsOf} from "@/core/module/types";
import type meta from "./meta";

/** 글댓비·IP·배지 설정 문구는 Kotlin/JS에서 생성한다. */
export default kotlinBridge.userInfoLabels() satisfies LabelsOf<typeof meta>;
