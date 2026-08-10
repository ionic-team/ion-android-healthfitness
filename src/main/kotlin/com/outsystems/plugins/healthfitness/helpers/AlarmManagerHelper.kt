package com.outsystems.plugins.healthfitness.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.outsystems.plugins.healthfitness.background.alarms.AlarmReceiver
import com.outsystems.plugins.healthfitness.data.Constants

class AlarmManagerHelper : AlarmManagerHelperInterface {

    /**
     * Responsible for setting an exact alarm, using the jobId as the action
     * of the intent, so that each alarm corresponds to a single background job.
     */
    override fun setAlarm(context: Context, jobId: String, extras: Bundle, triggerAtMillis: Long) {
        val alarmIntent = getAlarmIntent(context, jobId).apply {
            putExtras(extras)
        }
        val pendingIntent = getAlarmPendingIntent(context, alarmIntent)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)

        alarmManager.setAlarmClock(
            alarmClockInfo,
            pendingIntent
        )
    }

    /**
     * Responsible for canceling a previously set exact alarm, using the jobId as the action
     * of the intent, so that the correct alarm is cancelled.
     */
    override fun cancelAlarm(context: Context, jobId: String) {
        // create the same exact pending intent as the one when setting up the alarm
        val alarmIntent = getAlarmIntent(context, jobId)
        val pendingIntent = getAlarmPendingIntent(context, alarmIntent)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // cancel the alarm and the pending intent
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getAlarmIntent(context: Context, jobId: String) : Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = jobId
        }
    }

    private fun getAlarmPendingIntent(context: Context, alarmIntent: Intent) : PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ALARM_RECEIVER_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}