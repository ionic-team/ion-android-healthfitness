package io.ionic.libs.ionhealthfitnesslib.background.managers

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import io.ionic.libs.ionhealthfitnesslib.background.database.BackgroundJob
import io.ionic.libs.ionhealthfitnesslib.background.database.Notification
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumOperationType
import io.ionic.libs.ionhealthfitnesslib.data.HealthEnumTimeUnit
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecordsObject
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryParameters
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponse
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManagerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/***
 * Responsible for:
 * - doing an advanced query to validate if notification should be sent
 */
class BackgroundJobManager(
    private val healthConnectHelper: HealthConnectHelperInterface,
    private val healthConnectDataManager: HealthConnectDataManagerInterface
) : BackgroundJobManagerInterface {

    companion object {
        const val TAG = "BackgroundJobManager"
        const val BACKGROUND_JOB_NOT_FOUND = "Background job not found."
        const val VARIABLE_DOES_NOT_EXIST = "Variable does not exist."
        const val NOTIFICATION_ID_NULL = "Notification ID is null."
        const val NOTIFICATION_NOT_FOUND = "Notification not found."
        const val FREQUENCY_ALWAYS = "ALWAYS"
        const val FREQUENCY_MINUTE = "MINUTE"
        const val FREQUENCY_HOUR = "HOUR"
        const val FREQUENCY_DAY = "DAY"
        const val FREQUENCY_WEEK = "WEEK"
        const val FREQUENCY_MONTH = "MONTH"
        const val FREQUENCY_YEAR = "YEAR"
    }

    // queries the database with advanced query
    // and if the criteria is met
    override fun processBackgroundJob(
        context: Context,
        backgroundJobId: String,
        triggerNotification: (notification: Notification) -> Unit,
        stopProcessing: () -> Unit
    ) {
        //first fetch the background job
        //then, we verify if the criteria is met
        //if true, triggers a notification
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val job = healthConnectDataManager.getBackgroundJobBy(backgroundJobId)
                    ?: throw Exception(BACKGROUND_JOB_NOT_FOUND)

                // set operation type for advancedQuery
                val variable =
                    HealthRecordsObject.healthConnectVariables[HealthRecord.valueOf(job.variable).name]
                        ?: throw Exception(VARIABLE_DOES_NOT_EXIST)
                // for background jobs, we only deal with SUM or RAW for operation types
                val operationType =
                    if (variable.value.allowedOperations.keys.contains(HealthEnumOperationType.SUM)) {
                        HealthEnumOperationType.SUM
                    } else {
                        HealthEnumOperationType.RAW
                    }

                // determine if background job should be processed already
                val currentTimestamp = System.currentTimeMillis()
                val nextNotificationTimestamp = job.nextNotificationTimestamp

                val jobIsActive = job.isActive
                val isTimeToNotify = currentTimestamp >= nextNotificationTimestamp
                val shouldAlwaysNotify =
                    job.notificationFrequency == FREQUENCY_ALWAYS

                if (!jobIsActive || (!isTimeToNotify && !shouldAlwaysNotify)) {
                    stopProcessing()
                    return@launch
                }

                // update nextNotificationTimestamp of background job
                val nextNotificationCalendar = Calendar.getInstance()
                nextNotificationCalendar.timeInMillis = currentTimestamp
                nextNotificationCalendar.minimalDaysInFirstWeek = 4
                nextNotificationCalendar.firstDayOfWeek = Calendar.MONDAY

                var calendarUnit = Calendar.SECOND
                when (job.notificationFrequency) {
                    FREQUENCY_MINUTE -> { calendarUnit = Calendar.MINUTE }
                    FREQUENCY_HOUR -> { calendarUnit = Calendar.HOUR }
                    FREQUENCY_DAY -> { calendarUnit = Calendar.DATE }
                    FREQUENCY_WEEK -> { calendarUnit = Calendar.WEEK_OF_MONTH }
                    FREQUENCY_MONTH -> { calendarUnit = Calendar.MONTH }
                    FREQUENCY_YEAR -> { calendarUnit = Calendar.YEAR }
                }
                nextNotificationCalendar.add(
                    calendarUnit,
                    job.notificationFrequencyGrouping
                )
                nextNotificationCalendar.startOfUnit(calendarUnit)

                job.nextNotificationTimestamp = nextNotificationCalendar.timeInMillis
                healthConnectDataManager.updateBackgroundJob(job)

                // get notification for background job
                val notificationId = job.notificationId
                    ?: throw Exception(NOTIFICATION_ID_NULL)

                val notification =
                    healthConnectDataManager.getNotificationBy(notificationId)
                        ?: throw Exception(NOTIFICATION_NOT_FOUND)

                // get current date and time
                val calendar = Calendar.getInstance(TimeZone.getDefault())

                // if timeUnit is DAY, then we want to count the whole present day and not the last 24h
                if (job.timeUnit == HealthEnumTimeUnit.DAY.name) {
                    // set the time to the end of the day (23:59:59)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                }

                // get the timestamp from calendar
                val endDate = calendar.timeInMillis
                // we want to analyze data up until 30 days ago
                val month = 2592000000 // month in millis
                val startDate: Long = endDate - month


                // call advancedQuery to determine if condition was met
                val queryParams = HealthAdvancedQueryParameters(
                    HealthRecord.valueOf(job.variable),
                    Date(startDate),
                    Date(endDate),
                    HealthEnumTimeUnit.valueOf(job.timeUnit ?: FREQUENCY_DAY),
                    job.timeUnitGrouping ?: 1,
                    operationType
                )
                val queryResponse = healthConnectDataManager.advancedQuery(
                    queryParams,
                    healthConnectHelper.getOrCreateClient(context),
                    context
                )
                handleAdvancedQueryResponse(
                    job,
                    queryResponse,
                    notification,
                    triggerNotification,
                    stopProcessing
                )
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                stopProcessing()
            }
        }
    }

    private fun handleAdvancedQueryResponse(
        job: BackgroundJob,
        response: HealthAdvancedQueryResponse,
        notification: Notification,
        onSendNotification: (notification: Notification) -> Unit,
        onDoNothing: () -> Unit
    ) {
        var willTriggerJob = false

        if (response.results.isNotEmpty()) {
            val currentTime = Calendar.getInstance().timeInMillis
            // if the last bucket is in the future (e.g. for day), then we remove it from the results
            if (response.results.last().startDate > (currentTime / 1000)) { // working with seconds
                response.results.apply { removeAt(lastIndex) }
            }

            // we only want to consider results for which the startDate is today
            // if the last period has no values then we don't want to send a notification
            // e.g. for a daily steps goal, if the last day, which is today, doesn't have steps
            // then we don't want to send a notification
            if (isDateToday(response.results.last().startDate) && response.results.last().values.isNotEmpty()) {
                val comparison = job.comparison
                val triggerValue = job.value
                val currentValue = response.results.last().values.last()
                when (comparison) {
                    BackgroundJob.ComparisonOperationEnum.EQUALS.id ->
                        willTriggerJob = currentValue == triggerValue

                    BackgroundJob.ComparisonOperationEnum.GREATER.id ->
                        willTriggerJob = currentValue > triggerValue

                    BackgroundJob.ComparisonOperationEnum.LESS.id ->
                        willTriggerJob = currentValue < triggerValue

                    BackgroundJob.ComparisonOperationEnum.GREATER_OR_EQUALS.id ->
                        willTriggerJob = currentValue >= triggerValue

                    BackgroundJob.ComparisonOperationEnum.LESS_OR_EQUALS.id ->
                        willTriggerJob = currentValue <= triggerValue
                }
            }
        }
        if (willTriggerJob) {
            onSendNotification(notification)
            return
        }
        onDoNothing()
    }

    private fun Calendar.startOfUnit(unit: Int): Calendar {
        val unitVal: Int = when (unit) {
            Calendar.HOUR -> 1
            Calendar.DATE -> 2
            Calendar.WEEK_OF_MONTH -> 3
            Calendar.MONTH -> 4
            Calendar.YEAR -> 5
            else -> return this
        }

        if (unitVal >= 5) {
            this.set(Calendar.MONTH, 0)
        }
        if (unitVal >= 4) {
            this.set(Calendar.DAY_OF_MONTH, 1)
        }
        if (unitVal == 3) {
            this.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        if (unitVal >= 2) {
            this.set(Calendar.HOUR_OF_DAY, 0)
        }
        this.set(Calendar.MILLISECOND, 0)
        this.set(Calendar.SECOND, 0)
        this.set(Calendar.MINUTE, 0)

        return this
    }

    private fun isDateToday(timestamp: Long): Boolean {
        // determine if timestamp falls inside current day
        val isToday = DateUtils.isToday(timestamp * 1000) // work with millis
        return isToday
    }

}