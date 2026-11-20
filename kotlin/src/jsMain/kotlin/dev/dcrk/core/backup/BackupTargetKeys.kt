package dev.dcrk

/** 6.2.2 백업 대상 키의 순서와 형식을 보존한다. 데이터베이스와 기기별 백업 메타는 포함하지 않는다. */
internal fun backupTargetKeys(
    modulesKey: String,
    defaultsKey: String,
    blockTypes: Array<String>,
    memoTypes: Array<String>,
    moduleIds: Array<String>
): Array<String> {
    val keys = mutableListOf(rawStorageKey(modulesKey), rawStorageKey(defaultsKey))
    keys.addAll(blockTypes.map { rawStorageKey(blockListKey(it)) })
    keys.addAll(memoTypes.map { rawStorageKey(memoMapKey(it)) })
    keys.addAll(moduleIds.map { rawStorageKey(moduleSettingsKey(it)) })
    return keys.toTypedArray()
}
