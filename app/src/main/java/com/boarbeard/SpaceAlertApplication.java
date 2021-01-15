package com.boarbeard;

import android.app.Application;

import com.boarbeard.io.ExternalMedia;

import timber.log.Timber;

public class SpaceAlertApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        ExternalMedia.init(this);
    }
}
