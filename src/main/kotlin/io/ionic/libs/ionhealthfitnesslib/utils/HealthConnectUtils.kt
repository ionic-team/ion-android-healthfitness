package io.ionic.libs.ionhealthfitnesslib.utils

import io.ionic.libs.ionhealthfitnesslib.data.Constants
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumTimeUnit
import io.ionic.libs.ionhealthfitnesslib.data.types.DateSplitterGroupingOperation
import java.time.Duration
import java.time.LocalDateTime

object HealthConnectUtils {
    /**There is an undocumented limit of 5000 buckets in a grouping query,
     *based in the amount of data available can occur that if the list of results is higher than 5000 buckets,
     *the returned exception is: android.health.connect.HealthConnectException: java.lang.IllegalArgumentException: Number of buckets must not exceed 5000
     *This function calculates the amount of splits that the query will have, and if the amount of splits is higher than 5000,
     */
    internal fun generateDateSplits(
        startDateInput: LocalDateTime,
        endDateInput: LocalDateTime,
        period: HealthEnumTimeUnit,
        timeUnitLength: Int
    ): List<DateSplitterGroupingOperation> {
        val maxBlocks = Constants.MAX_BLOCK_PER_QUERY

        val splits = mutableListOf<DateSplitterGroupingOperation>()
        //Calculate the amount of splits needed to cover the entire period
        var currentStart = startDateInput
        val periodToMinValue = period.value.second.toMinutes(timeUnitLength.toLong())
        val periodToMinutes = if (periodToMinValue <= 0) 1 else periodToMinValue
        var expectedBlocks =
            Duration.between(startDateInput, endDateInput).toMinutes() / periodToMinutes
        //we will use the position to identify each split position
        var position = 0
        // Calculate the time span for each block and adjust endDate accordingly to not exceed maxBlocks
        val blockDurationMinutes = periodToMinutes * maxBlocks

        //While for create the splits
        while (expectedBlocks > maxBlocks) {
            //add to the currentStart -> startDate the amount of minutes allowed by the maxBlocks
            var currentEnd = currentStart.plusMinutes(blockDurationMinutes)
            //verify that is inside the initial range
            if (currentEnd.isAfter(endDateInput)) {
                currentEnd = endDateInput
            }
            //add the new split
            splits.add(DateSplitterGroupingOperation(position, currentStart, currentEnd))
            //updated the currentStart to the currentEnd for the new iteration
            currentStart = currentEnd
            //calculate the new expected blocks for the new iteration
            expectedBlocks =
                Duration.between(currentStart, endDateInput).toMinutes() / periodToMinutes
            position++
        }

        if (currentStart.isBefore(endDateInput)) {
            splits.add(DateSplitterGroupingOperation(position, currentStart, endDateInput))
        }
        return splits
    }
}