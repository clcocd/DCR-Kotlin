package dev.dcrk

/**
 * 원본 core/module/define.ts와 같은 메타+구현 조합.
 * 메타의 스키마와 함수 객체는 복사하지 않고 참조를 보존한다.
 */
fun defineContentModule(meta: dynamic, impl: dynamic): dynamic {
    val result: dynamic = js("Object.assign")(js("({})"), meta)
    result.setup = impl.setup
    result.shortcuts = impl.shortcuts

    val toggles: dynamic = meta.toggles
    if (toggles == null) {
        result.pageToggles = js("undefined")
    } else {
        val actions: dynamic = impl.pageToggles
        val configured: dynamic = js("[]")
        for (index in 0 until (toggles.length as Int)) {
            val id = toggles[index] as String
            val action: dynamic = if (actions != null) actions[id] else null
            if (js("Boolean")(action) as Boolean) {
                val entry: dynamic = js("({})")
                entry.id = id
                configured.push(js("Object.assign")(entry, action))
            }
        }
        result.pageToggles = configured
    }
    return result
}

/** 배경 모듈은 메타의 활성화·설정 속성과 listen/apply만 가진다. */
fun defineBackgroundModuleKotlin(meta: dynamic, impl: dynamic): dynamic {
    val result: dynamic = js("({})")
    result.id = meta.id
    result.defaultEnable = meta.defaultEnable
    result.settings = meta.settings
    return js("Object.assign")(result, impl)
}
