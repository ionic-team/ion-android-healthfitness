package com.outsystems.plugins.healthfitness.background.managers

import android.content.Context
import com.outsystems.plugins.healthfitness.background.database.Notification

fun interface BackgroundJobManagerInterface {

    fun processBackgroundJob(
        context: Context,
        backgroundJobId: String,
        triggerNotification: (notification: Notification) -> Unit,
        stopProcessing: () -> Unit
    )

}