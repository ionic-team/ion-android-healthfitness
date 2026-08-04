package io.ionic.libs.ionhealthfitnesslib.helpers

import android.content.Context
import android.os.Bundle

interface ActivityTransitionHelperInterface {
    fun requestTransitionUpdates(
        jobId: String,
        extras: Bundle,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit
    )

    fun removeTransitionUpdates(
        jobId: String,
        context: Context,
        onSuccess: () -> Unit
    )
}