import {kotlinBridge} from "@/kotlin/bridge";
import type {CommentRcKey} from "@/core/messaging/protocol";

/** 디시 JS에서 옮긴 값. e2e 계약 검사가 디시 파일과 같은지 본다. */

/** 디시콘, 글자콘은 예전 키를 그대로 쓴다. */
export const ICON_SITE_KEY = "6Lc-Fr0UAAAAAOdqLYqPy53MxlRMIXpNXFvBliwI";
/** 댓글 v3 키. comment.js가 없는 곳에서도 돌게 옮겨 두며, rc_key와 같은 쌍이어야 한다. */
export const COMMENT_SITE_KEYS: Record<CommentRcKey, string> = {
    new: "6LfrAuMtAAAAABXdEPC0ZSItQjLjlt2lC4jo3VNX",
    new3: "6LcjA-MtAAAAAIdgo8TKvID6HS34Ac9AkzRjuJfY"
};

/** 디시 _d()의 알파벳 (common.js). 표준 base64 알파벳을 섞은 것이다. */
export const D_ALPHABET = "yL/M=zNa0bcPQdReSfTgUhViWjXkYIZmnpo+qArOBslCt2D3uE4Fv5G6wH178xJ9K";
/** _d() 복호화는 Kotlin/JS 구현을 사용한다. */
export const decodeD = kotlinBridge.decodeD;
