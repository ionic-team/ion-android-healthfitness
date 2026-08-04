package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName

data class HealthRecordSourceDevice(
    @SerializedName("type") val type: Type,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("model") val model: String?
) {
    enum class Type {
        WATCH,
        PHONE,
        SCALE,
        RING,
        HEAD_MOUNTED,
        FITNESS_BAND,
        CHEST_STRAP,
        SMART_DISPLAY,
        UNKNOWN
    }
}
