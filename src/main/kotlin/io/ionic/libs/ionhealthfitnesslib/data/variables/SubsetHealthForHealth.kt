package io.ionic.libs.ionhealthfitnesslib.data.variables

import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.units.Pressure
import com.google.gson.Gson
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumOperationType
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject.createHealthAdvancedQueryResponseBlock
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject.createHealthQueryReturnType
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject.processHealthAdvancedQueryResponseBlock
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdditionalData
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponseBlock
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthDataPoint
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthQueryReturnType
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthVariable
import io.ionic.libs.ionhealthfitnesslib.data.types.ValueRecordResult
import io.ionic.libs.ionhealthfitnesslib.helpers.toHealthRecordMetadata
import io.ionic.libs.ionhealthfitnesslib.utils.toLocalDateTime
import java.time.Duration

enum class HealthSleepStage(val id: Int, val stringValue: String) {
    STAGE_TYPE_UNKNOWN(0, "Unknown"),
    STAGE_TYPE_AWAKE(1, "Awake"),
    STAGE_TYPE_SLEEPING(2, "Sleep"),
    STAGE_TYPE_OUT_OF_BED(3, "OutOfBed"),
    STAGE_TYPE_LIGHT(4, "LightSleep"),
    STAGE_TYPE_DEEP(5, "DeepSleep"),
    STAGE_TYPE_REM(6, "REMSleep"),
    STAGE_TYPE_AWAKE_IN_BED(7, "AwakeInBed");

    companion object {
        fun getStringValueById(id: Int): String {
            return enumValues<HealthSleepStage>().find { it.id == id }?.stringValue
                ?: STAGE_TYPE_UNKNOWN.stringValue
        }
    }
}

object SubsetHealthForHealth {
    val heartRateVariable = HealthVariable(
        HeartRateRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to HeartRateRecord.BPM_AVG,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is HeartRateRecord.Sample) {
                ValueRecordResult(listOf(it.beatsPerMinute.toFloat()), null)
            } else if(it is HeartRateRecord){
                ValueRecordResult(
                    it.samples.map { sample -> sample.beatsPerMinute.toFloat() },
                    it.metadata.toHealthRecordMetadata(),
                    it.startTime.toLocalDateTime(),
                    it.endTime.toLocalDateTime()
                )
            }else {
                throw Exception("Error while reading HeartRateRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to iterate over the list of samples to get the values
            val record = recordItem as HeartRateRecord
            val listOfValues = mutableListOf<Float>()
            val datapointList = mutableListOf<HealthDataPoint>()
            //List of samples with the values, to get the beatsPerMinute operation in the getValue function
            for (item in record.samples) {
                val valueRecord = getValue(item)
                listOfValues.add(valueRecord.first())

                val datapointListSerie = if (includeDataPoint) listOf(
                    HealthRecordsObject.createDataPoint(
                        record.startTime.toLocalDateTime(),
                        valueRecord.first()
                    )
                ) else listOf()
                datapointList.addAll(datapointListSerie)
            }
            createHealthQueryReturnType(
                position = position,
                startDate = record.startTime,
                endDate = record.endTime,
                values = listOfValues,
                dataPointList = datapointList,
                recordMetadata = listOf(recordItem.metadata.toHealthRecordMetadata())
            )
        }
    )

    //If there is no stages in the sleep session, there is no valid data to analyze
    val sleepVariable =
        HealthVariable(
            SleepSessionRecord::class,
            mapOf(
                HealthEnumOperationType.RAW to null,
                HealthEnumOperationType.AVERAGE to null,
                HealthEnumOperationType.SUM to SleepSessionRecord.SLEEP_DURATION_TOTAL,
                HealthEnumOperationType.MAX to null,
                HealthEnumOperationType.MIN to null
            ),
            getValue = {
                if (it is SleepSessionRecord.Stage) {
                    ValueRecordResult(
                        listOf(it.stage.toFloat()),
                        null,
                        it.startTime.toLocalDateTime(),
                        it.endTime.toLocalDateTime()
                    )
                } else if (it is SleepSessionRecord) {
                    if (it.stages.isEmpty()) {
                        ValueRecordResult(
                            listOf(0.0f),
                            it.metadata.toHealthRecordMetadata(),
                            it.startTime.toLocalDateTime(),
                            it.endTime.toLocalDateTime()
                        )
                    } else {
                        ValueRecordResult(
                            it.stages.map { stage -> stage.stage.toFloat() },
                            it.metadata.toHealthRecordMetadata(),
                            it.startTime.toLocalDateTime(),
                            it.endTime.toLocalDateTime()
                        )
                    }
                } else if (it is Duration) {
                    ValueRecordResult(listOf(it.toMinutes().toFloat()), null)
                } else {
                    throw Exception("Error while reading SleepSessionRecord in getValue call")
                }
            },
            { _, recordItem, getValue, includeDataPoint ->
                //We need to iterate over the list of stages to get the values
                val gson: Gson by lazy { Gson() }
                val record = recordItem as SleepSessionRecord
                val result = mutableListOf<HealthAdvancedQueryResponseBlock>()

                val datapointList = mutableListOf<HealthDataPoint>()
                //Each stage will be added as AdditionalData
                for ((index, stage) in record.stages.withIndex()) {
                    val sleepStage = HealthSleepStage.getStringValueById(stage.stage)
                    val additionalData =
                        HealthAdditionalData(sleepStage, stage.stage.toString())

                    val valueRecord = getValue(stage)

                    val datapointListSerie = if (includeDataPoint) listOf(
                        HealthRecordsObject.createDataPoint(
                            record.startTime.toLocalDateTime(),
                            valueRecord.first()
                        )
                    ) else listOf()
                    datapointList.addAll(datapointListSerie)

                    result.add(
                        createHealthAdvancedQueryResponseBlock(
                            position = index,
                            startDate = stage.startTime,
                            endDate = stage.endTime,
                            values = listOf(valueRecord.first()),
                            additionalData = gson.toJson(additionalData),
                            recordMetadata = listOf(recordItem.metadata.toHealthRecordMetadata())
                        )
                    )
                }
                HealthQueryReturnType(null, result, datapointList)
            }
        )

    val bloodPressureVariable = HealthVariable(
        BloodPressureRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.MIN to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.AVERAGE to null,
        ),
        getValue = {
            if (it is Pressure) {
                ValueRecordResult(listOf(it.inMillimetersOfMercury.toFloat()), null)
            } else if (it is BloodPressureRecord) {
                ValueRecordResult(
                    listOf(
                        it.systolic.inMillimetersOfMercury.toFloat(),
                        it.diastolic.inMillimetersOfMercury.toFloat()
                    ),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                    )
            } else {
                throw Exception("Error while reading Pressure in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            /*We need to get the systolic and diastolic values;
            the datapoint will show only the systolic,
            but in the raw data, the systolic and diastolic are returned in the 'values' field as a list,
            where the first value is the systolic and the second value is the diastolic.*/
            val record = recordItem as BloodPressureRecord

            val valueSystolic = getValue(record.systolic)
            val valueDiastolic = getValue(record.diastolic)

            val datapointListSerie = if (includeDataPoint) listOf(
                HealthRecordsObject.createDataPoint(
                    record.time.toLocalDateTime(),
                    valueSystolic.first()
                )
            ) else listOf()

            createHealthQueryReturnType(
                position,
                startDate = record.time,
                endDate = record.time,
                values = listOf(valueSystolic.first(), valueDiastolic.first()),
                dataPointList = datapointListSerie,
                recordMetadata = listOf(recordItem.metadata.toHealthRecordMetadata())
            )
        }
    )

    val bloodGlucoseVariable = HealthVariable(
        BloodGlucoseRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if (it is BloodGlucoseRecord) {
                ValueRecordResult(
                    listOf(it.level.inMillimolesPerLiter.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            } else {
                throw Exception("Error while reading BloodGlucoseRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            //We need to get the blood glucose value in millimoles per liter.
            val record = recordItem as BloodGlucoseRecord
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

    val oxygenSaturationVariable = HealthVariable(
        OxygenSaturationRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if(it is OxygenSaturationRecord) {
                ValueRecordResult(
                    listOf(it.percentage.value.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            }
            else {
                throw Exception("Error while reading OxygenSaturationRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            val record = recordItem as OxygenSaturationRecord
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

    val bodyTemperatureVariable = HealthVariable(
        BodyTemperatureRecord::class,
        mapOf(
            HealthEnumOperationType.RAW to null,
            HealthEnumOperationType.AVERAGE to null,
            HealthEnumOperationType.MAX to null,
            HealthEnumOperationType.MIN to null
        ),
        getValue = {
            if(it is BodyTemperatureRecord) {
                ValueRecordResult(
                    listOf(it.temperature.inCelsius.toFloat()),
                    it.metadata.toHealthRecordMetadata(),
                    it.time.toLocalDateTime(),
                    it.time.toLocalDateTime()
                )
            }
            else {
                throw Exception("Error while reading BodyTemperatureRecord in getValue call")
            }
        },
        { position, recordItem, getValue, includeDataPoint ->
            val record = recordItem as BodyTemperatureRecord
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