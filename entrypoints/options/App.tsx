import {Ban, Database, Info, Keyboard, NotebookPen, Settings} from "lucide-react";
import * as react from "react";

import {Notice} from "@/components/dialogs";
import {ToastHost} from "@/components/overlay/Toasts";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {initBlocksStore} from "@/stores/blocks";
import {initMemosStore} from "@/stores/memos";
import {ExtensionPageVars, initModulesStore} from "@/stores/modules";
import {kotlinBridge} from "@/kotlin/bridge";

import {AboutTab} from "./tabs/AboutTab";
import {BlockTab} from "./tabs/BlockTab";
import {DataTab} from "./tabs/DataTab";
import {GeneralTab} from "./tabs/GeneralTab";
import {MemoTab} from "./tabs/MemoTab";
import {useOptionsStore} from "./optionsStore";
import {ShortcutTab} from "./tabs/ShortcutTab";

export const App = kotlinBridge.createOptionsPage({
    react, browser, develop: import.meta.env.DEV,
    loadRain: () => import("./DcconRain").then(({DcconRain}) => ({default: DcconRain})),
    useOptionsStore, initBlocksStore, initMemosStore, initModulesStore, ExtensionPageVars,
    Button, Separator, Notice, ToastHost, Ban, Database, Info, Keyboard, NotebookPen, Settings,
    AboutTab, BlockTab, DataTab, GeneralTab, MemoTab, ShortcutTab
});
