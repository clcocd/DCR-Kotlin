# DCRefresher Reborn Kotlin

DCRefresher Reborn 6.2.2(커밋 `19fdd7a`)를 기준으로 Kotlin/JS로 이식한 개인용 독립 저장소입니다. [Kotlin/JS 소스](kotlin/src/jsMain/kotlin/dev/dcrk)

팝업·옵션과 공용 UI, 글 목록·미리보기·댓글, 작성자 검색과 메모, 디시콘, 관리 도구, 저장소 동기화·백업, IP·밴 DB 생성과 조회를 Kotlin/JS에서 실행합니다. TypeScript는 WXT 진입점, 브라우저·라이브러리 연결, 설정과 타입 선언, 테스트에 사용합니다.

## 로컬 빌드

JDK 21, Node.js 26, Bun이 필요합니다. Gradle은 저장소의 Wrapper(8.14.3)를 사용합니다. Windows에서는 Git Bash 또는 WSL을 사용합니다.

```sh
./gradlew :kotlin:jsNodeTest
bun run build:kotlin
bun ci
bun run build
bun run build:firefox
```

Chrome 빌드는 `.output/chrome-mv3`, Firefox 빌드는 `.output/firefox-mv3`에 생성됩니다. Kotlin 파일을 바꾸면 `bun run build:kotlin`으로 JS 번들을 다시 생성합니다.

## 검증

GitHub Actions에서 Kotlin/JS 및 Vitest 단위 테스트, TypeScript 타입 검사, Chrome·Firefox MV3 빌드와 Chromium 모의 사이트 E2E를 실행합니다. Chromium E2E의 기존 실패는 원본 6.2.2 기준선과 비교합니다.

[이식 계획](docs/MIGRATION.md) · [원본 감사 기록](docs/KOTLIN-AUDIT.md) · [개인 패치 결정 기록](docs/PATCH-DECISIONS.md)

## 원본 및 라이선스

[green1052/DCRefresher-Reborn](https://github.com/green1052/DCRefresher-Reborn)의 소스와 파생 코드는 [GPL-3.0](LICENSE)을 따릅니다.
