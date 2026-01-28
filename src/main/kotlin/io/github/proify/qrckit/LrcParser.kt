package io.github.proify.qrckit

import io.github.proify.qrckit.model.LrcData
import io.github.proify.qrckit.model.LrcLine

object LrcParser {
    // 支持 [00:00.00] [00:00:00] [00:00.000]
    private val TIME_TAG_REGEX = Regex("""\[(\d+):(\d{1,2})(?:[.:](\d+))?]""")
    // 元数据正则：[key:value]
    private val META_TAG_REGEX = Regex("""\[(\w+)\s*:\s*([^]]*)]""")

    fun parseLrc(raw: String?): LrcData {
        if (raw.isNullOrBlank()) return LrcData(emptyMap(), emptyList())

        val tempEntries = mutableListOf<LrcLine>()
        val metaData = mutableMapOf<String, String>()

        raw.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || !trimmed.startsWith("[")) return@forEach

            val timeMatches = TIME_TAG_REGEX.findAll(trimmed).toList()

            if (timeMatches.isNotEmpty()) {
                // 歌词内容是最后一个时间标签之后的所有文本
                val lastMatch = timeMatches.last()
                val content = trimmed.substring(lastMatch.range.last + 1).trim()

                timeMatches.forEach { match ->
                    val ms = parseTimeToMs(
                        min = match.groupValues[1],
                        sec = match.groupValues[2],
                        frac = match.groupValues[3]
                    )
                    tempEntries.add(LrcLine(start = ms, text = content))
                }
            } else {
                // 尝试匹配元数据
                META_TAG_REGEX.matchEntire(trimmed)?.let { match ->
                    metaData[match.groupValues[1]] = match.groupValues[2].trim()
                }
            }
        }

        if (tempEntries.isEmpty()) return LrcData(metaData, emptyList())

        // 3. 排序并计算持续时间
        val sortedInitial = tempEntries.sortedBy { it.start }

        val resultLines = mutableListOf<LrcLine>()
        for (i in sortedInitial.indices) {
            val current = sortedInitial[i]
            val nextStart = if (i + 1 < sortedInitial.size) sortedInitial[i + 1].start else null

            val end = nextStart ?: (current.start + 5000) // 最后一行默认给5秒
            resultLines.add(current.copy(
                end = end,
                duration = end - current.start
            ))
        }

        return LrcData(metaData, resultLines)
    }

    /**
     * 修复后的时间转换逻辑：正确处理补位
     */
    private fun parseTimeToMs(min: String, sec: String, frac: String?): Long {
        val m = min.toLongOrNull() ?: 0L
        val s = sec.toLongOrNull() ?: 0L

        // 修复：分数部分必须按位处理
        // .5 -> 500ms, .05 -> 50ms, .005 -> 5ms
        val ms = when {
            frac.isNullOrEmpty() -> 0L
            frac.length == 1 -> frac.toLong() * 100
            frac.length == 2 -> frac.toLong() * 10
            frac.length == 3 -> frac.toLong()
            else -> frac.substring(0, 3).toLong()
        }

        return m * 60000 + s * 1000 + ms
    }
}