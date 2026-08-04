package io.ionic.libs.ionhealthfitnesslib.background.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.ionic.libs.ionhealthfitnesslib.data.Constants
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManager
import io.ionic.libs.ionhealthfitnesslib.viewmodel.HealthConnectDataManagerInterface
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelper
import io.ionic.libs.ionhealthfitnesslib.helpers.HealthConnectHelperInterface
import io.ionic.libs.ionhealthfitnesslib.data.HealthRecord
import io.ionic.libs.ionhealthfitnesslib.R
import io.ionic.libs.ionhealthfitnesslib.background.ClickActivity
import io.ionic.libs.ionhealthfitnesslib.background.DatabaseManager
import io.ionic.libs.ionhealthfitnesslib.background.managers.BackgroundJobManager
import io.ionic.libs.ionhealthfitnesslib.background.managers.BackgroundJobManagerInterface

open class BackgroundListenerService : Service() {
    companion object {
        const val TAG = "BackgroundListenerService"
        const val DEFAULT_FOREGROUND_CHANNEL_NAME = "HealthFitness"
        const val DEFAULT_NOTIFICATION_TITLE = "Measuring your health and fitness data."
        private const val HEALTH_CHANNEL_ID_BASE = "com.outsystems.healthfitness"
        val channelId =  HEALTH_CHANNEL_ID_BASE + "." + DEFAULT_FOREGROUND_CHANNEL_NAME.replace(" ", "")
        const val NO_BACKGROUND_JOB_ID = "Couldn't process background job: no valid background job id"
        const val NO_HEALTH_RECORD_NAME = "Couldn't process background job: no valid HealthRecordName found"
        const val CLICK_ACTIVITY_REQUEST_CODE = 1
    }

    protected lateinit var healthRecordName: HealthRecord
    protected lateinit var backgroundJobId: String
    protected lateinit var backgroundManager: BackgroundJobManagerInterface
    private lateinit var healthConnectHelper: HealthConnectHelperInterface
    private lateinit var healthConnectDataManager: HealthConnectDataManagerInterface

    // class used for the client Binder. Because we know this service always
    inner class LocalBinder : Binder()

    // Binder given to clients.
    private val binder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        //initializing what we'll need to process the information and save it
        healthConnectHelper = HealthConnectHelper()
        healthConnectDataManager = HealthConnectDataManager( DatabaseManager.getInstance(this))
        backgroundManager = BackgroundJobManager(healthConnectHelper, healthConnectDataManager)
    }

    // since the foreground service wasn't a thing with Google Fit,
    // introducing the channel name and channel id without a default would
    // be a breaking change
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            //optional fields
            val notificationTitle =
                intent?.getStringExtra(Constants.EXTRA_FOREGROUND_NOTIFICATION_TITLE) ?: DEFAULT_NOTIFICATION_TITLE
            val notificationDesc =
                intent?.getStringExtra(Constants.EXTRA_FOREGROUND_NOTIFICATION_DESCRIPTION) ?: ""

            // Before starting the service as foreground check that the app has the permissions
            val notification = createForegroundNotification(notificationTitle, notificationDesc)
            startForegroundWith(notification) // Start service in the foreground

            // Without health permissions the service cannot run in the foreground
            if (!hasPermissions()) {
                return START_NOT_STICKY
            }

            //first we check mandatory fields
            backgroundJobId = intent?.getStringExtra(Constants.EXTRA_BACKGROUND_JOB_ID) ?: ""
            if(backgroundJobId.isEmpty()) {
                Log.e(
                    TAG,
                    NO_BACKGROUND_JOB_ID
                )
                stopSelf()
                return START_NOT_STICKY
            }

            val healthRecordNameString = intent?.getStringExtra(Constants.EXTRA_HEALTH_RECORD) ?: ""
            if(healthRecordNameString.isEmpty()) {
                Log.e(
                    TAG,
                    NO_HEALTH_RECORD_NAME
                )
                stopSelf()
                return START_NOT_STICKY
            }
            healthRecordName = HealthRecord.valueOf(healthRecordNameString)
            return START_REDELIVER_INTENT
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun startForegroundWith(notification: Notification) {
        ServiceCompat.startForeground(
            this,
            100, // ID can always be the same, we want to always use the same notification
            notification,
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                //in older versions, we don't need to specify the service type
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            }
        )
    }

    /**
     * Creates a notification for the foreground service
     * This is required to keep the service running in the foreground
     */
    private fun createForegroundNotification(
        notificationTitle: String,
        notificationDesc: String): Notification {

        // channelId will be com.outsystems.healthfitness.[CHANNELNAME]
        val channel = NotificationChannel(
            channelId, DEFAULT_FOREGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDesc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return builder.build()
    }

    /**
     * Checks if the necessary permissions for this foreground service
     * have been granted
     * Returns <code>true</code> if all permissions have been granted
     * <code>false</code> if one is missing
     */
    private fun hasPermissions(): Boolean {
        val permissions: MutableList<String> = mutableListOf()
        if (Build.VERSION.SDK_INT >= 28) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            permissions.add(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
        }
        permissions.forEach { permissionRequest ->
            val permission = ContextCompat.checkSelfPermission(this, permissionRequest)
            if (permission == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    fun triggerNotificationWith(title: String, body: String, id: Int) {
        //build intent to call the ClickActivity
        val myIntent = Intent(this, ClickActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, CLICK_ACTIVITY_REQUEST_CODE,
            myIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val notificationToTrigger = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < 33 || (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            NotificationManagerCompat.from(this).notify(id, notificationToTrigger)
        }
    }

}