package io.ionic.libs.ionhealthfitnesslib

import android.app.Activity
import androidx.health.connect.client.HealthConnectClient
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError
import io.ionic.libs.ionhealthfitnesslib.mock.ActivityTransitionHelperMock
import io.ionic.libs.ionhealthfitnesslib.mock.AlarmManagerHelperMock
import io.ionic.libs.ionhealthfitnesslib.mock.DatabaseManagerMock
import io.ionic.libs.ionhealthfitnesslib.mock.HealthConnectHelperMock
import io.ionic.libs.ionhealthfitnesslib.repository.HealthConnectRepository
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManager
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class SetBackgroundJobTest {

    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val healthConnectViewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)
    private val healthConnectClientMock: HealthConnectClient = Mockito.mock(HealthConnectClient::class.java)
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)

    companion object {
        const val FOREGROUND_NOTIFICATION_TITLE = "Measuring your health and fitness data."
        const val FOREGROUND_NOTIFICATION_DESCRIPTION = "The app is running on the background."
    }

    @Test
    fun givenInvalidVariableWhenSetBackgroundJobThenVariableNotAvailableError() {

        val parameters = BackgroundJobParameters(
            "Test",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message, it.message)
                }
            )
        }
    }
    @Test
    fun givenVariableWithoutPermissionsWhenSetBackgroundJobThenVariableNotAuthorizedError() {

        healthConnectHelperMock.variableHasPermissions = false
        val parameters = BackgroundJobParameters(
            "HEART_RATE",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.READ_DATA_NO_PERMISSIONS.code, it.code)
                    Assert.assertEquals(HealthFitnessError.READ_DATA_NO_PERMISSIONS.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenExistentBackgroundJobWhenSetBackgroundJobThenBackgroundJobAlreadyExistsError() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = true

        val parameters = BackgroundJobParameters(
            "HEART_RATE",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValidAlarmVariableValidValueWhenSetBackgroundJobThenSomeError() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        alarmManagerHelperMock.shouldThrowException = true

        val parameters = BackgroundJobParameters(
            "WEIGHT",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValidActivityVariableValidValueWhenSetBackgroundJobThenSomeError() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        activityTransitionHelperMock.shouldSendError = true

        val parameters = BackgroundJobParameters(
            "STEPS",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.code, it.code)
                    Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.message, it.message)
                }
            )
        }
    }

    @Test
    fun givenValidAlarmVariableValidValueImmediateWhenSetBackgroundJobThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        alarmManagerHelperMock.shouldThrowException = false

        val parameters = BackgroundJobParameters(
            "WEIGHT",
            "0",
            "HIGHER",
            "TIME",
            1,
            "IMMEDIATE",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    // do nothing
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenValidActivityVariableValidValueImmediateWhenSetBackgroundJobThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        activityTransitionHelperMock.shouldSendError = false

        val parameters = BackgroundJobParameters(
            "STEPS",
            "0",
            "HIGHER",
            "TIME",
            1,
            "IMMEDIATE",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    // do nothing
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenValidAlarmVariableValidValueHourWhenSetBackgroundJobThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        alarmManagerHelperMock.shouldThrowException = false

        val parameters = BackgroundJobParameters(
            "WEIGHT",
            "0",
            "HIGHER",
            "TIME",
            1,
            "HOUR",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    // do nothing
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenValidAlarmVariableValidValueDayWhenSetBackgroundJobThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        alarmManagerHelperMock.shouldThrowException = false

        val parameters = BackgroundJobParameters(
            "WEIGHT",
            "0",
            "HIGHER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {
            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    // do nothing
                },
                {
                    Assert.fail()
                }
            )
        }
    }

    @Test
    fun givenValidAlarmVariableValidValueWeekAndAndroid15WhenSetBackgroundJobThenSuccess() {
        healthConnectHelperMock.variableHasPermissions = true
        databaseMock.backgroundJobExists = false
        alarmManagerHelperMock.shouldThrowException = false

        val parameters = BackgroundJobParameters(
            "WEIGHT",
            "0",
            "HIGHER",
            "TIME",
            1,
            "WEEK",
            "DAY",
            1,
            "Header",
            "Body"
        )

        runBlocking {

            healthConnectViewModel.requestReadDataBackgroundPermission(mockActivity)

            repository.setBackgroundJob(
                parameters,
                FOREGROUND_NOTIFICATION_TITLE,
                FOREGROUND_NOTIFICATION_DESCRIPTION,
                healthConnectClientMock,
                healthConnectHelperMock,
                alarmManagerHelperMock,
                activityTransitionHelperMock,
                mockActivity,
                {
                    // do nothing
                },
                {
                    Assert.fail()
                }
            )
        }
    }

}