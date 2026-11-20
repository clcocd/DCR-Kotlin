import {Ban, NotebookPen, Puzzle, Settings} from "lucide-react";
import * as react from "react";

import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {Field, FieldContent, FieldDescription, FieldLabel} from "@/components/ui/field";
import {Switch} from "@/components/ui/switch";
import {Toggle} from "@/components/ui/toggle";
import {sendMessage} from "@/core/messaging/protocol";
import {backupStorage} from "@/core/storage/items";
import {kotlinBridge} from "@/kotlin/bridge";
import features from "@/features/meta";
import {initBlocksStore, useBlocksStore} from "@/stores/blocks";
import {initMemosStore, useMemosStore} from "@/stores/memos";
import {ExtensionPageVars, initModulesStore, useModulesStore} from "@/stores/modules";

export const App = kotlinBridge.createPopup({
    react, browser, features, send: sendMessage, backupStorage,
    Ban, NotebookPen, Puzzle, Settings, Badge, Button, Card, CardContent, Field,
    FieldContent, FieldDescription, FieldLabel, Switch, Toggle, ExtensionPageVars,
    initBlocksStore, useBlocksStore, initMemosStore, useMemosStore, initModulesStore, useModulesStore
});
