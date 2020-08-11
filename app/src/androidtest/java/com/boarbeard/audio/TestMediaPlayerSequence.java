package com.boarbeard.audio;


import android.content.Context;
import android.media.MediaPlayer;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.boarbeard.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TestMediaPlayerSequence {

    @Test
    public void testStopBeforeStart() {
        ExposedMediaPlayerSequence sequence = new ExposedMediaPlayerSequence(getContext());
        sequence.addAudioClip(new MediaInfo(R.raw.alert, 5));
        sequence.stop();
        sequence.start();
    }

    @Test
    public void testReset() {
        ExposedMediaPlayerSequence sequence = new ExposedMediaPlayerSequence(getContext());
        sequence.addAudioClip(new MediaInfo(R.raw.alert, 5));
        sequence.addAudioClip(new MediaInfo(R.raw.data_transfer, 5));

        // Put us to data transfer
        sequence.nextIndex();
        sequence.start();
        assertEquals(1, sequence.playerIndex());

        sequence.pause();
        sequence.reset();

        sequence.start();
        assertEquals(0, sequence.playerIndex());
    }

    private class ExposedMediaPlayerSequence extends MediaPlayerSequence {

        public ExposedMediaPlayerSequence(Context context) {
            super(context);
        }

        public MediaPlayer currentMediaPlayer() {
            return activeMediaPlayer;
        }

        public int playerIndex() {
            return playerIndex;
        }
    }

}
