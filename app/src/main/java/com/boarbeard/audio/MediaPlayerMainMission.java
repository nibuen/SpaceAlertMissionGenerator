package com.boarbeard.audio;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.boarbeard.R;
import com.boarbeard.audio.parser.EventListParserFactory;
import com.boarbeard.ui.MissionActivity;
import com.boarbeard.ui.StopWatch;

import java.util.concurrent.TimeUnit;

public class MediaPlayerMainMission extends MediaPlayerSequence {

	private Handler timerHandler = new Handler();

	private long startTime;
	private long pauseTime;

	protected StopWatch stopWatch;

	protected MissionActivity missionActivity;
	protected String missionLog;

	private MediaPlayerCycle mediaPlayerBackgroundSounds;

	private SharedPreferences preferences; // Displays the log texts, so needs
											// log color preferences

	private final Runnable mPlayNextAudioTask = new Runnable() {
		public void run() {
			mediaPlayerBackgroundSounds.pause();
			playNextAudio();
		}
	};

	public MediaPlayerMainMission(MissionActivity missionActivity,
			StopWatch stopWatch, SharedPreferences preferences) {
		super(missionActivity);

		this.stopWatch = stopWatch;
		this.missionActivity = missionActivity;
		this.preferences = preferences; // Displays the log texts, so needs log
										// color preferences
		init();

		mediaPlayerBackgroundSounds = new MediaPlayerCycle(missionActivity);
		EventListParserFactory.getInstance().getParser(missionActivity)
				.createAmbiance(mediaPlayerBackgroundSounds);

	}

	public synchronized void start() {
		if (mediaPlayerList.size() <= playerIndex) {
			init();
		}

		stopWatch.start();

		stopped = false;
		if (paused) {
			paused = false;

			// Even when a running sound was paused, you have to update the
			// startTime for the next file
			startTime += System.nanoTime() - pauseTime;

			if (activeMediaPlayer == null) {
				mediaPlayerBackgroundSounds.start();
			} else {
				activeMediaPlayer.start();
			}
			if (activeMediaPlayer == null || activeMediaPlayer.isLooping()) {
				postAtTime(mPlayNextAudioTask, startTime
                        + mediaPlayerList.get(playerIndex).getStartTimeNanos());
			}
			return;
		}

		startTime = System.nanoTime();
		playNextAudio();
	}

    public synchronized void init() {
		playerIndex = 0;
		activeMediaPlayer = null;
		missionLog = "";
		missionActivity.updateMissionLog(missionLog);
		stopWatch.reset();
	}

	private synchronized void playNextAudio() {
		if (activeMediaPlayer != null) {
			// TODO reuse player
			activeMediaPlayer.release();
		}

		final MediaInfo mediaInfo = mediaPlayerList.get(playerIndex);
		activeMediaPlayer = MediaPlayer.create(missionActivity,
				mediaInfo.getResUri());

		updateMissionLog(mediaInfo);

		if (mediaInfo.isLoopUntilNext()
				&& playerIndex + 1 < mediaPlayerList.size()) {
			activeMediaPlayer.setLooping(true);
			planNextAudioTask();
		} else {
			activeMediaPlayer
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						// @Override
						public void onCompletion(MediaPlayer mp) {

							activeMediaPlayer.stop();
							activeMediaPlayer.release();
							activeMediaPlayer = null;

							if (stopped) {
								return;
							} else {
								planNextAudioTask();
							}
						}
					});
		}

		if (!paused && !stopped) {
			try {
				activeMediaPlayer.start();
			} catch (IllegalStateException e) {
				Toast.makeText(missionActivity, R.string.media_failed,
						Toast.LENGTH_LONG).show();
				stop();
			}
		}
	}

	private void updateMissionLog(final MediaInfo mediaInfo) {
		boolean bLogColors;
		if (!TextUtils.isEmpty(mediaInfo.toString())) {

			bLogColors = preferences.getBoolean("logColorsPreference", true);

			// Display log in colors (if preference is set to true)
			// Note: Some parts are now bold, regardless of the color settings
			// (Time, Unconfirmed, Serious - like the printed cards)
			if ((mediaInfo.getTimeColor() == null) || (bLogColors == false)) {
				missionLog = missionLog + "<b>" + stopWatch.toString()
						+ "</b> -- ";

			} else {
				missionLog = missionLog + "<b><font color=\""
						+ mediaInfo.getTimeColor() + "\">"
						+ StopWatch.formatTime(mediaInfo.getStartTimeNanos()) + "</b></font> -- ";
			}

			if ((mediaInfo.getTimeColor() == null) || (bLogColors == false)) {
				missionLog = missionLog + mediaInfo.toString() + "<br>";
			} else {
				missionLog = missionLog + "<font color=\""
						+ mediaInfo.getTextColor() + "\">"
						+ mediaInfo.toString() + "</font><br>";
			}

			missionActivity.updateMissionLog(missionLog);
		}
	}

    @Override
	public synchronized void pause() {
        super.pause();
		stopWatch.pause();
		mediaPlayerBackgroundSounds.pause();
		missionActivity.toggleOff();
		pauseTime = System.nanoTime();
		timerHandler.removeCallbacks(mPlayNextAudioTask);
	}

    @Override
	public synchronized void stop() {
        super.stop();
		missionActivity.toggleOff();
		mediaPlayerBackgroundSounds.stop();
		stopWatch.stop();
	}

	/**
	 *
	 */
	private void planNextAudioTask() {
		playerIndex++;
		if (mediaPlayerList.size() > playerIndex) {
			long nextTime = startTime
					+ mediaPlayerList.get(playerIndex).getStartTimeNanos();
			if (activeMediaPlayer == null
					&& nextTime > System.nanoTime()) {
				mediaPlayerBackgroundSounds.start();
			}

			postAtTime(mPlayNextAudioTask, nextTime);
		} else {
			stop();
		}
	}

    private void postAtTime(Runnable runnable, long timeInNanos) {
        long timeInMillis = TimeUnit.MILLISECONDS.convert(timeInNanos, TimeUnit.NANOSECONDS);
        timerHandler.postAtTime(runnable, timeInMillis);
    }

	/*
	 * Dumps the entire mission to the mission log.
	 */
	public void dumpMissionTreeToLog() {
		int i = 0;
		while (mediaPlayerList.size() > i) {
			final MediaInfo mediaInfo = mediaPlayerList.get(i);
			updateMissionLog(mediaInfo);
			i++;
		}
	}
}
