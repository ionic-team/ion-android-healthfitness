package com.outsystems.plugins.healthfitness

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.outsystems.plugins.healthfitness.utils.HealthConnectSharedState
import com.outsystems.plugins.healthfitness.viewmodel.PrivacyPolicyViewModel
import com.outsystems.plugins.healthfitness.viewmodel.PrivacyPolicyViewModelFactory

class PermissionsRationaleActivity : ComponentActivity() {
    //We create the instance of the ViewModel here and pass it to the composable
    private val viewModel: PrivacyPolicyViewModel by viewModels { PrivacyPolicyViewModelFactory(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Assign url from the SharedState, this was instantiated in the HealthConnectPermissionActivity
        val url = HealthConnectSharedState.privacyPolicyUrl ?: ""
        setContent {
            // Fetch content when the Activity is created
            viewModel.fetchContent(
                urlString = url,
                //we get the default text from the strings.xml file
                defaultContent = getString(R.string.default_privacy_policy_content),
                errorContent = getString(R.string.no_valid_privacy_policy_url)
            )
            //View that contains the content minting the status bar padding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                ContentView(viewModel)
            }
        }
    }

    @Composable
    fun ContentView(viewModel: PrivacyPolicyViewModel) {
        //To get the content from the ViewModel
        val content by viewModel.content.collectAsState()

        //We show the Webview when the content is not null
        AnimatedView(
            visible = content != null,
        ) {
            content?.let {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            loadData(it, "text/html", "UTF-8")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        //We show the CircularProgressIndicator when the content is null
        AnimatedView(
            visible = content == null,
        ) {
            CircularProgressIndicator()
        }
    }

    //Composable that animates the visibility of the view, doing a fade in and fade out animation
    @Composable
    fun AnimatedView(
        visible: Boolean,
        contentView: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            contentView()
        }
    }
}