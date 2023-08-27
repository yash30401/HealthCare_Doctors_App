package com.devyash.healthcaredoctorsapp.others

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthCareDoctorsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}