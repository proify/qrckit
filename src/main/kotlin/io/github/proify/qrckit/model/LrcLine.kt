package io.github.proify.qrckit.model

data class LrcLine(
    override val start: Long = 0,
    override val end: Long = 0,
    override val duration: Long = 0,
    val text: String
) : TimeRange