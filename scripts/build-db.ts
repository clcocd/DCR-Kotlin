/// <reference types="bun" />

/** IP, 밴 DB 만들기. 워크플로(db.yml)가 db/ 폴더를 data 브랜치로 올린다. */

import ky from "ky";
import {Reader} from "mmdb-lib";
import {long2ip, Netmask} from "netmask";
import {kotlinBridge} from "../kotlin/bridge";

await kotlinBridge.buildIpDatabase({
    http: ky, long2ip, reader: (data: ArrayBuffer) => new Reader(Buffer.from(data), {cache: new Map()}),
    netmask: (cidr: string) => new Netmask(cidr), file: Bun.file, write: Bun.write
});
