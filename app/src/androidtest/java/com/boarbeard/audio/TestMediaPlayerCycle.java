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
public class TestMediaPlayerCycle {

    @Test
    public void testCycle() {
        ExposedMediaPlayerCycle sequence = new ExposedMediaPlayerCycle(getContext());
        sequence.addAudioClip(new MediaInfo(R.raw.alert, 5));
        sequence.addAudioClip(new MediaInfo(R.raw.data_transfer, 5));

        // Push to end of cycle once
        sequence.nextIndex();
        sequence.nextIndex();
        sequence.start();

        assertEquals(0, sequence.playerIndex());
    }

    private class ExposedMediaPlayerCycle extends MediaPlayerCycle {

        public ExposedMediaPlayerCycle(Context context) {
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
