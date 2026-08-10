package com.outsystems.plugins.healthfitness

import android.app.Activity
import androidx.health.connect.client.HealthConnectClient
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.Notification
import com.outsystems.plugins.healthfitness.background.managers.BackgroundJobManager
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponseBlock
import com.outsystems.plugins.healthfitness.data.types.HealthDataPoint
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectDataManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.util.Date

class NotificationFrequencyTest {

    private val backgroundJobId = "some-background-job-id"
    private val variableName = "STEPS"
    private val responseValue = 10.0f
    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val backgroundJobManager = BackgroundJobManager(healthConnectHelperMock, dataManager)
    private val mockActivity = Mockito.mock(Activity::class.java)
    private val endDate: Long = Date().time
    private val month = 2592000000
    private val startDate: Long = endDate - month
    private val endDateNotToday = endDate - month
    private val startDateNotToday = endDateNotToday - month
    private val startDateInFuture = endDate + month
    private val endDateInFuture = startDateInFuture + month

    @Test
    fun givenNoBackgroundJobWhenProcessBackgroundJobsThenStopService(){
        databaseMock.apply {
            backgroundJobExists = false
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = variableName
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                }
            )
        }
        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenInvalidVariableWhenProcessBackgroundJobsThenStopProcessing(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = "DOES_NOT_EXIST"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                }
            )
        }

        // for some reason we have to do this
        HealthRecordsObject.healthConnectVariables

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenJobInactiveWhenProcessBackgroundJobsThenStopProcessing(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = false
                    variable = "STEPS"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.STEPS.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenJobInactiveAndRawOperationWhenProcessBackgroundJobsThenStopProcessing(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = false
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotTimeToNotifyNorShouldAlwaysWhenProcessBackgroundJobsThenStopProcessing(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "MINUTE" // so that it shouldn't always notify
                    nextNotificationTimestamp = System.currentTimeMillis() + 100000 // so that it isn't time to notify yet
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotTimeToNotifyButShouldAlwaysWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                    nextNotificationTimestamp = System.currentTimeMillis() + 100000 // so that it isn't time to notify yet
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencySecondWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "SECOND"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencyMinuteWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "MINUTE"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencyHourWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "HOUR"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencyDayWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "DAY"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencyWeekWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "WEEK"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenNotShouldAlwaysButTimeToNotifyAndFrequencyMonthWhenProcessBackgroundJobsThenSendNotification(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "MONTH"
                }
            )
        }


        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backgroundJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenResultEmptyWhenProcessBackgroundJobsThenDoNothing(){
        val results = mutableListOf<HealthAdvancedQueryResponseBlock>()
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenValuesEmptyWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf<Float>()
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenDateNotTodayWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDateNotToday, endDateNotToday, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenDateNotTodayAndHourWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDateNotToday, endDateNotToday, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "HOUR"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenDateInFutureWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDateInFuture, endDateInFuture, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionEqualMetWhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionEqualNotMetWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 3.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionGreaterMetWhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 11.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.GREATER.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionGreaterNotMetWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 3.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.GREATER.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionLesserMetWhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 3.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionLesserNotMetWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 11.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionGreaterOrEqualMetWhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 12.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.GREATER_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionGreaterOrEqualMet2WhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.GREATER_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionGreaterOrEqualNotMetWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 2.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.GREATER_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionLesserOrEqualMetWhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 2.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionLesserOrEqualMet2WhenProcessBackgroundJobsThenSendNotification(){
        val values = mutableListOf(2.0f, 10.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.assertTrue(true)
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenConditionLesserOrEqualNotMetWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 11.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "YEAR"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

    @Test
    fun givenConditionLesserOrEqualNotMetAndWeekOfMonthWhenProcessBackgroundJobsThenDoNothing(){
        val values = mutableListOf(2.0f, 11.0f)
        val results = mutableListOf(HealthAdvancedQueryResponseBlock(1, startDate, endDate, values))
        val resultDataPoints = mutableListOf(HealthDataPoint("someLabel", "someValue"))
        val dataManagerMock = HealthConnectDataManagerMock<Any?>().apply {
            queryResponse = HealthAdvancedQueryResponse(
                results,
                resultDataPoints
            )
            backgroundJobs.add(
                BackgroundJob().apply {
                    isActive = true
                    variable = "HEART_RATE"
                    comparison = BackgroundJob.ComparisonOperationEnum.LESS_OR_EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "WEEK"
                    timeUnit = "DAY"
                }
            )
            notifications.add(
                Notification()
            )
        }
        val backJobManager = BackgroundJobManager(healthConnectHelperMock, dataManagerMock)
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        //necessary for tests because healthConnectVariables holds lazy variables
        HealthRecordsObject.healthConnectVariables[HealthRecord.HEART_RATE.name]!!.value

        runBlocking {
            backJobManager.processBackgroundJob(
                mockActivity,
                backgroundJobId,
                {
                    Assert.fail()
                },
                {
                    Assert.assertTrue(true)
                }
            )
        }
    }

}