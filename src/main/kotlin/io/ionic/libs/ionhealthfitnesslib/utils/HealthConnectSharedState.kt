package io.ionic.libs.ionhealthfitnesslib.utils

import androidx.health.connect.client.HealthConnectClient

object HealthConnectSharedState {
    var client: HealthConnectClient? = null
    //Privacy policy url is being set in the HealthConnectPermissionActivity
    var privacyPolicyUrl: String? = null
}