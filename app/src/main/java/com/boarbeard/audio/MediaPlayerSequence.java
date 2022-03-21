package com.boarbeard.audio;

import android.content.Context;
import android.media.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerSequence {

    protected List<MediaInfo> mediaPlayerList = new ArrayList<>();

    protected int playerIndex = 0;
    protected boolean stopped = false;
    protected boolean paused = false;

    protected MediaPlayer activeMediaPlayer = null;

    private final WeakReference<Context> context;

    public MediaPlayerSequence(Context context) {
        this.context = new WeakReference<>(context);
    }

    public void addAudioClip(MediaInfo mediaInfo) {
        mediaPlayerList.add(mediaInfo);

    }

    public synchronized void start() {
        if (mediaPlayerList.size() <= playerIndex) {
            reset();
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

    public synchronized void stop() {
        stopped = true;
        if (activeMediaPlayer != null) {
            activeMediaPlayer.stop();
            activeMediaPlayer.release();
            activeMediaPlayer = null;
        }
    }

    private synchronized void playNextAudio(final MediaInfo mediaInfo) {
        Context c = context.get();
        if (c == null) {
            return;
        }
        activeMediaPlayer = MediaPlayer.create(c, mediaInfo.getResUri());
        activeMediaPlayer
                .setOnCompletionListener(mp -> {
                    activeMediaPlayer.stop();
                    activeMediaPlayer.release();

                    if (stopped)
                        return;

                    nextIndex();
                    if (mediaPlayerList.size() > playerIndex) {
                        playNextAudio(mediaPlayerList.get(playerIndex));
                    }
                });

        if (!paused && !stopped) {
            activeMediaPlayer.start();
        }
    }

    public synchronized void pause() {
        paused = true;
        if (activeMediaPlayer != null && activeMediaPlayer.isPlaying()) {
            activeMediaPlayer.pause();
        }
    }

    public void reset() {
        stop();
        playerIndex = 0;
    }

    public void nextIndex() {
        playerIndex++;
    }
}
