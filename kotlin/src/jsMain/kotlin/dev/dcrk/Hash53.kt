package dev.dcrk

/** 원본 utils/hash.ts의 cyrb53. Kotlin Int 연산에서 32비트 오버플로를 보존한다. */
fun hash53(text: String): Double {
    var h1 = -559038737
    var h2 = 1103547991
    for (char in text) {
        val code = char.code
        h1 = (h1 xor code) * -1640531535
        h2 = (h2 xor code) * 1597334677
    }
    h1 = ((h1 xor (h1 ushr 16)) * -2048144789) xor
        ((h2 xor (h2 ushr 13)) * -1028477387)
    h2 = ((h2 xor (h2 ushr 16)) * -2048144789) xor
        ((h1 xor (h1 ushr 13)) * -1028477387)
    return (h2 and 2097151).toDouble() * 4294967296.0 +
        (h1.toLong() and 4294967295L).toDouble()
}
