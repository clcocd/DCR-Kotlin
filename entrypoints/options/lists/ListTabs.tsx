import {cn} from "cn";
import {ClipboardCopy, FileDown, Plus, Trash2, Upload} from "lucide-react";
import * as react from "react";
import type {ReactNode} from "react";

import {ConfirmDialog} from "@/components/dialogs";
import {FOCUS_RING} from "@/components/focusRing";
import {TooltipButton} from "@/components/WithTooltip";
import {focusPanel} from "@/components/useReturnFocus";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {friendlyMessage, SAVE_FAILED} from "@/utils/error";
import {isRecord} from "@/utils/record";

import {kotlinBridge} from "@/kotlin/bridge";
import {ImportDialog} from "../shared/ImportDialog";
import {EmptyList, SearchInput} from "../shared/widgets";
import {notify, notifyDone} from "../optionsStore";
import {copyText} from "@/stores/ui";
import {RefresherSelect} from "../shared/RefresherSelect";


export const restoreRemoved = kotlinBridge.restoreOptionList;

const controls = {
    react, cn, focusRing: FOCUS_RING, focusPanel, friendlyMessage, saveFailed: SAVE_FAILED, isRecord,
    notify, notifyDone, copyText, ConfirmDialog, TooltipButton, Badge, Button, Card, CardContent,
    Table, TableBody, TableCell, TableHead, TableHeader, TableRow, Tabs, TabsContent, TabsList,
    TabsTrigger, ImportDialog, EmptyList, SearchInput, RefresherSelect, ClipboardCopy, FileDown, Plus, Trash2, Upload
};

export const ListRow = kotlinBridge.createOptionListRow(controls) as (props: {
    head: ReactNode; label: string; info: ReactNode; onEdit: () => void; onRemove: () => void
}) => ReactNode;

interface ListTabsProps<T extends string, I> {
    types: readonly T[];
    names: Record<T, string>;
    label: string;
    fileName: string;
    columns: [string, string];
    emptyText: (type: T) => string;
    exportData: () => unknown;
    /** 돌려준 수가 0이면 다른 데이터로 보고 실패로 알린다. */
    importData: (parsed: Record<string, unknown>) => Promise<number>;
    onClear: (type: T) => Promise<void>;
    onAdd: (type: T) => void;
    toolbar: (type: T) => ReactNode;
    /** 오래된 것부터 준다. 표시할 때 뒤집는다. */
    items: (type: T) => readonly I[];
    searchText: (item: I) => (string | undefined)[];
    galleryOf: (item: I) => string | undefined;
    onRemoveMany: (type: T, items: readonly I[]) => Promise<void>;
    itemKey: (item: I) => string;
    onRestore: (type: T, restore: (current: I[]) => I[]) => Promise<void>;
    row: (type: T, item: I, remove: () => void) => ReactNode;
}

export const ListTabs = kotlinBridge.createOptionListTabs(controls) as <T extends string, I>(props: ListTabsProps<T, I>) => ReactNode;
