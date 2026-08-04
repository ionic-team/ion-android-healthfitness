package io.ionic.libs.ionhealthfitnesslib.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManagerInterface
import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobsResponse
import io.ionic.libs.ionhealthfitnesslib.background.UpdateBackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryParameters
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponse
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessGroupPermission
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessPermission
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthLastRecordResponse
import io.ionic.libs.ionhealthfitnesslib.helpers.ActivityTransitionHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.AlarmManagerHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface

class HealthConnectRepository constructor(private val healthConnectDataManager: HealthConnectDataManagerInterface) :
    HealthConnectRepositoryInterface {

    override fun setPermissionVariables(
        customPermissions: Array<HealthFitnessPermission>,
        allVariables: HealthFitnessGroupPermission,
        fitnessVariables: HealthFitnessGroupPermission,
        healthVariables: HealthFitnessGroupPermission,
        profileVariables: HealthFitnessGroupPermission
    ): Set<String> {
        return healthConnectDataManager.setPermissionVariables(
            customPermissions,
            allVariables,
            fitnessVariables,
            healthVariables,
            profileVariables
        )
    }

    override suspend fun getLastRecord(
        healthRecord: HealthRecord,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        onSuccess: (HealthLastRecordResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.getLastRecord(
            healthRecord,
            healthConnectClient,
            healthConnectHelper,
            {
                onSuccess(it)
            },
            {
                onError(it)
            }
        )
    }

    override suspend fun writeData(
        healthRecord: HealthRecord,
        value: Double,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        packageName: String,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.writeData(
            healthRecord,
            value,
            healthConnectClient,
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

    override suspend fun setBackgroundJob(
        parameters: BackgroundJobParameters,
        foregroundNotificationTitle: String,
        foregroundNotificationDescription: String,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        alarmManagerHelper: AlarmManagerHelperInterface,
        activityTransitionHelper: ActivityTransitionHelperInterface,
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.setBackgroundJob(
            parameters,
            foregroundNotificationTitle,
            foregroundNotificationDescription,
            healthConnectClient,
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

    override fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.updateBackgroundJob(
            parameters,
            {
                onSuccess()
            },
            {
                onError(it)
            }
        )
    }

    override fun deleteBackgroundJob(
        jobId: String,
        context: Context,
        alarmManagerHelper: AlarmManagerHelperInterface,
        activityTransitionHelper: ActivityTransitionHelperInterface,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.deleteBackgroundJob(
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

    override fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.listBackgroundJobs(
            {
                onSuccess(it)
            },
            {
                onError(it)
            }
        )
    }
    override suspend fun disconnectFromHealthConnect(
        healthConnectClient: HealthConnectClient,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        try {
            healthConnectClient.permissionController.revokeAllPermissions()
            onSuccess()
        }
        catch(e: Exception) {
            onError(HealthFitnessError.DISABLE_HEALTH_CONNECT_ERROR)
        }
    }

    override suspend fun advancedQuery(
        parameters: HealthAdvancedQueryParameters,
        client: HealthConnectClient,
        context: Context
    ): HealthAdvancedQueryResponse {
        return healthConnectDataManager.advancedQuery(parameters, client, context)
    }

    override fun openHealthConnect(
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        healthConnectDataManager.openHealthConnect(
            context,
            {
                onSuccess()
            },
            {
                onError(it)
            }
        )
    }
}