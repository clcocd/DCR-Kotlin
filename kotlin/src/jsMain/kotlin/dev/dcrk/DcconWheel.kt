package dev.dcrk

/**
 * 검증된 디시콘 패키지 가로 휠 방식.
 * ref, MutationObserver, 스무스 스크롤 대신 네이티브 wheel 캡처를 쓴다.
 * 컨테이너는 UI 마운트 후 한 번 직접 찾아 전달한다.
 */
fun bindDcconPackageWheel(container: dynamic): () -> Unit {
    val onWheel: (dynamic) -> Unit = { event ->
        val delta = event.deltaY as Double
        if (delta != 0.0) {
            event.preventDefault()
            container.scrollLeft = (container.scrollLeft as Double) + delta
        }
    }
    val options: dynamic = js("({capture:true,passive:false})")
    container.addEventListener("wheel", onWheel, options)
    return { container.removeEventListener("wheel", onWheel, true) }
}
