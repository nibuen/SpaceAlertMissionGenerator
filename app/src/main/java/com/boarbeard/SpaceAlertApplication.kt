package com.boarbeard

import android.app.Application
import timber.log.Timber

class SpaceAlertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}