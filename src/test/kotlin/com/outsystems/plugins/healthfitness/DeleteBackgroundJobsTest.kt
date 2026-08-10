package com.outsystems.plugins.healthfitness

import android.app.Activity
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
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
import org.mockito.Mockito

class DeleteBackgroundJobsTest {

    private val databaseMock = DatabaseManagerMock()
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val viewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)

    companion object {
        private const val SAMPLE_ID = "25aea7f8-9c73-4ca8-97d4-ad63d7e1b854"
    }

    @Test
    fun givenNonExistentBackgroundJobWhenDeleteBackgroundJobThenBackgroundJobDoesNotExistError() {
        databaseMock.backgroundJobExists = false

        viewModel.deleteBackgroundJob(
            jobId = SAMPLE_ID,
            context = mockActivity,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXIST_ERROR.let {
                    Assert.assertEquals(error.code, it.code)
                    Assert.assertEquals(error.message, it.message)
                }
            }
        )
    }

    @Test
    fun givenExistentBackgroundJobAWhenDeleteBackgroundJobThenSomeError() {
        databaseMock.backgroundJobExists = true
        databaseMock.databaseHasError = true

        viewModel.deleteBackgroundJob(
            jobId = SAMPLE_ID,
            context = mockActivity,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                HealthFitnessError.DELETE_BACKGROUND_JOB_GENERIC_ERROR.let {
                    Assert.assertEquals(error.code, it.code)
                    Assert.assertEquals(error.message, it.message)
                }
            }
        )
    }

    @Test
    fun givenExistentBackgroundJobAndAlarmVariableWhenDeleteBackgroundJobThenSuccess() {
        databaseMock.backgroundJobExists = true
        databaseMock.databaseHasError = false
        databaseMock.backgroundJobs.add(BackgroundJob().apply { this.variable = "WEIGHT" })

        viewModel.deleteBackgroundJob(
            jobId = SAMPLE_ID,
            context = mockActivity,
            onSuccess = {
                Assert.assertTrue(true)
            },
            onError = {
                Assert.fail()
            }
        )
    }

    @Test
    fun givenExistentBackgroundJobAndActivityVariableWhenDeleteBackgroundJobThenSuccess() {
        databaseMock.backgroundJobExists = true
        databaseMock.databaseHasError = false
        databaseMock.backgroundJobs.add(BackgroundJob().apply { this.variable = "STEPS" })

        viewModel.deleteBackgroundJob(
            jobId = SAMPLE_ID,
            context = mockActivity,
            onSuccess = {
                Assert.assertTrue(true)
            },
            onError = {
                Assert.fail()
            }
        )
    }

}

