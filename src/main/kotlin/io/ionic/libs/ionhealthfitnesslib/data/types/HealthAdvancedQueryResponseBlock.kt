package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName

data class HealthAdvancedQueryResponseBlock(
    @SerializedName("block") val block : Int = 0,
    @SerializedName("startDate") var startDate : Long,
    @SerializedName("endDate") var endDate : Long,
    @SerializedName("values") val values : List<Float>,
    @SerializedName("additionalData") var additionalData : String = "",
    @SerializedName("recordMetadataList") val recordMetadata: List<HealthRecordMetadata> = emptyList()
)