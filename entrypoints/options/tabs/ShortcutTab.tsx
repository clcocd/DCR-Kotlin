import {ExternalLink} from "lucide-react";
import * as react from "react";

import {Button} from "@/components/ui/button";
import {kotlinBridge} from "@/kotlin/bridge";
import {Kbd} from "@/components/ui/kbd";

import {Section} from "../shared/widgets";

export const ShortcutTab = kotlinBridge.createShortcutTab({
    react, browser, firefox: import.meta.env.BROWSER === "firefox", ExternalLink, Button, Kbd, Section
});
