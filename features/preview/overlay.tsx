import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

// 미리보기의 오버레이 호스트. components/overlay/ContentRoot.tsx가 features/*/overlay.tsx를 모아 그린다.
import {Frame} from "./ui/Frame";
import {Popups} from "./ui/Popups";

export default kotlinBridge.createPreviewHost(react, Frame, Popups);
