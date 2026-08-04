package io.ionic.libs.ionhealthfitnesslib.background.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.ionic.libs.ionhealthfitnesslib.helpers.AlarmManagerHelper
import io.ionic.libs.ionhealthfitnesslib.data.Constants
import io.ionic.libs.ionhealthfitnesslib.background.services.HealthRecordService

class AlarmReceiver : BroadcastReceiver() {

    private var alarmManagerHelper = AlarmManagerHelper()

    companion object {
        const val TAG = "AlarmReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null) {
            processBackgroundJobs(context, intent)
        }
    }

    private fun processBackgroundJobs(context: Context, intent: Intent) {
        try {
            // setup next alarm for the same background job
            // we have to do this because there is no such thing as a repeating exact alarm
            // only inexact alarms can be repeating
            val jobId = intent.getStringExtra(Constants.EXTRA_BACKGROUND_JOB_ID)
            val extras = intent.extras

            if (!jobId.isNullOrEmpty() && extras != null) {
                val intervalInMillis = intent.getLongExtra(
                    Constants.EXTRA_INTERVAL_IN_MILLIS,
                    60 * 1000L // default value is 1 minute
                )

                alarmManagerHelper.setAlarm(
                    context,
                    jobId,
                    extras,
                    System.currentTimeMillis() + intervalInMillis,
                )
            }

            // start foreground service for background jobs
            val healthRecordServiceIntent = Intent(context, HealthRecordService::class.java).apply {
                putExtras(intent)
            }
            context.startForegroundService(healthRecordServiceIntent)

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }

    }
}