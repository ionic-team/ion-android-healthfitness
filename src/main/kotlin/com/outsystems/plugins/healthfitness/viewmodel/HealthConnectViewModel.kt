package com.outsystems.plugins.healthfitness.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outsystems.plugins.healthfitness.HealthConnectPermissionActivity
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.data.Constants
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_DENIED
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_GRANTED
import com.outsystems.plugins.healthfitness.data.Constants.REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryParameters
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessGroupPermission
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessPermission
import com.outsystems.plugins.healthfitness.data.types.HealthLastRecordResponse
import com.outsystems.plugins.healthfitness.helpers.ActivityTransitionHelperInterface
import com.outsystems.plugins.healthfitness.helpers.AlarmManagerHelperInterface
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelperInterface
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepositoryInterface
import com.outsystems.plugins.healthfitness.store.HealthStoreException
import com.outsystems.plugins.healthfitness.utils.HealthConnectSharedState
import kotlinx.coroutines.launch

class HealthConnectViewModel(
    private val healthConnectRepository: HealthConnectRepositoryInterface,
    private val healthConnectHelper: HealthConnectHelperInterface,
    private val alarmManagerHelper: AlarmManagerHelperInterface,
    private val activityTransitionHelper: ActivityTransitionHelperInterface
) :
    ViewModel() {

    fun initAndRequestPermissions(
        activity: Activity,
        customPermissions: Array<HealthFitnessPermission>,
        allVariables: HealthFitnessGroupPermission,
        fitnessVariables: HealthFitnessGroupPermission,
        healthVariables: HealthFitnessGroupPermission,
        profileVariables: HealthFitnessGroupPermission,
        privacyPolicyUrl: String,
        onHealthConnectAvailable: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        if (privacyPolicyUrl.isNotBlank()) {
            verifyHealthConnectClient(
                activity.applicationContext, HealthConnectSharedState,
                {
                    val permissionSet = healthConnectRepository.setPermissionVariables(
                        customPermissions,
                        allVariables,
                        fitnessVariables,
                        healthVariables,
                        profileVariables
                    ).toMutableSet()
                    appendReadHealthDataHistoryPermissionIfApplicable(permissionSet, activity)
                    val permissionIntent = Intent(
                        activity, HealthConnectPermissionActivity::class.java
                    )
                    permissionIntent.putStringArrayListExtra(
                        Constants.EXTRA_PERMISSION_LIST,
                        ArrayList(permissionSet.toList())
                    )
                    //We pass the privacy policy url to the HealthConnectPermissionActivity
                    permissionIntent.putExtra(
                        Constants.EXTRA_PRIVACY_POLICY_URL,
                        privacyPolicyUrl
                    )
                    onHealthConnectAvailable()
                    activity.startActivityForResult(
                        permissionIntent,
                        REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH
                    )
                },
                {
                    onError(it)
                }
            )
        } else {
            onError(HealthFitnessError.PRIVACY_POLICY_NOT_DEFINED_ERROR)
        }
    }

    private fun appendReadHealthDataHistoryPermissionIfApplicable(
        permissionSet: MutableSet<String>,
        activity: Activity
    ) {
        val healthConnectFeatures = HealthConnectSharedState.client?.features ?: run {
            return
        }
        if (healthConnectFeatures.getFeatureStatus(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY) !=
            HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
        ) {
            return
        }
        if (!hasReadHealthDataHistoryPermissionDefined(activity)) {
            return
        }
        permissionSet.add(HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY)
    }

    private fun hasReadHealthDataHistoryPermissionDefined(activity: Activity): Boolean {
        return try {
            val packageManager = activity.packageManager
            val permissionsInPackage = packageManager.getPackageInfo(
                activity.packageName,
                PackageManager.GET_PERMISSIONS
            ).requestedPermissions ?: arrayOf()
            permissionsInPackage.find {
                it == HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
            } != null
        } catch (e: Exception) {
            false
        }
    }

    fun getLastRecord(
        healthRecord: HealthRecord,
        onSuccess: (HealthLastRecordResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        HealthConnectSharedState.client?.let {
            viewModelScope.launch {
                healthConnectRepository.getLastRecord(
                    healthRecord,
                    it,
                    healthConnectHelper,
                    {
                        onSuccess(it)
                    },
                    {
                        onError(it)
                    }
                )
            }
        } ?: onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR)
    }

    fun writeData(
        healthRecord: HealthRecord,
        value: Double,
        packageName: String,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        HealthConnectSharedState.client?.let {
            viewModelScope.launch {
                healthConnectRepository.writeData(
                    healthRecord,
                    value,
                    it,
                    healthConnectHelper,
                    packageName,
                    {
                        onSuccess()
                    },
                    {
                        onError(it)
                    }
                )
            }
        } ?: onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR)
    }

    @RequiresApi(35)
    fun requestReadDataBackgroundPermission(
        activity: Activity
    ) {
        val permissionIntent = Intent(
            activity, HealthConnectPermissionActivity::class.java
        )
        permissionIntent.putStringArrayListExtra(
            Constants.EXTRA_PERMISSION_LIST,
            arrayListOf(HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND)
        )
        activity.startActivityForResult(
            permissionIntent,
            REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH
        )
    }

    fun setBackgroundJob(
        parameters: BackgroundJobParameters,
        foregroundNotificationTitle: String,
        foregroundNotificationDescription: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        HealthConnectSharedState.client?.let {
            viewModelScope.launch {
                healthConnectRepository.setBackgroundJob(
                    parameters,
                    foregroundNotificationTitle,
                    foregroundNotificationDescription,
                    it,
                    healthConnectHelper,
                    alarmManagerHelper,
                    activityTransitionHelper,
                    context,
                    {
                        onSuccess()
                    },
                    {
                        onError(it)
                    }
                )
            }
        } ?: onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR)
    }

    fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectRepository.updateBackgroundJob(
            parameters,
            {
                onSuccess()
            },
            {
                onError(it)
            }
        )
    }

    fun deleteBackgroundJob(
        jobId: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectRepository.deleteBackgroundJob(
            jobId,
            context,
            alarmManagerHelper,
            activityTransitionHelper,
            {
                onSuccess()
            },
            {
                onError(it)
            }
        )
    }

    fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectRepository.listBackgroundJobs(
            {
                onSuccess(it)
            },
            {
                onError(it)
            }
        )
    }

    fun disconnectFromHealthConnect(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        verifyHealthConnectClient(
            activity.applicationContext,
            HealthConnectSharedState,
            {
                HealthConnectSharedState.client?.let { client ->
                    viewModelScope.launch {
                        healthConnectRepository.disconnectFromHealthConnect(
                            client,
                            {
                                onSuccess()
                            },
                            {
                                onError(it)
                            }
                        )
                    }
                } ?: onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_ERROR)
            },
            {
                onError(it)
            }
        )
    }

    fun openHealthConnect(
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectRepository.openHealthConnect(
            context,
            {
                onSuccess()
            },
            {
                onError(it)
            }
        )
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        if (requestCode == REQUEST_PERMISSION_ACTIVITY_KEY_HEALTH) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    when (intent?.extras?.getInt(Constants.EXTRA_RESULT_PERMISSION_KEY_GLOBAL)) {
                        EXTRA_RESULT_PERMISSION_GRANTED -> {
                            onSuccess()
                        }

                        EXTRA_RESULT_PERMISSION_DENIED -> {
                            onError(HealthFitnessError.PERMISSIONS_DENIED_ERROR)
                        }
                    }
                }
            }
        }

    }

    private fun verifyHealthConnectClient(
        context: Context,
        healthConnectShareState: HealthConnectSharedState,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        val availabilityStatus = healthConnectHelper.getSdkStatus(context)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            healthConnectShareState.client = null
            onError(HealthFitnessError.HEALTH_CONNECT_NOT_AVAILABLE_ERROR)
            return
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            healthConnectShareState.client = null
            onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR)
            return
        }
        healthConnectShareState.client = healthConnectHelper.getOrCreateClient(context)
        onSuccess()
    }

    fun advancedQuery(
        parameters: HealthAdvancedQueryParameters,
        context: Context,
        onSuccess: (HealthAdvancedQueryResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        viewModelScope.launch {
            try {
                HealthConnectSharedState.client?.let {
                    val response = healthConnectRepository.advancedQuery(
                        parameters,
                        it,
                        context
                    )
                    onSuccess(response)
                }
            } catch (e: HealthStoreException) {
                onError(e.error)
            } catch (e: Exception) {
                onError(HealthFitnessError.READ_DATA_ERROR)
            }
        }
    }
}