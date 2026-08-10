package com.outsystems.plugins.healthfitness.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outsystems.plugins.healthfitness.utils.HttpConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

//We are using a View Model to fetch and store the content of the Privacy Policy
class PrivacyPolicyViewModel(private val savedStateHandle: SavedStateHandle, private val httpConnector: HttpConnector = HttpConnector) : ViewModel() {
    companion object {
        //key to store the content in the SavedStateHandle
        const val CONTENT_KEY = "PRIVACY_POLICY_CONTENT"
    }
    //Flow to store the content
    private val _content = MutableStateFlow<String?>(savedStateHandle[CONTENT_KEY])
    val content: StateFlow<String?> = _content

    //Function to fetch the content from the url passed as parameter
    fun fetchContent(urlString: String, defaultContent: String, errorContent: String) {
        //Fetch the content from the url passed as parameter using the viewmodel coroutine scope
        viewModelScope.launch {
            val fetchedContent =
                //return the content of the url if url is not null or empty
                if (urlString.isNotBlank()) {
                    val url = URL(urlString)
                    //we need to do this in the context of the coroutine scope
                    val result = withContext(Dispatchers.IO) {
                        try {
                            httpConnector.connectionURL(url)
                        } catch (e: Exception) {
                            /*if there is any error we return the previously loaded content instead the errorContent
                              if there is not any content saved in saved state handle then return the errorContent*/
                            savedStateHandle[CONTENT_KEY] ?: errorContent
                        }
                    }
                    // Save the fetched content in saved state handle for process death recovery
                    savedStateHandle[CONTENT_KEY] = result
                    result
                } else {
                    /*if url is null or empty return the previously loaded content instead the default content
                    if there is not any content saved in saved state handle then return the default content*/
                    savedStateHandle[CONTENT_KEY] ?: defaultContent
                }
            //update the content in the flow
            _content.value = fetchedContent
        }
    }
}