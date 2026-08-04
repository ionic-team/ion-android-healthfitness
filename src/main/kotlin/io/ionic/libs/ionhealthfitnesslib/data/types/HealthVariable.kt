package io.ionic.libs.ionhealthfitnesslib.data.types

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.Record
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumOperationType
import kotlin.reflect.KClass

data class HealthVariable(
    /*This is the param that will represent the record type.*/
    val className: KClass<out Record>,
    /*The structure of this param is a Map as key, the HealthEnumOperationType that represent the variable and a AggregateMetric that is allowed to be used with this variable and metric.
    If the value is null means that the operation is allowed by the HealthConnect Plugin but there is no representation in the HealthConnect SDK, meaning this that a manual
    operation should be performed.*/
    var allowedOperations: Map<HealthEnumOperationType, AggregateMetric<*>?>,
    /*This is the function that will be used to get the value of the variable.Based on the record type.*/
    val getValue: (Any) -> ValueRecordResult,
    /*This is the function that will be used to process the data from the record and return a HealthQueryReturnType. This function should be implemented in each case because
    there is no possible way to get a common object with access to starTime,etc. Staring with API34 we could use InstantRecord but for lower versions is not available.*/
    val mappingFunction: (Int, Record, (Any) -> List<Float>, Boolean) -> HealthQueryReturnType
)