# 개발 지침

이 저장소는 원본 DCRefresher Reborn 6.2.2를 Kotlin/JS로 이식하기 위한 **독립 Private 프로젝트**다.

1. **Think Before Coding**: 구현 전 원본 코드·`docs/` 문서에서 동작을 확인하고 변경 범위를 특정한다. 확인되지 않은 사실을 만들지 않는다.
2. **Simplicity First**: 지금 검증할 기능에 필요한 최소 코드만 작성한다. 일회용 추상화나 추측성 옵션을 추가하지 않는다.
3. **Surgical Changes**: 요청과 직접 관련된 파일만 수정한다. 동작 중인 개인 패치를 이유 없이 다른 방식으로 바꾸지 않는다.
4. **Goal-Driven Execution**: 변경 전 재현/기준을 확보하고, 변경 후 컴파일·테스트·실제 확장 실행 여부를 분리하여 확인한다.
5. **Kotlin 중심**: 기능·UI를 Kotlin으로 구현하는 것을 목표로 한다. 브라우저 API 호출, manifest, 패키징을 위해 다른 언어가 필요할 때에는 최소로 유지한다.
6. **호환성 우선**: 모듈 ID, 설정/백업 형식, 기존 UI, content/background/MAIN world 경계를 보존한다.
7. **작업 순서·최종본 보존**: 순정 6.2.2의 Kotlin 이식과 기능 동등성 검증을 먼저 진행한다. 개인 패치의 **구현 사양은 SHA-256 `5ff415d0c1810c7eac65ade248898a3056d26f37c6938c90dfe1099e406b72a0` 최종 ZIP의 실제 JS/CSS 및 `PATCH-NOTES.md`만** 기준으로 삼고 `docs/FINAL-ZIP-REVERSE-ENGINEERING.md`와 `docs/FINAL-ZIP-PATCH-INVENTORY.md`에서 확인한다. 앞의 문서는 대표 구현, 뒤의 문서는 세부 목록이므로 한 문서만 보고 ‘전수 역공학 완료’로 결론내리지 않는다. 구형 YouTube iframe/URL/1px/OG 템플릿은 ZIP에서 폐기됐다. 이전 합의·역사 기록으로 ZIP의 구현 사양을 덮어쓰지 않는다. 검증되지 않은 ZIP 기능은 검증 완료라고 주장하지 않는다.
8. **PR 기반 개발**: 단계별 브랜치/PR, 통과한 변경만 `main`에 병합한다. 커밋 메시지는 한국어 본문과 명확한 변경 범위를 선호한다.
9. **GPL-3.0**: 원본 코드/파생 코드를 포함하면 기존 라이선스와 저작권 고지를 보존한다.

진행 계획: `docs/MIGRATION.md`. **최종 개인 패치 구현 기준: `docs/FINAL-ZIP-REVERSE-ENGINEERING.md`와 원본 ZIP**. 과거 기록: `docs/PATCH-DECISIONS.md`. 이식 감사: `docs/KOTLIN-AUDIT.md`.
