package io.github.proify.qrckit

import io.github.proify.qrckit.model.LyricLine
import io.github.proify.qrckit.model.LyricWord
import io.github.proify.qrckit.model.QrcData

object QrcParser {
    // Line: [123,456]
    private val linePattern = Regex("""\[(\d+)\s*,\s*(\d+)]""")

    // Word: 歌词(123,456)
    private val wordPattern = Regex("""([^(]+)\((\d+)\s*,\s*(\d+)\)""")

    // Meta: [ti:Song Title]
    private val metaPattern = Regex("""\[(\w+)\s*:\s*([^]]*)]""")

    // XML Content: 提取 LyricContent 属性
    private val lyricContentRegex = Regex("""LyricContent="([^"]+)"""")

    fun parseXML(xml: String?): List<QrcData> {
        if (xml.isNullOrBlank()) return emptyList()

        return lyricContentRegex.findAll(xml).map { match ->
            val lyricContent = match.groupValues[1]
            val (meta, lines) = parseLyric(lyricContent)
            QrcData(meta, lines)
        }.toList()
    }

    fun parseLyric(content: String): Pair<Map<String, String>, List<LyricLine>> {
        val metaData = mutableMapOf<String, String>()
        val lines = mutableListOf<LyricLine>()

        // 1. 提取元数据（如 [ti:标题], [ar:歌手]）
        metaPattern.findAll(content).forEach {
            metaData[it.groupValues[1]] = it.groupValues[2].trim()
        }

        // 2. 提取歌词行
        // 使用正则寻找所有行起始标签 [start, duration]
        val lineMatches = linePattern.findAll(content).toList()

        for (i in lineMatches.indices) {
            val currentMatch = lineMatches[i]
            val lineStart = currentMatch.groupValues[1].toLong()
            val lineDur = currentMatch.groupValues[2].toLong()

            // 截取当前行标签到下一个标签之间的内容
            val bodyEnd = if (i + 1 < lineMatches.size) lineMatches[i + 1].range.first else content.length
            val lineBody = content.substring(currentMatch.range.last + 1, bodyEnd)

            if (lineBody.isNotBlank()) {
                lines.add(parseLineBody(lineStart, lineDur, lineBody))
            }
        }

        return metaData to lines.sortedBy { it.start }
    }

    private fun parseLineBody(lineStart: Long, lineDur: Long, rawBody: String): LyricLine {
        val words = wordPattern.findAll(rawBody).map { match ->
            val text = match.groupValues[1]
            val wOffset = match.groupValues[2].toLong() // 注意：QRC 单词时间通常是相对行的偏移量值，也可能是绝对值
            val wDur = match.groupValues[3].toLong()

            // 这里根据实际 QRC 规范处理：
            // 如果 wOffset 是从 0 开始的相对值，则 start = lineStart + wOffset
            // 绝大多数 QRC 单词时间戳是相对于整首歌的绝对时间，直接使用即可
            LyricWord(
                start = wOffset,
                end = wOffset + wDur,
                duration = wDur,
                text = text
            )
        }.toList()

        return LyricLine(
            start = lineStart,
            duration = lineDur,
            end = lineStart + lineDur,
            text = words.joinToString("") { it.text.orEmpty() },
            words = words
        )
    }
}