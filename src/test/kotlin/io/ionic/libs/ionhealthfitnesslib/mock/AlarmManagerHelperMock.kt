package io.ionic.libs.ionhealthfitnesslib.mock

import android.content.Context
import android.os.Bundle
import io.ionic.libs.ionhealthfitnesslib.helpers.AlarmManagerHelperInterface

class AlarmManagerHelperMock : AlarmManagerHelperInterface {

    var shouldThrowException = false
    override fun setAlarm(context: Context, jobId: String, extras: Bundle, triggerAtMillis: Long) {
        // do nothing
        if (shouldThrowException) {
            throw Exception()
        }
    }

    override fun cancelAlarm(context: Context, jobId: String) {
        // do nothing
    }
}