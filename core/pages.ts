/** 콘텐츠 스크립트가 도는 주소. 페이지 CSS(entrypoints/page.content.css)도 같은 곳에 넣는다 (wxt.config.ts). */
export const CONTENT_MATCHES = ["https://*.dcinside.com/*"];
export const CONTENT_EXCLUDE_MATCHES = [
    "https://event.dcinside.com/*",
    "https://h5.dcinside.com/*",
    "https://m.dcinside.com/*",
    "https://mall.dcinside.com/*",
    "https://wiki.dcinside.com/*",
    "https://gallog.dcinside.com/*",
    // 이미지 팝업(viewimagePop.php)은 원래 gall 탭과 같은 렌더러에서 돌아 번들 평가, 저장소 읽기 비용이 그 탭에 그대로 더해진다.
    // 로그인 페이지(sign)는 제외해도 폰트만 빠진다.
    "https://image.dcinside.com/*",
    "https://sign.dcinside.com/*"
];

export const BOARD_PAGE = /\/board\/(view|lists)/;
export const VIEW_PAGE = /\/board\/view/;
export const WRITE_PAGE = /\/board\/(write|modify)/;
export const LIST_PAGE = /\/board\/lists/;
