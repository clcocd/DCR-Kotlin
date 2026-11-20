import {BookOpen, Bug, ClipboardCopy, Code, Heart, MessageCircle, Star, Tag, Users} from "lucide-react";
import * as react from "react";

import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {CLOUD_QUOTA} from "@/core/backup";
import {dbMetaStorage} from "@/core/storage/items";
import features from "@/features/meta";
import {useBlocksStore} from "@/stores/blocks";
import {useMemosStore} from "@/stores/memos";
import {useModulesStore} from "@/stores/modules";

import {kotlinBridge} from "@/kotlin/bridge";
import {Section} from "../shared/widgets";
import {copyText} from "@/stores/ui";

export const AboutTab = kotlinBridge.createAboutTab({
    react, browser, firefox: import.meta.env.BROWSER === "firefox", quota: CLOUD_QUOTA, dbMetaStorage,
    features, useBlocksStore, useMemosStore, useModulesStore, copyText, Section, Badge, Button,
    BookOpen, Bug, ClipboardCopy, Code, Heart, MessageCircle, Star, Tag, Users
});
