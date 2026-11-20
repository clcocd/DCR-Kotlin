package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertContentEquals

class BackupTargetKeysTest {
    @Test
    fun backedUpKeysKeepOriginalPrefixesAndOrdering() {
        assertContentEquals(
            arrayOf(
                "refresher:modules",
                "refresher:block:defaults",
                "refresher:block:NICK", "refresher:block:ID", "refresher:block:IP",
                "refresher:memo:UID", "refresher:memo:NICK", "refresher:memo:IP",
                "refresher:module:preview:settings", "refresher:module:userinfo:settings"
            ),
            backupTargetKeys(
                "local:refresher:modules", "local:refresher:block:defaults",
                arrayOf("NICK", "ID", "IP"), arrayOf("UID", "NICK", "IP"),
                arrayOf("preview", "userinfo")
            )
        )
    }

    @Test
    fun missingOptionalTypesDoNotIntroduceOtherKeys() {
        assertContentEquals(
            arrayOf("refresher:modules", "refresher:block:defaults"),
            backupTargetKeys(
                "local:refresher:modules", "local:refresher:block:defaults",
                emptyArray(), emptyArray(), emptyArray()
            )
        )
    }
}
