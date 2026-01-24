package io.github.proify.qrckit.model

data class RichLyricLine(
    override val start: Long = 0,
    override val end: Long = 0,
    override val duration: Long = 0,
    val text: String? = null,
    val translation: String? = null,
    val roma: String? = null,
    val words: List<LyricWord> = emptyList()
) : TimeRange