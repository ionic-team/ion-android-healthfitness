package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName

data class HealthLastRecordResponse(
    @SerializedName("result") val result: List<Float> = emptyList(),
    @SerializedName("recordMetadata") val metadata: HealthRecordMetadata? = null
)
