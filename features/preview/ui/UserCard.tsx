import {cn} from "cn";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";
import {useShallow} from "zustand/react/shallow";

import {FOCUS_RING} from "@/components/focusRing";
import {useGallogActivity} from "@/components/overlay/gallogActivity";
import {badgeViewOf, DEFAULT_BADGE_VIEW, showsUid} from "@/core/badges";
import {banReasonsOf, databaseVersion, ipInfoOf, onDatabaseChange, passesIpFilter} from "@/core/database";
import {useRunningModuleSettings} from "@/core/module/useModuleSettings";
import type {User} from "@/core/preview/types";
import {useUserMemo} from "@/stores/memos";
import {isFresh, isLowActivity, useUiStore} from "@/stores/ui";

import {previewGallery, usePreviewStore} from "./previewStore";

export const UserCard = kotlinBridge.createWriterCard<{user: User; fetchRatio?: boolean; op?: boolean}>({
    react, cn, useShallow, focusRing: FOCUS_RING, useGallogActivity, badgeViewOf, defaultBadgeView: DEFAULT_BADGE_VIEW,
    showsUid, banReasonsOf, databaseVersion, ipInfoOf, onDatabaseChange, passesIpFilter, useRunningModuleSettings,
    useUserMemo, isFresh, isLowActivity, uiStore: useUiStore, previewGallery, previewStore: usePreviewStore
});
