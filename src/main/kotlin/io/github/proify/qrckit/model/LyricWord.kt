package io.github.proify.qrckit.model

data class LyricWord(
    override val start: Long = 0,
    override val end: Long,
    override val duration: Long,
    val text: String? = null
) : TimeRange