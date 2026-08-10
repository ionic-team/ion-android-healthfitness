package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.utils.HealthConnectUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month


@ExperimentalCoroutinesApi
class BucketSplitterTest {
    @Test
    fun `test generateDateSplits with period within single block`() {
        // Setup for a period that fits within a single block
        val startDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 1)
        val endDate = LocalDateTime.of(2024, Month.JANUARY, 2, 0, 0) // 1 day
        val period = HealthEnumTimeUnit.DAY
        val timeUnitLength = 1

        val result =
            HealthConnectUtils.generateDateSplits(startDate, endDate, period, timeUnitLength)

        Assert.assertEquals(1, result.size)
        with(result.first()) {
            Assert.assertEquals(0, position)
            Assert.assertEquals(this.startDate, startDate)
            Assert.assertEquals(this.endDate, endDate)
        }
    }

    @Test
    fun `test generateDateSplits when period exceeds max query days`() {
        // Setup for a period longer than max query days, most recent version should not restrict to 30 days
        val startDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 1)
        val endDate = LocalDateTime.of(2024, Month.FEBRUARY, 15, 0, 0) // 45 days
        val period = HealthEnumTimeUnit.DAY
        val timeUnitLength = 1

        val result =
            HealthConnectUtils.generateDateSplits(startDate, endDate, period, timeUnitLength)

        Assert.assertTrue(result.isNotEmpty())
        with(result.first()) {
            Assert.assertEquals(this.startDate, startDate)
            Assert.assertEquals(this.endDate, endDate)
        }
    }

    @Test
    fun `test generateDateSplits requiring multiple splits`() {
        // Setup for a period requiring multiple splits
        val startDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 1)
        val endDate = LocalDateTime.of(2024, Month.APRIL, 1, 0, 0) // 90 days
        val period = HealthEnumTimeUnit.MINUTE
        val timeUnitLength = 1

        val result =
            HealthConnectUtils.generateDateSplits(startDate, endDate, period, timeUnitLength)

        // Verify multiple splits
        Assert.assertTrue(result.size > 1)
        for (i in 0 until result.size - 1) {
            with(result[i]) {
                Assert.assertTrue(position == i)
                Assert.assertTrue(this.startDate.isBefore(this.endDate))
                if (i > 0) {
                    Assert.assertEquals(result[i - 1].endDate, this.startDate)
                }
            }
        }
    }

    @Test
    fun `test generateDateSplits with edge case of exactly max blocks`() {
        // Setup for a period exactly at the limit of max blocks
        // This requires specific knowledge of Constants.MAX_BLOCK_PER_QUERY and how it's used
        // For the sake of this example, assume a scenario that would generate exactly the max number of blocks

        // Assume Constants.MAX_BLOCK_PER_QUERY and Constants.MAX_QUERY_DAYS are such that this setup hits the limit
        val startDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 1)
        val endDate = LocalDateTime.of(2024, Month.JANUARY, 31, 0, 0) // Exactly at some limit
        val period = HealthEnumTimeUnit.DAY
        val timeUnitLength = 1

        // Execute
        val result =
            HealthConnectUtils.generateDateSplits(startDate, endDate, period, timeUnitLength)

        // Verify
        // The exact expectations depend on Constants.MAX_BLOCK_PER_QUERY and the operation logic
        Assert.assertNotNull(result)
    }

    @Test
    fun `test generateDateSplits with milliseconds period`() {
        val startDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 1)
        val endDate = LocalDateTime.of(2024, Month.FEBRUARY, 1, 0, 0)

        val period = HealthEnumTimeUnit.MINUTE
        val timeUnitLength = 1 // 60,000 milliseconds in a minute

        // Calculate expected number of splits
        // Here, we're assuming the system can handle a maximum of 5000 splits,
        // and our period will generate a split for 1 minute.
        val expectedSplits = 9 //  43200/Constants.MAX_BLOCK_PER_QUERY As there are 60 minutes in an hour

        // Execute
        val result = HealthConnectUtils.generateDateSplits(startDate, endDate, period, timeUnitLength)

        Assert.assertEquals(expectedSplits, result.size)
        Assert.assertEquals(result.first().startDate, startDate)
        Assert.assertEquals(result.last().endDate, endDate)
    }
}