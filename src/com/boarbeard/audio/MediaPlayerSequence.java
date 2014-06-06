package com.boarbeard.audio;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerSequence {

	protected List<MediaInfo> mediaPlayerList = new ArrayList<MediaInfo>();

	protected int playerIndex = 0;
	protected boolean stopped = false;
	protected boolean paused = false;

	protected MediaPlayer activeMediaPlayer = null;

	private Context context;

	public MediaPlayerSequence(Context context) {
		this.context = context;
	}

	public void addAudioClip(MediaInfo mediaInfo) {
		mediaPlayerList.add(mediaInfo);

	}

	public synchronized void start() {
		if (mediaPlayerList.size() <= playerIndex) {
			init();
		}

		stopped = false;
		if (paused) {
			paused = false;
			if (activeMediaPlayer != null) {
				activeMediaPlayer.start();
				return;
			}
		}

		playNextAudio(mediaPlayerList.get(playerIndex));
	}

	public synchronized void init() {
		playerIndex = 0;
		activeMediaPlayer = null;
	}

	public synchronized void stop() {
		stopped = true;
		if (activeMediaPlayer != null) {
			activeMediaPlayer.stop();
			activeMediaPlayer.release();
			activeMediaPlayer = null;
		}
	}

	private synchronized void playNextAudio(final MediaInfo mediaInfo) {
		activeMediaPlayer = MediaPlayer.create(context, mediaInfo.getResUri());
		activeMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {

						activeMediaPlayer.stop();
						activeMediaPlayer.release();

						if (stopped)
							return;

						playerIndex = nextIndex();
						if (mediaPlayerList.size() > playerIndex) {
							playNextAudio(mediaPlayerList.get(playerIndex));
						}
					}
				});

		if (!paused && !stopped) {
			activeMediaPlayer.start();
		}
	}

	public int nextIndex() {
		return playerIndex + 1;
	}

	public synchronized void pause() {
		paused = true;
		if (activeMediaPlayer != null && activeMediaPlayer.isPlaying()) {
			activeMediaPlayer.pause();
		}
	}

	public void reset() {
		stop();
		init();
	}

}
