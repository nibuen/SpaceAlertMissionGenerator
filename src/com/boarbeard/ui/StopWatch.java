package com.boarbeard.ui;

import android.widget.TextView;

import com.boarbeard.MissionTimer;

public class StopWatch {
	/**
	 * @param millis
	 * @return
	 */
	public  static CharSequence formatTime(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		seconds = seconds % 60;

		StringBuilder sb = new StringBuilder().append(minutes).append(':');
		if (seconds < 10) {
			sb.append(0);
		}
		return sb.append(seconds);
	}

	private final TextView timeTextView;

	private final MissionTimer timer;

	private final Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long now = timer.missionTimeMillis();
			timeTextView.setText(formatTime(now));
			long nextRun = now + 1000 - now % 1000; // next full second
			timer.runAtTime(this, nextRun);
		}
	};

	public StopWatch(TextView display) {
		this(display, new MissionTimer());
	}

	/**
	 * @param display
	 * @param timer
	 */
	public StopWatch(TextView display, MissionTimer timer) {
		super();
		this.timeTextView = display;
		this.timer = timer;
		mUpdateTimeTask.run();
	}

	public void start() {
		timer.start();
	}

	public void pause() {
		timer.pause();
	}

	public void stop() {
		timer.pause();
	}

	public void reset() {
		timer.reset();
		mUpdateTimeTask.run();
	}

	@Override
	public String toString() {

		return formatTime(timer.missionTimeMillis()).toString();
	}

}
