# 최종 ZIP 역공학 기준선 (6.2.2)

본 문서는 **사용자 최종 ZIP에서 직접 확인한 코드**를 기준으로 한다. 이전 대화의 템플릿이나 구형 패치 동작을 이식 요구사항으로 취급하지 않는다.

- 입력: `dcrefresher-reborn-6.2.2-doublecon-empty-message-unified.zip`
- SHA-256: `5ff415d0c1810c7eac65ade248898a3056d26f37c6938c90dfe1099e406b72a0`
- 출처: ZIP 내부 `PATCH-NOTES.md`, `manifest.json`, `content-scripts/content.js`, `content-scripts/content.css`, `content-scripts/page.css`, `content-scripts/local-fixes.js`, `content-scripts/x-embed-main.js`
- **주의**: ZIP 노트에서 실제 사이트 렌더링 및 서버 댓글 등록은 최종 검증되지 않았다. 코드 존재를 실사용 검증 통과로 해석하지 않는다.
- Manifest: Chrome용 MV3에서 `declarativeNetRequestWithHostAccess` 및 `https://*.dcinside.co.kr/*` 선택 host 권한이 있다. 현 Kotlin `wxt.config.ts`에는 이 두 선언이 없어 차이가 확인되지만, 기능 의존 관계 검증 없이 무조건 추가할 사양이라고 단정하지 않는다. ZIP에는 Firefox용 Manifest가 없다.

## ZIP에서 코드로 확인한 동작

### YouTube — 새 구조만 유지

- `content.css`: `.refresher-html .dc-external-video` = `display:flex; flex-direction:column; align-items:flex-start`; 내부 `iframe`은 `max-width:100%`, 내부 `a.lnk`는 `display:block`.
- `content.js`의 `__rfYoutubeFallback(e)`: HTML에 이미 포함된 YouTube iframe/embed ID를 모으고, 일반 링크의 `youtu.be/<id>`, `youtube.com/watch?v=<id>`, `/live/<id>`, `/shorts/<id>`에서 11자리 ID를 추출. 중복 ID는 생성하지 않으며 `.og-div` 안 링크는 무시. 새 링크 바로 앞(가능하면 부모 `p` 앞)에 `youtube.com/embed/<id>` iframe(560×315)을 보충.
- `PATCH-NOTES.md`: **구형 iframe → URL → 1px → OG 카드 복원 함수와 호출 제거**. 이 구형 구조는 재구현 대상이 아니다.

### OG 카드 — YouTube와 별개

- `content.css`: `a.og-wrap`은 `inline-block`, `width:70%`, `height:105px`; `.og-img`은 좌측 `float`/104px, `.og-info`는 우측 `float`/`calc(100% - 104px)`; `.og-tit`는 `inline-block`/`max-width:70%`.
- 다크모드 색상 보정 존재. 과거 flex 배치를 이 ZIP의 요구 동작으로 간주하지 않는다.

### 더블콘 — 선택 UI와 요청 직렬화

- `content.js`: `__rfMixSlots`, `__rfMixActive`, `__rfMixStart`, `__rfMixSelect`, `__rfMixRemove`, `__rfMixSwap`. 글자콘 모드의 **단일 ‘더블콘 만들기’ 버튼**에서 기존 디시콘 선택창을 열어 두 번째 항목으로 글자콘 또는 이미지 디시콘을 고른다.
- 선택창에서 디시콘 목록이 비거나 요청이 실패하면 `__rfMixLoaded`로 빈 상태를 표시하며 **글자콘** 버튼을 남긴다. 패키지 목록이 있을 때는 상단 글자콘 버튼, 목록이 비었을 때는 빈 상태 화면의 글자콘 버튼.
- 두 슬롯의 선택, 텍스트·색상 수정, 삭제, 순서 교환을 지원한다. 글자콘 슬롯이 빈 문자열이면 전송을 막는다.
- **빈 일반 댓글 입력 처리:** 전송 UI의 중단 조건이 `!r && !n && !__rfDouble`이므로 두 슬롯이 존재하면 일반 댓글 텍스트가 비어도 제출 단계로 진행한다. 단, 각 글자콘 슬롯의 빈 내용은 별도 검증으로 전송을 차단한다. 이 코드가 ZIP 파일명 `empty-message`의 유일한 의미라고 단정하지 않는다.
- `Ud`가 `POST https://gall.dcinside.com/txtcon/insert_double`로 보냄. 글자콘 슬롯 N: `slotN_type=txtcon`, `txtcon_text_N`, `txtcon_bg_N`, `txtcon_color_N`. 이미지 디시콘 슬롯 N: `slotN_type=dccon`, `package_idx_N`, `detail_idx_N`. N은 1, 2.
- 일반 글자콘은 `/txtcon/insert`를 사용해 기존 경로를 유지.
- 댓글 표시 CSS: `.double_txtcon > .double_txtcon_item`과, 정화된 댓글에서 `.coment_dccon_txt`/`.written_dccon`이 연속 형제로 나온 경우 모두 가로 정렬. 각 항목 100×100px.
- **서버 등록 성공은 미검증**.

### X 임베드

- `x-embed-main.js`: MAIN world, `window.__refresherXBridge` 중복 설치 방지. `refresher-x-render` → 10초 이내 Twitter widgets 준비 → `twttr.widgets.createTweet(tweetId, stage, {theme,lang:"ko",dnt:true})` → `refresher-x-done` 또는 `refresher-x-error`.
- `manifest.json`에 독립 콘텐츠 스크립트로 등록돼 있다.

### 로그인 박스

- `local-fixes.js`: top frame의 `gall.dcinside.com`에서 `refresherPushToRight`가 있고 `refresherCompact`는 없을 때만 `#login_box`를 `#hdline_ban` 앞으로 옮긴다. 앞에 주석 앵커를 삽입해 조건 해제 시 **원래 위치로 되돌린다**. 루트 class 변경 감시.
- `page.css`: `refresherGallLoginBox` 활성화 시 박스 위치 스타일.

### 본문·스타일

- `content.css`: `.refresher-frame .px-8`, `.pl-8`의 25px 패딩. `.dark .refresher-html iframe:fullscreen {filter:none!important}`. 본문 다크모드, 스포일러, 테이블 및 OG 보정.
- 디시콘 패키지 휠: `content.js`에 `onWheel` 방식이 존재. 이 ZIP 안의 동작을 기준으로 비교하며 별도 캡처식 가로 휠을 추가하지 않음.

## 최종 ZIP 실행 코드 추가 확인

- **10초 캐시:** `content.js`의 게시글 캐시는 `Date.now() - fetchedAt < 1e4`, 댓글 캐시는 `Date.now() - commentsAt < 1e4`일 때 재사용하는 실제 조건이 있다.
- **목록 복귀 새로고침:** `__rfResume`는 페이지가 보일 때 중복 갱신을 방지하며 `queueMicrotask`로 이전 타이머를 초기화하고 필요 시 즉시 갱신한다. `visibilitychange`, bfcache `pageshow`, `popstate`와 연결된다.
- **작성자 검색 결과:** `__rfLastFill`은 URL과 현재 게시글 번호를 키로 하고 `6e4`(60초) 이내의 검색 행을 재사용한다. 다른 검색 조건·상태 제약과 함께 작동한다.
- **최근 디시콘:** 기존 선택창의 ‘최근’ 버튼이 `/dccon/lists`에 `target:recent`, `page:0`를 보낸다.
- **디시콘 호버:** `__rfInstallDcconHover`는 `.written_dccon`에 설명과 ‘디시콘 보기’ 버튼을 제공하며 180ms 지연 숨김과 이벤트 정리를 구현했다.
- **인라인 답글·초안:** `__rfInlineMode`는 답글별 `|reply:<번호>` 키를 사용하고, `__rfPreviousReplies`는 동일 배열 스레드를 재사용한다.
- **글자콘 시각 보정:** `__rfTxtPending`, `__rfTxtIntersect`, `__rfTxtStop`는 `IntersectionObserver` 기반의 표시/해제 로직이다.
- **작성 장치 아이콘:** `__rfDeviceIcon`은 원문의 `.title_device` 스타일을 계산·복사해 미리보기에 표시한다.
- **본문 색상:** `__rfAdaptColors`는 `[style],font[color]`의 색을 CSS 사용자 변수 및 중립색 플래그로 변환한다.
- **Instagram:** `__rfRenderInstagram`이 인스타그램 `/p/`, `/reel/`, `/tv/` 게시물의 인용 블록을 embed iframe으로 변환한다.
- **X 렌더링 후처리:** `__rfRenderX`는 X 링크에 `.x-wrap`를 만들고 숨긴 stage에서 렌더한다. 전체 대기 5초, 렌더된 iframe 높이가 100px 미만이면 500ms 후 오류 판정, 오류 안내와 원본 링크를 표시한다. 별도 MAIN world 브리지의 10초 위젯 준비 timeout과 **서로 다른 타이머**다.

## 노트에 있지만 세부 계약을 아직 복원하지 않은 항목

`PATCH-NOTES.md`는 새로고침 체감 보정, 본문 캐시, 댓글·대댓글 위치·초안·빈 공간 클릭, 최근 디시콘·호버 정보, NetworkError 로그, 디시 전체화면 보정을 언급한다. 각 기능의 **정확한 조건·순정 대비 변경점**은 압축 코드 내 추가 추적이 필요한 상태이다. 파일명의 `empty-message`도 동작 명세를 임의로 만들지 않는다.

## 현재 Kotlin 프로젝트와의 차이 (이 표의 요구 동작은 ZIP에서만 추출)

| ZIP 코드 계약 | 현재 Kotlin 검색 결과 | 판정 |
| --- | --- | --- |
| 더블콘 두 슬롯, `insert_double` | `features/dccon/Txtcon.kt`, `features/preview/CommentSubmission.kt`, `WriteCommentView.kt` | 일반 `/txtcon/insert` 경로는 있으나 더블콘 전송·선택 UI는 미이식 |
| 새 `.dc-external-video` CSS | `features/preview/PostParser.kt`, 스타일 리소스 | ZIP 특유의 새 레이아웃 미이식 |
| 일반 링크의 YouTube iframe 보충 | `core/dom/Sanitize.kt`, `features/preview/PostParser.kt` | 기존 iframe 정화는 있으나 링크 보충 로직 미이식 |
| X MAIN world 이벤트 스크립트 | 현재 WXT entrypoints | 이벤트·별도 스크립트 미이식 |
| 로그인 박스 위치 복원 | 현재 레이아웃 기능 | 앵커 기반 이동·복원 미이식 |
| 디시콘 가로 휠 | `features/dccon/DcconViews.kt` | 구현 존재. ZIP과 조건 동등성은 별도 검증 |
| OG/25px/전체화면 CSS | 기존 스타일 | ZIP 선언값에 대한 비교·이식 필요 |

Kotlin 경로는 `kotlin/src/jsMain/kotlin/dev/dcrk/` 기준이다. 위 표는 현재 코드와의 대조 결과이고, **현재 Kotlin 구현을 ZIP의 요구 동작으로 대체 해석하지 않는다.**

## 검증 경계

ZIP의 CRC, manifest 직접 참조 파일 7개(누락 0개), JS 10개 파일의 Node.js 문법 검사 및 핵심 구현 마커 13개 검증을 통과했다. ZIP 정적 소스에서 확인한 사실을 위에 기록했다. 6.2.2 순정 소스나 과거 대화 기준을 덮어쓰지 않는다. **코드 확인 / 브라우저 확인 / 실제 서버 쓰기 확인**을 구분하여 완료를 판정한다. 특히 YouTube의 폐기된 구형 1px 템플릿은 복원 금지이며, 더블콘 전송 성공은 아직 미확인이다.

## 2026-11-21 전수조사 판정 및 잔여 계약
**과거 개인 패치 합의 대조:** [`PATCH-HISTORY-CROSSCHECK.md`](PATCH-HISTORY-CROSSCHECK.md)에서 이전 논의의 필수/제외/순정 기능을 현재 상세 목록과 교차 비교하고, 실제 ZIP 구현이 미확인인 항목은 별도 표시한다.


**판정: 주요 기능의 정적 역공학은 기록돼 있으나, 최종 ZIP의 개인 패치 전체에 대한 전수조사는 아직 완료로 판정하지 않는다.** 위에서 기록한 구현 마커·CSS 존재 확인과 함수 전체 제어 흐름/예외 분기·원본 6.2.2 대비 차이 전수 대조는 서로 다른 작업이다. 또한 기존 문서의 ZIP 해시·CRC·문법 검사 결과는 **이전 분석에서 기록한 결과**이며 재검사 결과로 표기하지 않는다.

2026-11-21에 이전 상세 분석 문서 `FINAL-ZIP-REVERSE-ENGINEERING(1).md`와 현재 저장소 문서를 교차 검토하여, 보고서에는 있으나 위에 생략되었던 구체 사항을 복구한다. 아래의 **기존 보고서상 코드 확인**은 이번 작업에서 ZIP 실행 파일을 재열람했다는 뜻이 아니다.

- **Manifest의 상세 계약(기존 보고서상 확인):** Chrome MV3, 버전 6.2.2, 최소 Chrome 140. 권한 `alarms`, `contextMenus`, `storage`, `scripting`, `unlimitedStorage`, `declarativeNetRequestWithHostAccess`; 기본 host `https://*.dcinside.com/*`, 선택 host `https://*.dcinside.co.kr/*`. 추가 권한의 Kotlin 이식 필요성은 별도 판정.
- **더블콘 요청의 공통 값(기존 보고서상 확인):** 각 `slotN_type`과 타입별 필드 외에도 게시글 `id/no`, 대댓글 `c_no/reply_no`, 닉네임/비밀번호, captcha 및 Turnstile 등 기존 제출 기본 필드를 유지한다. 선택 모드에는 `__rfPickTxtcon`, 빈 목록에는 `__rfMixLoaded`가 관여하며 `__rfMixSlots`/선택/삭제/교환과 구분해야 한다.
- **X MAIN world 메시지 세부(기존 보고서상 확인):** `data-refresher-x-stage` 대상과 `dataset.tweetId`를 사용하고, `refresher-x-render` 요청 후 `refresher-x-done`/`refresher-x-error`로 결과를 전파한다. 위젯 준비 10초와 미리보기 렌더 실패 5초 제한은 독립적이다.
- **글자콘 표시 수명주기(기존 보고서상 확인):** `__rfTxtPending`/`__rfTxtIntersect`/`__rfTxtStop` 외에 `__rfTxtUnwatch` 정리 경로가 기록돼 있다. 최초 표시뿐 아니라 중단·해제 조건을 포함한 동등성 검증이 필요하다.

| 전수조사 대상 | 현재 문서로 확인된 사실 | 여전히 복원되지 않은 계약 |
| --- | --- | --- |
| 새로고침 | `__rfResume`와 복귀 이벤트 연결 | 요청 시작 기준 주기, 중복 요청·일시정지·실패·재시도·타임아웃의 전체 분기 및 순정 대비 수정 범위 |
| 본문/댓글 10초 캐시 | `fetchedAt`/`commentsAt`의 10초 조건 | 생성·무효화·수정/삭제 후 처리·뒤로 가기 경로 전체; Kotlin의 다른 TTL과 동일 목적 여부 |
| 댓글/대댓글/초안 | `__rfInlineMode`, `|reply:<번호>`, `__rfPreviousReplies` | 본문·댓글 위치별 입력 UI, 포커스, 저장/삭제 시점, 빈 공간 클릭의 정확한 트리거 |
| 최근 디시콘/호버 | `target:recent` 요청과 180ms 닫기 | 응답 경쟁·목록 없는 경우·차단 디시콘·정리의 모든 예외 분기 |
| NetworkError 로그 | 오류명·메시지 분기 존재 | 기록 조건, 전파 범위, 개인정보 출력 여부, 실패와 일반 취소 구분 |
| 동영상/다크모드/표/OG | 25px 패딩, iframe fullscreen 반전 제외, 주요 카드 CSS | 모든 관련 선택자·우선순위·원본 대비 차이·실제 사이트 렌더 결과 |
| YouTube/X/Instagram | 링크 후처리·브리지·인스타 embed 경로 존재 | 중복/실패/정리/페이지 전환 전체 분기와 실사이트 동등성 |
| 더블콘 전송 | 슬롯 직렬화·빈 글자콘 차단·일반 댓글 빈 입력 예외 | 서버가 `/txtcon/insert_double` 요청을 수락하는지 및 성공/실패 후 UI 상태 |

**완료 판정 조건:** 최종 ZIP의 `PATCH-NOTES.md` 각 항목을 실행 JS/CSS 위치와 대응시키고, 원본 6.2.2와의 차이를 구분하며, 표의 미복원 계약을 조사한 뒤 정적 코드 확인과 실사이트 검증을 별도로 기록한다. **Kotlin 개인 패치 이식은 원본 기능 동등성 검증 완료 전에는 시작하지 않는다.**
