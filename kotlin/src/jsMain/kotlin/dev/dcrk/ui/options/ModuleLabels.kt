package dev.dcrk

/** 원본 기능별 설정 문구. 옵션·팝업/WXT가 필요할 때만 객체를 만든다. */
internal fun moduleLabels(id: String): dynamic = when (id) {
    "block" -> blockLabels()
    "fonts" -> fontsLabels()
    "layout" -> layoutLabels()
    "preview" -> previewLabels()
    "refresh" -> refreshLabels()
    "search" -> searchLabels()
    "stealth" -> stealthLabels()
    "write" -> writeLabels()
    else -> error("알 수 없는 기능 레이블: $id")
}

private fun blockLabels(): dynamic {
    val BLUR_GROUP = props(
        "name" to "흐리게 처리",
        "desc" to "차단된 내용을 숨기지 않고 흐리게 처리합니다."
    )
    val DUPLICATE_GROUP = props(
        "name" to "같은 댓글 접기",
        "desc" to "같은 내용의 댓글이 여러 번 달리면 첫 댓글만 남기고 접습니다. 미리보기에도 적용됩니다."
    )
    return props(
        "name" to "콘텐츠 차단",
        "description" to "보고 싶지 않은 유저, 글, 댓글을 숨기거나 흐리게 합니다.",
        "settings" to props(
            "replyRemove" to props(
                "name" to "대댓글도 가리기",
                "desc" to "차단된 댓글의 대댓글도 함께 가립니다."
            ),
            "blur" to props(
                "group" to BLUR_GROUP,
                "name" to "사용",
                "desc" to "차단된 내용을 흐리게 처리합니다."
            ),
            "blurReveal" to props(
                "group" to BLUR_GROUP,
                "name" to "마우스를 올리면 보기",
                "desc" to "흐리게 처리된 내용에 마우스를 올린 동안 원래대로 보여 줍니다."
            ),
            "blurStrength" to props(
                "group" to BLUR_GROUP,
                "name" to "강도",
                "desc" to "차단된 내용을 흐리게 하는 정도입니다."
            ),
            "foldDuplicate" to props(
                "group" to DUPLICATE_GROUP,
                "name" to "사용",
                "desc" to "같은 댓글을 한 줄로 접습니다."
            ),
            "duplicateCount" to props(
                "group" to DUPLICATE_GROUP,
                "name" to "반복 횟수",
                "desc" to "이만큼 반복되면 접습니다."
            ),
            "duplicateMinLength" to props(
                "group" to DUPLICATE_GROUP,
                "name" to "최소 글자 수",
                "desc" to "이보다 짧은 댓글(ㅋㅋ 등)은 반복돼도 접지 않습니다."
            )
        ),
        "toggles" to props(
            "reveal" to "가린 내용 보기"
        ),
        "commands" to props(
            "blockReveal" to "이 페이지에서 가린 내용 보기"
        )
    )
}

private fun fontsLabels(): dynamic {
    return props(
        "name" to "폰트 교체",
        "description" to "페이지 전반의 폰트를 교체합니다.",
        "settings" to props(
            "customFonts" to props(
                "name" to "폰트 이름",
                "desc" to "쉼표로 구분한 폰트 이름입니다. 앞의 폰트가 없으면 다음 폰트를 씁니다. (빈칸이면 기본 폰트)"
            ),
            "changeDCFont" to props(
                "name" to "디시인사이드 폰트 교체",
                "desc" to "미리보기 창 같은 DCRefresher Reborn의 폰트뿐만 아니라 디시인사이드의 폰트와 본문 크기까지 바꿉니다."
            ),
            "bodyFontSize" to props(
                "name" to "본문 폰트 크기",
                "desc" to "게시글 본문의 폰트 크기입니다. 미리보기 창(미니 미리보기 포함)은 본문과 댓글 모두 +2px로 표시됩니다."
            )
        )
    )
}

private fun layoutLabels(): dynamic {
    return props(
        "name" to "레이아웃 수정",
        "description" to "디시 레이아웃을 변경합니다.",
        "settings" to props(
            "activePixel" to props(
                "name" to "컴팩트 모드 활성화 조건",
                "desc" to "브라우저 가로가 이 값보다 작으면 컴팩트 모드를 활성화합니다."
            ),
            "forceCompact" to props(
                "name" to "컴팩트 모드 강제 사용",
                "desc" to "항상 컴팩트 모드를 사용합니다."
            ),
            "useCompactModeOnView" to props(
                "name" to "게시글 보기 컴팩트 모드",
                "desc" to "게시글 보기에서도 컴팩트 모드를 사용합니다."
            ),
            "hideGalleryView" to props(
                "name" to "갤러리 뷰 숨기기",
                "desc" to "갤러리 정보, 최근 방문 갤러리 영역을 숨깁니다."
            ),
            "hideUselessView" to props(
                "name" to "잡다 링크 숨기기",
                "desc" to "이슈줌, 타갤 개념글, 뉴스, 힛갤 등의 콘텐츠를 오른쪽 영역에서 숨깁니다."
            ),
            "hideGalleryImage" to props(
                "name" to "갤러리 대문 숨기기",
                "desc" to "갤러리 대문을 숨깁니다."
            ),
            "removeNotice" to props(
                "name" to "갤러리 공지 숨기기",
                "desc" to "글 목록에서 공지사항을 숨깁니다."
            ),
            "removeDCNotice" to props(
                "name" to "디시 공지 숨기기",
                "desc" to "글 목록에서 운영자의 게시글을 숨깁니다."
            ),
            "removeGamemeca" to props(
                "name" to "게임메카 숨기기",
                "desc" to "글 목록에서 게임메카 게시글을 숨깁니다."
            ),
            "removeAi" to props(
                "name" to "AI 글 숨기기",
                "desc" to "글 목록에서 AI 표시가 붙은 글을 숨깁니다."
            ),
            "pushToRight" to props(
                "name" to "본문 영역 전체로 확장",
                "desc" to "\"잡다 링크 숨기기\" 옵션이 켜져 있으면 본문 영역을 확장합니다."
            )
        )
    )
}

private fun previewLabels(): dynamic {
    val FRAME_GROUP = props(
        "name" to "미리보기 창",
        "desc" to "미리보기 창의 너비와 바깥 배경입니다."
    )
    return props(
        "name" to "미리보기",
        "description" to "글 목록에서 클릭 또는 우클릭으로 미리보기 창을 띄워 줍니다.",
        "settings" to props(
            "previewWidth" to props(
                "group" to FRAME_GROUP,
                "name" to "창 너비",
                "desc" to "미리보기 창의 너비입니다. 브라우저 창이 좁으면 그에 맞춰 줄어듭니다."
            ),
            "toggleBackgroundBlur" to props(
                "group" to FRAME_GROUP,
                "name" to "바깥 배경 흐리게",
                "desc" to "미리보기 창 바깥 배경을 흐리게 처리합니다. (성능이 떨어질 수 있음)"
            ),
            "popupBlur" to props(
                "name" to "팝업 바깥 배경 흐리게",
                "desc" to "메모, 차단 같은 팝업 창 바깥 배경을 흐리게 처리합니다."
            ),
            "scrollToSkip" to props(
                "name" to "스크롤하여 게시글 이동",
                "desc" to "미리보기 맨 아래에서 한 번 더 스크롤하면 이전(번호가 작은) 게시글로, 맨 위에서는 다음(번호가 큰) 게시글로 넘어갑니다."
            ),
            "reversePreviewKey" to props(
                "name" to "미리보기 키 반전",
                "desc" to "좌클릭으로 미리보기를 열고 우클릭으로 게시글로 이동합니다. 댓글 수도 같습니다."
            ),
            "longPressDelay" to props(
                "name" to "길게 누르기 판정 시간",
                "desc" to "마우스 오른쪽 버튼을 이 시간 이상 눌렀다 뗄 때 기본 우클릭 메뉴가 나오게 합니다. (Windows 전용 - Shift+우클릭은 어디서나 기본 메뉴)"
            ),
            "colorPreviewLink" to props(
                "name" to "주소창에 게시글 주소 표시",
                "desc" to "미리보기를 여는 동안 주소창과 탭 제목을 그 게시글로 바꿉니다. 연 글은 방문 기록에 남아 목록에서 방문한 글 색으로 보입니다."
            ),
            "autoRefreshComment" to props(
                "name" to "댓글 자동 새로고침",
                "desc" to "일정 주기로 댓글을 자동으로 새로고침합니다."
            ),
            "highlightNewComments" to props(
                "name" to "새 댓글 강조",
                "desc" to "댓글을 새로고침했을 때 새로 들어온 댓글을 잠깐 강조합니다."
            ),
            "commentRefreshInterval" to props(
                "name" to "댓글 자동 새로고침 주기",
                "desc" to "댓글 자동 새로고침 주기입니다."
            ),
            "expandRecognizeRange" to props(
                "name" to "게시글 인식 범위 확장",
                "desc" to "행 전체를 클릭해도 미리보기가 열리게 합니다."
            ),
            "imageViewer" to props(
                "name" to "이미지 크게 보기",
                "desc" to "본문 이미지를 누르면 미리보기 안에서 크게 봅니다. ←/→로 넘깁니다. 끄면 원본 보기를 새 탭으로 엽니다."
            ),
            "listKeyboard" to props(
                "name" to "목록 키보드 이동",
                "desc" to "J/K로 글을 고르고 Enter로 미리보기, O로 게시글을 엽니다. Esc로 선택을 풉니다."
            ),
            "disableCache" to props(
                "name" to "캐시 비활성화",
                "desc" to "미리보기 캐시를 사용하지 않습니다."
            ),
            "archiveArticle" to props(
                "name" to "삭제된 글과 댓글 보존",
                "desc" to "미리보기로 본 지 1분이 안 된 글과 댓글이 삭제되어도 이전 내용을 보여 줍니다. 새로고침한 글 목록에서 사라진 글도 붉게 표시해 남깁니다."
            ),
            "blockImage" to props(
                "name" to "이미지 아이콘 없는 게시글 이미지 숨기기",
                "desc" to "이미지 아이콘이 없는 게시글에 이미지가 있으면 숨깁니다."
            )
        )
    )
}

private fun refreshLabels(): dynamic {
    val BACKGROUND_GROUP = props(
        "name" to "숨은 탭",
        "desc" to "다른 탭을 보는 동안의 새로고침입니다."
    )
    return props(
        "name" to "글 목록 새로고침",
        "description" to "글 목록을 자동으로 새로고침합니다.",
        "settings" to props(
            "refreshRate" to props(
                "name" to "새로고침 주기",
                "desc" to "글 목록을 새로고침하는 주기입니다. 새 글이 올라오는 1페이지에서만 자동으로 새로고침합니다. 새 글이 한동안 없으면 주기를 조금씩 늘려 최대 4배까지 기다립니다. 새 글이 올라오거나 목록 위에서 마우스, 키보드를 쓰면 원래 주기로 돌아옵니다."
            ),
            "fadeIn" to props(
                "name" to "새 게시글 효과",
                "desc" to "새로 추가된 게시글에 효과를 넣습니다."
            ),
            "useBetterBrowse" to props(
                "name" to "페이지 이동 시 목록만 교체",
                "desc" to "페이지 번호를 누르면 페이지 전체를 다시 불러오지 않고 목록만 바꿉니다."
            ),
            "noRefreshOnSearch" to props(
                "name" to "검색 중 자동 새로고침 안 함",
                "desc" to "검색 중에는 자동 새로고침을 하지 않습니다."
            ),
            "pauseOnHover" to props(
                "name" to "목록 위에서 자동 새로고침 안 함",
                "desc" to "마우스를 글 목록 위에 올려 두는 동안에는 자동 새로고침을 하지 않습니다. 누르려던 글이 밀리지 않습니다."
            ),
            "titleCount" to props(
                "name" to "탭 제목에 새 글 수 표시",
                "desc" to "다른 창을 보는 동안 들어온 새 글 수를 탭 제목 앞에 (3)처럼 붙입니다. 다른 탭을 보는 동안에도 세려면 '숨은 탭에서도 새로고침'을 켜세요. 탭으로 돌아오면 지웁니다."
            ),
            "readMark" to props(
                "name" to "'여기까지 읽음' 선 표시",
                "desc" to "다른 창이나 탭을 보는 동안 새 글이 들어왔으면, 돌아올 때 그동안 들어온 글 아래에 '여기까지 읽음' 선을 긋습니다. 다음에 돌아올 때까지 남습니다."
            ),
            "backgroundRefresh" to props(
                "group" to BACKGROUND_GROUP,
                "name" to "숨은 탭에서도 새로고침",
                "desc" to "다른 탭을 보는 동안에도 아래 주기로 새로고침합니다. 끄면 탭으로 돌아올 때까지 쉽니다."
            ),
            "backgroundRefreshRate" to props(
                "group" to BACKGROUND_GROUP,
                "name" to "숨은 탭 새로고침 주기",
                "desc" to "숨은 탭의 새로고침 주기입니다. 새 글이 한동안 없으면 이 주기도 최대 4배까지 늘어납니다."
            ),
            "doNotColorVisited" to props(
                "name" to "방문한 글 색 구분 안 함",
                "desc" to "글 목록에서 방문한 글도 방문하지 않은 글과 같은 색으로 표시합니다."
            )
        ),
        "toggles" to props(
            "pause" to "자동 새로고침 일시정지"
        ),
        "commands" to props(
            "refreshLists" to "새로고침",
            "refreshPause" to "자동 새로고침 일시정지"
        )
    )
}

private fun searchLabels(): dynamic {
    return props(
        "name" to "검색",
        "description" to "검색 결과가 한 쪽에 못 미치면 다음 구간을 이어 붙이고, 아이디, IP로 한 사람의 글을 찾습니다.",
        "settings" to props(
            "maxSearches" to props(
                "name" to "최대 다음 검색 횟수",
                "desc" to "한 번에 이어서 검색할 최대 횟수입니다."
            )
        )
    )
}

private fun stealthLabels(): dynamic {
    return props(
        "name" to "스텔스 모드",
        "description" to "페이지에 표시되는 이미지를 숨깁니다.",
        "toggles" to props(
            "reveal" to "이미지 잠시 보이기"
        ),
        "commands" to props(
            "stealthPause" to "이미지 잠시 보이기"
        )
    )
}

private fun writeLabels(): dynamic {
    return props(
        "name" to "글쓰기",
        "description" to "글쓰기 페이지를 변경합니다.",
        "settings" to props(
            "preventExit" to props(
                "name" to "나가기 방지",
                "desc" to "작성 중인 글이 있으면 페이지를 나가기 전에 확인합니다."
            ),
            "webpConvert" to props(
                "name" to "이미지 WebP 변환",
                "desc" to "올리는 이미지를 WebP로 바꿔 용량을 줄입니다. 움직이는 이미지(GIF, APNG)와 WebP, AVIF, WebP로 바꾸면 더 커지는 이미지는 그대로 올립니다."
            ),
            "webpQuality" to props(
                "name" to "WebP 품질",
                "desc" to "낮을수록 용량이 줄고 화질이 떨어집니다."
            ),
            "webpConcurrency" to props(
                "name" to "동시 변환 수",
                "desc" to "여러 장을 한꺼번에 바꾸는 수입니다. 높을수록 빠르지만 메모리를 더 씁니다."
            ),
            "obfuscateName" to props(
                "name" to "이미지 이름 숨기기",
                "desc" to "올리는 이미지의 파일 이름을 무작위로 바꿉니다."
            )
        )
    )
}
