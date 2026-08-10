package com.outsystems.plugins.healthfitness.data.variables

import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import com.outsystems.plugins.healthfitness.data.HealthEnumOperationType
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject.createDataPoint
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject.createHealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject.processHealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.types.HealthDataPoint
import com.outsystems.plugins.healthfitness.data.types.HealthQueryReturnType
import com.outsystems.plugins.healthfitness.data.types.HealthVariable
import com.outsystems.plugins.healthfitness.data.types.ValueRecordResult
import com.outsystems.plugins.healthfitness.helpers.toHealthRecordMetadata
import com.outsystems.plugins.healthfitness.utils.toLocalDateTime

object SubsetHealthForFitness {
    val stepsVariable = HealthVariable(
        StepsRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.SUM to StepsRecord.COUNT_TOTAL,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            //There is cases like steps when the aggregation returns directly the count value as float ref: HealthConnectDataManager processData -> valueMetric
            if (it is StepsRecord) {
                ValueRecordResult(
                    listOf(it.count.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.startTime.toLocalDateTime(),
                    it.endTime.toLocalDateTime()
                )
            } else {
                throw Exception("Error while reading StepsRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the count value of each step record
            val record = recordItem as StepsRecord
            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.startTime,
                endDate = record.endTime,
                getValue = getValue
            )
        })

    val caloriesBurnedRecord = HealthVariable(
        TotalCaloriesBurnedRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.SUM to TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null,
        ),
        getValue = {
            if (it is TotalCaloriesBurnedRecord) {
                //Executed in the rest of the cases
                ValueRecordResult(
                    listOf(it.energy.inKilocalories.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.startTime.toLocalDateTime(),
                    it.endTime.toLocalDateTime()
                )
            } else if (it is Energy) {
                //Executed when is SUM
                ValueRecordResult(listOf(it.inKilocalories.toFloat()), null)
            } else {
                throw Exception("Error while reading TotalCaloriesBurnedRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the calories value of each total calories record
            val record = recordItem as TotalCaloriesBurnedRecord
            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.startTime,
                endDate = record.endTime,
                getValue = getValue
            )
        }
    )

    val distanceVariable = HealthVariable(
        DistanceRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.SUM to DistanceRecord.DISTANCE_TOTAL,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is DistanceRecord) {
                ValueRecordResult(
                    listOf(it.distance.inMeters.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.startTime.toLocalDateTime(),
                    it.endTime.toLocalDateTime()
                )
            } else if (it is Length) {
                ValueRecordResult(listOf(it.inMeters.toFloat()), null)
            } else {
                throw Exception("Error while reading DistanceRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the distance in meters value of each distance record
            val record = recordItem as DistanceRecord
            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.startTime,
                endDate = record.endTime,
                getValue = getValue
            )
        }
    )


    //There is an actual issue with the API with the aggregation for this variable, commented in the code until updates and emulated with manual operation for now
    //https://support.google.com/googleplay/android-developer/thread/254881347/java-lang-illegalargumentexception-unsupported-aggregation-type-speedseries-speed-avg-health-connect?hl=en
    //Issue tracker https://issuetracker.google.com/issues/327273591
    val walkingSpeedVariable = HealthVariable(
        SpeedRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is SpeedRecord.Sample) {
                ValueRecordResult(listOf(it.speed.inMetersPerSecond.toFloat()), null)
            } else if (it is SpeedRecord) {
                ValueRecordResult(
                    it.samples.map { sample -> sample.speed.inMetersPerSecond.toFloat() },
                    it.metadata.toHealthRecordMetadata(),
                    it.startTime.toLocalDateTime(),
                    it.endTime.toLocalDateTime()
                    )
            } else {
                throw Exception("Error while reading SpeedRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the speed in kilometers per hour for each SpeedRecord; we need to iterate over each sample
            val record = recordItem as SpeedRecord
            val result = mutableListOf<HealthAdvancedQueryResponseBlock>()
            val datapointList = mutableListOf<HealthDataPoint>()

            //We need to iterate over each sample in order to get the value
            for (sample in record.samples) {
                val valueRecord = getValue(sample)

                val datapointListSerie = if (includeDataPoint) listOf(
                    createDataPoint(
                        record.startTime.toLocalDateTime(),
                        valueRecord.first()
                    )
                ) else listOf()
                datapointList.addAll(datapointListSerie)
                result.add(
                    createHealthAdvancedQueryResponseBlock(
                        position,
                        startDate = sample.time,
                        endDate = sample.time,
                        values = listOf(valueRecord.first()),
                        recordMetadata = listOf(recordItem.metadata.toHealthRecordMetadata())
                    )
                )
            }
            HealthQueryReturnType(null, result, datapointList)
        }
    )
}