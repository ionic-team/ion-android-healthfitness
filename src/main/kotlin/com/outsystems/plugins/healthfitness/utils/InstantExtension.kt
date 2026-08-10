package com.outsystems.plugins.healthfitness.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Instant.toLocalDateTime(): LocalDateTime =
    this.atZone(ZoneId.of("UTC")).toLocalDateTime()
