package io.github.proify.qrckit.model

data class LrcData(
    val metaData: Map<String, String>,
    val lines: List<LrcLine>
)