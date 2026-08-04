package io.ionic.libs.ionhealthfitnesslib.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import io.ionic.libs.ionhealthfitnesslib.background.transition.ActivityTransitionReceiver
import io.ionic.libs.ionhealthfitnesslib.data.Constants

class ActivityTransitionHelper : ActivityTransitionHelperInterface {

    companion object{
        private const val TAG = "HealthConnectDataManager"
    }

    /**
     * Responsible for requesting activity transition updates, using the jobId as the action
     * of the intent, using the Activity Recognition Transition API.
     */
    override fun requestTransitionUpdates(
        jobId: String,
        extras: Bundle,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val intent = getActivityTransitionIntent(context, jobId).apply {
            putExtras(extras)
        }
        val pendingIntent = getActivityTransitionPendingIntent(context, intent)

        val request = ActivityTransitionRequest(getActivityTransitions())
        val task = ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener {
            onSuccess()
        }

        task.addOnFailureListener { e: Exception ->
            // Log error message and call error closure
            Log.d(TAG, e.message.toString())
            onError()
        }

    }

    /**
     * Responsible for removing activity transition updates, using the jobId as the action
     * of the intent, using the Activity Recognition Transition API.
     */
    override fun removeTransitionUpdates(
        jobId: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        // create the same exact pending intent as the one when setting up the background job
        val intent = getActivityTransitionIntent(context, jobId)
        val pendingIntent = getActivityTransitionPendingIntent(context, intent)

        // remove activity updates for this background job
        val task =
            ActivityRecognition.getClient(context).removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            // cancel pending intent for this background job
            pendingIntent.cancel()
            onSuccess()
        }

        task.addOnFailureListener { e: Exception ->
            // in this case, we simply log the error message,
            // and the background job was already deleted from the database
            Log.e(TAG, e.message.toString())
        }
    }

    private fun getActivityTransitionIntent(context: Context, jobId: String) : Intent {
        return Intent(context, ActivityTransitionReceiver::class.java).apply {
            action = jobId
        }
    }

    private fun getActivityTransitionPendingIntent(context: Context, intent: Intent) : PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ACTIVITY_RECEIVER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getActivityTransitions(): List<ActivityTransition> {
        // Register for every activity available (IN_VEHICLE, ON_BICYCLE, RUNNING, STILL, WALKING).
        // As this is the only way to offer the background job feature without using
        // exact alarms, we want to listen to as many activity types as possible.
        val transitions = mutableListOf<ActivityTransition>()
        val activities = listOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.RUNNING,
            DetectedActivity.STILL,
            DetectedActivity.WALKING
        )
        for (activity in activities) {
            transitions += getActivityTransition(
                activity,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER
            )
            transitions += getActivityTransition(
                activity,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT
            )
        }
        return transitions
    }

    private fun getActivityTransition(
        activityType: Int,
        activityTransition: Int
    ): ActivityTransition {
        return ActivityTransition.Builder()
            .setActivityType(activityType)
            .setActivityTransition(activityTransition)
            .build()
    }

}