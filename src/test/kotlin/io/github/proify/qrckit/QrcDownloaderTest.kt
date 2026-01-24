package io.github.proify.qrckit

import kotlin.test.Test

class QrcDownloaderTest {
    @Test
    fun testDownload() {
        val raw = QrcDownloader.downloadLyrics("269741123")
        val data = raw.lyricData

        data.translationData.lines.forEach { println(it) }
        data.richLyricLine.forEach { println(it) }
    }
}