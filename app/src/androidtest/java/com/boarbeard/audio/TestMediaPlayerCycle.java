package com.boarbeard.audio;


import android.content.Context;
import android.media.MediaPlayer;
import android.test.AndroidTestCase;

import com.boarbeard.R;

public class TestMediaPlayerCycle extends AndroidTestCase {


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
