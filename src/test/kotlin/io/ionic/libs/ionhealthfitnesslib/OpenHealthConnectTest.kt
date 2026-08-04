package io.ionic.libs.ionhealthfitnesslib

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import io.ionic.libs.ionhealthfitnesslib.data.Constants
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

class OpenHealthConnectTest {

    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val healthConnectViewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)
    private val mockPackageManager: PackageManager = Mockito.mock(PackageManager::class.java)
    private val mockIntent: Intent = Mockito.mock(Intent::class.java)

    @Test
    fun givenSomeExceptionWhenOpenHealthConnectThenOpenHealthConnectError() {
        healthConnectViewModel.openHealthConnect(
            mockActivity,
            {
                Assert.fail()
            },
            {
                Assert.assertEquals(HealthFitnessError.OPEN_HEALTH_CONNECT_ERROR.code, it.code)
                Assert.assertEquals(HealthFitnessError.OPEN_HEALTH_CONNECT_ERROR.message, it.message)
            }
        )
    }

    @Test
    fun givenIntentNullWhenOpenHealthConnectThenHealthConnectNotInstalledError() {
        Mockito.doReturn(mockPackageManager).`when`(mockActivity).packageManager
        Mockito.doReturn(null).`when`(mockPackageManager).getLaunchIntentForPackage(Constants.HEALTH_CONNECT_APP_PACKAGE)
        healthConnectViewModel.openHealthConnect(
            mockActivity,
            {
                Assert.fail()
            },
            {
                Assert.assertEquals(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_ERROR.code, it.code)
                Assert.assertEquals(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_ERROR.message, it.message)
            }
        )
    }

    @Test
    fun givenIntentValidWhenOpenHealthConnectThenSuccess() {
        Mockito.doReturn(mockPackageManager).`when`(mockActivity).packageManager
        Mockito.doReturn(mockIntent).`when`(mockPackageManager).getLaunchIntentForPackage(Constants.HEALTH_CONNECT_APP_PACKAGE)
        healthConnectViewModel.openHealthConnect(
            mockActivity,
            {
                // do nothing, the test should pass here and simply pass
            },
            {
                Assert.fail()
            }
        )
    }



}