package com.example.damprojectfinal.core


object ApiConstants {
    const val BASE_URL = "http://192.168.1.7:3000"

    fun getFullImageUrl(path: String?): String? {
        if (path == null) return null
        return if (path.startsWith("http")) {
            path
        } else {
            "$BASE_URL$path"
        }
    }
}