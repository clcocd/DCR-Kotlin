import {kotlinBridge} from "@/kotlin/bridge";
import type {LabelsOf} from "@/core/module/types";
import {ENGINE_IDS, ENGINES} from "./meta";
import type meta from "./meta";

/** 이미지 검색 엔진 목록과 그룹 문구는 Kotlin/JS에서 생성한다. */
export default kotlinBridge.imageSearchLabels(ENGINE_IDS, ENGINES) satisfies LabelsOf<typeof meta>;
