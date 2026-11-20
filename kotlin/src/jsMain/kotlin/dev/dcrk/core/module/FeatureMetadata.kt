package dev.dcrk

internal fun assembleFeatureInfo(modules: dynamic, icons: dynamic, labels: dynamic, fallbackIcon: dynamic): Array<dynamic> =
    entriesOf(modules).map { item ->
        val path = item[0] as String
        val meta = item[1]
        val root = path.removeSuffix("meta.ts")
        val label = labels[root + "labels.ts"] ?: props("name" to meta.id, "description" to "")
        val raw = meta.settings
        val settingsLabels = label.settings
        val settings = if (!truthy(raw)) raw else fromEntries(entriesOf(raw).map { pair ->
            val key = pair[0]
            val text = if (settingsLabels != null) settingsLabels[key] else null
            arrayOf<dynamic>(key, mergeUiProps(pair[1], text ?: props("name" to key, "desc" to "")))
        }.toTypedArray())
        mergeUiProps(meta, props("name" to label.name, "description" to label.description, "settings" to settings,
            "labels" to label, "icons" to (icons[root + "icons.ts"] ?: props("module" to fallbackIcon))))
    }.toTypedArray()

internal fun featureLoaders(modules: dynamic): dynamic =
    fromEntries(entriesOf(modules).map { item ->
        val path = item[0] as String
        arrayOf<dynamic>(path.removePrefix("./").removeSuffix("/index.ts"), item[1])
    }.toTypedArray())

internal fun makeManifestCommands(current: dynamic, metas: Array<dynamic>, labels: Array<dynamic>): dynamic {
    val commands = props()
    val hasProperty: dynamic = js("(key, object) => key in object")
    for ((index, meta) in metas.withIndex()) {
        val label = labels.getOrNull(index)
        for (entry in entriesOf(meta.commands ?: props())) {
            val name = entry[0]
            if (hasProperty(name, commands) == true) error("단축키 이름이 겹칩니다: $name (${meta.id})")
            val labelCommands = label?.commands
            val desc = if (labelCommands == null) undefinedValue() else labelCommands[name]
            if (label?.name == null || desc === undefinedValue()) error("단축키 설명이 labels.ts에 없습니다: $name (${meta.id})")
            val command = props("description" to "${label.name}: $desc")
            if (truthy(entry[1].key)) command.suggested_key = props("default" to entry[1].key)
            commands[name] = command
        }
    }
    if (ownKeys(commands).isEmpty()) return current
    val result = mergeUiProps(current, commands)
    val count = jsValues(result).count { truthy(it.suggested_key) }
    if (count > 4) error("크롬은 기본 단축키를 4개까지만 받습니다 (지금 ${count}개)")
    return result
}

internal fun featureFolders(root: String, file: String, s: dynamic): Array<String> {
    val directory = s.resolve(root, "features")
    val entries = s.readdirSync(directory, props("withFileTypes" to true)) as Array<dynamic>
    return entries.filter { it.isDirectory() == true && s.existsSync(s.resolve(directory, it.name, file)) == true }
        .map { it.name as String }.sorted().toTypedArray()
}

internal fun moduleTypeEntries(metaFolders: Array<String>, moduleFolders: Array<String>): Array<dynamic> {
    val modules = moduleFolders.joinToString(" | ") { "typeof import(\"@/features/$it/index\").default" }
    val metas = metaFolders.joinToString(" | ") { "typeof import(\"@/features/$it/meta\").default" }
    val types = "import type {ModuleApiMap, ModuleSettingsMap} from \"@/core/module/types\";\n\ndeclare module \"@/core/module/types\" {\n" +
        "    interface ModuleApis extends ModuleApiMap<$modules> {}\n" +
        "    interface ModuleSettings extends ModuleSettingsMap<$metas> {}\n}\n"
    val ids = js("JSON.stringify")(metaFolders)
    return arrayOf<dynamic>(props("path" to "types/modules.d.ts", "text" to types, "tsReference" to true),
        props("path" to "module-ids.ts", "text" to "export default $ids;\n"))
}

internal fun featureStyleEntry(folders: Array<String>): dynamic =
    props("path" to "page-styles.css", "text" to folders.joinToString("\n", postfix = "\n") { "@import \"../features/$it/page.css\";" })
