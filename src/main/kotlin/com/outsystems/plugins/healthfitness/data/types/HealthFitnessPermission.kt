package com.outsystems.plugins.healthfitness.data.types

import com.google.gson.annotations.SerializedName
import com.outsystems.plugins.healthfitness.data.HealthEnumAccessType
import com.outsystems.plugins.healthfitness.data.HealthRecord

data class HealthFitnessPermission(
    @SerializedName("Variable") val variable : HealthRecord,
    @SerializedName("AccessType") val accessType : HealthEnumAccessType
)