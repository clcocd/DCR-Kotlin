# DCRefresher Reborn Kotlin

DCRefresher Reborn을 Kotlin/JS 중심으로 이식하는 **개인용 독립 저장소**입니다. 원본 GitHub 저장소의 Fork가 아닙니다.

## 목표

- 기능·설정·UI를 가능한 한 Kotlin으로 개발하고, Kotlin/JS가 생성한 JavaScript를 브라우저 확장 프로그램으로 패키징합니다.
- 원본 **DCRefresher Reborn 6.2.2**(사용자가 제공한 소스 ZIP)를 기준으로 기존 동작을 보존합니다.
- Chrome Manifest V3를 우선 지원하고, Firefox 호환성을 별도로 검증합니다.
- 기존 개인 패치는 원본 호환성 확보 후 **기능별 PR**로 이식하고 병합합니다.
- GitHub Actions에서 빌드와 테스트를 통과한 산출물만 배포합니다.

## 현 상태

**원본 6.2.2 소스 가져오기 및 Kotlin/JS 첫 모듈·Chrome MV3 빌드 CI 검증 완료** 상태입니다. Kotlin 갤러리 종류 판정 코드가 포함된 JavaScript가 확장 ZIP에 들어가는 것을 GitHub Actions에서 검증했습니다. **전체 Kotlin 포팅 및 개인 패치의 기능별 이식은 아직 미완료**입니다. 패키지 빌드 성공을 실사이트 동작 검증으로 간주하지 않습니다.

이식 기준과 단계별 검증 항목은 [개발 계획](docs/MIGRATION.md)을 참고합니다.

## 원본 및 라이선스

기준 프로젝트: [green1052/DCRefresher-Reborn](https://github.com/green1052/DCRefresher-Reborn)

원본 코드는 GPL-3.0 라이선스입니다. 원본 코드 또는 그 파생물을 가져올 때 기존 저작권 고지와 해당 라이선스를 보존합니다. 비공개 저장소 운영이 원본 라이선스를 없애지는 않습니다.
