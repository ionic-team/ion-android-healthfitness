package com.outsystems.plugins.healthfitness.viewmodel

import android.app.AlarmManager
import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponseBlock
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitness.background.database.Notification
import com.outsystems.plugins.healthfitness.data.Constants
import com.outsystems.plugins.healthfitness.data.HealthEnumAccessType
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.HealthRecordsObject
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryParameters
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import com.outsystems.plugins.healthfitness.data.types.HealthEnumAdvancedQueryResultType
import com.outsystems.plugins.healthfitness.data.types.HealthEnumAdvancedQueryReturnType
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessGroupPermission
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessPermission
import com.outsystems.plugins.healthfitness.data.types.HealthLastRecordResponse
import com.outsystems.plugins.healthfitness.data.types.HealthTimeFrame
import com.outsystems.plugins.healthfitness.helpers.ActivityTransitionHelperInterface
import com.outsystems.plugins.healthfitness.helpers.AdvancedQueryOptions
import com.outsystems.plugins.healthfitness.helpers.AlarmManagerHelperInterface
import com.outsystems.plugins.healthfitness.helpers.HealthAdvancedQuery
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelper
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelperInterface
import com.outsystems.plugins.healthfitness.helpers.toHealthRecordMetadata
import com.outsystems.plugins.healthfitness.store.HealthStoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class HealthConnectDataManager(
    private val database: DatabaseManagerInterface,
    private val healthConnectClientHelper: HealthConnectHelperInterface = HealthConnectHelper()
) :
    HealthConnectDataManagerInterface {
    companion object{
        private const val TAG = "HealthConnectDataManager"
    }

    override fun setPermissionVariables(
        customPermissions: Array<HealthFitnessPermission>,
        allVariables: HealthFitnessGroupPermission,
        fitnessVariables: HealthFitnessGroupPermission,
        healthVariables: HealthFitnessGroupPermission,
        profileVariables: HealthFitnessGroupPermission
    ): Set<String> {

        val permissionSet = mutableSetOf<String>()

        // permissions for all variables
        if(allVariables.isActive){
            permissionSet.addAll(createPermissionsForAllVariables(allVariables))
        }
        else {
            // permissions for all fitness variables
            if(fitnessVariables.isActive){
                permissionSet.addAll(createPermissionsForFitnessVariables(fitnessVariables.accessType))
            }
            // permissions for all health variables
            if(healthVariables.isActive){
                permissionSet.addAll(createPermissionsForHealthVariables(healthVariables.accessType))
            }
            // permissions for all profile variables
            if(profileVariables.isActive){
                permissionSet.addAll(createPermissionsForProfileVariables(profileVariables.accessType))
            }
            // custom permissions
            permissionSet.addAll(createCustomPermissionsForVariables(customPermissions))
        }
        return permissionSet
    }

    override suspend fun getLastRecord(
        healthRecord: HealthRecord,
        healthConnectClient: HealthConnectClient,
        healthConnectHelper: HealthConnectHelperInterface,
        onSuccess: (HealthLastRecordResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {

        val permissions = createCustomPermissionsForVariables(
            arrayOf(
                HealthFitnessPermission(
                    healthRecord,
                    HealthEnumAccessType.READ
                )
            )
        )

        if (!healthConnectHelper.checkPermissions(healthConnectClient, permissions)) {
            onError(HealthFitnessError.READ_DATA_NO_PERMISSIONS)
            return
        }

        try {
            val record = healthConnectHelper.readLastRecord(healthConnectClient, healthRecord)

            record?.let {
                val result = healthRecord.getVariableFieldValue(record)
                val metadata = record.metadata.toHealthRecordMetadata()
                onSuccess(HealthLastRecordResponse(result, metadata))
            } ?: onSuccess(HealthLastRecordResponse(emptyList(), null))
        } catch (e: Exception) {
            onError(HealthFitnessError.READ_DATA_ERROR)
        }
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
        val writeMethod = getWriteProfileDataMethod(healthRecord, healthConnectHelper)
        if (writeMethod == null) {
            onError(HealthFitnessError.WRITE_DATA_NOT_PROFILE_ERROR)
            return
        }

        val permissions = createCustomPermissionsForVariables(
            arrayOf(
                HealthFitnessPermission(
                    healthRecord,
                    HealthEnumAccessType.WRITE
                )
            )
        )

        if (!healthConnectHelper.checkPermissions(healthConnectClient, permissions)) {
            onError(HealthFitnessError.WRITE_DATA_NO_PERMISSIONS)
            return
        }

        try {
            val metadata = Metadata.manualEntry()
            writeMethod.invoke(value, metadata, healthConnectClient)
            onSuccess()
        } catch (_: IllegalArgumentException) {
            onError(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR)
        } catch (_: Exception) {
            onError(HealthFitnessError.WRITE_DATA_ERROR)
        }

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

        try {
            // if variable exists, then it must be in the HealthRecordName enum
            val healthRecord = HealthRecord.valueOf(parameters.variable)

            val permissions = createCustomPermissionsForVariables(
                arrayOf(
                    HealthFitnessPermission(
                        healthRecord,
                        HealthEnumAccessType.READ
                    )
                )
            )

            //check for permissions for the variable
            if (!healthConnectHelper.checkPermissions(healthConnectClient, permissions)) {
                onError(HealthFitnessError.READ_DATA_NO_PERMISSIONS)
                return
            }

            val notification = Notification().apply {
                this.title = parameters.notificationHeader
                this.body = parameters.notificationBody
            }
            val backgroundJob = BackgroundJob().apply {
                this.variable = parameters.variable
                this.comparison = parameters.condition
                this.value = parameters.value.toFloat()
                this.timeUnit = parameters.timeUnit
                this.timeUnitGrouping = parameters.timeUnitGrouping
                this.notificationFrequency =
                    parameters.notificationFrequency.toString()
                this.notificationFrequencyGrouping =
                    parameters.notificationFrequencyGrouping ?: 1
                this.nextNotificationTimestamp = System.currentTimeMillis()
            }
            val backgroundJobId = backgroundJob.id

            val extras = Bundle().apply {
                putString(Constants.EXTRA_HEALTH_RECORD, healthRecord.name)
                putString(Constants.EXTRA_BACKGROUND_JOB_ID, backgroundJobId)
                putString(Constants.EXTRA_FOREGROUND_NOTIFICATION_TITLE, foregroundNotificationTitle)
                putString(Constants.EXTRA_FOREGROUND_NOTIFICATION_DESCRIPTION, foregroundNotificationDescription)
            }

            // determine if variable is an activity variable
            if (Constants.ACTIVITY_VARIABLES.contains(healthRecord.name)) {
                // if it is, then we want to set the background job using the Activity Recognition Transition API
                activityTransitionHelper.requestTransitionUpdates(
                    backgroundJobId,
                    extras,
                    context,
                    {
                        // we only want to insert the job in the database if the request was successful
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                database.runInTransaction {
                                    val notificationId = database.insert(notification)
                                    backgroundJob.notificationId = notificationId
                                    database.insert(backgroundJob)
                                }
                                onSuccess()
                            } catch (e: Exception) {
                                activityTransitionHelper.removeTransitionUpdates(
                                    backgroundJobId,
                                    context
                                )
                                {
                                    if (e is SQLiteException) {
                                        onError(HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR)
                                    } else {
                                        onError(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR)
                                    }
                                }
                            }
                        }
                    },
                    {
                        onError(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR)
                    }
                )

            } else {
                // save backgroundJob details in database
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        database.runInTransaction {
                            val notificationId = database.insert(notification)
                            backgroundJob.notificationId = notificationId
                            database.insert(backgroundJob)
                        }
                    } catch (e: SQLiteException) {
                        onError(HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR)
                        return@launch
                    } catch (e: Exception) {
                        onError(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR)
                        return@launch
                    }

                    // if it isn't, then we want to set the background job using an exact alarm, using the AlarmManager
                    // use AlarmManager to schedule alarms to run background jobs
                    // the minimum frequency recommended for Android apps is minute
                    val intervalInMillis = when(parameters.jobFrequency) {
                        Constants.FREQUENCY_IMMEDIATE -> 60 * 1000L // minute
                        Constants.FREQUENCY_HOUR -> AlarmManager.INTERVAL_HOUR
                        Constants.FREQUENCY_DAY -> AlarmManager.INTERVAL_DAY
                        else -> AlarmManager.INTERVAL_DAY * 7 // week
                    }

                    val triggerAtMillis = System.currentTimeMillis() + intervalInMillis
                    extras.putLong(Constants.EXTRA_INTERVAL_IN_MILLIS, intervalInMillis)

                    alarmManagerHelper.setAlarm(context, backgroundJobId, extras, triggerAtMillis)
                    onSuccess()
                }
            }
        } catch (e: IllegalArgumentException) {
            onError(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
        } catch (e: Exception) {
            onError(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR)
        }
    }

    override fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        try {
            val job = database.fetchBackgroundJob(parameters.id)
                ?: throw HealthStoreException(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXIST_ERROR)

            job.update(parameters)

            val notification = job.notificationId?.let { database.fetchNotification(it) }
            if (notification != null) {
                notification.update(
                    parameters.notificationHeader ?: "",
                    parameters.notificationBody ?: ""
                )
                database.updateNotification(notification)
            }

            database.updateBackgroundJob(job)
            onSuccess()
        } catch (e: HealthStoreException) {
            onError(e.error)
        } catch (e: Exception) {
            onError(HealthFitnessError.UPDATE_BACKGROUND_JOB_GENERIC_ERROR)
        }
    }

    override fun updateBackgroundJob(backgroundJob: BackgroundJob) {
        database.updateBackgroundJob(backgroundJob)
    }

    override fun deleteBackgroundJob(
        jobId: String,
        context: Context,
        alarmManagerHelper: AlarmManagerHelperInterface,
        activityTransitionHelper: ActivityTransitionHelperInterface,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        try {
            // fetch the BackgroundJob associated with the argument `jobId`.
            val job = database.fetchBackgroundJob(jobId)
                ?: throw HealthStoreException(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXIST_ERROR)

            // delete it from the database
            database.deleteBackgroundJob(job)

            // determine if variable is an activity variable
            if (Constants.ACTIVITY_VARIABLES.contains(job.variable)) {
                // remove transition updates and cancel pending intent for this background job
                activityTransitionHelper.removeTransitionUpdates(
                    jobId, context
                ) {
                    onSuccess()
                }
            } else {
                // cancel alarm and pending intent for this background job
                alarmManagerHelper.cancelAlarm(context, jobId)
                onSuccess()
            }

        } catch (e: HealthStoreException) {
            onError(e.error)
        } catch (e: Exception) {
            onError(HealthFitnessError.DELETE_BACKGROUND_JOB_GENERIC_ERROR)
        }
    }

    override fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        try {
            onSuccess(BackgroundJobsResponse(buildListBackgroundJobsResult(database.fetchBackgroundJobs())))
        } catch (e: Exception) {
            onError(HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR)
        }
    }

    override suspend fun getBackgroundJobBy(id: String): BackgroundJob? {
        return database.fetchBackgroundJob(id)
    }

    override suspend fun getNotificationBy(id: Long): Notification? {
        return database.fetchNotification(id)
    }

    private fun getWriteProfileDataMethod(
        healthRecord: HealthRecord,
        healthConnectHelper: HealthConnectHelperInterface,
    ): (suspend (Double, Metadata, HealthConnectClient) -> Unit)? {
        return when (healthRecord) {
            HealthRecord.WEIGHT ->
                HealthRecord.WEIGHT.getWriterMethod(healthConnectHelper)
            HealthRecord.HEIGHT ->
                HealthRecord.HEIGHT.getWriterMethod(healthConnectHelper)
            HealthRecord.BODY_FAT_PERCENTAGE ->
                HealthRecord.BODY_FAT_PERCENTAGE.getWriterMethod(healthConnectHelper)
            HealthRecord.BASAL_METABOLIC_RATE ->
                HealthRecord.BASAL_METABOLIC_RATE.getWriterMethod(healthConnectHelper)
            else -> {
                null
            }
        }
    }

    private fun createPermissionsForAllVariables(
        allVariablesPermissions: HealthFitnessGroupPermission?
    ): Set<String> {
        val result = mutableSetOf<String>()
        allVariablesPermissions?.let {
            result.addAll(createPermissionsForFitnessVariables(it.accessType))
            result.addAll(createPermissionsForHealthVariables(it.accessType))
            result.addAll(createPermissionsForProfileVariables(it.accessType))
        }
        return result
    }

    private fun createPermissionsForFitnessVariables(accessType: String): Set<String> {
        val result = mutableSetOf<String>()
        when(accessType) {
            HealthEnumAccessType.WRITE.name -> {
                result.add(HealthPermission.getWritePermission(StepsRecord::class))
                result.add(HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class))
                result.add(HealthPermission.getWritePermission(DistanceRecord::class))
                result.add(HealthPermission.getWritePermission(SpeedRecord::class))
            }
            HealthEnumAccessType.READWRITE.name -> {
                result.add(HealthPermission.getReadPermission(StepsRecord::class))
                result.add(HealthPermission.getWritePermission(StepsRecord::class))
                result.add(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
                result.add(HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class))
                result.add(HealthPermission.getReadPermission(DistanceRecord::class))
                result.add(HealthPermission.getWritePermission(DistanceRecord::class))
                result.add(HealthPermission.getReadPermission(SpeedRecord::class))
                result.add(HealthPermission.getWritePermission(SpeedRecord::class))
            }
            else -> {
                result.add(HealthPermission.getReadPermission(StepsRecord::class))
                result.add(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
                result.add(HealthPermission.getReadPermission(DistanceRecord::class))
                result.add(HealthPermission.getReadPermission(SpeedRecord::class))
            }
        }
        return result
    }

    private fun createPermissionsForHealthVariables(accessType: String): Set<String> {
        val result = mutableSetOf<String>()
        when(accessType) {
            HealthEnumAccessType.WRITE.name -> {
                result.add(HealthPermission.getWritePermission(HeartRateRecord::class))
                result.add(HealthPermission.getWritePermission(SleepSessionRecord::class))
                result.add(HealthPermission.getWritePermission(BloodPressureRecord::class))
                result.add(HealthPermission.getWritePermission(BloodGlucoseRecord::class))
                result.add(HealthPermission.getWritePermission(OxygenSaturationRecord::class))
                result.add(HealthPermission.getWritePermission(BodyTemperatureRecord::class))
            }
            HealthEnumAccessType.READWRITE.name -> {
                result.add(HealthPermission.getReadPermission(HeartRateRecord::class))
                result.add(HealthPermission.getWritePermission(HeartRateRecord::class))
                result.add(HealthPermission.getReadPermission(SleepSessionRecord::class))
                result.add(HealthPermission.getWritePermission(SleepSessionRecord::class))
                result.add(HealthPermission.getReadPermission(BloodPressureRecord::class))
                result.add(HealthPermission.getWritePermission(BloodPressureRecord::class))
                result.add(HealthPermission.getReadPermission(BloodGlucoseRecord::class))
                result.add(HealthPermission.getWritePermission(BloodGlucoseRecord::class))
                result.add(HealthPermission.getReadPermission(OxygenSaturationRecord::class))
                result.add(HealthPermission.getWritePermission(OxygenSaturationRecord::class))
                result.add(HealthPermission.getReadPermission(BodyTemperatureRecord::class))
                result.add(HealthPermission.getWritePermission(BodyTemperatureRecord::class))
            }
            else -> {
                result.add(HealthPermission.getReadPermission(HeartRateRecord::class))
                result.add(HealthPermission.getReadPermission(SleepSessionRecord::class))
                result.add(HealthPermission.getReadPermission(BloodPressureRecord::class))
                result.add(HealthPermission.getReadPermission(BloodGlucoseRecord::class))
                result.add(HealthPermission.getReadPermission(OxygenSaturationRecord::class))
                result.add(HealthPermission.getReadPermission(BodyTemperatureRecord::class))
            }
        }
        return result
    }

    private fun createPermissionsForProfileVariables(accessType: String): Set<String> {
        val result = mutableSetOf<String>()
        when(accessType) {
            HealthEnumAccessType.WRITE.name -> {
                result.add(HealthPermission.getWritePermission(WeightRecord::class))
                result.add(HealthPermission.getWritePermission(HeightRecord::class))
                result.add(HealthPermission.getWritePermission(BodyFatRecord::class))
                result.add(HealthPermission.getWritePermission(BasalMetabolicRateRecord::class))
            }
            HealthEnumAccessType.READWRITE.name -> {
                result.add(HealthPermission.getReadPermission(WeightRecord::class))
                result.add(HealthPermission.getWritePermission(WeightRecord::class))
                result.add(HealthPermission.getReadPermission(HeightRecord::class))
                result.add(HealthPermission.getWritePermission(HeightRecord::class))
                result.add(HealthPermission.getReadPermission(BodyFatRecord::class))
                result.add(HealthPermission.getWritePermission(BodyFatRecord::class))
                result.add(HealthPermission.getReadPermission(BasalMetabolicRateRecord::class))
                result.add(HealthPermission.getWritePermission(BasalMetabolicRateRecord::class))
            }
            else -> {
                result.add(HealthPermission.getReadPermission(WeightRecord::class))
                result.add(HealthPermission.getReadPermission(HeightRecord::class))
                result.add(HealthPermission.getReadPermission(BodyFatRecord::class))
                result.add(HealthPermission.getReadPermission(BasalMetabolicRateRecord::class))
            }
        }
        return result
    }

    private fun createCustomPermissionsForVariables(customPermissions: Array<HealthFitnessPermission>) : Set<String> {
        val result = mutableSetOf<String>()
        customPermissions.forEach { permission ->
            try {
                val healthConnectVariablePermission = HealthRecord.valueOf(permission.variable.name).getPermissions()
                when(permission.accessType) {
                    HealthEnumAccessType.WRITE -> {
                        // write permission is the second in the Pair
                        result.add(healthConnectVariablePermission.second)
                    }
                    HealthEnumAccessType.READWRITE -> {
                        result.add(healthConnectVariablePermission.first)
                        result.add(healthConnectVariablePermission.second)
                    }
                    else -> {
                        // read permission is the first in the Pair
                        result.add(healthConnectVariablePermission.first)
                    }
                }
            } catch (e: Exception) {
                throw HealthStoreException(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
            }
        }
        return result
    }

    private fun buildListBackgroundJobsResult(jobsList: List<BackgroundJob>?) : List<BackgroundJobsResponseBlock>{
        val responseJobList : MutableList<BackgroundJobsResponseBlock> = mutableListOf()
        jobsList?.let {
            for (job in jobsList){
                val notification = job.notificationId?.let { id -> database.fetchNotification(id) }
                responseJobList.add(
                    BackgroundJobsResponseBlock(
                        job.variable,
                        job.comparison,
                        job.value,
                        notification?.title,
                        notification?.body,
                        job.notificationFrequency,
                        job.notificationFrequencyGrouping,
                        job.isActive,
                        job.id
                    )
                )
            }
        }
        return responseJobList
    }

    override suspend fun advancedQuery(
        parameters: HealthAdvancedQueryParameters,
        client: HealthConnectClient,
        context: Context
    ): HealthAdvancedQueryResponse {
        val variable = HealthRecordsObject.healthConnectVariables[parameters.variable.name]
            ?: throw HealthStoreException(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)

        if (!variable.value.allowedOperations.containsKey(parameters.operationType)) {
            throw HealthStoreException(HealthFitnessError.OPERATION_NOT_ALLOWED)
        }

        if (!checkPermissions(client, parameters.variable)) {
            throw HealthStoreException(HealthFitnessError.PERMISSIONS_DENIED_READ_ERROR)
        }

        try{
            val removeEmptyBlock =
                parameters.returnType == HealthEnumAdvancedQueryReturnType.REMOVE_EMPTY_BLOCKS
            val includeDataPoint =
                parameters.resultType == HealthEnumAdvancedQueryResultType.DATA_POINT || parameters.resultType == HealthEnumAdvancedQueryResultType.ALL_DATA
            val includeRawData =
                parameters.resultType == HealthEnumAdvancedQueryResultType.RAW_DATA || parameters.resultType == HealthEnumAdvancedQueryResultType.ALL_DATA
            val query = HealthAdvancedQuery(healthConnectClientHelper,
                variable.value,
                parameters.startDate.toLocalDateTime(),
                parameters.endDate.toLocalDateTime(),
                parameters.operationType,
                HealthTimeFrame(parameters.timeUnit, parameters.timeUnitLength),
                AdvancedQueryOptions(removeEmptyBlock,
                    includeDataPoint,
                    includeRawData, parameters.pageSize,
                    parameters.pageToken.takeIf { it?.isNotEmpty() == true })
                ,
                context
            )
            return query.executeQuery()
        }catch (e: Exception){
            //We are logging this error for better troubleshooting
            Log.e(TAG, e.message.toString())
            throw HealthStoreException(HealthFitnessError.READ_DATA_ERROR)
        }
    }

    override fun openHealthConnect(
        context: Context,
        onSuccess: () -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(Constants.HEALTH_CONNECT_APP_PACKAGE)
            if (intent != null) {
                context.startActivity(intent)
                onSuccess()
            } else { // Health Connect app is not installed
                onError(HealthFitnessError.HEALTH_CONNECT_NOT_INSTALLED_ERROR)
            }
        } catch (e: Exception) {
            onError(HealthFitnessError.OPEN_HEALTH_CONNECT_ERROR)
        }
    }

    private suspend fun checkPermissions(client: HealthConnectClient, variable: HealthRecord): Boolean {
        val permissions = createCustomPermissionsForVariables(
            arrayOf(HealthFitnessPermission(variable, HealthEnumAccessType.READ))
        )
        return healthConnectClientHelper.checkPermissions(client, permissions)
    }

    private fun Date.toLocalDateTime(): LocalDateTime {
        val asInstant = this.toInstant()
        return asInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}

