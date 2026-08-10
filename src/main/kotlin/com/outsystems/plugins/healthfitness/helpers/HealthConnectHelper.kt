package com.outsystems.plugins.healthfitness.helpers

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Power
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.utils.HealthConnectSharedState
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import kotlin.reflect.KClass

class HealthConnectHelper: HealthConnectHelperInterface {
    override fun getSdkStatus(context: Context): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    override fun getOrCreateClient(context: Context): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }

    override suspend fun readLastRecord(
        healthConnectClient: HealthConnectClient,
        healthRecordName: HealthRecord
    ):  Record? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = healthRecordName.record,
                timeRangeFilter = TimeRangeFilter.before(
                    Instant.now()
                ),
                ascendingOrder = false,
            ))
        if (response.records.isEmpty()) {
            return null
        }

        return response.records.first()
    }

    override suspend fun checkPermissions(
        healthConnectClient: HealthConnectClient,
        permissions: Set<String>
    ): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    override suspend fun writeWeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        healthConnectClient.insertRecords(
            listOf(
                WeightRecord(
                    time = Instant.now(),
                    zoneOffset = null,
                    weight = Mass.kilograms(value),
                    metadata = metadata
                )
            )
        )
    }

    override suspend fun writeHeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        healthConnectClient.insertRecords(
            listOf(
                HeightRecord(
                    time = Instant.now(),
                    zoneOffset = null,
                    height = Length.meters(value/100), // height always comes in cm
                    metadata = metadata
                )
            )
        )
    }

    override suspend fun writeBodyFatRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        healthConnectClient.insertRecords(
            listOf(
                BodyFatRecord(
                    time = Instant.now(),
                    zoneOffset = null,
                    percentage = Percentage(value),
                    metadata = metadata
                )
            )
        )
    }

    override suspend fun writeBasalMetabolicRateRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        healthConnectClient.insertRecords(
            listOf(
                BasalMetabolicRateRecord(
                    time = Instant.now(),
                    zoneOffset = null,
                    basalMetabolicRate = Power.kilocaloriesPerDay(value),
                    metadata = metadata
                )
            )
        )
    }

    override suspend fun readRecords(
        className: KClass<out Record>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int,
        pageToken: String?,
        context: Context
    ): ReadRecordsResponse<out Record> {
        val connectClient = getHealthConnectClient(context)
        val adjustedPageSize = if(pageSize <= 0) 1000 else pageSize
        return connectClient.let {
            val request = ReadRecordsRequest(
                className,
                timeRangeFilter = TimeRangeFilter.between(startDate, endDate),
                pageSize = adjustedPageSize,
                pageToken = pageToken
            )
            it.readRecords(request)
        }
    }

    override suspend fun groupByPeriod(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByPeriod> {
        val connectClient = getHealthConnectClient(context)
        return connectClient.let {
            val periodAggregate = when (period) {
                HealthEnumTimeUnit.WEEK -> Period.ofWeeks(timeUnitLength)
                HealthEnumTimeUnit.MONTH -> Period.ofMonths(timeUnitLength)
                HealthEnumTimeUnit.YEAR -> Period.ofYears(timeUnitLength)
                else -> Period.ofDays(timeUnitLength)
            }
            val aggregatePeriod = AggregateGroupByPeriodRequest(
                setOf(operationToExecute),
                TimeRangeFilter.between(startDate, endDate),
                periodAggregate
            )
            it.aggregateGroupByPeriod(aggregatePeriod)
        }
    }

    override suspend fun groupByDuration(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByDuration> {
        val connectClient = getHealthConnectClient(context)
        connectClient.let {
            val durationAggregate = when (period) {
                HealthEnumTimeUnit.MINUTE -> Duration.ofMinutes(timeUnitLength.toLong())
                else -> Duration.ofHours(timeUnitLength.toLong()) // Default case, consider refining based on actual defaults
            }
            val aggregateDuration = AggregateGroupByDurationRequest(
                setOf(operationToExecute),
                timeRangeFilter = TimeRangeFilter.between(
                    startDate.toInstant(ZoneOffset.UTC),
                    endDate.toInstant(ZoneOffset.UTC)
                ),
                timeRangeSlicer = durationAggregate
            )
            // Execute the query and handle the response.
            return it.aggregateGroupByDuration(aggregateDuration)
        }
    }

    /**
     * Obtains an instance of HealthConnectClient.
     * It tries to obtain it from HealthConnectSharedState.
     * If necessary, obtains it directly from HealthConnectClient
     */
    private fun getHealthConnectClient(context: Context): HealthConnectClient {
        return HealthConnectSharedState.client ?: getOrCreateClient(context)
    }

}