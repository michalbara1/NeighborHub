package com.example.neighborhub.utils

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp

class CloudinaryUtils : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("CloudinaryUtils", "Initializing Cloudinary...")
        FirebaseApp.initializeApp(this)

        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dxzj1wktd",
            "api_key" to "589877463742111",
            "api_secret" to "8qCTGSOv63Bm_Rt1VMU5tweqW2k"
        )
        MediaManager.init(this, config)
    }
}