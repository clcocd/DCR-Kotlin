import {Toast} from "@base-ui/react/toast";
import {CircleAlert, Info, TriangleAlert, X} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {Button} from "@/components/ui/button";
import {useUiStore} from "@/stores/ui";

export const ToastHost = kotlinBridge.createToastHost({react, Toast, Button, store: useUiStore, CircleAlert, Info, TriangleAlert, X});
