package io.ionic.libs.ionhealthfitnesslib.data.types
data class HealthQueryReturnType(
    val responseBlock: HealthAdvancedQueryResponseBlock? = null,
    val responseBlockList: List<HealthAdvancedQueryResponseBlock>? = null,
    val dataPointList: List<HealthDataPoint>
)