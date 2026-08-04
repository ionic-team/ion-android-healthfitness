package io.ionic.libs.ionhealthfitnesslib.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobsResponse
import io.ionic.libs.ionhealthfitnesslib.background.UpdateBackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryParameters
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponse
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessGroupPermission
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessPermission
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthLastRecordResponse
import io.ionic.libs.ionhealthfitnesslib.helpers.ActivityTransitionHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.AlarmManagerHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface

interface HealthConnectRepositoryInterface {
    fun setPermissionVariables(
        customPermissions: Array<HealthFitnessPermission>,
        allVariables: HealthFitnessGroupPermission,
        fitnessVariables: HealthFitnessGroupPermission,
        healthVariables: HealthFitnessGroupPermission,
        profileVariables: HealthFitnessGroupPermission
    ): Set<String>

    suspend fun getLastRecord(
        healthRecord: HealthRecord,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        onSuccess: (HealthLastRecordResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    )

    suspend fun writeData(
        healthRecord: HealthRecord,
        value: Double,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        packageName: String,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    )

    suspend fun setBackgroundJob(
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
    )

    fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    )

    fun deleteBackgroundJob(
        jobId: String,
        context: Context,
        alarmManagerHelper: AlarmManagerHelperInterface,
        activityTransitionHelper: ActivityTransitionHelperInterface,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    )

    fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    )

    suspend fun disconnectFromHealthConnect(
        healthConnectClient: HealthConnectClient,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    )
    
    suspend fun advancedQuery(
        parameters: HealthAdvancedQueryParameters,
        client: HealthConnectClient,
        context: Context
    ): HealthAdvancedQueryResponse

    fun openHealthConnect(
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    )
}