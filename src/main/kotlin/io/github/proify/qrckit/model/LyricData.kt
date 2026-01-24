package io.github.proify.qrckit.model

import io.github.proify.qrckit.LrcParser
import io.github.proify.qrckit.QrcParser
import kotlin.math.abs

data class LyricData(
    val lyrics: String? = null,
    val translation: String? = null,
    val roma: String? = null
) {
    val lyricData: List<QrcData> by lazy { QrcParser.parseXML(lyrics) }
    val translationData: LrcData by lazy { LrcParser.parseLrc(translation) }
    val romaData: LrcData by lazy { LrcParser.parseLrc(roma) }

    val richLyricLine: List<RichLyricLine> by lazy {

        lyricData.first().lines.map { line ->
            val matchedTrans = translationData.lines.firstOrNull { abs(it.start - line.start) < 100 }
            val matchedRoma = romaData.lines.firstOrNull { abs(it.start - line.start) < 100 }

            RichLyricLine(
                start = line.start,
                end = line.end,
                duration = line.duration,
                text = line.text,
                translation = matchedTrans?.text,
                roma = matchedRoma?.text,
                words = line.words
            )
        }
    }
}