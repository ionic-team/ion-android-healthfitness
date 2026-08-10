package com.outsystems.plugins.healthfitness.utils

import androidx.health.connect.client.HealthConnectClient

object HealthConnectSharedState {
    var client: HealthConnectClient? = null
    //Privacy policy url is being set in the HealthConnectPermissionActivity
    var privacyPolicyUrl: String? = null
}