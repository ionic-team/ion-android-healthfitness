package io.ionic.libs.ionhealthfitnesslib.data.types
import com.google.gson.annotations.SerializedName
data class HealthAdditionalData(
    @SerializedName("Type") var type : String,
    @SerializedName("Value") val value : String
)