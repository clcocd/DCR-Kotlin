import {defineModule} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";

import meta from "./meta";

export default defineModule(meta, {
    setup: kotlinBridge.setupStealth,

    shortcuts: {
        stealthPause: (api) => kotlinBridge.stealthShortcut(api.toggle, api.isRevealed, useUiStore.getState().showToast)
    },

    pageToggles: {
        reveal: {
            desc: "스텔스로 숨긴 이미지를 보입니다",
            isOn: (api) => api.isRevealed(),
            toggle: (api) => api.toggle()
        }
    }
});
