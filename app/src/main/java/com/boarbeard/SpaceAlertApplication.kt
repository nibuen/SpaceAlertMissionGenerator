package com.boarbeard

import android.app.Application
import com.boarbeard.io.ExternalMedia
import timber.log.Timber

class SpaceAlertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        ExternalMedia.init(this)
    }
}