package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName

data class HealthAdvancedQueryResponse(
    @SerializedName("results") val results : MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf(),
    @SerializedName("resultDataPoints") val resultDataPoints: MutableList<HealthDataPoint> = mutableListOf(),
    @SerializedName("pageToken") val pageToken : String? = null,
)