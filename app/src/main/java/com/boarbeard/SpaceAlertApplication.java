package com.boarbeard;

import android.app.Application;

import com.boarbeard.io.ExternalMedia;

public class SpaceAlertApplication extends Application {
	private static SpaceAlertApplication singleton;

	public static SpaceAlertApplication getInstance() {
		return singleton;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;

		ExternalMedia.init(this);
	}
}
