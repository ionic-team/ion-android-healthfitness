package io.ionic.libs.ionhealthfitnesslib.data.types

import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumTimeUnit
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.WeekFields
import java.util.Locale

//Represents a time frame, e.g., 2weeks, 2 days, 6hours
data class HealthTimeFrame(
    val unit: HealthEnumTimeUnit,
    val interval: Int

) {
    /**
     * Taking into account the current Time Frame and a given LocalDateTime <code>time</code>,
     * returns the start date time where <code>time</code> belongs to
     */
    fun getBucketStartFor(time: LocalDateTime): LocalDateTime {
        when (unit) {
            HealthEnumTimeUnit.MINUTE -> {
                val minute = (time.minute / interval) * interval
                return LocalDateTime.of(time.year, time.month, time.dayOfMonth, time.hour, minute)
            }

            HealthEnumTimeUnit.HOUR -> {
                val hour = (time.hour / interval) * interval
                return LocalDateTime.of(time.year, time.month, time.dayOfMonth, hour, 0)
            }

            HealthEnumTimeUnit.DAY -> {
                val day = (time.dayOfMonth / interval) * interval
                return LocalDateTime.of(time.year, time.month, if (day == 0) 1 else day, 0, 0)
            }

            HealthEnumTimeUnit.WEEK -> {
                val weekAndYear = getWeekFrom(time)
                val week = ((weekAndYear.first - 1) / interval) * interval + 1
                return getWeekStartDate(week, weekAndYear.second)
            }

            HealthEnumTimeUnit.MONTH -> {
                val month = (time.month.value / interval) * interval
                return LocalDateTime.of(
                    time.year,
                    if (month == 0) Month.JANUARY else Month.of(month),
                    1,
                    0,
                    0
                )
            }

            HealthEnumTimeUnit.YEAR -> {
                val year = (time.year / interval) * interval
                return LocalDateTime.of(
                    year, time.month, 1, 0, 0
                )
            }
        }
    }

    fun getBucketEndFor(time: LocalDateTime): LocalDateTime {
        val interval = this.interval.toLong()
        return when (unit) {
            HealthEnumTimeUnit.MINUTE -> time.plusMinutes(interval)
            HealthEnumTimeUnit.HOUR -> time.plusHours(interval)
            HealthEnumTimeUnit.DAY -> time.plusDays(interval)
            HealthEnumTimeUnit.WEEK -> time.plusWeeks(interval)
            HealthEnumTimeUnit.MONTH -> time.plusMonths(interval)
            HealthEnumTimeUnit.YEAR -> time.plusYears(interval)
        }
    }

    /**
     * Returns the number of the week the given time belongs to
     */
    private fun getWeekFrom(time: LocalDateTime): Pair<Int, Int> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekInYear = time.get(weekFields.weekOfWeekBasedYear())
        val weekYear = time.get(weekFields.weekBasedYear())
        return Pair(weekInYear, weekYear)
    }

    /**
     * Given a week's number in a year, it returns that week's start date
     * It takes into account if the week starts on a Monday or a Sunday
     */
    private fun getWeekStartDate(weekNumber: Int, year: Int): LocalDateTime {
        //of(year, month, dayOfMonth, hour, minute)
        //first we calculate the first day of the year
        val firsDayOfTheYear = LocalDateTime.of(year, 1, 1, 0, 0)
        //then we figure out how many days until the closes monday/sunday
        val daysUntilStart =
            if (firsDayOfTheYear.dayOfWeek.value == 1) 0 else 8 - firsDayOfTheYear.dayOfWeek.value
        val firstWeekStart = firsDayOfTheYear.plusDays(daysUntilStart.toLong())

        //we add the week number to the first monday/sunday and as such we have the week's date
        return firstWeekStart.plusWeeks((weekNumber - 1).toLong())
    }
}
