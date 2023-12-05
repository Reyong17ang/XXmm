package com.example.mod_p2p.ext

fun String.plusInt(num: Int): String {
    val originalNumber = this.toInt()
    val result = originalNumber + num
    return result.toString()
}
fun addStrings(s1: String, s2: String): String {
    val thisSize = parseSizeString(s1)
    val otherSize = parseSizeString(s2)
    val totalSize = thisSize + otherSize

    // Convert totalSize back to a readable string
    val sizeUnits = arrayOf("B", "KB", "MB", "GB", "TB")
    var index = 0
    var size = totalSize.toDouble()
    while (size > 1024 && index < sizeUnits.size - 1) {
        size /= 1024
        index++
    }
    return "%.3f %s".format(size, sizeUnits[index])
}

fun parseSizeString(sizeString: String): Double {
    val sizeUnits = mapOf(
        "B" to 1.0,
        "KB" to 1024.0,
        "MB" to 1024.0 * 1024.0,
        "GB" to 1024.0 * 1024.0 * 1024.0,
        "TB" to 1024.0 * 1024.0 * 1024.0 * 1024.0
    )
    val sizeRegex = Regex("(\\d+(\\.\\d+)?)\\s*(B|KB|MB|GB|TB)", RegexOption.IGNORE_CASE)
    val matchResult = sizeRegex.find(sizeString) ?: return 0.0
    val (size, _, unit) = matchResult.destructured
    val sizeValue = size.toDouble()
    val unitValue = sizeUnits[unit.toUpperCase()] ?: 1.0
    return sizeValue * unitValue
}