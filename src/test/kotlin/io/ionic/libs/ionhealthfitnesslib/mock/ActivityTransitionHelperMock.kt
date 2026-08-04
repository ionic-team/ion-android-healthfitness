package io.ionic.libs.ionhealthfitnesslib.mock

import android.content.Context
import android.os.Bundle
import io.ionic.libs.ionhealthfitnesslib.helpers.ActivityTransitionHelperInterface

class ActivityTransitionHelperMock : ActivityTransitionHelperInterface {

    var shouldSendError = false
    override fun requestTransitionUpdates(
        jobId: String,
        extras: Bundle,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        if (shouldSendError) {
            onError()
        } else {
            onSuccess()
        }
    }

    override fun removeTransitionUpdates(jobId: String, context: Context, onSuccess: () -> Unit) {
        onSuccess()
    }
}