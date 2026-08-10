package com.outsystems.plugins.healthfitness.data.types
import com.google.gson.annotations.SerializedName
data class HealthDataPoint(
    @SerializedName("Label") val label: String,
    @SerializedName("Value") val value: String
)