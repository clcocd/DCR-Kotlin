import {ChevronDown, ChevronUp, ExternalLink, Link2} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {Button} from "@/components/ui/button";
import {Spinner} from "@/components/ui/spinner";
import {TooltipButton} from "@/components/WithTooltip";
import {getEntry, setEntry} from "@/core/preview/cache";
import {captchaImage, viewUrl, vote} from "@/core/preview/request";
import type {PostInfo} from "@/core/preview/types";
import {copyText} from "@/stores/ui";
import {notifyManage} from "@/stores/notify";

import {usePreviewStore} from "./previewStore";

export const Votes = kotlinBridge.createVotes<{post: PostInfo}>({
    react, Button, Spinner, TooltipButton, ChevronDown, ChevronUp, ExternalLink, Link2,
    getEntry, setEntry, captchaImage, viewUrl, vote, copyText, notifyManage, store: usePreviewStore
});
