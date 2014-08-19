package com.boarbeard.audio;


import android.content.Context;
import android.media.MediaPlayer;
import android.test.AndroidTestCase;

import com.boarbeard.R;

public class TestMediaPlayerSequence extends AndroidTestCase {


    public void testStopBeforeStart() {
        ExposedMediaPlayerSequence sequence = new ExposedMediaPlayerSequence(getContext());
        sequence.addAudioClip(new MediaInfo(R.raw.alert, 5));
        sequence.stop();
        sequence.start();
    }

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
