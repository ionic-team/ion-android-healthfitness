package com.outsystems.plugins.healthfitness.viewmodel

import com.outsystems.plugins.healthfitness.utils.HttpConnector
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

class PrivacyPolicyViewModelFactory(
    private val owner: SavedStateRegistryOwner,
    private val defaultArgs: Bundle? = null,
    private val httpConnector: HttpConnector = HttpConnector // Assuming HttpConnector is an object or provide a way to instantiate it
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(PrivacyPolicyViewModel::class.java)) {
            return PrivacyPolicyViewModel(handle, httpConnector) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
