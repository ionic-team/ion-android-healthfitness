package io.ionic.libs.ionhealthfitnesslib.background.managers

import android.content.Context
import io.ionic.libs.ionhealthfitnesslib.background.database.Notification

fun interface BackgroundJobManagerInterface {

    fun processBackgroundJob(
        context: Context,
        backgroundJobId: String,
        triggerNotification: (notification: Notification) -> Unit,
        stopProcessing: () -> Unit
    )

}