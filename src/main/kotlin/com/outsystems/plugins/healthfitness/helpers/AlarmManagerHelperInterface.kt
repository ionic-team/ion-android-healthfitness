package com.outsystems.plugins.healthfitness.helpers

import android.content.Context
import android.os.Bundle

interface AlarmManagerHelperInterface {
    fun setAlarm(context: Context, jobId: String, extras: Bundle, triggerAtMillis: Long)
    fun cancelAlarm(context: Context, jobId: String)
}