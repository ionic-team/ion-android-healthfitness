package com.outsystems.plugins.healthfitness.helpers

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.response.ReadRecordsResponse
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthRecord
import java.time.LocalDateTime
import kotlin.reflect.KClass

interface HealthConnectHelperInterface {
    fun getSdkStatus(context: Context): Int
    fun getOrCreateClient(context: Context): HealthConnectClient
    suspend fun readLastRecord(
        healthConnectClient: HealthConnectClient,
        healthRecordName: HealthRecord
    ): Record?

    suspend fun checkPermissions(
        healthConnectClient: HealthConnectClient,
        permissions: Set<String>
    ): Boolean
    suspend fun writeWeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    )
    suspend fun writeHeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    )
    suspend fun writeBodyFatRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    )
    suspend fun writeBasalMetabolicRateRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    )

    suspend fun readRecords(
        className: KClass<out Record>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int = 1000,
        pageToken: String? = null,
        context: Context
    ): ReadRecordsResponse<out Record>

    suspend fun groupByPeriod(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByPeriod>

    suspend fun groupByDuration(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByDuration>

}