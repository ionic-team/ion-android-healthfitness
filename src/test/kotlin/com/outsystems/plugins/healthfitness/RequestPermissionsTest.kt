package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.health.connect.client.HealthConnectClient
import com.outsystems.plugins.healthfitness.data.Constants
import com.outsystems.plugins.healthfitness.data.HealthEnumAccessType
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessGroupPermission
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessPermission
import com.outsystems.plugins.healthfitness.mock.ActivityTransitionHelperMock
import com.outsystems.plugins.healthfitness.mock.AlarmManagerHelperMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthConnectHelperMock
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.store.HealthStoreException
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectViewModel
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class RequestPermissionsTest {
    private val databaseMock = DatabaseManagerMock()
    private val dataManager = HealthConnectDataManager(databaseMock)
    private val repository = HealthConnectRepository(dataManager)
    private val healthConnectHelperMock = HealthConnectHelperMock()
    private val alarmManagerHelperMock = AlarmManagerHelperMock()
    private val activityTransitionHelperMock = ActivityTransitionHelperMock()
    private val viewModel = HealthConnectViewModel(repository, healthConnectHelperMock, alarmManagerHelperMock, activityTransitionHelperMock)
    private val mockActivity: Activity = Mockito.mock(Activity::class.java)
    private val mockContext: Context = Mockito.mock(Context::class.java)
    private val privacyPolicyFakeUrl = "https://fakeurl.com"
    private val accessTypes = listOf("READ", "READWRITE", "WRITE")


    @Test
    fun givenHealthConnectAvailableAndValidAllVariablesWhenRequestingPermissionsThenSuccess() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_AVAILABLE
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        for(access in accessTypes) {
            val customPermissions =
                arrayOf(HealthFitnessPermission(HealthRecord.STEPS, HealthEnumAccessType.READ))
            val groupPermissions = HealthFitnessGroupPermission(true, access)

            try {
                viewModel.initAndRequestPermissions(
                    mockActivity,
                    customPermissions,
                    groupPermissions,
                    groupPermissions,
                    groupPermissions,
                    groupPermissions,
                    privacyPolicyFakeUrl,
                    {
                        // do nothing, we want to continue
                    },
                    {
                        Assert.fail()
                    }
                )
            } catch (hse: HealthStoreException) {
                Assert.fail()
            }
        }
    }

    @Test
    fun givenHealthConnectAvailableAndValidAllGroupVariablesWhenRequestingPermissionsThenSuccess() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_AVAILABLE
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        val customPermissions = arrayOf(HealthFitnessPermission(HealthRecord.HEART_RATE, HealthEnumAccessType.READ))

        for(access in accessTypes) {
            val groupPermissions = HealthFitnessGroupPermission(true, access)
            val allVariablePermissions = HealthFitnessGroupPermission(false, access)

            try {
                viewModel.initAndRequestPermissions(
                    mockActivity,
                    customPermissions,
                    allVariablePermissions,
                    groupPermissions,
                    groupPermissions,
                    groupPermissions,
                    privacyPolicyFakeUrl,
                    {
                        // do nothing, we want to continue
                    },
                    {
                        Assert.fail()
                    }
                )
            } catch (hse: HealthStoreException) {
                Assert.fail()
            }
        }
    }

    @Test
     fun `given HealthConnect Available And Valid CustomVariables then Success`() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_AVAILABLE
        healthConnectHelperMock.apply {
            healthClientMock = Mockito.mock(HealthConnectClient::class.java)
        }
        val customPermissions = arrayOf(
            HealthFitnessPermission(HealthRecord.HEART_RATE, HealthEnumAccessType.READ),
            HealthFitnessPermission(HealthRecord.STEPS, HealthEnumAccessType.READ)
        )

        for(access in accessTypes){
            val groupPermissions = HealthFitnessGroupPermission(false, access)
            val allVariablePermissions = HealthFitnessGroupPermission(false, access)

            try {
                viewModel.initAndRequestPermissions(
                    mockActivity,
                    customPermissions,
                    allVariablePermissions,
                    groupPermissions,
                    groupPermissions,
                    groupPermissions,
                    privacyPolicyFakeUrl,
                    {
                        // do nothing, we want to continue
                    },
                    {
                        Assert.fail()
                    }
                )
            }
            catch(hse: HealthStoreException) {
                Assert.fail()
            }
        }

    }


    @Test
    fun givenHealthConnectNotAvailableWhenRequestingPermissionsThenError() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_UNAVAILABLE
        val customPermissions = arrayOf(
            HealthFitnessPermission(HealthRecord.HEART_RATE, HealthEnumAccessType.READ),
            HealthFitnessPermission(HealthRecord.STEPS, HealthEnumAccessType.READ)
        )
        val groupPermissions = HealthFitnessGroupPermission(false, "READ")
        val allVariablePermissions = HealthFitnessGroupPermission(false, "READ")

        try {
            viewModel.initAndRequestPermissions(
                mockActivity,
                customPermissions,
                allVariablePermissions,
                groupPermissions,
                groupPermissions,
                groupPermissions,
                privacyPolicyFakeUrl,
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
        catch(hse: HealthStoreException) {
            Assert.fail()
        }
    }

    @Test
    fun givenHealthConnectNotInstalledWhenRequestingPermissionsThenError() {
        Mockito.doReturn(mockContext).`when`(mockActivity).applicationContext
        healthConnectHelperMock.sdkStatus = HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
        val customPermissions = arrayOf(
            HealthFitnessPermission(HealthRecord.HEART_RATE, HealthEnumAccessType.READ),
            HealthFitnessPermission(HealthRecord.STEPS, HealthEnumAccessType.READ)
        )
        val groupPermissions = HealthFitnessGroupPermission(false, "READ")
        val allVariablePermissions = HealthFitnessGroupPermission(false, "READ")

        try {
            viewModel.initAndRequestPermissions(
                mockActivity,
                customPermissions,
                allVariablePermissions,
                groupPermissions,
                groupPermissions,
                groupPermissions,
                privacyPolicyFakeUrl,
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
        catch(hse: HealthStoreException) {
            Assert.fail()
        }
    }

    // tests on handleActivityResult
    @Test
    fun givenPermissionsDeniedWhenHandleActivityResultThenError() {
        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        Mockito.doReturn(Constants.EXTRA_RESULT_PERMISSION_DENIED).`when`(mockBundle).getInt(Constants.EXTRA_RESULT_PERMISSION_KEY_GLOBAL)
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras

        viewModel.handleActivityResult(
            Constants.REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH, Activity.RESULT_OK, mockIntent,
            {
                Assert.fail()
            },
            {
                Assert.assertEquals(
                    HealthFitnessError.PERMISSIONS_DENIED_ERROR.code,
                    it.code
                )
                Assert.assertEquals(
                    HealthFitnessError.PERMISSIONS_DENIED_ERROR.message,
                    it.message
                )
            }
        )
    }

    @Test
    fun givenPermissionsGrantedWhenHandleActivityResultThenSuccess() {
        val mockIntent = Mockito.mock(Intent::class.java)
        val mockBundle = Mockito.mock(Bundle::class.java)

        Mockito.doReturn(Constants.EXTRA_RESULT_PERMISSION_GRANTED).`when`(mockBundle).getInt(Constants.EXTRA_RESULT_PERMISSION_KEY_GLOBAL)
        Mockito.doReturn(mockBundle).`when`(mockIntent).extras

        viewModel.handleActivityResult(
            Constants.REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH, Activity.RESULT_OK, mockIntent,
            {
                // do nothing, should pass
            },
            {
                Assert.fail()
            }
        )
    }

}