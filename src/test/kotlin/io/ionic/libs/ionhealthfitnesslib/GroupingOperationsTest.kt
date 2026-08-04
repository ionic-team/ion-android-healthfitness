package io.ionic.libs.ionhealthfitnesslib

import android.app.Activity
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumOperationType
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumTimeUnit
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponseBlock
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthDataPoint
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthTimeFrame
import io.ionic.libs.ionhealthfitnesslib.helpers.AdvancedQueryOptions
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthAdvancedQuery
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface
import io.ionic.libs.ionhealthfitnesslib.mock.HealthConnectHelperMock
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


@ExperimentalCoroutinesApi
class GroupingOperationsTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var healthConnectClientHelper: HealthConnectHelperInterface
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)
    private val recordAmount: Long = 4L
    private lateinit var endDate: LocalDateTime
    private lateinit var startDate: LocalDateTime

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        healthConnectClientHelper = HealthConnectHelperMock()

        endDate = LocalDateTime.now()
        startDate = endDate.minusHours(recordAmount)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aggregateFromRawData_WithSUMOperation_IncludesAllDataPoints() = runBlocking {

        val variable = HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value
        val operationType = HealthEnumOperationType.SUM
        val queryOptions = AdvancedQueryOptions(
            removeEmptyBlock = true,
            includeDataPoint = true,
            includeRawData = true
        )
        val yearlyQuery = HealthAdvancedQuery(
            healthConnectClientHelper,
            variable,
            startDate,
            endDate,
            operationType,
            HealthTimeFrame(HealthEnumTimeUnit.YEAR, 1),
            queryOptions,
            mockActivity
        )

        val healthAdvancedQueryResponseYear = yearlyQuery.aggregateFromRawData()
        TestCase.assertEquals(
            recordAmount * 10F,
            healthAdvancedQueryResponseYear.results[0].values[0]
        )

        val monthlyQuery = HealthAdvancedQuery(
            healthConnectClientHelper,
            variable,
            startDate,
            endDate,
            operationType,
            HealthTimeFrame(HealthEnumTimeUnit.MONTH, 1),
            queryOptions,
            mockActivity
        )

        val healthAdvancedQueryResponseMonth = monthlyQuery.aggregateFromRawData()
        TestCase.assertEquals(
            recordAmount * 10F,
            healthAdvancedQueryResponseMonth.results[0].values[0]
        )

        val dailyQuery = HealthAdvancedQuery(
            healthConnectClientHelper,
            variable,
            startDate,
            endDate,
            operationType,
            HealthTimeFrame(HealthEnumTimeUnit.DAY, 1),
            queryOptions,
            mockActivity
        )
        val healthAdvancedQueryResponseDay = dailyQuery.aggregateFromRawData()
        TestCase.assertEquals(recordAmount * 10.0f, healthAdvancedQueryResponseDay.results[0].values[0])

        val hourlyQuery = HealthAdvancedQuery(
            healthConnectClientHelper,
            variable,
            startDate,
            endDate,
            operationType,
            HealthTimeFrame(HealthEnumTimeUnit.HOUR, 1),
            queryOptions,
            mockActivity
        )
        val healthAdvancedQueryResponseMinute = hourlyQuery.aggregateFromRawData()
        TestCase.assertEquals(10.0f, healthAdvancedQueryResponseMinute.results[0].values[0])
    }


    @Test
    fun convertToAdvanceQueryResult_WithSUMOperation_IncludesAllDataPoints() = runBlocking {
        val variable = HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value
        val operationType = HealthEnumOperationType.SUM

        val resultSteps = 10L

        val mockAggregationResult: AggregationResult = mock()
        whenever(
            mockAggregationResult[any<AggregateMetric<*>>()]
        ).thenReturn(
            resultSteps
        )
        val queryOptions = AdvancedQueryOptions(
            removeEmptyBlock = false,
            includeDataPoint = false,
            includeRawData = true
        )
        val query = HealthAdvancedQuery(
            healthConnectClientHelper,
            variable,
            startDate,
            endDate,
            operationType,
            HealthTimeFrame(HealthEnumTimeUnit.YEAR, 1),
            queryOptions,
            mockActivity
        )
        val healthAdvancedQueryResponse = query.convertToAdvanceQueryResult(
            0,
            startTime = startDate,
            endTime = endDate,
            mockAggregationResult
        )
        TestCase.assertEquals(resultSteps.toFloat(), healthAdvancedQueryResponse.first[0].values[0])
    }

    @Test
    fun handleDurationAggregateOperation_IncludesAllDataPoints() = runBlocking {
        val variable = HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value

        val operationType = HealthEnumOperationType.SUM

        val blockList: MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf()
        val datapointList: MutableList<HealthDataPoint> = mutableListOf()

        val queryOptions = AdvancedQueryOptions(
            removeEmptyBlock = false,
            includeDataPoint = true,
            includeRawData = true
        )
        val query =
            spy(HealthAdvancedQuery(
                healthConnectClientHelper,
                variable,
                startDate,
                endDate,
                operationType,
                HealthTimeFrame(HealthEnumTimeUnit.HOUR, 1),
                queryOptions,
                mockActivity
            ))

        val mockAggregationResult: AggregationResult = mock()

        doReturn(Pair(blockList, datapointList)).`when`(query).convertToAdvanceQueryResult(
           1,
            startDate,
            endDate,
            mockAggregationResult
        )

       val response = query.handleAggregation(
            startDate,
            endDate,
            healthConnectClientHelper::groupByDuration,
            query::convertTimestamps
        )
        TestCase.assertEquals(2, response.results.size)
    }

    @Test
    fun handlePeriodAggregateOperation_IncludesAllDataPoints() = runBlocking {
        val variable = HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value

        val operationType = HealthEnumOperationType.SUM

        val blockList: MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf()
        val datapointList: MutableList<HealthDataPoint> = mutableListOf()

        val queryOptions = AdvancedQueryOptions(
            removeEmptyBlock = false,
            includeDataPoint = true,
            includeRawData = true
        )
        val query = spy(
            HealthAdvancedQuery(
                healthConnectClientHelper,
                variable,
                startDate,
                endDate,
                operationType,
                HealthTimeFrame(HealthEnumTimeUnit.MINUTE, 1),
                queryOptions,
                mockActivity
            )
        )
        val mockAggregationResult: AggregationResult = mock()
        whenever(
            query.convertToAdvanceQueryResult(
                1,
                startDate,
                endDate,
                mockAggregationResult
            )
        ).thenReturn(Pair(blockList, datapointList))

        val response = query.handleAggregation(
            startDate,
            endDate,
            healthConnectClientHelper::groupByPeriod
        )
        TestCase.assertEquals(2, response.results.size)
    }

    @Test
    fun readRawData_WithPagination_IncludesPageToken() = runBlocking {
        val variable = HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value
        val operationType = HealthEnumOperationType.RAW

        val pageTokens = arrayOf("page-2", "page-3", "page-4")
        var pageToken: String? = null
        val timeFrame = HealthTimeFrame(HealthEnumTimeUnit.HOUR, 1)
        val bucket = timeFrame.getBucketStartFor(endDate)

        for (i in 1..4) {
            val queryOptions = AdvancedQueryOptions(
                removeEmptyBlock = true,
                includeDataPoint = true,
                includeRawData = true,
                pageSize = 1, pageToken = pageToken
            )
            val query = HealthAdvancedQuery(
                healthConnectClientHelper,
                variable,
                startDate,
                endDate,
                operationType,
                timeFrame,
                queryOptions,
                mockActivity
            )
            val healthAdvancedQueryResponse = query.aggregateFromRawData()

            pageToken = healthAdvancedQueryResponse.pageToken

            val expected = bucket.minusHours((i - 1).toLong())
            TestCase.assertEquals(pageToken, if (i < 4) pageTokens[i - 1] else null)
            TestCase.assertEquals(
                expected,
                LocalDateTime.ofInstant(Instant.ofEpochSecond(healthAdvancedQueryResponse.results[0].endDate), ZoneId.systemDefault().rules.getOffset(Instant.now()))
            )
        }
    }

}