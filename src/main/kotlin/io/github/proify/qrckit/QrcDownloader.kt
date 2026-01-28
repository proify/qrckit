/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

@file:Suppress("SpellCheckingInspection")

package io.github.proify.qrckit

import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

object QrcDownloader {
    private const val LYRIC_URL = "https://c.y.qq.com/qqmusic/fcgi-bin/lyric_download.fcg"
    private var USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    @Throws(Exception::class)
    fun downloadLyrics(
        musicId: String,
        userAgent: String = USER_AGENT
    ): LyricResponse {
        val params = mapOf(
            "version" to "15",
            "miniversion" to "100",
            "lrctype" to "4",
            "musicid" to musicId
        )
        val postData = params.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }

        val url = URI.create(LYRIC_URL).toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("Referer", "https://c.y.qq.com/")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        conn.outputStream.use { it.write(postData.toByteArray()) }

        val raw = conn.inputStream.bufferedReader().use { it.readText() }
        return LyricResponse(musicId, raw)
    }
}