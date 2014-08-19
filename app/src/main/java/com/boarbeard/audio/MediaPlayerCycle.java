package com.boarbeard.audio;

import android.content.Context;

public class MediaPlayerCycle extends MediaPlayerSequence {
	
	public MediaPlayerCycle(Context context) {
		super(context);
	}

	@Override
	public void nextIndex() {
		playerIndex = (playerIndex + 1) % mediaPlayerList.size();
	}

}
