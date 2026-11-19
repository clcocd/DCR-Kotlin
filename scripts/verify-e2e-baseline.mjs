import {readFileSync, appendFileSync} from "node:fs";

const baseline = new Map([
    ["e2e/list.spec.ts:183", "유저 메뉴의 아이디/IP 검색은 글쓴이 검색에 IP 거르기를 얹고, 닉네임 검색은 닉네임으로만 찾는다"],
    ["e2e/options.spec.ts:79", "폰트 교체 설정이 옵션 페이지 본문 글꼴에 걸린다 (크롬이 확장 페이지 body에 넣는 글꼴에 지지 않는다)"],
    ["e2e/preview.spec.ts:666", "디시콘 창에서 고른 디시콘을 CSRF 토큰과 함께 디시콘 댓글로 보낸다"],
    ["e2e/preview.spec.ts:742", "관리자는 댓글을 골라 한 번에 지운다"],
    ["e2e/preview.spec.ts:813", "유동 댓글은 비밀번호를 물어 지우고, 지운 뒤 목록을 다시 받는다"]
]);

const report = JSON.parse(readFileSync(process.argv[2], "utf8"));
const anomalies = [];
const walk = (suite) => {
    for (const spec of suite.specs ?? []) {
        const path = (spec.file ?? suite.file ?? "").replaceAll("\\", "/");
        const filename = path.match(/(?:^|\/)e2e\/(.+\.spec\.ts)$/)?.[1]
            ?? path.match(/([^/]+\.spec\.ts)$/)?.[1] ?? path;
        const key = `e2e/${filename}:${spec.line}`;
        for (const test of spec.tests ?? []) {
            if (test.status === "unexpected" || test.status === "flaky") {
                anomalies.push({key, title: spec.title, status: test.status});
            }
        }
    }
    for (const child of suite.suites ?? []) walk(child);
};
for (const suite of report.suites ?? []) walk(suite);

const stats = report.stats ?? {};
const total = (stats.expected ?? 0) + (stats.unexpected ?? 0) + (stats.flaky ?? 0) + (stats.skipped ?? 0);
const unexpected = anomalies.filter(({key, title}) => baseline.get(key) !== title);
const mismatchedCounts = (stats.unexpected ?? 0) !== anomalies.filter(({status}) => status === "unexpected").length
    || (stats.flaky ?? 0) !== anomalies.filter(({status}) => status === "flaky").length;
const invalid = total < 108 || (stats.skipped ?? 0) !== 0 || mismatchedCounts || (report.errors?.length ?? 0) > 0;

const text = [
    "## Chromium E2E — 순정 6.2.2 실패 기준선 대비",
    "",
    `총 ${total}건 / 통과 ${stats.expected ?? 0}건 / 실패 ${stats.unexpected ?? 0}건 / flaky ${stats.flaky ?? 0}건 / skipped ${stats.skipped ?? 0}건`,
    "",
    "**주의:** 기준선 실패가 남아 있으면 테스트 전체 통과가 아닙니다. 새로운 실패만 CI 오류로 취급합니다.",
    "",
    ...anomalies.map(({key, title, status}) => `- ${baseline.get(key) === title ? "순정 기준선 실패" : "**신규/변경 실패**"}: \`${key}\` — ${title} (${status})`),
    ""
].join("\n");
console.log(text);
if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY, text + "\n");
for (const {key, title, status} of anomalies) {
    console.log(`::warning file=${key.split(":")[0]},line=${key.split(":")[1]}::${baseline.get(key) === title ? "순정 기준선에서 재현된 E2E 실패/flaky" : "새로운 E2E 실패/flaky"}: ${title} (${status})`);
}

if (invalid || unexpected.length) {
    console.error("Chromium E2E에 새로운 실패, 누락 검사 또는 잘못된 결과 데이터가 있습니다.");
    process.exitCode = 1;
}
