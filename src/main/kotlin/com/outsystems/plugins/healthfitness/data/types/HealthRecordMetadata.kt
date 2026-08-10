package com.outsystems.plugins.healthfitness.data.types

import com.google.gson.annotations.SerializedName

data class HealthRecordMetadata(
    @SerializedName("recordingMethod") val inputMethod: HealthRecordInputMethod,
    @SerializedName("originApp") val originAppPackageName: String,
    @SerializedName("device") val device: HealthRecordSourceDevice?
)
