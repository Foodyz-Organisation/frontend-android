package com.example.damprojectfinal.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A generic ViewModel factory used to instantiate ViewModels that require custom constructor arguments,
 * such as API services or NavControllers, which cannot be provided by the default factory.
 */
class ViewModelFactory<T : ViewModel>(val creator: () -> T) : ViewModelProvider.Factory {

    // We suppress the unchecked cast warning because the creator function guarantees the type T.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // We run the provided lambda to create the ViewModel instance.
        return creator() as T
    }
}