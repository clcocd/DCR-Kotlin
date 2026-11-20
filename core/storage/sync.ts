import pLimit from "p-limit";
import {storage, type StorageItemKey} from "wxt/utils/storage";

import {kotlinBridge} from "@/kotlin/bridge";
import {onBfcacheRestore} from "@/utils/dom";
import {saveOrReload} from "@/utils/error";

export interface StorageSync {
    load(): Promise<void>;
    start(signal?: AbortSignal): Promise<void>;
    save(write: () => Promise<void>, failure: string): Promise<void>;
}

export interface ListSyncOptions<T extends string, V> {
    types: readonly T[];
    keyOf: (type: T) => StorageItemKey;
    normalize: (value: unknown) => V;
    get: () => Record<T, V>;
    set: (lists: Record<T, V>) => void;
    failure: string;
    extra?: Partial<Record<StorageItemKey, (value: unknown) => void>>;
    lock?: string;
}

const api = kotlinBridge.createStorageSync({
    storage, onBfcacheRestore, saveOrReload, limit: pLimit,
    runtimeActive: () => Boolean(browser.runtime?.id),
    allowLocks: () => !(import.meta.env.BROWSER === "firefox" && location.protocol !== "moz-extension:")
});

export const watchStorage = api.watch;
export const readItems = api.read;
export const storageSync = api.sync;
export const typedListSync = api.lists;
