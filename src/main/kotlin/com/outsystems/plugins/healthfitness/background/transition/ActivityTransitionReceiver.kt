package com.outsystems.plugins.healthfitness.background.transition

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.outsystems.plugins.healthfitness.background.services.HealthRecordService

class ActivityTransitionReceiver: BroadcastReceiver() {

    companion object {
        const val TAG = "ActivityTransitionReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (context != null && intent != null) {
                val healthRecordServiceIntent = Intent(context, HealthRecordService::class.java).apply {
                    putExtras(intent)
                }
                ContextCompat.startForegroundService(context, healthRecordServiceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }
}
