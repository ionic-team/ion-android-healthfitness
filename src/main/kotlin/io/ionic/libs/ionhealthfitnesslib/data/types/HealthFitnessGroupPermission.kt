package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName

data class HealthFitnessGroupPermission (
    @SerializedName("IsActive") val isActive : Boolean = false,
    @SerializedName("AccessType") val accessType : String = ""
)
