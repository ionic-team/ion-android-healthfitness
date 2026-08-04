package io.ionic.libs.ionhealthfitnesslib.background.services

import android.content.Intent

class HealthRecordService: BackgroundListenerService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startType = super.onStartCommand(intent, flags, startId)
        if (startType == START_NOT_STICKY){
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        backgroundManager.processBackgroundJob(
            applicationContext,
            backgroundJobId,
            triggerNotification = { notification ->
                triggerNotificationWith(
                    notification.title,
                    notification.body,
                    notification.notificationID
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            },
            stopProcessing = {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        )
        return START_REDELIVER_INTENT
    }

}