package com.outsystems.plugins.healthfitness.helpers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.Record
import com.outsystems.plugins.healthfitness.data.HealthEnumOperationType
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject
import com.outsystems.plugins.healthfitness.data.types.DateSplitterGroupingOperation
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.types.HealthDataPoint
import com.outsystems.plugins.healthfitness.data.types.HealthRecordMetadata
import com.outsystems.plugins.healthfitness.data.types.HealthTimeFrame
import com.outsystems.plugins.healthfitness.data.types.HealthVariable
import com.outsystems.plugins.healthfitness.data.types.ObjectGroupingOperationByTimeGroup
import com.outsystems.plugins.healthfitness.data.types.ValueRecordResult
import com.outsystems.plugins.healthfitness.utils.HealthConnectUtils
import com.outsystems.plugins.healthfitness.utils.toEpoch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

//Represents the different options an advanced query can have
data class AdvancedQueryOptions(
    val removeEmptyBlock: Boolean = false,
    val includeDataPoint: Boolean = false,
    val includeRawData: Boolean = false,
    val pageSize: Int = 1000,
    val pageToken: String? = null
)

class HealthAdvancedQuery(
    private val healthConnectClientHelper: HealthConnectHelperInterface = HealthConnectHelper(),
    private val variable: HealthVariable,
    private val startTime: LocalDateTime,
    private val endTime: LocalDateTime,
    private val operationType: HealthEnumOperationType,
    private val timeFrame: HealthTimeFrame,
    private val options: AdvancedQueryOptions,
    private val context: Context
)  {

    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = Dispatchers.Default
    }

    suspend fun executeQuery(): HealthAdvancedQueryResponse {
        //Based on the operationType, we want to get from the allowedOperations the exact operation to execute
        //If the operation to execute is null, it means that the operation is not supported by the variable out of the box,
        //so we need to read the raw data and execute the custom operation
        variable.allowedOperations[operationType]?.let {
            return aggregateData()
        } ?: return aggregateFromRawData()
    }


    /**
     * ===== START: Health Connect Query Aggregation =====
     * Methods related with Health Connect Queries
     * aka, we can call their metrics for aggregation
     */

    /**
     * Transforms Health Connect Aggregation Result into an AdvancedQueryResponse
     */
    private suspend fun aggregateData(): HealthAdvancedQueryResponse {
        val bucketStartTime = timeFrame.getBucketStartFor(startTime)
        val bucketEndTime = timeFrame.getBucketEndFor(timeFrame.getBucketStartFor(endTime))
        //We need to split the query in multiple queries based on the dates if the amount of data is higher than 5000
        val listOfDatesGrouping =
            HealthConnectUtils.generateDateSplits(
                bucketStartTime,
                bucketEndTime,
                timeFrame.unit,
                timeFrame.interval
            )

        return runHealthQuery(
            listOfDatesGrouping
        )
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun handleAggregation(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        aggregateFunction: suspend (
            operationToExecute: AggregateMetric<*>,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            period: HealthEnumTimeUnit,
            timeUnitLength: Int,
            context: Context
        ) -> List<Any>,
        convertTimestamps: ((AggregationResultGroupedByDuration) -> Pair<LocalDateTime, LocalDateTime>)? = null
    ): HealthAdvancedQueryResponse {
        val response = aggregateFunction(
            variable.allowedOperations[operationType]!!,
            startTime,
            endTime,
            timeFrame.unit,
            timeFrame.interval,
            context
        )
        val blockList: MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf()
        val datapointList: MutableList<HealthDataPoint> = mutableListOf()

        for ((index, itemResponse) in response.withIndex()) {
            val start: LocalDateTime
            val end: LocalDateTime
            var aggregation: AggregationResult
            if (convertTimestamps != null) {
                val item = itemResponse as AggregationResultGroupedByDuration
                convertTimestamps(item).apply {
                    start = first
                    end = second
                }
                aggregation = item.result
            } else {
                val item = itemResponse as AggregationResultGroupedByPeriod
                start = item.startTime
                end = item.endTime
                aggregation = item.result
            }
            val (blockListItem, datapointListItem) = convertToAdvanceQueryResult(
                index,
                start,
                end,
                aggregation
            )
            blockList.addAll(blockListItem)
            datapointList.addAll(datapointListItem)
        }
        return HealthAdvancedQueryResponse(blockList, datapointList)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun convertToAdvanceQueryResult(
        index: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        result: AggregationResult,
    ): Pair<MutableList<HealthAdvancedQueryResponseBlock>, MutableList<HealthDataPoint>> {
        val blockList: MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf()
        val datapointList: MutableList<HealthDataPoint> = mutableListOf()
        val metadataList: MutableList<HealthRecordMetadata> = mutableListOf()
        val startTimeData = startTime.toEpoch()
        val endTimeData = endTime.toEpoch()
        val listOfValues = mutableListOf<Float>()

        val valueMetric = result[variable.allowedOperations[operationType]!!]
        valueMetric?.let {
            val valueMetricResult: Float = if (valueMetric is Long) {
                valueMetric.toFloat()
            } else {
                val retrievedValue = variable.getValue(it)
                retrievedValue.metadata?.let { metadata ->
                    metadataList.add(metadata)
                }
                retrievedValue.value.first()
            }
            listOfValues.add(valueMetricResult)

            if (options.includeDataPoint) {
                datapointList.add(
                    HealthRecordsObject.createDataPoint(
                        startTime,
                        valueMetricResult
                    )
                )
            }
        }

        if (options.includeRawData && (!options.removeEmptyBlock || listOfValues.isNotEmpty())) {
            blockList.add(
                HealthAdvancedQueryResponseBlock(
                    index,
                    startTimeData,
                    endTimeData,
                    listOfValues
                )
            )
        }
        return Pair(blockList, datapointList)
    }

    /**
     * This function will execute all queries that are in the list of datesGrouping,  created by generateDateSplits
     * The results will be merged in the end.
     * @param listOfDatesGrouping dates splits for aggregation
     */
    private suspend fun runHealthQuery(
        listOfDatesGrouping: List<DateSplitterGroupingOperation>
    ): HealthAdvancedQueryResponse {
        val healthAdvanceQueryResponse = HealthAdvancedQueryResponse()
        val listOfResultsGrouping = mutableListOf<ObjectGroupingOperationByTimeGroup>()

        val aggregationToCall = when(timeFrame.unit) {
            HealthEnumTimeUnit.MINUTE, HealthEnumTimeUnit.HOUR  -> {
                healthConnectClientHelper::groupByDuration
            }
            else -> healthConnectClientHelper::groupByPeriod
        }

        val convertTimestampsCallback = when(timeFrame.unit) {
            HealthEnumTimeUnit.MINUTE, HealthEnumTimeUnit.HOUR  -> {
                ::convertTimestamps
            }
            else -> null
        }
        val job = coroutineScope.launch {
            //We could run the queries in parallel, but we will execute sequentially for now. because there is a limitation in the Health Connect SDK
            //when running in parallel: Transaction failed on small parcel; remote process probably died, but this could also be caused by running out of binder buffer space
            listOfDatesGrouping.forEachIndexed { index, dateGrouping ->
                // Execute the query and handle the response.
                val result = handleAggregation(
                    dateGrouping.startDate,
                    dateGrouping.endDate,
                    aggregationToCall,
                    convertTimestampsCallback
                )
                val objectGrouping = ObjectGroupingOperationByTimeGroup(
                    index,
                    result
                )
                listOfResultsGrouping.add(objectGrouping)
            }
            val results = listOfResultsGrouping.sortedBy { it.position }
            //Merge all the results in the end
            results.forEach { item ->
                healthAdvanceQueryResponse.results.addAll(item.healthAdvanceQueryResponse.results)
                healthAdvanceQueryResponse.resultDataPoints.addAll(item.healthAdvanceQueryResponse.resultDataPoints)
            }
        }
        //We are suspending the coroutine until the job is finished
        job.join()
        return healthAdvanceQueryResponse
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun convertTimestamps(item: AggregationResultGroupedByDuration): Pair<LocalDateTime, LocalDateTime> {
        return Pair(LocalDateTime.ofInstant(item.startTime, item.zoneOffset), LocalDateTime.ofInstant(item.endTime, item.zoneOffset))
    }
    /**
     * ===== END: Health Connect Query Aggregation =====
     */


    //Some operations are not supported by HealthConnect in some variables like SUM, AVERAGE, ect, in that case
    //we choose the raw operation and we process that data in order to get the correct result expected by the operation
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun aggregateFromRawData(): HealthAdvancedQueryResponse {
        val response =
            healthConnectClientHelper.readRecords(
                variable.className, startTime, endTime,
                options.pageSize, options.pageToken, context
            )
        return aggregateRecordsToHealthResponse(
            response.records,
            response.pageToken
        )
    }

    private fun aggregateRecordsToHealthResponse(
        records: List<Record>,
        pageToken: String? = null
    ): HealthAdvancedQueryResponse {
        //Create the bucket list of blocks
        val listRecordsGrouped: MutableList<HealthAdvancedQueryResponseBlock> = mutableListOf()
        //Create the bucket list of datapoints
        val resultDataPoints: MutableList<HealthDataPoint> = mutableListOf()

        val buckets: MutableMap<LocalDateTime, MutableList<ValueRecordResult>> = mutableMapOf()
        for (record in records) {
            val recordValueItem = variable.getValue(record)
            // Enhanced error message for null start times
            val time = recordValueItem.startTime
                ?: throw IllegalArgumentException("Record start time cannot be null.")
            val bucket = this.timeFrame.getBucketStartFor(time)
            if (!buckets.containsKey(bucket))
                buckets[bucket] = mutableListOf(recordValueItem)
            else buckets[bucket]?.add(recordValueItem)
        }

        for (bucket in buckets) {
            //Execute the grouping operation for every bucket;
            // note that AVERAGE and SUM cannot have metadata
            val valuesList: List<Float>
            val metadataList: MutableList<HealthRecordMetadata> = mutableListOf()
            when (operationType) {
                HealthEnumOperationType.AVERAGE -> {
                    valuesList = listOf(bucket.value.map { it.value.average() }.average().toFloat())
                }

                HealthEnumOperationType.MIN -> {
                    bucket.value.minByOrNull { it.value.min() }.let { valueRecord ->
                        valuesList = listOf(valueRecord?.value?.min() ?: 0.0f)
                        valueRecord?.metadata?.let { metadata ->
                            metadataList.add(metadata)
                        }
                    }
                }

                HealthEnumOperationType.MAX -> {
                    bucket.value.maxByOrNull { it.value.max() }.let { valueRecord ->
                        valuesList = listOf(valueRecord?.value?.max() ?: 0.0f)
                        valueRecord?.metadata?.let { metadata ->
                            metadataList.add(metadata)
                        }
                    }
                }

                HealthEnumOperationType.SUM -> {
                    valuesList = listOf(bucket.value.map { it.value.sum() }.sum())
                }
                //the default operation is RAW
                else -> {
                    valuesList = bucket.value.flatMap { it.value }
                    metadataList.addAll(createMetadataFromRawValues(bucket))
                }
            }
            //Add the processed block to the listRecordsGrouped based on the removeEmptyBlock and includeDataPoint criteria
            if (options.includeRawData && (!options.removeEmptyBlock || valuesList.isNotEmpty())) {
                val startTime = bucket.key
                val endTime = timeFrame.getBucketEndFor(startTime)
                val block = listRecordsGrouped.size
                listRecordsGrouped.add(
                    HealthAdvancedQueryResponseBlock(
                        block,
                        startTime.toEpoch(),
                        endTime.toEpoch(),
                        valuesList,
                        recordMetadata = metadataList
                    )
                )
            }
            if (options.includeDataPoint) {
                valuesList.forEach {
                    val startTime = bucket.key
                    resultDataPoints.add(
                        HealthDataPoint(
                            startTime.toLocalDate().toString(),
                            it.toString()
                        )
                    )
                }
            }
        }
        return HealthAdvancedQueryResponse(listRecordsGrouped, resultDataPoints, pageToken)
    }

    /**
     * Creates the metadata list based on the raw health values returned, such that we try to create a 1:1 mapping between values and metadata.
     * For certain variables like blood pressure, a record will contain more than one value, but only one metadata.
     * This method will replicate the metadata in order for there to be the number of metadata equal to the number of values.
     */
    private fun createMetadataFromRawValues(
        bucket:  Map.Entry<LocalDateTime, MutableList<ValueRecordResult>>
    ): List<HealthRecordMetadata> = bucket.value.flatMap {
        if (it.metadata == null) {
            emptyList()
        } else {
            List(it.value.size) { _ -> it.metadata }
        }
    }
}