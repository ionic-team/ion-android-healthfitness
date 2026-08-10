package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.Notification
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.mock.ActivityTransitionHelperMock
import com.outsystems.plugins.healthfitness.mock.AlarmManagerHelperMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectViewModel
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

