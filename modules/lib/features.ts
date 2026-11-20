import {existsSync, readdirSync} from "node:fs";
import {resolve} from "node:path";

import {kotlinBridge} from "../../kotlin/bridge";

export const featureFolders = (root: string, file: string): string[] =>
    kotlinBridge.featureFolders(root, file, {existsSync, readdirSync, resolve});
