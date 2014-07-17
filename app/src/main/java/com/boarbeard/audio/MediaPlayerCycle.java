package com.boarbeard.audio;

import com.boarbeard.ui.MissionActivity;

public class MediaPlayerCycle extends MediaPlayerSequence {
	
	public MediaPlayerCycle(MissionActivity missionActivity) {
		super(missionActivity);
	}

	@Override
	public int nextIndex() {
		playerIndex = (playerIndex + 1) % mediaPlayerList.size();
		return playerIndex;
	}

}
