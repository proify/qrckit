/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.qrckit

import io.github.proify.qrckit.model.LrcData
import io.github.proify.qrckit.model.LrcLine
object LrcParser {
    // 更加通用的时间正则：支持 [mm:ss] [mm:ss.SS] [mm:ss.SSS]
    private val TIME_TAG_REGEX = Regex("""\[(\d+):(\d{1,2})(?:[.:](\d+))?]""")
    // 元数据正则：[key:value]
    private val META_TAG_REGEX = Regex("""\[(\w+)\s*:\s*(.*)]""")

    fun parseLrc(raw: String?): LrcData {
        if (raw.isNullOrBlank()) return LrcData(emptyMap(), emptyList())

        val tempEntries = mutableListOf<LrcLine>()
        val metaData = mutableMapOf<String, String>()

        raw.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || !trimmed.startsWith("[")) return@forEach

            // 1. 提取行内所有的时间标签
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
                // 2. 尝试匹配元数据
                META_TAG_REGEX.matchEntire(trimmed)?.let { match ->
                    metaData[match.groupValues[1]] = match.groupValues[2].trim()
                }
            }
        }

        // 3. 排序并计算持续时间
        val sortedLines = tempEntries.sortedBy { it.start }
            .zipWithNext { current, next ->
                current.copy(
                    end = next.start,
                    duration = next.start - current.start
                )
            }.toMutableList()

        // 补全最后一行
        tempEntries.lastOrNull()?.let { last ->
            val finalLine = if (sortedLines.isNotEmpty() && sortedLines.last().start == last.start) {
                // 如果 zipWithNext 已经处理了最后一行（通常不会），跳过
                null
            } else {
                last.copy(end = last.start + 5000, duration = 5000) // 默认最后一行 5s
            }
            finalLine?.let { sortedLines.add(it) }
        }

        return LrcData(metaData, sortedLines)
    }

    /**
     * 将解析到的分、秒、分数转换为毫秒
     */
    private fun parseTimeToMs(min: String, sec: String, frac: String?): Long {
        val m = min.toLongOrNull() ?: 0L
        val s = sec.toLongOrNull() ?: 0L

        // 处理分数部分 (ms/centiseconds)
        // .1 -> 100ms, .12 -> 120ms, .123 -> 123ms
        val ms = when {
            frac == null -> 0L
            frac.length == 1 -> frac.toLong() * 100
            frac.length == 2 -> frac.toLong() * 10
            frac.length >= 3 -> frac.substring(0, 3).toLong()
            else -> 0L
        }

        return m * 60000 + s * 1000 + ms
    }
}