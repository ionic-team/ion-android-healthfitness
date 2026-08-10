package com.outsystems.plugins.healthfitness.data.types
import com.google.gson.annotations.SerializedName
import com.outsystems.plugins.healthfitness.data.HealthEnumOperationType
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthRecord
import java.util.Date
enum class HealthEnumAdvancedQueryReturnType(val value : String) {
    ALL_DATA("ALL_DATA"),
    REMOVE_EMPTY_BLOCKS("REMOVE_EMPTY_BLOCKS")
}
enum class HealthEnumAdvancedQueryResultType(val value : String) {
    ALL_DATA("ALL_DATA"),
    RAW_DATA("RAW_DATA"),
    DATA_POINT("DATA_POINT")
}
data class HealthAdvancedQueryParameters(
    @SerializedName("Variable") var variable : HealthRecord,
    @SerializedName("StartDate") val startDate : Date,
    @SerializedName("EndDate") val endDate : Date,
    @SerializedName("TimeUnit") var timeUnit: HealthEnumTimeUnit = HealthEnumTimeUnit.DAY,
    @SerializedName("TimeUnitLength") var timeUnitLength : Int = 1,
    @SerializedName("OperationType") var operationType : HealthEnumOperationType = HealthEnumOperationType.RAW,
    @SerializedName("Limit") val limit : Int? = null,
    @SerializedName("PageSize") val pageSize : Int = 1000,
    @SerializedName("PageToken") val pageToken : String? = null,
    @SerializedName("AdvancedQueryReturnType") val returnType : HealthEnumAdvancedQueryReturnType = HealthEnumAdvancedQueryReturnType.ALL_DATA,
    @SerializedName("AdvancedQueryResultType") val resultType : HealthEnumAdvancedQueryResultType = HealthEnumAdvancedQueryResultType.ALL_DATA
)