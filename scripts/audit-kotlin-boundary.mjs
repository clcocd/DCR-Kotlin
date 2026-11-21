import {readdirSync, readFileSync} from "node:fs";
import {join, relative} from "node:path";

// Kotlin 브리지를 직접 참조하지 않는 TS 파일을 찾는다.
// 브리지 미참조는 미이식 판정이 아니다(빌드 설정·타입 연결부가 포함된다).
const roots = ["core", "features", "entrypoints", "stores", "components", "utils"];
const results = [];
function walk(dir) {
    for (const entry of readdirSync(dir, {withFileTypes: true})) {
        const path = join(dir, entry.name);
        if (entry.isDirectory()) {
            walk(path);
        } else if (entry.isFile() && /\.tsx?$/.test(entry.name) && !/\.d\.ts$/.test(entry.name)) {
            const source = readFileSync(path, "utf8");
            results.push({
                path: relative(".", path).replaceAll("\\", "/"),
                bridged: /kotlinBridge|__DCR_KOTLIN__/.test(source),
                lines: source.split("\n").length
            });
        }
    }
}
for (const root of roots) walk(root);
const missing = results.filter(({bridged}) => !bridged);
console.log(`Runtime TypeScript files: ${results.length}`);
console.log(`Direct Kotlin bridge references: ${results.length - missing.length}`);
console.log(`Without direct bridge reference (investigate, not necessarily unported): ${missing.length}`);
for (const entry of missing) console.log(`  ${entry.path} (${entry.lines} lines)`);
