package io.ionic.libs.ionhealthfitnesslib

import android.app.Activity
import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
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