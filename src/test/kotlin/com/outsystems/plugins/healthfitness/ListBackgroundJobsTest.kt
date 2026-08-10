package com.outsystems.plugins.healthfitness

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

class ListBackgroundJobsTest {

    @Test
    fun givenExistsBackgroundJobsWhenListBackgroundJobsThenSomeError() {

        val databaseMock = DatabaseManagerMock().apply {
            databaseHasError = true
        }
        val dataManager = HealthConnectDataManager(databaseMock)
        val repository = HealthConnectRepository(dataManager)
        val healthConnectHelperMock = HealthConnectHelperMock()
        val alarmManagerHelperMock = AlarmManagerHelperMock()
        val activityTransitionHelperMock = ActivityTransitionHelperMock()
        val viewModel =
            HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)

        viewModel.listBackgroundJobs(
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(error.code, HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR.message)
            })
    }

    @Test
    fun givenNoBackgroundJobsWhenListBackgroundJobsThenSuccess() {
        val databaseMock = DatabaseManagerMock()
        val dataManager = HealthConnectDataManager(databaseMock)
        val repository = HealthConnectRepository(dataManager)
        val healthConnectHelperMock = HealthConnectHelperMock()
        val alarmManagerHelperMock = AlarmManagerHelperMock()
        val activityTransitionHelperMock = ActivityTransitionHelperMock()
        val viewModel =
            HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)

        viewModel.listBackgroundJobs(
            onSuccess = { response ->
                Assert.assertTrue(response.results.isEmpty())
                Assert.assertEquals(response.results.size, 0)
            },
            onError = {
                Assert.fail()
            }
        )
    }

    @Test
    fun givenExistsBackgroundJobsWhenListBackgroundJobsThenSuccess() {
        val databaseMock = DatabaseManagerMock().apply {
            hasBackgroundJobs = true
        }
        val dataManager = HealthConnectDataManager(databaseMock)
        val repository = HealthConnectRepository(dataManager)
        val healthConnectHelperMock = HealthConnectHelperMock()
        val alarmManagerHelperMock = AlarmManagerHelperMock()
        val activityTransitionHelperMock = ActivityTransitionHelperMock()
        val viewModel =
            HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)

        viewModel.listBackgroundJobs(
            onSuccess = { response ->
                Assert.assertTrue(response.results.isNotEmpty())
                Assert.assertEquals(response.results.size, 1)
            },
            onError = {
                Assert.fail()
            }
        )
    }

}

