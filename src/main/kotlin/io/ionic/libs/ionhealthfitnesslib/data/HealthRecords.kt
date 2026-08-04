package io.ionic.libs.ionhealthfitnesslib.data

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.google.gson.Gson
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponseBlock
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthDataPoint
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthQueryReturnType
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthRecordMetadata
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthVariable
import io.ionic.libs.ionhealthfitnesslib.data.variables.SubsetHealthForFitness
import io.ionic.libs.ionhealthfitnesslib.data.variables.SubsetHealthForHealth
import io.ionic.libs.ionhealthfitnesslib.data.variables.SubsetHealthForProfile
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.toHealthRecordMetadata
import io.ionic.libs.ionhealthfitnesslib.utils.toLocalDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

enum class HealthEnumTimeUnit(val value : Pair<String, TimeUnit>) {
    MINUTE(Pair("MINUTE", TimeUnit.MINUTES)),
    HOUR(Pair("HOUR", TimeUnit.HOURS)),
    DAY(Pair("DAY", TimeUnit.DAYS)),
    WEEK(Pair("WEEK", TimeUnit.DAYS)),
    MONTH(Pair("MONTH", TimeUnit.DAYS)),
    YEAR(Pair("YEAR", TimeUnit.DAYS))
}

enum class HealthEnumAccessType {
    READ,
    WRITE,
    READWRITE,
}
enum class HealthRecord(val record: KClass<out Record>) {
    BASAL_METABOLIC_RATE(BasalMetabolicRateRecord::class),
    BLOOD_GLUCOSE(BloodGlucoseRecord::class),
    BLOOD_PRESSURE(BloodPressureRecord::class),
    BODY_FAT_PERCENTAGE(BodyFatRecord::class),
    DISTANCE(DistanceRecord::class),
    HEART_RATE(HeartRateRecord::class),
    HEIGHT(HeightRecord::class),
    SLEEP(SleepSessionRecord::class),
    WALKING_SPEED(SpeedRecord::class),
    CALORIES_BURNED(TotalCaloriesBurnedRecord::class),
    STEPS(StepsRecord::class),
    WEIGHT(WeightRecord::class),
    OXYGEN_SATURATION(OxygenSaturationRecord::class),
    BODY_TEMPERATURE(BodyTemperatureRecord::class);

    fun getPermissions(): Pair<String, String> {
        return Pair(
            HealthPermission.getReadPermission(record),
            HealthPermission.getWritePermission(record)
        )
    }

    fun getVariableFieldValue(recordInstance: Record): List<Float> {
        return when(this) {
            BASAL_METABOLIC_RATE -> listOf((recordInstance as BasalMetabolicRateRecord).basalMetabolicRate.inKilocaloriesPerDay.toFloat())
            BLOOD_GLUCOSE -> listOf((recordInstance as BloodGlucoseRecord).level.inMillimolesPerLiter.toFloat())
            BLOOD_PRESSURE -> {
                val bloodPressure = recordInstance as BloodPressureRecord
                listOf(
                    bloodPressure.systolic.inMillimetersOfMercury.toFloat(),
                    bloodPressure.diastolic.inMillimetersOfMercury.toFloat()
                )
            }
            BODY_FAT_PERCENTAGE -> listOf((recordInstance as BodyFatRecord).percentage.value.toFloat())
            DISTANCE -> listOf((recordInstance as DistanceRecord).distance.inMeters.toFloat())
            HEART_RATE -> listOf((recordInstance as HeartRateRecord).samples.first().beatsPerMinute.toFloat())
            HEIGHT -> listOf(((recordInstance as HeightRecord).height.inMeters * 100).toFloat()) // centimeters
            SLEEP -> {
                val sleepRecord = (recordInstance as SleepSessionRecord)
                val toReturn = sleepRecord.endTime.epochSecond - sleepRecord.startTime.epochSecond
                listOf((toReturn/60).toFloat()) // duration should be in minutes
            }
            WALKING_SPEED -> listOf((recordInstance as SpeedRecord).samples.first().speed.inMetersPerSecond.toFloat())
            CALORIES_BURNED -> listOf((recordInstance as TotalCaloriesBurnedRecord).energy.inKilocalories.toFloat())
            STEPS -> listOf((recordInstance as StepsRecord).count.toFloat())
            WEIGHT -> listOf((recordInstance as WeightRecord).weight.inKilograms.toFloat())
            OXYGEN_SATURATION -> listOf((recordInstance as OxygenSaturationRecord).percentage.value.toFloat())
            BODY_TEMPERATURE -> listOf((recordInstance as BodyTemperatureRecord).temperature.inCelsius.toFloat())
        }
    }

    fun getWriterMethod(healthConnectHelper: HealthConnectHelperInterface): (suspend (Double, Metadata, HealthConnectClient) -> Unit)? {
        return when(this){
            WEIGHT -> healthConnectHelper::writeWeightRecord
            HEIGHT -> healthConnectHelper::writeHeightRecord
            BODY_FAT_PERCENTAGE -> healthConnectHelper::writeBodyFatRecord
            BASAL_METABOLIC_RATE -> healthConnectHelper::writeBasalMetabolicRateRecord
            else -> null
        }
    }

}

enum class HealthEnumOperationType {
    SUM,
    MIN,
    MAX,
    AVERAGE,
    RAW
}

object HealthRecordsObject {
    private val healthDataPointFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    val gson: Gson by lazy { Gson() }

    fun createHealthAdvancedQueryResponseBlock(
        position: Int,
        startDate: Instant,
        endDate: Instant,
        values: List<Float>,
        additionalData: String = "",
        recordMetadata: List<HealthRecordMetadata>
    ): HealthAdvancedQueryResponseBlock {
        return HealthAdvancedQueryResponseBlock(
            block = position,
            startDate = startDate.toLocalDateTime().toEpochSecond(Constants.ZONE_OFFSET),
            endDate = endDate.toLocalDateTime().toEpochSecond(Constants.ZONE_OFFSET),
            values = values,
            additionalData = gson.toJson(additionalData),
            recordMetadata = recordMetadata
        )
    }

    fun createHealthQueryReturnType(
        position: Int,
        startDate: Instant,
        endDate: Instant,
        values: List<Float>,
        recordMetadata: List<HealthRecordMetadata>,
        dataPointList: List<HealthDataPoint>
    ): HealthQueryReturnType {
        return HealthQueryReturnType(
            responseBlock = createHealthAdvancedQueryResponseBlock(
                position = position,
                startDate = startDate,
                endDate = endDate,
                values = values,
                recordMetadata = recordMetadata
            ), dataPointList =  dataPointList
        )
    }

    fun processHealthAdvancedQueryResponseBlock(
        position: Int,
        includeDataPoint: Boolean,
        record: Record,
        startDate: Instant,
        endDate: Instant,
        getValue: (Any) -> List<Float>
    ): HealthQueryReturnType {
        val value = getValue(record)
        val metadata = listOf(record.metadata.toHealthRecordMetadata())
        val datapointList = if (includeDataPoint) listOf(
            createDataPoint(
                startDate.toLocalDateTime(),
                value.first()
            )
        ) else listOf()
        return createHealthQueryReturnType(position, startDate, endDate, value, metadata, datapointList)
    }

    fun createDataPoint(
        startTimeData: LocalDateTime,
        valueMetric: Float
    ): HealthDataPoint {
        val label = healthDataPointFormatter.format(
            startTimeData
        )
        return HealthDataPoint(label, valueMetric.toString())
    }

    val healthConnectVariables: Map<String, Lazy<HealthVariable>> by lazy {
        mapOf(
            HealthRecord.HEART_RATE.name to lazy {
                SubsetHealthForHealth.heartRateVariable
            },
            HealthRecord.STEPS.name to lazy {
                SubsetHealthForFitness.stepsVariable
            },
            HealthRecord.WEIGHT.name to lazy {
                SubsetHealthForProfile.weightVariable
            },
            HealthRecord.HEIGHT.name to lazy {
                SubsetHealthForProfile.heightVariable
            },
            HealthRecord.CALORIES_BURNED.name to lazy {
                SubsetHealthForFitness.caloriesBurnedRecord
            },
            HealthRecord.SLEEP.name to lazy {
                SubsetHealthForHealth.sleepVariable
            },
            HealthRecord.BLOOD_PRESSURE.name to lazy {
                SubsetHealthForHealth.bloodPressureVariable
            },
            HealthRecord.BLOOD_GLUCOSE.name to lazy {
                SubsetHealthForHealth.bloodGlucoseVariable
            },
            HealthRecord.BODY_FAT_PERCENTAGE.name to lazy {
                SubsetHealthForProfile.bodyFatPercentage
            },
            HealthRecord.BASAL_METABOLIC_RATE.name to lazy {
                SubsetHealthForProfile.basalMetabolicRateVariable
            },
            HealthRecord.WALKING_SPEED.name to lazy {
                SubsetHealthForFitness.walkingSpeedVariable
            },
            HealthRecord.DISTANCE.name to lazy {
                SubsetHealthForFitness.distanceVariable
            },
            HealthRecord.OXYGEN_SATURATION.name to lazy {
                SubsetHealthForHealth.oxygenSaturationVariable
            },
            HealthRecord.BODY_TEMPERATURE.name to lazy {
                SubsetHealthForHealth.bodyTemperatureVariable
            }
        )
    }
}


