package com.outsystems.plugins.healthfitness.mock

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.Temperature
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelperInterface
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass

class HealthConnectHelperMock: HealthConnectHelperInterface {

    var sdkStatus = HealthConnectClient.SDK_AVAILABLE
    var variableHasPermissions = true
    var shouldThrowIllegalArgument = false
    var shouldThrowException = false
    var shouldReturnEmptyList = false
    var shouldReturnBloodPressure = true
    var shouldReturnOxygenSaturation = false
    var shouldReturnBodyTemperature = false
    lateinit var healthClientMock : HealthConnectClient

    private val currentTime = Instant.now()

    private val metadataUnknown = Metadata.unknownRecordingMethod()
    private val metadataAutomatic = Metadata.autoRecorded(
        Device(type = Device.TYPE_WATCH, manufacturer = "Samsung", model = "Galaxy Watch")
    )
    private val stepsRecord = StepsRecord(
        startTime = currentTime,
        startZoneOffset = null,
        endTime = currentTime.plusMillis(1000),
        endZoneOffset = null,
        count = 2L,
        metadata = metadataAutomatic
    )
    private val pressure = Pressure.millimetersOfMercury(30.0)
    private val bloodPressureRecord = BloodPressureRecord(
        time = Instant.now(),
        zoneOffset = null, systolic =  pressure, diastolic = pressure, metadata = metadataUnknown)
    private val percentage = Percentage(99.7)
    private val oxygenSaturationRecord = OxygenSaturationRecord(
        time = Instant.now(), zoneOffset = null, percentage = percentage, metadata = metadataUnknown
    )
    private val bodyTemperature = Temperature.celsius(39.0)
    private val bodyTemperatureRecord = BodyTemperatureRecord(
        time = Instant.now(), zoneOffset = null, temperature = bodyTemperature, metadata = metadataUnknown
    )
    override fun getSdkStatus(context: Context): Int {
        return sdkStatus
    }

    override fun getOrCreateClient(context: Context): HealthConnectClient {
        if (this::healthClientMock.isInitialized) {
            return healthClientMock
        }
        return Mockito.mock(HealthConnectClient::class.java)
    }

    override suspend fun readLastRecord(
        healthConnectClient: HealthConnectClient,
        healthRecordName: HealthRecord
    ): Record? {
        if(shouldReturnEmptyList)
            return null
        if (shouldThrowException)
            throw Exception()

        if(shouldReturnBloodPressure)
            return bloodPressureRecord

        if(shouldReturnOxygenSaturation)
            return oxygenSaturationRecord

        if (shouldReturnBodyTemperature)
            return bodyTemperatureRecord

        return stepsRecord
    }

    override suspend fun checkPermissions(
        healthConnectClient: HealthConnectClient,
        permissions: Set<String>
    ): Boolean {
        return variableHasPermissions
    }

    override suspend fun writeWeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        if (shouldThrowIllegalArgument) {
            throw IllegalArgumentException()
        } else if (shouldThrowException) {
            throw Exception()
        }
    }

    override suspend fun writeHeightRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        if (shouldThrowIllegalArgument) {
            throw IllegalArgumentException()
        } else if (shouldThrowException) {
            throw Exception()
        }
    }

    override suspend fun writeBodyFatRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        if (shouldThrowIllegalArgument) {
            throw IllegalArgumentException()
        } else if (shouldThrowException) {
            throw Exception()
        }
    }

    override suspend fun writeBasalMetabolicRateRecord(
        value: Double,
        metadata: Metadata,
        healthConnectClient: HealthConnectClient
    ) {
        if (shouldThrowIllegalArgument) {
            throw IllegalArgumentException()
        } else if (shouldThrowException) {
            throw Exception()
        }
    }

    override suspend fun readRecords(
        className: KClass<out Record>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int,
        pageToken: String?,
        context: Context
    ): ReadRecordsResponse<out Record> {
        val listOfRecord = mutableListOf<StepsRecord>()

        val pageTokens = arrayOf("page-2", "page-3", "page-4")
        val startIndex = if (pageToken == null || !pageTokens.contains(pageToken)) {
            1
        } else {
            pageTokens.indexOf(pageToken) + 2
        }

        var endTime = endDate
        endTime = endTime.minusHours((startIndex - 1).toLong())
        val endInstant = endTime.toInstant(ZoneOffset.UTC)

        for (i in startIndex..startIndex-1+pageSize.coerceAtMost(4)) {
            val record = StepsRecord(
                count = 10,
                startTime = endTime.minusHours(1).toInstant(ZoneOffset.UTC),
                endTime = endInstant,
                endZoneOffset = null,
                startZoneOffset = null,
                metadata = metadataUnknown
            )
            endTime = endTime.minusHours(1)
            listOfRecord.add(record)
        }

        val mockRecordsResponse = Mockito.mock(ReadRecordsResponse::class.java)
        Mockito.doReturn(listOfRecord).`when`(mockRecordsResponse).records
        if(pageSize < 4) {
            var nextPage: String? = null
            if (startIndex < 4) {
                val nextPageIndex = startIndex - 1 // Adjusting for the +2 offset in startIndex
                nextPage = if (nextPageIndex >= 0 && nextPageIndex < pageTokens.size) {
                    pageTokens[nextPageIndex]
                } else {
                    null
                }
            }
            Mockito.doReturn(nextPage).`when`(mockRecordsResponse).pageToken
        }
        return mockRecordsResponse
    }

    override suspend fun groupByPeriod(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByPeriod> {
        val mockAggregationResultGroupedByPeriod= mock<AggregationResultGroupedByPeriod>()
        whenever(mockAggregationResultGroupedByPeriod.startTime).thenReturn(
            startDate
        )
        whenever(mockAggregationResultGroupedByPeriod.endTime).thenReturn(
            endDate
        )
        val mockAggregationResultGroupedByPeriod1 = mock<AggregationResultGroupedByPeriod>()
        whenever(mockAggregationResultGroupedByPeriod1.startTime).thenReturn(
            startDate
        )
        whenever(mockAggregationResultGroupedByPeriod1.endTime).thenReturn(
            endDate
        )

        val result = mock<AggregationResult>()
        whenever(mockAggregationResultGroupedByPeriod.result).thenReturn(
            result
        )
        whenever(mockAggregationResultGroupedByPeriod1.result).thenReturn(
            result
        )

        return listOf(
            mockAggregationResultGroupedByPeriod,
            mockAggregationResultGroupedByPeriod1
        )
    }

    override suspend fun groupByDuration(
        operationToExecute: AggregateMetric<*>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int,
        context: Context
    ): List<AggregationResultGroupedByDuration> {
        val mockAggregationResultGroupedByDuration = mock<AggregationResultGroupedByDuration>()
        whenever(mockAggregationResultGroupedByDuration.startTime).thenReturn(
            startDate.toInstant(
                ZoneOffset.UTC
            )
        )
        whenever(mockAggregationResultGroupedByDuration.zoneOffset).thenReturn(
                ZoneOffset.UTC
        )

         whenever(mockAggregationResultGroupedByDuration.endTime).thenReturn(
            endDate.toInstant(
                ZoneOffset.UTC
            )
        )
        val mockAggregationResultGroupedByDuration1 = mock<AggregationResultGroupedByDuration>()
        whenever(mockAggregationResultGroupedByDuration1.startTime).thenReturn(
            startDate.toInstant(
                ZoneOffset.UTC
            )
        )
        whenever(mockAggregationResultGroupedByDuration1.endTime).thenReturn(
            endDate.toInstant(
                ZoneOffset.UTC
            )
        )

         whenever(mockAggregationResultGroupedByDuration1.zoneOffset).thenReturn(
             ZoneOffset.UTC
        )

        val result = mock<AggregationResult>()
        whenever(mockAggregationResultGroupedByDuration.result).thenReturn(
            result
        )
        whenever(mockAggregationResultGroupedByDuration1.result).thenReturn(
            result
        )

        return listOf(
            mockAggregationResultGroupedByDuration,
            mockAggregationResultGroupedByDuration1
        )
    }
}
