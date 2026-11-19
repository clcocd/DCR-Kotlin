import {defineModule} from "@/core/module/define";
import {useUiStore} from "@/stores/ui";

import meta from "./meta";

import {kotlinBridge} from "@/kotlin/bridge";

export default defineModule(meta, {
    setup(ctx) {
        ctx.onRevoke(() => {
            kotlinBridge.stealthDispose();
        });

        kotlinBridge.stealthEnable();

        // 광고까지 다 받는 load를 기다리면 한참 늦으므로 body가 생기자마자 만든다.
        if (document.body) {
            kotlinBridge.stealthCreateButton();
        } else {
            document.addEventListener("DOMContentLoaded", kotlinBridge.stealthCreateButton, {once: true, signal: ctx.signal});
        }

        return {isRevealed: kotlinBridge.stealthIsRevealed, toggle: kotlinBridge.stealthToggle};
    },

    shortcuts: {
        stealthPause: (api) => {
            api.toggle();
            useUiStore.getState().showToast(api.isRevealed() ? "이미지를 보이게 했습니다." : "이미지를 숨겼습니다.");
        }
    },

    pageToggles: {
        reveal: {
            desc: "스텔스로 숨긴 이미지를 보입니다",
            isOn: (api) => api.isRevealed(),
            toggle: (api) => api.toggle()
        }
    }
});
