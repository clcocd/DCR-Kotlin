import {kotlinBridge} from "@/kotlin/bridge";

/** 두 번 누르기 확인. 같은 key를 시간 안에 다시 부르면 true이며, Kotlin이 상태를 관리한다. */
export const createDoublePress = kotlinBridge.createDoublePress;
