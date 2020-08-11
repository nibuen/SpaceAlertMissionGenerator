package com.boarbeard.audio;

import android.net.Uri;

public class MediaInfo {

	public static final String BASE_RESOURCE = "android.resource://com.boarbeard/";

	private Uri resUri;
	private int duration;
	private boolean loopUntilNext;
	private long startTimeNanos;
	private String description;
	private String timeColor; // Color of the time when printed in log
	private String textColor; // Color of the text when printed in log

	public MediaInfo(int resId, int duration) {
		this(convertResIdToUri(resId), duration);
	}

	public static Uri convertResIdToUri(int resId) {
		return Uri.parse(BASE_RESOURCE + resId);
	}

	public MediaInfo(Uri resUri, int duration) {
		this.resUri = resUri;
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public Uri getResUri() {
		return resUri;
	}

	public boolean isLoopUntilNext() {
		return loopUntilNext;
	}

	public void setLoopUntilNext(boolean loopUntilNext) {
		this.loopUntilNext = loopUntilNext;
	}

	public MediaInfo copy() {
		MediaInfo mediaInfo = new MediaInfo(resUri, duration);
		mediaInfo.description = description;
		mediaInfo.loopUntilNext = loopUntilNext;
		return mediaInfo;
	}

	@Override
	public String toString() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStartTimeNanos(long startTimeNanos) {
		this.startTimeNanos = startTimeNanos;
	}

	public long getStartTimeNanos() {
		return startTimeNanos;
	}

	public String getTimeColor() {
		return timeColor;
	}

	public void setTimeColor(String timeColor) {
		this.timeColor = timeColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

}
