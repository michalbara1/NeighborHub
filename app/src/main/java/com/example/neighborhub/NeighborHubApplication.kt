package com.example.neighborhub

import android.app.Application
import com.google.firebase.FirebaseApp

class NeighborHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}