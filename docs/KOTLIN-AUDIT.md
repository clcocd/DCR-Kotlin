# Kotlin/JS 전환 감사 — 1차 기준 기록 (2026-11-20)

> **상태: 전환·실사용 검증 미완료.** 이 문서는 전체 완료 선언이 아니라 기준선, 코드 변경 목록, 확인된 결함과 미검증 영역을 분리한 기록이다. 완료하지 않은 검사는 통과로 표시하지 않는다.

## 비교 기준

- 기준 커밋: `c1b857b1bf0a0ca3eddfbdebfb2885868cf691c2` (6.2.2 원본 가져오기)
- 감사 시작 `main`: `e7de6f22b9ddaf1a9c77d9e3c2abc723d9f21a66`
- GitHub 비교: `c1b857b1bf...main` 의 변경 파일/추가 파일 목록과 현재 실제 소스 확인.
- 개인 패치 ZIP: `dcrefresher-reborn-6.2.2-doublecon-empty-message-unified.zip`의 `PATCH-NOTES.md`는 **참고 자료**. 소스 기준선으로 삼지 않음. 최종 이식 판단은 `docs/PATCH-DECISIONS.md`에 분리.

## 실제 저장소의 코드 분포 (감사 시작 `main`)

| 구분 | 확인된 수 |
| --- | ---: |
| `kotlin/src/jsMain` Kotlin 파일 | 18 |
| `kotlin/src/jsTest` Kotlin 테스트 파일 | 13 |
| TypeScript/TSX 소스 파일 (테스트 제외) | 249 |
| `tests/unit` Vitest 테스트 파일 | 97 |
| 원본 가져오기 이후 변경된 원래 TS 기능 코드 파일 | 18 |

**18개 Kotlin 소스 중 `Main.kt`는 런타임 브리지, 나머지는 원본 기능의 일부 처리 함수다.** 현재 구조는 원본 TypeScript/WXT/React 앱이 Kotlin 함수들을 호출하는 혼합 구현이다. 기능 모듈 11개의 완전한 Kotlin 대체가 아니라는 점을 분명히 한다.

### Kotlin으로 옮겨 연결된 부분

- URL/갤러리 타입/검색 주소, 53비트 해시, 닉콘 분류.
- 댓글 응답 해석, 댓글 비교·전처리·음성 주소 처리.
- 차단 목록 정규화 및 텍스트 처리.
- 단축키/두 번 누르기 상태, 글꼴 CSS, 레이아웃 선택자 CSS.
- 스텔스 버튼 DOM, 새 글 수 탭 제목, 공통 DOM/테마 처리, 미리보기 미디어의 접근성 표시.

위 항목은 **구현이 존재한다는 뜻**이고, 각각의 모든 실제 페이지 경계 조건이 검증되었다는 뜻은 아니다.

### 아직 Kotlin 전체 전환이 안 된 주요 구역

- `core/module/`: 모듈 수명주기, 레지스트리, 설정 변경 연결.
- `core/storage/`, `core/backup.ts`, `core/settings-transfer.ts`: 저장·동기화·백업/복원.
- `core/http/client.ts`, `core/messaging/`: 요청 제한·취소·인증·컨텍스트 간 메시지.
- `core/preview/parser.ts`, `submit.ts`, `request.ts`, `cache.ts`: 본문/댓글 로딩과 실제 제출 경로.
- `features/refresh/`: 새로고침 주기 및 로딩 상태 전체(제목 카운터만 일부 Kotlin).
- `features/preview/ui/`: React/TSX 미리보기·댓글·디시콘 팝업 등(미디어 표시 보조만 Kotlin).
- `features/write`, `search`, `manage`, `userinfo`, `block`, `imagesearch`: 모듈 전체 Kotlin 전환 미완료.
- `entrypoints/`, 옵션·팝업 및 다수 공통 UI: WXT/React/TS 유지.

## 원본 6.2.2 소스 무결성 검사

- 사용자 원본 `DCRefresher-Reborn-develop.zip`과 최초 가져오기 `c1b857b1bf...`를 파일 경로/내용 Git blob SHA 기준으로 비교했다.
- 원본 비교 대상 **383개** 중 최초 `main`의 원본 파일은 **382개 일치, 1개 누락**. 누락: `docs/README.md`.
- 근본 원인: 가져오기 workflow의 `rsync --exclude='README.md'`가 루트뿐 아니라 중첩 디렉터리의 `README.md`까지 제외함.
- `docs/README.md` 원본 파일을 **동일 blob SHA `948cf95d3233b10e78db0b13b0ee74d1d6a86698`**로 복구했다.
- `rsync --exclude='/README.md'`로 바꾸고 `docs/README.md` 존재 검사 추가. 복구한 원본 파일 383개의 경로+blob SHA 지문이 ZIP과 일치했다.

## CI 검증 범위와 실제 실행 결과

개발용 CI에는 다음이 포함된다.

1. `gradle -p kotlin jsNodeTest jsBrowserDistribution`
2. `bun ci` → `bun run compile` → `bun run test`
3. `bun run build`; Kotlin 번들·MV3 Manifest 포함 검사.
4. **실제 Chromium 확장 E2E** (`bunx playwright test --project=chromium`). 가짜 디시 페이지를 사용하고 서버 쓰기는 하지 않는다.

첫 전체 Chromium E2E 비교는 다음과 같다.

| 비교 대상 | CI run | E2E 통과 | E2E 실패 |
| --- | --- | ---: | ---: |
| 순정 6.2.2 (Kotlin 적용 전) | `44587145395` | 103 | 5 |
| Kotlin 혼합 `audit/6-2-2-parity` | `44585966465` | 103 | 5 |

**두 결과의 실패 테스트 항목은 정확히 같다.** 현 시점의 실패가 새 Kotlin 이식으로 발생했다는 근거는 없다. 반대로 원본 자체의 결함/테스트 불일치/환경 문제 중 어느 원인인지는 아직 확인되지 않았으므로 정상 통과라고 표현하지 않는다.

기준선에서 이미 실패하는 항목:

1. `e2e/list.spec.ts:183` — 작성자 메뉴 아이디/IP 및 닉네임 검색.
2. `e2e/options.spec.ts:79` — 옵션 페이지 본문 글꼴 기대값(기대 `Gulim, sans-serif`, 실제 `Gulim, sans-serif, sans-serif`).
3. `e2e/preview.spec.ts:666` — 디시콘 선택 뒤 입력창 비활성 판정.
4. `e2e/preview.spec.ts:742` — 관리자 댓글 삭제 후 `data-deleted` 표시.
5. `e2e/preview.spec.ts:813` — 비회원 댓글 삭제 후 `data-deleted` 표시.

`scripts/verify-e2e-baseline.mjs`는 **108개 전체 테스트를 실행한 결과를 검사**하고, 위에서 원본도 실패한 정확한 5개 이외에 **새 실패·미실행·건너뛰기·잘못된 결과 JSON이 발생하면 CI를 실패**시킨다. 원본 5건은 명시적인 경고와 실행 요약으로 남긴다. 이것은 ‘108개 모두 성공’이라는 판정이 아니다.

**아직 검증하지 못한 항목:** 실제 디시 서버와 로그인 세션, Firefox 실행 및 크로스 브라우저, 모든 기능 모듈/팝업 UI의 Kotlin 전환 완결, 사용자 개인 패치 ZIP의 실제 동작.

## 감사 중 확인된 구체적 차이

- `SubmitResult.kt`: **일반 텍스트 응답에서 `message`, `detail` 속성 자체가 빠지는 차이**. 원본 JS는 `{result, message, detail}`로 세 키가 존재하면서 값이 `undefined`이다. 객체 열거 결과까지 같도록 수정하고 `response-parity.test.ts` 회귀 검사를 추가. CI 결과는 해당 PR 검사 완료 시에만 통과로 표시.
- `features/preview/ui/DcconPopup.tsx`: 이번 Kotlin 메인 변경에는 포함되지 않았음. 6.2.2 순정 구현이 유지됨. 오래된 개인 캡처 휠 패치를 옮기려던 PR #15는 **미병합 종료**.
- 이전 `AGENTS.md` 및 `MIGRATION.md`에 폐기된 휠 패치 유지 지침이 남아 있었음. 최종 승인 기준으로 수정함.
- 원본 소스가 미완료 상태이므로 개인 패치를 지금 합치지 않도록 개발 단계 순서를 수정함.

## 추가 기능 동등성 검증이 필요한 항목

전체 Kotlin 전환 전 아래 항목은 원본 동작과 실제 호출 경로를 기준으로 별도 회귀 검증이 필요하다. **미검증은 실패나 성공을 뜻하지 않는다.**

- DOM 콘텐츠 업데이트, 이벤트 정리, 설정 변경 중 재주입·탭 복귀.
- 댓글 수신/등록/삭제, 차단·비회원 글, 접근성 및 음성 댓글.
- 로그인·권한·Turnstile 등 원본 인증/제출 경로.
- YouTube/X/OG/디시콘 등 미리보기 UI와 이미지 보기.
- 설정 저장/가져오기·브라우저 간 동기화.
- 새로고침 연속 실행·대기열·취소와 오류 처리.
- Firefox 및 실제 페이지 E2E.

## 이식 진행 원칙

기존 합의대로 **순정 6.2.2 전체 Kotlin 이식 → 원본과 기능 동등성 검증 → 개인 패치 선별 이식 → 통합 검증 → 릴리스 판단**의 순서를 적용한다. 기존 `main`에서 빌드가 성공했다는 사실만으로 다음 단계가 완료됐다고 보지 않는다. 패치 선택 시 `docs/PATCH-DECISIONS.md`를 기준으로 한다.

## 추가 Firefox 전체 콘텐츠 E2E 기준선 (2026-11-21)

PR #71 CI `44877926525`에서 Firefox 콘텐츠 E2E 78건 실행 결과 **71건 통과, 3건 실패, 4건 건너뜀**을 확인했다. Firefox 순정 6.2.2 실행 결과는 아직 없으므로 이 3건을 '순정 Firefox 결함'으로 확정하지 않는다.

- 실패: `e2e/preview.spec.ts`의 디시콘 전송(666), 관리자 댓글 삭제(742), 비회원 댓글 삭제(813). Chromium의 기존 실패 5건 중 3건과 항목이 일치한다.
- 건너뜀: Firefox IP DB 두 건(9, 14), 미리보기 스크롤바(42), 댓글 클릭 리스너(490).
- 추가한 CI 검증은 위 최초 기준선과 일치하는 실패·건너뜀만 허용하고 신규 실패·누락 검사·검사 결과 손상을 차단한다. 기준선의 기존 실패를 성공으로 취급하지 않는다.
- 실제 디시의 읽기 전용 E2E 7건은 별도 CI 검사로 전부 통과했다. 로그인 상태의 쓰기·삭제 경로와 Firefox 팝업·옵션 페이지는 여전히 완전한 검증이 아니다.

## 2단계 런타임 TypeScript 연결부 전수 목록 (2026-11-21)

GitHub Actions `44880408649`의 `scripts/audit-kotlin-boundary.mjs` 실행 결과, `core/`, `features/`, `entrypoints/`, `stores/`, `components/`, `utils/`의 TypeScript/TSX 런타임 파일은 **215개**다.

- Kotlin 브리지(`kotlinBridge` 등)를 직접 참조하는 파일: **195개**. 직접 참조한다는 사실만으로 해당 파일의 모든 로직이 Kotlin으로 이전됐다고 증명되지는 않는다.
- Kotlin 브리지를 직접 참조하지 않는 파일: **20개**. 별도 내용 확인 결과, 타입 선언 2개, WXT 주소 설정 1개, 모듈 아이콘 10개, WXT 모듈 메타 수집 1개, 기능 재수출 1개, WXT 진입점 2개, 스타일·포털 연결 3개로 분류했다.
- 위 20개에는 독립적인 사용자 기능의 TypeScript 구현이 확인되지 않았다. 이 예외 목록 이외에 브리지를 참조하지 않는 새 TS 파일이 생기면 감사 CI가 실패하도록 했다.
- 주의: 이 검사는 정적 코드 분포 검증이며 브리지 내부 TypeScript가 모두 단순 연결인지 또는 원본과 런타임 동등성이 100%인지 증명하는 검사는 아니다. 기존 Chromium E2E 순정 기준선 실패 5건, Firefox 최초 기준선 실패 3건 및 건너뜀 4건, 실제 계정으로 실행할 수 없는 쓰기 요청은 별도 검증 상태로 유지한다.
