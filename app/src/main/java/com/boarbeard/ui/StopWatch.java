package com.boarbeard.ui;

import android.os.Handler;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class StopWatch {

    private volatile long startTime;
    private volatile long pauseTime;

    /**
	 * @param nanos
	 * @return
	 */
	public static CharSequence formatTime(long nanos) {
		long seconds = TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS);
		long minutes = seconds / 60;
		seconds = seconds % 60;

		StringBuilder sb = new StringBuilder().append(minutes).append(':');
		if (seconds < 10) {
			sb.append(0);
		}
		return sb.append(seconds);
	}

	private final TextView timeTextView;

    private Handler timerHandler = new Handler();

	private final Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
            updateClock();
            timerHandler.postDelayed(this, 500); // Update clock twice a second
		}
	};

	/**
	 * @param display
     * */
	public StopWatch(TextView display) {
		this.timeTextView = display;
        reset();
	}

	public void start() {
        startTime = System.nanoTime() - missionTimeInNanos();
        pauseTime = Long.MIN_VALUE;
        timerHandler.post(mUpdateTimeTask);
	}

	public void pause() {
        pauseTime = System.nanoTime();
        timerHandler.removeCallbacks(mUpdateTimeTask);
	}

	public void stop() {
		pause();
	}

    public void reset() {
        startTime = Long.MIN_VALUE;
        pauseTime = Long.MIN_VALUE;
        updateClock();
    }

    public long missionTimeInNanos() {
        if (isPaused()) {
            return pauseTime - startTime;
        } else if (isRunning()) {
            return System.nanoTime() - startTime;
        } else {
            return 0;
        }
    }

    public boolean isPaused() {
        return pauseTime > startTime;
    }

    public boolean isRunning() {
        return pauseTime < startTime;
    }

    private void updateClock() {
        long now = missionTimeInNanos();
        timeTextView.setText(formatTime(now));
    }

    @Override
	public String toString() {
        return formatTime(missionTimeInNanos()).toString();
	}

}
