# 최종 ZIP 개인 패치 상세 목록 — 정적 코드 조사

> 조사 입력: `dcrefresher-reborn-6.2.2-doublecon-empty-message-unified.zip`  
> SHA-256: `5ff415d0c1810c7eac65ade248898a3056d26f37c6938c90dfe1099e406b72a0`  
> ZIP의 `PATCH-NOTES.md`, `content-scripts/content.js`, `content.css`, `page.css`, `local-fixes.js`, `x-embed-main.js`를 기준으로 작성. 이전 버전의 패치 합의는 근거로 사용하지 않는다.

**정정 (2026-11-21):** PR #76의 목록은 일부 대표 구현만 뽑은 **부분 조사**이다. 이를 ‘개인 패치 전체 역공학 완료’로 판단하거나 사용하면 안 된다. 이 문서는 개별 동작으로 세분화한 보강 목록이다. 순정 6.2.2의 컴파일 산출물과 ZIP 전 파일을 바이트·구문 단위로 완전 비교하지 않았으므로 **누락이 전혀 없다고 확언할 수 없다**.

## 출처·판정 표기

- **노트+코드**: `PATCH-NOTES.md`가 개인 패치 유지/테스트 패치로 명시하고 ZIP의 실행 코드도 확인.
- **노트**: ZIP이 유지한다고 명시하지만 해당 변경과 순정의 차이를 아직 격리하지 못함.
- **코드**: ZIP에서 별도 구현을 확인했으나 노트가 독립 개인 패치로 분류하지 않은 기능. ‘ZIP에 있음’과 ‘순정 대비 개인 변경’을 혼동하지 않음.
- **순정 반영**: ZIP 노트가 공식 6.2.2 개선사항으로 명시. 코드에 존재해도 개인 패치로 세지 않음.

## A. 새로고침·캐시

| 세부 동작 | 근거 | 판정 |
| --- | --- | --- |
| 목록 새로고침 체감 보정 | `content.js`의 `__rfReturn`, `__rfResume` | 노트+코드 |
| 탭 복귀·페이지 복원·기록 이동 후 재갱신 | `__rfResume`; `visibilitychange`, `pageshow`, `popstate` 연결 | 노트+코드 |
| 요청 시간·주기·중복 요청 방지 | `__rfReturn`의 조건 검사와 요청 중 상태 | 노트+코드 (정확한 차이는 추가 분리 필요) |
| 게시글 본문을 최근 10초 동안 재사용 | `content.js`의 `fetchedAt` 비교 `< 1e4` | 노트+코드 |
| 댓글 목록을 최근 10초 동안 재사용 | `content.js`의 `commentsAt` 비교 `< 1e4` | 코드 (ZIP에 존재, 순정과의 차이 미확정) |

## B. 미디어 미리보기·외부 링크

| 세부 동작 | 근거 | 판정 |
| --- | --- | --- |
| 현재 디시 구조의 YouTube 영상·링크 세로 배치 | `content.css`의 `.dc-external-video` | 노트+코드 |
| YouTube 일반 링크에서 빠진 iframe 자동 보충 | `__rfYoutubeFallback`; watch/live/shorts/youtu.be 링크, ID 중복 방지 | 노트+코드 |
| 구형 ‘iframe → URL → 1px → OG 카드’ **폐기** | `PATCH-NOTES.md` 제거 명시 | **폐기된 동작, 이식 금지** |
| X 링크를 `.x-wrap`로 감싸 렌더링 | `__rfRenderX` | 노트+코드 |
| X MAIN world 위젯 호출과 완료/실패 통신 | `x-embed-main.js`: `refresher-x-render/done/error` | 노트+코드 |
| X 로딩 timeout·낮은 iframe 높이·실패 시 원본 링크 | `__rfRenderX`: 5초/500ms; 브리지 10초 | 코드 (X 기능의 일부) |
| Instagram 인용 블록을 iframe으로 교체 | `__rfRenderInstagram`, /p/, /reel/, /tv/ | 코드 (독립 개인 변경 여부 미확정) |
| 디시 자체 영상 및 iframe 여백 보정 | 노트의 ‘디시 자체 영상 보정’과 미디어 CSS | 노트 (세부 차이 분리 필요) |

## C. 본문·다크모드·디자인

| 세부 동작 | 근거 | 판정 |
| --- | --- | --- |
| OG 카드의 원본 inline-block / float 배치 | `content.css`: `og-wrap`, `og-img`, `og-info`, `og-tit` | 노트+코드 |
| OG 카드 다크모드 색상 | `content.css`의 `:is(.dark,.dark-theme)` OG 셀렉터 | 노트+코드 |
| 본문 사용자 지정 글자색·배경색 처리 | `__rfAdaptColors`: `[style],font[color]`, CSS 변수 | 노트+코드 |
| 어두운 무채색 글자의 다크모드 보정 | `__rfAdaptColors`: `data-refresher-neutral-color` | 코드 (색상 패치 일부) |
| 표의 셀 테두리·정렬·크기 처리 | `content.css`의 `.refresher-html table`, `td` | 노트+코드 |
| 스포일러 경고 UI 크기·중앙 정렬·아이콘·다크 이미지 | `content.css`에 후첨된 `#spoiler_warning` 선택자 블록 | 코드 (노트에서 독립 패치로 식별하지 않음) |
| 전체화면 디시 iframe의 다크모드 반전 방지 | `content.css`: `.dark .refresher-html iframe:fullscreen{filter:none!important}` | 노트+코드 |
| 미리보기 본문의 좌우 여백을 25px로 조정 | `content.css`: `.refresher-frame .px-8`, `.pl-8` | 노트+코드 |

## D. 댓글·답글·글자콘·더블콘

| 세부 동작 | 근거 | 판정 |
| --- | --- | --- |
| 답글 입력칸의 인라인 표시·위치 | `__rfInlineMode`, ZIP 노트의 댓글·대댓글 위치 | 노트+코드 |
| 답글 번호마다 다른 초안 키 | `__rfInlineMode`와 `|reply:<번호>` | 노트+코드 (순정의 게시글별 초안과는 별개) |
| 댓글 입력창 빈 공간 클릭 | `PATCH-NOTES.md` 개인 유지 목록 | 노트 (이벤트 조건 미분리) |
| 더블콘 시작 ‘더블콘 만들기’ **단일 버튼** | `__rfMixStart`, `__rfPickMix`, `__rfPickTxtcon` | 노트+코드 |
| 글자콘+글자콘 / 글자콘+이미지 디시콘 두 슬롯 구성 | `__rfMixSlots`, `__rfMixStart` | 노트+코드 |
| 디시콘 목록이 없거나 실패해도 글자콘 선택 가능 | `__rfMixLoaded`와 팝업 분기 | 노트+코드 |
| 더블콘 슬롯 선택·글자/색 편집·삭제·순서 교환 | `__rfMixSelect`, `__rfMixEdit`, `__rfMixColor`, `__rfMixRemove`, `__rfMixSwap` | 노트+코드 |
| 일반 댓글이 비어도 더블콘을 제출 흐름에 진입 | `!r && !n && !__rfDouble` 분기 | 코드 (파일명 ‘empty-message’ 의미 전체인지는 미확정) |
| 더블콘 중 글자콘 슬롯의 빈 텍스트 차단 | `__rfDouble?.some(...!slot.text.trim())` | 노트+코드 |
| 서버 요청 `/txtcon/insert_double`, 슬롯별 타입·내용 | `content.js`의 `Ud`, `slotN_type`, `txtcon_text_N`, `package_idx_N` 등 | 노트+코드, 서버 성공 미검증 |
| 기존 일반 글자콘 / 디시콘 전송 경로 보존 | `/txtcon/insert`, 기존 디시콘 경로 | 노트+코드 |
| 원본/평탄화 더블콘 댓글을 가로로 표시 | `content.css`의 `.double_txtcon`, 인접 `.coment_dccon_txt` / `.written_dccon` | 노트+코드 |

## E. 디시콘·작성자·오류

| 세부 동작 | 근거 | 판정 |
| --- | --- | --- |
| 최근 디시콘 목록·선택 UI | `__rfLoadRecent`, `/dccon/lists`의 `target:recent` | 노트+코드 |
| 디시콘 마우스 호버 설명 표시 | `__rfInstallDcconHover`와 `.refresher-dccon-infoview` | 노트+코드 |
| 호버 팝업에서 ‘디시콘 보기’ 및 지연 닫기 | 같은 함수의 버튼·180ms 타이머 | 코드 (호버 기능 일부) |
| 미리보기 작성 기기 아이콘을 원본 스타일로 복원 | `__rfDeviceIcon`, `.title_device`, `getComputedStyle` | 코드 (독립 개인 변경 여부 미확정) |
| 로그인 박스를 조건부로 이동하고 원위치로 복원 | `local-fixes.js`와 `page.css`의 `refresherGallLoginBox` | 노트+코드 |
| NetworkError 상세 로그 | `PATCH-NOTES.md`의 개인 유지 목록 | 노트 (순정과 다른 로깅 조건·출력 포맷 미분리) |

## 개인 패치와 혼동하면 안 되는 순정 6.2.2 반영 사항

ZIP의 `PATCH-NOTES.md`에서 공식 6.2.2 개선사항으로 별도 분류한다: 디시콘 패키지 **onWheel 가로 휠**·스크롤바 숨김, 댓글 숫자칸 0↔1 보정, 스레드 재렌더링 최적화(`__rfPreviousReplies` 관련), 글자콘 감시자 정리(`__rfTxtPending` 등), 작성자 검색 결과 60초 재사용(`__rfLastFill`), 버튼 포인터 커서 및 6.2.2 UI/라이브러리 산출물. ZIP에 존재한다고 이를 새 개인 패치로 중복 이식하지 않는다.

## 남은 불확실성 — ‘완전 역공학 완료’로 부르지 않음

1. 순정 6.2.2의 공식 **Chrome 빌드 산출물과 최종 ZIP** 사이의 파일·구문 수준 **전체 차이 목록**은 아직 확보하지 못했다. 비교가 끝나기 전 코드에 숨은 추가 변경이 없다고 확정하지 않는다.
2. 노트만 있는 항목(빈 공간 클릭, 상세 NetworkError, 디시 자체 영상 처리)은 실제 코드에서 원본 대비 달라진 분기를 모두 특정하지 못했다.
3. `__rfDeviceIcon`, `__rfRenderInstagram`, `#spoiler_warning` 등은 최종 ZIP에 실제 구현이 있는 사실만 확정했다. 노트의 개인 변경 설명이 없어 **독립 패치로 만들어진 이력**은 별도로 증명되지 않는다.
4. 컴파일 산출물 정적 확인 ≠ Chrome 실사이트 렌더링 / 더블콘 서버 전송 성공. 이 둘은 테스트 완료로 표시하지 않는다.

이 목록은 **앞선 대표 기능만 적은 목록을 대체하는 확대된 정적 구현 목록**이다. 이전 대화의 폐기된 구현을 요구사항으로 복원하지 않는다.
