package com.example.damprojectfinal.core

import com.example.damprojectfinal.core.api.BaseUrlProvider

/**
 * @deprecated Use BaseUrlProvider instead. This file is kept for backward compatibility.
 * Will be removed in future versions.
 */
object ApiConstants {
    @Deprecated("Use BaseUrlProvider.BASE_URL instead", ReplaceWith("BaseUrlProvider.BASE_URL"))
    val BASE_URL: String = BaseUrlProvider.BASE_URL

    fun getFullImageUrl(path: String?): String? {
        return BaseUrlProvider.getFullImageUrl(path)
    }
}