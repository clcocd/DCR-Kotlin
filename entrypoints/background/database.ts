import {kotlinBridge} from "@/kotlin/bridge";
import {updateDatabase} from "@/core/database";
import {IP_FORMAT} from "@/core/ipdb";
import {dbMetaStorage} from "@/core/storage/items";

export const startDatabaseUpdates = (): ((force?: boolean) => Promise<void>) => kotlinBridge.startDatabaseUpdates({
    browser, updateDatabase, dbMetaStorage, format: IP_FORMAT, production: import.meta.env.PROD
});
