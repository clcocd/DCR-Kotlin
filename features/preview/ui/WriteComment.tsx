import {Send, Smile, Type, X} from "lucide-react";
import QuickLRU from "quick-lru";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Spinner} from "@/components/ui/spinner";
import {Textarea} from "@/components/ui/textarea";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {TooltipButton, WithTooltip} from "@/components/WithTooltip";
import {captchaImage, normalizeTxtcon, TXTCON_BACKGROUNDS, TXTCON_COLORS} from "@/core/preview/request";
import {postKey} from "@/core/preview/cache";
import {useUiStore} from "@/stores/ui";
import {isTimeoutError} from "@/utils/error";
import {loggedInUserId} from "@/utils/user";

import {postComment, prefetchTurnstile, turnstileFields} from "../comment-submit";
import {saveNonmember, savedNonmember} from "../nonmember";
import {DcconPopup} from "./DcconPopup";
import {NO_REPLY, refreshCommentsOf, usePreviewStore} from "./previewStore";

export const WriteComment = kotlinBridge.createWriteCommentView({
    react, Button, Input, Spinner, Textarea, ToggleGroup, ToggleGroupItem, TooltipButton, WithTooltip,
    Send, Smile, Type, X, captchaImage, normalizeTxtcon, backgrounds: TXTCON_BACKGROUNDS, colors: TXTCON_COLORS, postKey,
    useUiStore, isTimeoutError, loggedInUserId, postComment, prefetchTurnstile, turnstileFields, saveNonmember,
    savedNonmember, DcconPopup, noReply: NO_REPLY, refreshCommentsOf, store: usePreviewStore,
    drafts: new QuickLRU<string, string>({maxSize: 10})
});
