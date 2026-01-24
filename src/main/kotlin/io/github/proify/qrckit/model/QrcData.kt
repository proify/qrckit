package io.github.proify.qrckit.model

data class QrcData(
    val metaData: Map<String, String>,
    val lines: List<LyricLine>
)