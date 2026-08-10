package com.outsystems.plugins.healthfitness.data.variables

import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import com.outsystems.plugins.healthfitness.data.HealthEnumOperationType
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject.processHealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.types.HealthVariable
import com.outsystems.plugins.healthfitness.data.types.ValueRecordResult
import com.outsystems.plugins.healthfitness.helpers.toHealthRecordMetadata
import com.outsystems.plugins.healthfitness.utils.toLocalDateTime

object SubsetHealthForProfile {
    val weightVariable = HealthVariable(
        WeightRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to WeightRecord.WEIGHT_AVG,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is WeightRecord) {
                ValueRecordResult(
                    listOf(it.weight.inKilograms.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            } else if (it is Mass) {
                ValueRecordResult(listOf(it.inKilograms.toFloat()), metadata = null)
            } else {
                throw Exception("Error while reading WeightRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the weight in kilograms for the WeightRecord
            val record = recordItem as WeightRecord

            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.time,
                endDate = record.time,
                getValue = getValue
            )
        }
    )

    val heightVariable = HealthVariable(
        HeightRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to HeightRecord.HEIGHT_AVG,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is HeightRecord) {
                //We need to get the height and convert to cm
                ValueRecordResult(
                    listOf(it.height.inMeters.toFloat()*100),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime()
                )
            } else if (it is Length) {
                //We need to get the height and convert to cm
                ValueRecordResult(listOf(it.inMeters.toFloat()*100), null)
            } else {
                throw Exception("Error while reading HeightRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the height in inMeters for the HeightRecord
            val record = recordItem as HeightRecord

            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.time,
                endDate = record.time,
                getValue = getValue
            )
        }
    )

    val bodyFatPercentage = HealthVariable(
        BodyFatRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is BodyFatRecord) {
                ValueRecordResult(
                    listOf(it.percentage.value.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            } else {
                throw Exception("Error while reading BodyFatRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the percentage value of the BodyFatRecord
            val record = recordItem as BodyFatRecord

            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.time,
                endDate = record.time,
                getValue = getValue
            )
        }
    )

    val basalMetabolicRateVariable = HealthVariable(
        BasalMetabolicRateRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.SUM to BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is BasalMetabolicRateRecord) {
                ValueRecordResult(
                    listOf(it.basalMetabolicRate.inKilocaloriesPerDay.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            } else if (it is Energy) {
                ValueRecordResult(listOf(it.inKilocalories.toFloat()), null)
            } else {
                throw Exception("Error while reading BasalMetabolicRateRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the basal metabolic rate in kilocalories per day value of the BodyFatRecord.
            val record = recordItem as BasalMetabolicRateRecord

            processHealthAdvancedQueryResponseBlock(
                position = position,
                includeDataPoint = includeDataPoint,
                record = record,
                startDate = record.time,
                endDate = record.time,
                getValue = getValue
            )
        }
    )
}
