package com.outsystems.plugins.healthfitness.data.types

import java.time.LocalDateTime

data class ValueRecordResult(
    val value: List<Float>,
    val metadata: HealthRecordMetadata?,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null
)
