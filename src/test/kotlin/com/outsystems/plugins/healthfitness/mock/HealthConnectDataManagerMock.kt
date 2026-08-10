package com.outsystems.plugins.healthfitness.mock

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.Notification
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryParameters
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessGroupPermission
import com.outsystems.plugins.healthfitness.data.types.HealthLastRecordResponse
import com.outsystems.plugins.healthfitness.helpers.ActivityTransitionHelperInterface
import com.outsystems.plugins.healthfitness.helpers.AlarmManagerHelperInterface
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelperInterface
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManagerInterface

class HealthConnectDataManagerMock<HealthFitnessPermission> : HealthConnectDataManagerInterface {

    private val notImplemented = "Not yet implemented"
    lateinit var queryResponse : HealthAdvancedQueryResponse
    var backgroundJobs: MutableList<BackgroundJob> = mutableListOf()
    var notifications: MutableList<Notification> = mutableListOf()

    override fun setPermissionVariables(
        customPermissions: Array<com.outsystems.plugins.healthfitness.data.types.HealthFitnessPermission>,
        allVariables: HealthFitnessGroupPermission,
        fitnessVariables: HealthFitnessGroupPermission,
        healthVariables: HealthFitnessGroupPermission,
        profileVariables: HealthFitnessGroupPermission
    ): Set<String> {
        TODO(notImplemented)
    }

    override suspend fun getLastRecord(
        healthRecord: HealthRecord,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        onSuccess: (HealthLastRecordResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO(notImplemented)
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
        TODO(notImplemented)
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
        TODO(notImplemented)
    }

    override fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO(notImplemented)
    }

    override fun updateBackgroundJob(backgroundJob: BackgroundJob) {
        // do nothing
    }

    override fun deleteBackgroundJob(
        jobId: String,
        context: Context,
        alarmManagerHelper: AlarmManagerHelperInterface,
        activityTransitionHelper: ActivityTransitionHelperInterface,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO(notImplemented)
    }

    override fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO(notImplemented)
    }

    override suspend fun getBackgroundJobBy(id: String): BackgroundJob? {
        return backgroundJobs.first()
    }

    override suspend fun getNotificationBy(id: Long): Notification? {
        return notifications.first()
    }

    override suspend fun advancedQuery(
        parameters: HealthAdvancedQueryParameters,
        client: HealthConnectClient,
        context: Context
    ): HealthAdvancedQueryResponse {
        return queryResponse
    }

    override fun openHealthConnect(
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO(notImplemented)
    }
}