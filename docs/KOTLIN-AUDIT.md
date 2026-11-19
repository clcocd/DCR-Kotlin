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

## 기존 테스트가 검증하는 범위

`.github/workflows/build.yml`의 개발용 CI:
1. `gradle -p kotlin jsNodeTest jsBrowserDistribution`
2. `bun ci` → `bun run compile` → `bun run test`
3. `bun run build`
4. 생성된 Kotlin 번들의 존재, MV3 Manifest 존재, 최종 결과물에 `__DCR_KOTLIN__` 문자열 포함 여부 검사.

이 순서의 성공은 **Kotlin 테스트·TypeScript 검사·Vitest 테스트·Chrome MV3 빌드의 성공**을 뜻한다. 다음은 여기서 검증하지 않는다.

- 실제 디시인사이드 서버와 로그인한 Chrome/Firefox에서의 기능 동작.
- 브라우저로 화면을 열어 확인하는 원본↔Kotlin E2E 시각적·상호작용 동등성.
- 모든 기능 모듈·팝업/옵션 UI의 Kotlin 이식 여부.
- 사용자 개인 ZIP에 들어 있는 테스트 패치의 실제 사이트 동작.
- Firefox 확장 패키징/실행 및 크로스 브라우저 회귀.

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
