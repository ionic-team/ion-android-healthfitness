package io.ionic.libs.ionhealthfitnesslib.data.types
import com.google.gson.annotations.SerializedName
data class HealthDataPoint(
    @SerializedName("Label") val label: String,
    @SerializedName("Value") val value: String
)