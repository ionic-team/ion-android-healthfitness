package io.ionic.libs.ionhealthfitnesslib.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Instant.toLocalDateTime(): LocalDateTime =
    this.atZone(ZoneId.of("UTC")).toLocalDateTime()
