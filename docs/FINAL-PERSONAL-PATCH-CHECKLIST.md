# 최종 개인 패치 이식 체크리스트

순정 `dcrefresher-reborn-6.2.2-chrome.zip`과 최종 개인 패치 `dcrefresher-reborn-6.2.2-doublecon-empty-message-unified.zip`의 실제 차이를 DCR-Kotlin 소스 위치에 연결한다. 최종 ZIP의 SHA-256은 `5ff415d0c1810c7eac65ade248898a3056d26f37c6938c90dfe1099e406b72a0`이다. 상위 폴더 한 겹을 제외하면 수정 4개, 추가 3개, 동일 15개, 삭제 0개다.

오른쪽 열은 이식 후 확인할 조건이다. Kotlin 소스 링크는 작업을 시작할 위치를 가리킨다. 구현과 실제 사이트 검증 상태는 별도로 기록한다. 참고 문서는 [최종 ZIP 세부 목록](FINAL-ZIP-PATCH-INVENTORY.md)과 [이행 계획](MIGRATION.md)이다.

## 기능별 PR 순서

구현을 시작하기 전에 [이행 계획](MIGRATION.md)의 순정 6.2.2 동등성 검증 상태를 확인한다. 기존 Kotlin 단위 테스트와 Chrome·Firefox 확장 테스트에서 댓글·초안, 미리보기 요청 취소, 목록 갱신의 결과를 기준으로 기록한다.

각 구현 PR의 코드 정리는 해당 기능을 이식하는 데 필요한 범위로 한정한다. 전체 구조 개편, 브리지 재구성, 번들 분리와 성능 최적화는 모든 개인 패치 이식과 동작 검증을 마친 뒤 별도 PR에서 검토한다.

아래 번호는 구현 순서다. 각 단계는 별도 PR로 열고, 해당하는 상세 항목과 검증 결과를 PR 본문에 적는다. 선행 PR이 필요한 작업은 그 PR이 병합된 기준으로 분기한다. 상세표 21개 항목에 각각 담당 PR을 하나씩 배정했다.

| 단계와 브랜치 | 담당 체크리스트 항목 | 변경 경계와 선행 작업 | PR 완료 기준 |
| --- | --- | --- | --- |
| 1. 본문 캐시 (`port/personal-post-cache`) | 본문 캐시 | `PreviewNavigation.kt`의 본문 캐시 조건. 다른 단계 없이 진행 가능. | 본문 재사용 한도 10초. 본문 2초 신선도, 댓글 재사용 10초, 공유 캐시 엔트리 보관 60초와 보관글 폴백을 각각 확인. |
| 2. 인라인 대댓글 (`port/personal-inline-replies`) | 인라인 대댓글, 초안 분리와 정리, 댓글 영역 클릭과 닉네임 | `CommentView.kt`, `CommentListView.kt`, `WriteCommentView.kt`, `PreviewFrame.kt`. 이 PR에서 폼의 답글 대상과 초안 키를 확정하고 3단계에서 재사용. | 부모 댓글·대댓글 아래 폼, 하단 일반 폼, 닫기·재열기·포커스, 클릭 예외, 닉네임 대체, 전송 후 해당 초안만 정리되는지 확인. |
| 3. 글자콘 더블콘 (`port/personal-txtcon-double`) | 기존 더블콘 표시, 더블콘 작성 UI, 더블콘 선택창과 빈 목록, 더블콘 전송 | 2단계 병합 후. 작성 폼·디시콘 선택창·전송 분기·슬롯 직렬화·댓글 표시 CSS를 함께 변경. | 조합·순서·색 편집·삭제, 구매 디시콘 없음·실패의 대체 버튼, 빈 글자콘 차단, 슬롯 요청과 일반 댓글·단일 콘 전송 회귀 확인. 실서버 수락 여부 별도 기록. |
| 4. 최근 디시콘과 호버 (`port/personal-dccon-tools`) | 최근 사용 디시콘, 디시콘 호버 | 3단계 병합 후 같은 선택창에 최근 목록을 연결. 호버는 미리보기와 상세 팝업 경계에서 처리. | 최근 목록 로딩·빈 응답·요청 오류, 패키지 전환과 닫기 시 취소, 차단 콘 제외, 상세 열기·스크롤·호버 종료 확인. |
| 5. 목록 새로고침 (`port/personal-list-refresh`) | 복귀 및 초기 갱신, 자동 갱신 주기·실패 | `RefreshModule.kt`와 `RefreshLoader.kt`에서 타이머·요청을 함께 변경. `PreviewController.kt`의 닫기 신호만 연결. | 초기·탭·기록 복귀와 미리보기 닫기, 중복 요청, 일시정지, 백그라운드·첫 페이지 조건, 지터·백오프·오류 및 취소 로그 확인. |
| 6. 외부 미디어 (`port/personal-external-media`) | YouTube, X 임베드, 인스타그램 임베드 | 1단계의 본문 정리 경로를 기준으로 작업. YouTube는 HTML 후처리와 CSS, X·인스타그램은 미리보기 훅으로 처리. `MAIN` 진입점은 X에 필요한 범위로 추가. | YouTube 중복·OG 예외·세로 배치, X 오류·시간 제한·정리, 인스타그램 캡션, 일반·미니 미리보기의 미디어 숨김 설정 확인. 실제 위젯 결과 별도 기록. |
| 7. 본문·미리보기 표시 (`port/personal-preview-appearance`) | 사용자 지정 색상·표, OG 카드, 영상 전체화면·스포일러, 작성 기기 아이콘, 25px 여백 | 6단계 병합 후 본문 처리와 `overlay.css`에 적용. 2~4단계가 바꾼 댓글·선택창 코드와 CSS가 겹치는 곳은 최신 코드를 기준으로 맞춘다. | 밝은·어두운 테마, 표·OG 카드, 전체화면, 스포일러 본체 보존, 기기 아이콘 유무, 미리보기 영역의 25px 여백 확인. |
| 8. 로그인 박스 (`port/personal-login-box`) | 로그인 박스 | `Layout.kt`와 페이지 CSS만 변경. 오버레이에 영향을 주는 7단계와 별개로 검토 가능. | 갤러리 최상위 페이지에서 조건에 맞을 때 이동, 컴팩트 모드·설정 전환·모듈 종료 시 복원, 헤더와 검색창 배치 확인. |

각 구현 PR은 관련 단위·브라우저 테스트와 Kotlin/JS·확장 빌드 결과를 기록한다. 직접 확인할 수 없는 실사이트 쓰기·위젯 동작은 미검증으로 표시한다. 모든 단계가 병합되면 순정 대비 기존 기능 회귀와 최종 개인 패치 ZIP 대비 이식 동작을 Chrome·Firefox에서 함께 확인한다.

## 착수 전 검증 현황

2026-11-21 기준으로 확인한 결과다. 이 체크리스트의 문서 검토와 구현 착수 여부를 각각 판단한다.

| 구분 | 확인 결과와 남은 작업 |
| --- | --- |
| 기준 `main` | `e9dc258`의 [CI](https://github.com/clcocd/DCR-Kotlin/actions/runs/44890042313)에서 Kotlin·TypeScript·Vitest, Chrome·Firefox 빌드, 양쪽 브라우저 E2E 기준선 비교와 실제 사이트 읽기 검사가 통과했다. 비교 검사는 기록된 기존 실패를 허용한다. |
| Firefox 댓글 전송 테스트 | 문서 전용 [실행 1](https://github.com/clcocd/DCR-Kotlin/actions/runs/44893539640)과 [실행 2](https://github.com/clcocd/DCR-Kotlin/actions/runs/44893672160)에서 `e2e/preview.spec.ts`의 댓글 전송 대상 검사(`comment_id`, `comment_no`)가 재시도에서만 통과해 CI가 실패했다. 개인 패치 실행 코드가 없는 커밋에서도 발생했다. 순정·현재 기준에서 같은 테스트를 반복 비교하고 원인을 기록한다. [#79 최신 검사](https://github.com/clcocd/DCR-Kotlin/pull/79/checks)는 별도로 확인한다. |
| 실제 사이트 동등성 | 로그인 상태의 댓글 쓰기·삭제 및 Firefox 팝업·옵션 등은 [Kotlin 감사](KOTLIN-AUDIT.md)의 미검증 항목으로 남아 있다. 읽기 전용 실행과 한 차례 CI 통과만으로 완료 처리하지 않는다. |
| 구현 착수 | 위 동등성 검증을 마친 뒤 1단계 본문 캐시부터 시작한다. 바꿀 값은 `PreviewNavigation.kt`의 본문 재사용 시간이다. 공유 캐시 엔트리 보관 60초와 댓글 재사용 10초는 유지한다. |

## 댓글·대댓글·디시콘

| 기능 | 최종 ZIP의 근거 | DCR-Kotlin에서 작업할 위치 | 확인 조건 |
| --- | --- | --- | --- |
| 인라인 대댓글 | `content.js`의 `__rfInlineMode`, `refresher-inline-reply` | [`createCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentView.kt#L18)의 댓글 행 다음에 [`createWriteCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/WriteCommentView.kt#L6)를 마운트. [`createPreviewFrame`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewFrame.kt#L5) 하단 폼은 새 댓글을 담당. [`createCommentList`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentListView.kt#L55)에서 활성 답글이 속한 스레드를 표시. | 부모 댓글과 대댓글 각각 바로 아래에 표시, 키보드 포커스, 답글 취소, 하단 폼 분리. |
| 초안 분리와 정리 | `__rfInlineMode`, `\|reply:`, 전송 성공 분기 | [`createWriteCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/WriteCommentView.kt#L6)의 `initialKey` / `saveDraft` / `submit`. | 글과 답글 대상마다 초안 분리. 성공 시 전송한 초안 키만 정리하고 다른 폼과 전송 중 수정한 새 초안 보존. ZIP에서 발견한 키 불일치는 회귀 테스트로 방지. |
| 댓글 영역 클릭과 닉네임 | `onClick`의 `댓글에 답글` 버튼 / `gallog_icon`의 `nickname an` | [`createCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentView.kt#L18)의 행 클릭 핸들러와 `user.nick` 계산. | 일반 영역 좌클릭과 반복 클릭, 선택 중 텍스트와 링크·버튼·작성자·콘·미디어 제외. HTML 닉네임이 없으면 `name` 사용. |
| 기존 더블콘 표시 | `content.css`의 `.double_txtcon`, 인접 콘 선택자, 100px 슬롯 | [`features/preview/overlay.css`](../features/preview/overlay.css)의 `.refresher-comment-html`, [`createCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentView.kt#L18)의 `data-dccon` 분류. | 래퍼가 있는 댓글과 평탄화된 형식, 이미지 두 개 및 글자·이미지 양쪽 순서. |
| 더블콘 작성 UI | `__rfMixStart`, `__rfMixSelect`, `__rfMixSwap`, `__rfMixRemove`, `__rfMixEdit`, `__rfMixColor` | [`createWriteCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/WriteCommentView.kt#L6)의 글자콘 모드·입력·색 팔레트, [`createDcconPicker`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/dccon/DcconViews.kt#L5)의 두 번째 슬롯 선택, [`features/preview/overlay.css`](../features/preview/overlay.css)의 슬롯 스타일. | 버튼 한 개, 첫 슬롯의 현재 텍스트·색 보존, 글자+글자·글자+이미지, 슬롯 선택·편집·교환·삭제 및 단일 콘 복귀. |
| 더블콘 선택창과 빈 목록 | `__rfPickMix`, `__rfPickTxtcon`, `__rfMixLoaded` | [`createDcconPicker`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/dccon/DcconViews.kt#L5)의 헤더·목록 로딩 및 빈 그리드 분기, [`DcconPopupProps`](../features/preview/ui/DcconPopup.tsx#L19). | 목록이 있으면 상단 글자콘 버튼, 구매 목록 없음·실패면 중앙 안내와 글자콘 버튼. 두 번째 슬롯은 단일 선택. 일반 선택 모드의 토글과 오류 상태도 확인. |
| 더블콘 전송 | `txtcon_double_submit`, `Ud`의 슬롯 필드, 작성 폼의 `__rfDouble` | [`createWriteCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/WriteCommentView.kt#L6)의 `submit` → [`createCommentSender.post`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentSubmission.kt#L113) → [`createTxtconSubmission`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/dccon/Txtcon.kt#L48) → [`txtcon_submit URL 정의`](../kotlin/src/jsMain/kotlin/dev/dcrk/core/RuntimeConstants.kt#L13). | 슬롯 순서에 따른 타입·텍스트·색·패키지/상세 ID. 일반 댓글 입력이 비어도 더블콘 분기로 진입하되 빈 글자콘 슬롯은 경고. 기존 단일 글자콘·이미지 디시콘 요청 회귀 검사. 서버 등록은 별도 확인. |
| 최근 사용 디시콘 | `__rfLoadRecent`, `target=recent`, `page=0` | [`createDcconRequests`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/dccon/DcconRequests.kt#L6) → [`DcconRequests 브리지`](../core/preview/dccon.ts) → [`createDcconPicker`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/dccon/DcconViews.kt#L5). | 최근 버튼·로딩·로그인 오류, 패키지 전환/닫기 시 요청 취소, 두 선택 모드에서 재사용. 빈 최근 목록과 빈 구매 목록의 표시 조건을 각각 확인. |
| 디시콘 호버 | `__rfInstallDcconHover`, `.refresher-dccon-infoview` | [`createPreviewFrame`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewFrame.kt#L5)의 표시 수명주기, [`overlay.portal`](../components/overlay/shadow.ts#L2), [`디시콘 상세 팝업`](../features/preview/ui/DcconInfoPopup.tsx), [`features/preview/overlay.css`](../features/preview/overlay.css). | 차단된 콘 제외, conalt/alt/title 우선순위, 화면 가장자리 위치, 180ms 닫기, 스크롤 숨김과 정리. |

## 새로고침·캐시

| 기능 | 최종 ZIP의 근거 | DCR-Kotlin에서 작업할 위치 | 확인 조건 |
| --- | --- | --- | --- |
| 복귀 및 초기 갱신 | `__rfResume`, `__rfReturn`, `refreshOnReturn` | [`setupRefresh`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/refresh/RefreshModule.kt#L14)의 초기 준비·visibilitychange·pageshow·popstate, [`미리보기 닫기에서 catchUp 호출`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewController.kt#L132), [`createRefreshLoader.load`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/refresh/RefreshLoader.kt#L41)의 2초 제한. | 복귀 경로만 2초 제한 예외. 진행 중 요청, 일시정지, 숨김/백그라운드 설정, 첫 페이지, UI 상호작용 보호 확인. |
| 자동 갱신 주기·실패 | `C`, `S`, `Refresh failed:`, `cause` | [`setupRefresh`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/refresh/RefreshModule.kt#L14)의 `armNext` / `loadNow`, [`createRefreshLoader.load`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/refresh/RefreshLoader.kt#L41)의 미리보기·오프스크린 보류와 catch. | 미리보기 중에도 갱신, 무변경 배수·활동 리스너 제거, 요청 시작 전 타이머, 실패 백오프·0.5~2초 지터. 취소·BlockedError·cause 로그 확인. |
| 본문 캐시 | `fetchedAt` < 10초 | [`createPreviewPostService.cached`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewNavigation.kt#L20)의 본문 60초 기준. | 본문 10초, 댓글 캐시 10초, 본문의 2초 신선도와 보관글 폴백은 각각 확인. |

## 본문·외부 미디어·레이아웃

| 기능 | 최종 ZIP의 근거 | DCR-Kotlin에서 작업할 위치 | 확인 조건 |
| --- | --- | --- | --- |
| YouTube | `__rfYoutubeFallback`, `.dc-external-video`, `.dc-video-portrait` | [`createPreviewPostService.process`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewNavigation.kt#L33)의 HTML 정리 직전, [`HTML 정리 브리지`](../utils/sanitize.ts#L4), [`features/preview/overlay.css`](../features/preview/overlay.css). | watch/live/shorts/youtu.be, 기존 ID 중복 제거, OG 링크 제외, 미디어 제거 설정, 플레이어 위·링크 아래와 9:16. |
| X 임베드 | `__rfRenderX`, `x-embed-main.js`의 render/done/error | [`createPreviewMediaHooks`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/layout/MediaLayout.kt#L166)에서 미리보기 본문 처리, [`WXT 콘텐츠 진입점`](../entrypoints/content/index.ts#L7)과 별도의 MAIN world 진입점, [`features/preview/overlay.css`](../features/preview/overlay.css). | 상태 URL·기존 래퍼, 위젯 스크립트 재사용, 테마·언어·dnt, 5초 콘텐츠 제한/10초 위젯 제한/높이 판정, 안내 링크와 수명주기 정리. |
| 인스타그램 임베드 | `__rfRenderInstagram` | [`createPreviewMediaHooks`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/layout/MediaLayout.kt#L166)에서 X와 같은 본문 수명주기. | 인용 블록의 p/reel/tv 및 captioned, 도메인 검사. 일반 미리보기와 미니 미리보기에서 확인하고 `tooltipMediaHide`를 켠 상태에서 미디어가 다시 생성되지 않는지 검사. ZIP의 공유 훅은 이 설정을 직접 확인하지 않아 X·인스타그램이 정리 이후 다시 만들어질 수 있다. |
| 사용자 지정 색상·표 | `__rfAdaptColors`, `data-refresher-neutral-color`, CSS 색 변수 | [`createPreviewPostService.process`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewNavigation.kt#L33)의 HTML 정리 이후, [`features/preview/overlay.css`](../features/preview/overlay.css)의 `.refresher-html`과 다크모드. | 어두운 무채색, 나머지 글자색·배경색, font[color], 테이블 테두리·셀 정렬을 밝은/어두운 테마에서 확인. |
| OG 카드 | `a.og-wrap`, `.og-img`, `.og-info`, `.og-tit`, `.og-txt`, `.og-lnk` | [`features/preview/overlay.css`](../features/preview/overlay.css)의 기존 OG 카드 블록. | 카드 폭 70%·높이 105px, 이미지 104px, 제목 최대 폭 70%·말줄임, 다크모드 카드와 링크. |
| 영상 전체화면·스포일러 | `iframe:fullscreen`, `#spoiler_warning_tip`, `#spoiler_warning` | [`restoreMedia`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PostParser.kt#L35)의 도움말 제거 및 [`features/preview/overlay.css`](../features/preview/overlay.css)의 본문 영상·경고 스타일. | 전체화면 다크 필터, 원본 도움말 제거, 남은 경고창의 중앙 정렬·여백·스크롤 아이콘·다크 이미지·닫기 숨김. |
| 작성 기기 아이콘 | `__rfDeviceIcon`, `.title_device`, `getComputedStyle` | [`parsePostInfo`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PostParser.kt#L92)에서 기기 HTML 추출, [`createPreviewFrame`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewFrame.kt#L5)의 `DialogTitle` 옆에서 원문 스타일 적용. | 원문의 아이콘 유무, 스타일 계산에 사용하는 임시 노드 제거, 미리보기 제목과 포커스 유지. |
| 25px 여백 | `.refresher-frame .px-8`, `.refresher-frame .pl-8` | [`features/preview/overlay.css`](../features/preview/overlay.css)의 `.refresher-frame`과 이를 사용하는 [`createPreviewFrame`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/PreviewFrame.kt#L5), [`createCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/CommentView.kt#L18), [`createWriteCommentView`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/preview/WriteCommentView.kt#L6). | 프레임 안의 지정 가로 여백만 25px 적용. |
| 로그인 박스 | `local-fixes.js`, `refresherGallLoginBox` | [`setupLayout`](../kotlin/src/jsMain/kotlin/dev/dcrk/features/layout/Layout.kt#L35)의 오른쪽 배치/컴팩트 상태와 정리, [`assets/styles/content.css`](../assets/styles/content.css)의 페이지 스타일. | `gall.dcinside.com` 최상위 문서에서만 `#login_box`를 `#hdline_ban` 앞으로 이동. 조건 해제 시 복원. 헤더 125px과 로고·검색창·로그인 박스 위치 확인. |

## 순정에 이미 있는 기능과 최종본 판정

- 디시콘 패키지 가로 휠·스크롤바 숨김, 댓글 수 0↔1 처리, 스레드 배열 재사용, 글자콘 감시자 정리, 작성자 검색 결과 60초 재사용, 포인터 커서는 양쪽 ZIP에서 확인했다. Kotlin의 기존 동작을 회귀 검사한다.
- 최종 글자콘 더블콘은 단일 진입 버튼과 목록 실패 시 대체 버튼을 사용한다. 노트의 목록 요청 없는 독립 버튼은 이전 설명이다.
- 최종 YouTube 동작은 링크 보충과 새 외부 영상 구조 배치다. 구형 복원 템플릿이나 1px 강제 규칙은 이 체크리스트에 포함하지 않는다.
- 댓글 영역 클릭으로 답글 열기·닫기는 최종 ZIP의 이벤트 처리 코드에 따라 2단계에 포함한다.
- ZIP에서 새로 추가된 실행 스크립트 두 개는 매니페스트의 `document_start`에서 실행된다. X는 `MAIN`에서, 로그인 박스는 일반 콘텐츠 실행 환경에서 등록된다. 앱의 기존 제외 URL·권한·브라우저 경계를 확인한다.
- 정적 비교로는 실제 서버의 더블콘 수락, 미디어 위젯 응답, 다크모드 전체화면 렌더링을 확인할 수 없다. 이식 완료 판정에는 브라우저 검증 결과를 기록한다.

## 과거 결정과 남은 검증

[PR #78의 이력 대조](https://github.com/clcocd/DCR-Kotlin/pull/78/files)에서 아래 항목을 이어받았다. 이력에만 있는 동작의 채택 여부는 최종 ZIP 코드와 대조해 판단한다. 이 문서의 파일·함수 매핑은 구현 착수 위치이며, [이행 계획](MIGRATION.md)의 순정 6.2.2 동등성 검증은 별도로 진행한다.

- **미리보기 안내문:** 최종 ZIP도 `.writing_view_box`의 HTML을 저장하기 전에 `.tip_box2.tip_video`와 `#spoiler_warning_tip`을 제거한다. `#spoiler_warning` 본체, 경고 이미지, 일반 본문, YouTube 플레이어·링크, OG 카드는 보존한다. 경고창 내부 여백은 `padding: 85px 0 110px`이다.
- **과거 제외 결정:** 별도 개인 Turnstile 브리지, 읽음 구분선 강제 제거, 디시콘 선택 즉시 전송, 구형 YouTube 복원 템플릿은 이식 대상에서 제외한다. 과거 미리보기 테마·최소 높이·답글 들여쓰기 수치는 최종 ZIP에서 확인한 값만 적용한다. 순정의 PageUp/PageDown, 작성자 검색, 삭제 확인, Turnstile·FCM 흐름은 회귀 검증 대상으로 둔다.
- **요청과 정리:** 더블콘을 보낼 때 슬롯 필드와 함께 게시글 번호, 답글 번호, 비회원 입력 및 기존 인증 필드를 확인한다. 목록 갱신의 30초 요청 제한, 중복 요청과 복귀 신호, 재시도·취소·오류 원인 로깅을 각각 확인한다. X 미리보기의 준비 영역·타이머·관찰자와 최근 디시콘 요청은 글 이동·창 닫기 후 정리되는지 확인한다. 순정 글자콘 감시자의 해제 경로도 회귀 검증한다.
- **브라우저와 서버:** 게시글·댓글 캐시의 생성, 갱신, 작성·삭제·뒤로가기 이후 상태를 확인한다. Chrome 140 이상 및 저장소가 지원하는 Firefox에서 실행 환경, 미디어·다크모드·전체화면 렌더링, X 위젯의 완료·실패 메시지, 오류 로그에 불필요한 개인 정보가 포함되지 않는지를 확인한다. 실서버 더블콘 수락과 성공·실패 후 폼 상태는 별도로 검증한다. 두 ZIP의 매니페스트 권한은 동일하다.
