package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Intent
import android.health.connect.HealthPermissions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.lifecycle.lifecycleScope
import com.outsystems.plugins.healthfitness.data.Constants
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_DENIED
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_GRANTED
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_KEY_GLOBAL
import com.outsystems.plugins.healthfitness.data.Constants.EXTRA_RESULT_PERMISSION_KEY_LIST
import com.outsystems.plugins.healthfitness.utils.HealthConnectSharedState
import kotlinx.coroutines.launch

class HealthConnectPermissionActivity : ComponentActivity() {

    var permissions : Set<String> = mutableSetOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get permission list from intent
        permissions = intent.getStringArrayListExtra(Constants.EXTRA_PERMISSION_LIST)?.toList()?.toSet() ?: mutableSetOf()
        val privacyPolicyUrl = intent.getStringExtra(Constants.EXTRA_PRIVACY_POLICY_URL) ?: ""
        //We set the privacy policy url in the shared state, so we can use it later
        if (privacyPolicyUrl.isNotBlank()) {
            HealthConnectSharedState.privacyPolicyUrl = privacyPolicyUrl
        }

        val client = HealthConnectSharedState.client
        client?.let {
            lifecycleScope.launch {
                checkPermissionsAndRun(client)
            }
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
    }

    private val requestPermissions = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        val result = if (granted.intersect(permissions).isNotEmpty()) { // at least one permission was given
            EXTRA_RESULT_PERMISSION_GRANTED
        } else { // all permissions were denied
            EXTRA_RESULT_PERMISSION_DENIED
        }
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_RESULT_PERMISSION_KEY_GLOBAL, result)
        if (granted.isNotEmpty()) {
            returnIntent.putExtra(EXTRA_RESULT_PERMISSION_KEY_LIST, arrayListOf(granted.toList()))
        }
        putExtraDataAccordingToPermission(returnIntent)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            val returnIntent = Intent()
            returnIntent.putExtra(EXTRA_RESULT_PERMISSION_KEY_GLOBAL, EXTRA_RESULT_PERMISSION_GRANTED)
            putExtraDataAccordingToPermission(returnIntent)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            requestPermissions.launch(permissions)
        }
    }

    /**
     * To identify a result coming from requesting specific permissions
     */
    private fun putExtraDataAccordingToPermission(intent: Intent) {
        if (permissions.contains(HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND)) {
            intent.putExtra(Constants.EXTRA_CONTAINS_READ_DATA_BACKGROUND, true)
        } else if (permissions.contains(HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY)) {
            intent.putExtra(Constants.EXTRA_CONTAINS_READ_HEALTH_DATA_HISTORY, true)
        }
    }

}