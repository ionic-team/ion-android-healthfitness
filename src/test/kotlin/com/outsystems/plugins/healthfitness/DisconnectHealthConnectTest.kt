package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.mock.ActivityTransitionHelperMock
import com.outsystems.plugins.healthfitness.mock.AlarmManagerHelperMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class DisconnectHealthConnectTest {

    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val healthConnectClientMock: HealthConnectClient = Mockito.mock(HealthConnectClient::class.java)
    private val permissionControllerMock: PermissionController = Mockito.mock(PermissionController::class.java)
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val viewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)
    private val mockContext: Context = Mockito.mock(Context::class.java)

    @Test
    fun givenHealthConnectNotAvailableWhenDisconnectingThenError() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_UNAVAILABLE

        viewModel.disconnectFromHealthConnect(
            mockActivity,
            {
                Assert.fail()
            },
            {
                Assert.assertEquals(
                    HealthFitnessError.HEALTH_CONNECT_NOT_AVAILABLE_ERROR.code,
                    it.code
                )
                Assert.assertEquals(
                    HealthFitnessError.HEALTH_CONNECT_NOT_AVAILABLE_ERROR.message,
                    it.message
                )
            }
        )
    }

    @Test
    fun givenHealthConnectNotInstalledWhenDisconnectingThenError() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED

        viewModel.disconnectFromHealthConnect(
            mockActivity,
            {
                Assert.fail()
            },
            {
                Assert.assertEquals(
                    HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR.code,
                    it.code
                )
                Assert.assertEquals(
                    HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR.message,
                    it.message
                )
            }
        )
    }

    @Test
    fun givenNullPermissionControllerWhenDisconnectingThenError() {
        runBlocking {
            try {
                repository.disconnectFromHealthConnect(
                    healthConnectClientMock,
                    {
                        Assert.fail()
                    },
                    {
                        Assert.assertEquals(
                            HealthFitnessError.DISABLE_HEALTH_CONNECT_ERROR.code,
                            it.code
                        )
                        Assert.assertEquals(
                            HealthFitnessError.DISABLE_HEALTH_CONNECT_ERROR.message,
                            it.message
                        )
                    }
                )
            }
            catch (ignored: Exception) {
                Assert.fail()
            }
        }
    }

    @Test
    fun givenValidPermissionControllerWhenDisconnectingThenSuccess() {
        `when`(healthConnectClientMock.permissionController).thenReturn(permissionControllerMock)
        runBlocking {
            try {
                repository.disconnectFromHealthConnect(
                    healthConnectClientMock,
                    {
                        // do nothing, success
                    },
                    {
                        Assert.fail()
                    }
                )
            }
            catch (ignored: Exception) {
                Assert.fail()
            }
        }
    }

}