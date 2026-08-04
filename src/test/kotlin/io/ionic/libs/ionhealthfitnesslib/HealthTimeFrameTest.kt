package io.ionic.libs.ionhealthfitnesslib

import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumTimeUnit
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthTimeFrame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month


@ExperimentalCoroutinesApi
class HealthTimeFrameTest {

    @Test
    fun `test getBucketStartFor with different minute intervals for same day dates`() {
        //10 minute intervals
        val tenMinute = HealthTimeFrame(HealthEnumTimeUnit.MINUTE, 10)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 6)
        var startExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        Assert.assertEquals(startExpected,  tenMinute.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 12)
        startExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 10)
        Assert.assertEquals(startExpected, tenMinute.getBucketStartFor(date))

        //13 minute intervals
        val thirteen = HealthTimeFrame(HealthEnumTimeUnit.MINUTE, 13)

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 7)
        startExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        Assert.assertEquals(startExpected, thirteen.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 13)
        startExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 13)
        Assert.assertEquals(startExpected, thirteen.getBucketStartFor(date))

    }

     @Test
    fun `test getBucketEndFor with different minute intervals for same day dates`() {
        //10 minute intervals
        val tenMinute = HealthTimeFrame(HealthEnumTimeUnit.MINUTE, 10)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 6)
        var endExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 16)
        Assert.assertEquals(endExpected,  tenMinute.getBucketEndFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 12)
        endExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 22)
        Assert.assertEquals(endExpected, tenMinute.getBucketEndFor(date))

        //13 minute intervals
        val thirteen = HealthTimeFrame(HealthEnumTimeUnit.MINUTE, 13)

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 7)
        endExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 20)
        Assert.assertEquals(endExpected, thirteen.getBucketEndFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 13)
        endExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 26)
        Assert.assertEquals(endExpected, thirteen.getBucketEndFor(date))

    }

    @Test
    fun `test getBucketStartFor with different hourly intervals for same day dates`() {
        //5 hour intervals
        val fiveHour = HealthTimeFrame(HealthEnumTimeUnit.HOUR, 5)
        //7 hour intervals
        val sevenHour = HealthTimeFrame(HealthEnumTimeUnit.HOUR, 7)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 6)
        var fiveHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        var sevenHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 7, 0)

        Assert.assertEquals(fiveHourExpected, fiveHour.getBucketStartFor(date))
        Assert.assertEquals(sevenHourExpected, sevenHour.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 3, 12)
        fiveHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        sevenHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)

        Assert.assertEquals(fiveHourExpected, fiveHour.getBucketStartFor(date))
        Assert.assertEquals(sevenHourExpected, sevenHour.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 11, 7)
        fiveHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        sevenHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 7, 0)

        Assert.assertEquals(fiveHourExpected, fiveHour.getBucketStartFor(date))
        Assert.assertEquals(sevenHourExpected, sevenHour.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 22, 17)
        fiveHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 20, 0)
        sevenHourExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 21, 0)

        Assert.assertEquals(fiveHourExpected, fiveHour.getBucketStartFor(date))
        Assert.assertEquals(sevenHourExpected, sevenHour.getBucketStartFor(date))
    }

    @Test
    fun `test getBucketStartFor different day intervals`() {
        //3 day intervals
        val threeDays = HealthTimeFrame(HealthEnumTimeUnit.DAY, 3)
        //4 day intervals
        val fourDays = HealthTimeFrame(HealthEnumTimeUnit.DAY, 4)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        var threeDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        Assert.assertEquals(threeDaysExpected, threeDays.getBucketStartFor(date))
        Assert.assertEquals(threeDaysExpected, fourDays.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 3, 3, 12)
        threeDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 3, 0, 0)
        var fourDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)

        Assert.assertEquals(threeDaysExpected, threeDays.getBucketStartFor(date))
        Assert.assertEquals(fourDaysExpected, fourDays.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 7, 11, 7)
        threeDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 6, 0, 0)
        fourDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 4, 0, 0)
        Assert.assertEquals(threeDaysExpected, threeDays.getBucketStartFor(date))
        Assert.assertEquals(fourDaysExpected, fourDays.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 22, 22, 17)
        threeDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 21, 0, 0)
        fourDaysExpected = LocalDateTime.of(2024, Month.JANUARY, 20, 0, 0)
        Assert.assertEquals(threeDaysExpected, threeDays.getBucketStartFor(date))
        Assert.assertEquals(fourDaysExpected, fourDays.getBucketStartFor(date))
    }

    @Test
    fun `test getBucketStartFor with different month intervals`() {
        //3 day intervals
        val threeMonths = HealthTimeFrame(HealthEnumTimeUnit.MONTH, 3)
        //4 day intervals
        val sixMonths = HealthTimeFrame(HealthEnumTimeUnit.MONTH, 6)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        var threeMonthsExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        Assert.assertEquals(threeMonthsExpected, threeMonths.getBucketStartFor(date))
        Assert.assertEquals(threeMonthsExpected, sixMonths.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.MARCH, 5, 3, 12)
        threeMonthsExpected = LocalDateTime.of(2024, Month.MARCH, 1, 0, 0)
        var sixMonthsExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)

        Assert.assertEquals(threeMonthsExpected, threeMonths.getBucketStartFor(date))
        Assert.assertEquals(sixMonthsExpected, sixMonths.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.MAY, 7, 11, 7)
        threeMonthsExpected = LocalDateTime.of(2024, Month.MARCH, 1, 0, 0)
        sixMonthsExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        Assert.assertEquals(threeMonthsExpected, threeMonths.getBucketStartFor(date))
        Assert.assertEquals(sixMonthsExpected, sixMonths.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.AUGUST, 22, 22, 17)
        threeMonthsExpected = LocalDateTime.of(2024, Month.JUNE, 1, 0, 0)
        sixMonthsExpected = LocalDateTime.of(2024, Month.JUNE, 1, 0, 0)
        Assert.assertEquals(threeMonthsExpected, threeMonths.getBucketStartFor(date))
        Assert.assertEquals(sixMonthsExpected, sixMonths.getBucketStartFor(date))
    }

    @Test
    fun `test getBucketStartFor with different week intervals`() {
        //3 day intervals
        val threeWeeks = HealthTimeFrame(HealthEnumTimeUnit.WEEK, 3)
        //4 day intervals
        val fourWeeks = HealthTimeFrame(HealthEnumTimeUnit.WEEK, 4)

        var date = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0)
        var threeWeeksExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        Assert.assertEquals(threeWeeksExpected, threeWeeks.getBucketStartFor(date))
        Assert.assertEquals(threeWeeksExpected, fourWeeks.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.MARCH, 5, 3, 12)
        threeWeeksExpected = LocalDateTime.of(2024, Month.MARCH, 4, 0, 0)
        val fourWeeksExpected = LocalDateTime.of(2024, Month.FEBRUARY, 26, 0, 0)

        Assert.assertEquals(threeWeeksExpected, threeWeeks.getBucketStartFor(date))
        Assert.assertEquals(fourWeeksExpected, fourWeeks.getBucketStartFor(date))
    }

    @Test
    fun `test getBucketStartFor with different year intervals`() {
        //3 year intervals
        val threeYears = HealthTimeFrame(HealthEnumTimeUnit.YEAR, 3)
        //1 year intervals
        val oneYear = HealthTimeFrame(HealthEnumTimeUnit.YEAR, 1)

        var date = LocalDateTime.of(2021, Month.JUNE, 1, 10, 0)
        var threeYearsExpected = LocalDateTime.of(2019, Month.JUNE, 1, 0, 0)
        var oneYearExpected = LocalDateTime.of(2021, Month.JUNE, 1, 0, 0)
        Assert.assertEquals(threeYearsExpected, threeYears.getBucketStartFor(date))
        Assert.assertEquals(oneYearExpected, oneYear.getBucketStartFor(date))

        date = LocalDateTime.of(2024, Month.JANUARY, 1, 3, 12)
        threeYearsExpected = LocalDateTime.of(2022, Month.JANUARY, 1, 0, 0)
        oneYearExpected = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)

        Assert.assertEquals(threeYearsExpected, threeYears.getBucketStartFor(date))
        Assert.assertEquals(oneYearExpected, oneYear.getBucketStartFor(date))
    }

}