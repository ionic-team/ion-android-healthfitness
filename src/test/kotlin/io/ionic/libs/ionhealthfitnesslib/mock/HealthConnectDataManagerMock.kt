package io.ionic.libs.ionhealthfitnesslib.mock

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.background.BackgroundJobsResponse
import io.ionic.libs.ionhealthfitnesslib.background.UpdateBackgroundJobParameters
import io.ionic.libs.ionhealthfitnesslib.background.database.BackgroundJob
import io.ionic.libs.ionhealthfitnesslib.background.database.Notification
import io.ionic.libs.ionhealthfitnesslib.data.HealthFitnessError
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryParameters
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthAdvancedQueryResponse
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessGroupPermission
import io.ionic.libs.ionhealthfitnesslib.data.types.HealthLastRecordResponse
import io.ionic.libs.ionhealthfitnesslib.helpers.ActivityTransitionHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.AlarmManagerHelperInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManagerInterface

class HealthConnectDataManagerMock<HealthFitnessPermission> : HealthConnectDataManagerInterface {

    private val notImplemented = "Not yet implemented"
    lateinit var queryResponse : HealthAdvancedQueryResponse
    var backgroundJobs: MutableList<BackgroundJob> = mutableListOf()
    var notifications: MutableList<Notification> = mutableListOf()

    override fun setPermissionVariables(
        customPermissions: Array<io.ionic.libs.ionhealthfitnesslib.data.types.HealthFitnessPermission>,
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