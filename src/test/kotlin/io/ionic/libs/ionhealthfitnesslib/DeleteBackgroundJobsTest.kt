package io.ionic.libs.ionhealthfitnesslib

import android.app.Activity
import io.ionic.libs.ionhealthfitnesslib.background.database.BackgroundJob
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

