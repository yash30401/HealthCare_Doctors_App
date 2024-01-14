package com.devyash.healthcaredoctorsapp.others

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthCareDoctorsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        AndroidThreeTen.init(this)
    }
}