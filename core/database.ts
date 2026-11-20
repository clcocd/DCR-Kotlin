import {http} from "@/core/http/client";
import {urls} from "@/core/http/urls";
import {createIpLookup, IP_FORMAT, parseIpData, parseStoredIpData} from "@/core/ipdb";
import {storage} from "wxt/utils/storage";
import {createStore} from "zustand/vanilla";

import {DB_KEYS, DB_META_KEY, dbMetaStorage, writeDatabase} from "@/core/storage/items";
import {watchStorage} from "@/core/storage/sync";
import {onBfcacheRestore} from "@/utils/dom";
import {kotlinBridge} from "@/kotlin/bridge";

/** 배지 색. 첫 번째 후보의 국가/VPN으로 정한다. */
export type IpCategory = "korea" | "japan" | "china" | "foreign" | "vpn";

interface IpInfo {
    /** 후보를 겹치지 않게 모두 쓰고 한국은 국가를 생략한다. */
    label: string;
    /** label과 같은 후보를 한 줄에 하나씩 (툴팁용). */
    title: string;
    category: IpCategory;
}

export type IpInfoFilter = "all" | "foreign" | "vpn" | "none";

interface DatabaseService {
    updateDatabase(force?: boolean): Promise<void>;
    databaseVersion(): number;
    subscribeDatabase(listener: () => void): () => void;
    onDatabaseChange(listener: () => void): () => void;
    releaseDatabase(): void;
    initDatabase(): Promise<void>;
    ipInfoOf(ip: string): IpInfo | undefined;
    passesIpFilter(info: IpInfo, filter: IpInfoFilter): boolean;
}

export const {
    updateDatabase, databaseVersion, subscribeDatabase, onDatabaseChange, releaseDatabase,
    initDatabase, ipInfoOf, passesIpFilter
} = kotlinBridge.createDatabase<DatabaseService>({
    http, urls: urls.database, storage, createIpLookup, format: IP_FORMAT, parseIpData, parseStoredIpData,
    keys: DB_KEYS, metaKey: DB_META_KEY, dbMetaStorage, writeDatabase, watchStorage, onBfcacheRestore,
    versionStore: createStore(() => 0)
});
