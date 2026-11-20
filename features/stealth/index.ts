import {defineModule} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";

import meta from "./meta";

const controls = kotlinBridge.createStealthControls((message) => useUiStore.getState().showToast(message));

export default defineModule(meta, {
    setup: kotlinBridge.setupStealth,
    ...controls
});
