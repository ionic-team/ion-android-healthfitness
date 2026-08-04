package io.ionic.libs.ionhealthfitnesslib.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


fun LocalDateTime.toEpoch(): Long {
    // get offset of device timezone to return correct timestamp
    val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
    return this.toEpochSecond(offset)
}