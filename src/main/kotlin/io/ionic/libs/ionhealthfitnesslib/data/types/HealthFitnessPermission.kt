package io.ionic.libs.ionhealthfitnesslib.data.types

import com.google.gson.annotations.SerializedName
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumAccessType
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord

data class HealthFitnessPermission(
    @SerializedName("Variable") val variable : HealthRecord,
    @SerializedName("AccessType") val accessType : HealthEnumAccessType
)