/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.qrckit.model

data class LyricWord(
    override val start: Long = 0,
    override val end: Long,
    override val duration: Long,
    val text: String? = null
) : TimeRange