package io.ionic.libs.ionhealthfitnesslib

import io.ionic.libs.ionhealthfitnesslib.background.UpdateBackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.background.database.BackgroundJob
import io.ionic.libs.ionhealthfitnesslib.background.database.Notification
import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError
import io.ionic.libs.ionhealthfitnesslib.mock.ActivityTransitionHelperMock
import io.ionic.libs.ionhealthfitnesslib.mock.AlarmManagerHelperMock
import io.ionic.libs.ionhealthfitnesslib.mock.DatabaseManagerMock
import io.ionic.libs.ionhealthfitnesslib.mock.HealthConnectHelperMock
import io.ionic.libs.ionhealthfitnesslib.repository.HealthConnectRepository
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManager
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectViewModel
import org.junit.Assert
import org.junit.Test

class UpdateBackgroundJobsTest {

    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val viewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)

    companion object {
        private const val SAMPLE_ID = "25aea7f8-9c73-4ca8-97d4-ad63d7e1b854"
    }

    // new tests
    @Test
    fun givenNonExistentBackgroundJobWhenUpdateBackgroundJobThenBackgroundJobDoesNotExistError() {
        databaseMock.backgroundJobExists = false
        val parameters = UpdateBackgroundJobParameters(
            SAMPLE_ID,
            100F,
            "HIGHER",
            "DAY",
            1,
            true,
            "Header",
            "Body"
        )
        viewModel.updateBackgroundJob(parameters,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXIST_ERROR.code, error.code)
                Assert.assertEquals(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXIST_ERROR.message, error.message)
            }
        )
    }

    @Test
    fun givenExistentBackgroundJobWhenUpdateBackgroundJobThenSomeError() {
        databaseMock.backgroundJobExists = true
        databaseMock.databaseHasError = true
        val parameters = UpdateBackgroundJobParameters(
            SAMPLE_ID,
            100F,
            "HIGHER",
            "DAY",
            1,
            true,
            "Header",
            "Body"
        )
        viewModel.updateBackgroundJob(parameters,
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(HealthFitnessError.UPDATE_BACKGROUND_JOB_GENERIC_ERROR.code, error.code)
                Assert.assertEquals(HealthFitnessError.UPDATE_BACKGROUND_JOB_GENERIC_ERROR.message, error.message)
            }
        )
    }

    @Test
    fun givenExistentBackgroundJobWhenUpdateBackgroundJobThenSuccess() {
        databaseMock.backgroundJobExists = true
        databaseMock.databaseHasError = false
        databaseMock.backgroundJobs.add(BackgroundJob().apply { notificationId = 1234134L })
        databaseMock.notifications.add(Notification())

        val parameters = UpdateBackgroundJobParameters(
            SAMPLE_ID,
            100F,
            "HIGHER",
            "DAY",
            1,
            true,
            "Header",
            "Body"
        )

        viewModel.updateBackgroundJob(parameters,
            {
                // do nothing
            },
            {
                Assert.fail()
            }
        )
    }

}

