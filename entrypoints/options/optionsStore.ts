import {create} from "zustand";

import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";

interface OptionsState {
    rain: number;
    notice: string | null;
    startRain: () => void;
    endRain: () => void;
}

/** 옵션 페이지 화면 상태. 사이드바와 탭들이 props를 거치지 않고 같이 쓴다. */
export const useOptionsStore = create<OptionsState>()(kotlinBridge.optionStoreState);

/** 확인 알림(실패 등) - 탭마다 상태를 두지 않고 이것으로 띄운다. */
export const notify = (message: string): void => useOptionsStore.setState({notice: message});

export type Done = string | { message: string; undo: () => void };

export const notifyDone = (done: Done): void => kotlinBridge.notifyOptionDone(useUiStore.getState().showToast, done);
