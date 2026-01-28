package io.github.proify.qrckit

import io.github.proify.qrckit.model.LyricLine
import io.github.proify.qrckit.model.LyricWord
import io.github.proify.qrckit.model.QrcData

object QrcParser {

    // 1. 行标签正则：匹配 [123,456]
    private val linePattern = Regex("""\[(\d+)\s*,\s*(\d+)]""")

    // 2. 单词正则（核心修复）：
    // (.*?) -> 非贪婪匹配，允许文本中包含 "("，直到遇到标准的时间戳格式
    // \((\d+)\s*,\s*(\d+)\) -> 严格匹配 (开始时间,持续时间)
    private val wordPattern = Regex("""(.*?)\((\d+)\s*,\s*(\d+)\)""")

    // 3. 元数据正则：匹配 [ti:标题]
    private val metaPattern = Regex("""\[(\w+)\s*:\s*([^]]*)]""")

    // 4. XML 属性提取正则
    private val lyricContentRegex = Regex("""LyricContent\s*=\s*"([^"]+)"""")

    /**
     * 入口方法：解析包含 QRC 的 XML 字符串
     */
    fun parseXML(xml: String?): List<QrcData> {
        if (xml.isNullOrBlank()) return emptyList()

        return lyricContentRegex.findAll(xml).mapNotNull { match ->
            // 步骤 A: 提取原始 XML 属性值
            val rawContent = match.groupValues[1]

            // 步骤 B: 关键修复 - 反转义 XML 字符 (如 &apos; -> ')
            val cleanContent = unescapeXml(rawContent)

            if (cleanContent.isBlank()) null else {
                val (meta, lines) = parseLyric(cleanContent)
                QrcData(meta, lines)
            }
        }.toList()
    }

    /**
     * 核心逻辑：解析纯 QRC 文本内容
     */
    fun parseLyric(content: String): Pair<Map<String, String>, List<LyricLine>> {
        val metaData = mutableMapOf<String, String>()
        val lines = mutableListOf<LyricLine>()

        // 1. 提取元数据
        metaPattern.findAll(content).forEach {
            metaData[it.groupValues[1]] = it.groupValues[2].trim()
        }

        // 2. 提取歌词行 (支持换行格式和单行压缩格式)
        val lineMatches = linePattern.findAll(content).toList()

        for (i in lineMatches.indices) {
            val currentMatch = lineMatches[i]
            // 安全转换，防止崩溃
            val lineStart = currentMatch.groupValues[1].toLongOrNull() ?: 0L
            val lineDur = currentMatch.groupValues[2].toLongOrNull() ?: 0L

            // 截取当前行标签结束 -> 下一行标签开始 之间的内容
            val bodyStart = currentMatch.range.last + 1
            val bodyEnd = if (i + 1 < lineMatches.size) {
                lineMatches[i + 1].range.first
            } else {
                content.length
            }

            if (bodyStart < bodyEnd) {
                val lineBody = content.substring(bodyStart, bodyEnd)
                // 仅当内容有效时解析，去除纯空白行干扰
                if (lineBody.isNotBlank()) {
                    lines.add(parseLineBody(lineStart, lineDur, lineBody))
                }
            }
        }

        return metaData to lines.sortedBy { it.start }
    }

    /**
     * 单词解析：处理每一行的具体文本和字时间
     */
    private fun parseLineBody(lineStart: Long, lineDur: Long, rawBody: String): LyricLine {
        // 去除可能的首尾空白字符，但保留单词间的空格
        val trimmedBody = rawBody.trim('\n', '\r')

        val words = wordPattern.findAll(trimmedBody).mapNotNull { match ->
            val text = match.groupValues[1]
            val wStart = match.groupValues[2].toLongOrNull() ?: 0L
            val wDur = match.groupValues[3].toLongOrNull() ?: 0L

            // 过滤掉极其异常的数据
            if (wDur < 0) null else {
                LyricWord(
                    start = wStart, // QRC 绝大多数情况是绝对时间
                    end = wStart + wDur,
                    duration = wDur,
                    text = text
                )
            }
        }.toList()

        // 如果该行没有匹配到任何单词（可能是空行或者纯文本行），做一个兜底处理
        val finalText = if (words.isEmpty()) {
            // 移除所有 QRC 标签后的纯文本，用于显示
            trimmedBody.replace(Regex("""\(\d+,\d+\)"""), "")
        } else {
            words.joinToString("") { it.text ?: "" }
        }

        return LyricLine(
            start = lineStart,
            duration = lineDur,
            end = lineStart + lineDur,
            text = finalText,
            words = words
        )
    }

    /**
     * 工具方法：简单的 XML 实体反转义
     * 解决 &apos; (单引号), &quot; (双引号) 等问题
     */
    private fun unescapeXml(source: String): String {
        return source
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&apos;", "'")
            .replace("&quot;", "\"")
            // 处理 QRC 中可能出现的换行转义
            .replace("&#10;", "\n")
            .replace("&#13;", "\r")
    }
}