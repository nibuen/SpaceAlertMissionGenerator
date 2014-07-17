package com.boarbeard.audio.parser;

import java.util.Map.Entry;

import android.util.Log;

import com.boarbeard.audio.MediaPlayerSequence;
import com.boarbeard.generator.beimax.EventList;
import com.boarbeard.generator.beimax.event.Announcement;
import com.boarbeard.generator.beimax.event.DataTransfer;
import com.boarbeard.generator.beimax.event.Event;
import com.boarbeard.generator.beimax.event.IncomingData;
import com.boarbeard.generator.beimax.event.Threat;
import com.boarbeard.generator.beimax.event.WhiteNoise;
import com.boarbeard.generator.beimax.event.WhiteNoiseRestored;

public abstract class EventListParser {

	private static final String LOG_TAG = EventListParser.class.getName();

	public void parse(EventList input, MediaPlayerSequence output) {

		for (Entry<Integer, Event> entry : input.getEntrySet()) {
			dispatch(entry.getValue(), entry.getKey() * 1000, output);
		}
	}

	public abstract void createAmbiance(MediaPlayerSequence output);

	private void dispatch(Event event, int startTime, MediaPlayerSequence output) {
		if (event instanceof Announcement) {
			visitAnnouncement((Announcement) event, startTime, output);
		} else if (event instanceof DataTransfer) {
			visitDataTransfer((DataTransfer) event, startTime, output);
		} else if (event instanceof IncomingData) {
			visitIncomingData((IncomingData) event, startTime, output);
		} else if (event instanceof Threat) {
			visitThreat((Threat) event, startTime, output);
		} else if (event instanceof WhiteNoise) {
			visitWhiteNoise((WhiteNoise) event, startTime, output);
		} else if (event instanceof WhiteNoiseRestored) {
			visitWhiteNoiseRestored((WhiteNoiseRestored) event, startTime,
					output);
		} else {
			Log.w(LOG_TAG, "Unknown event: " + event);
		}

	}

	protected abstract void visitWhiteNoiseRestored(WhiteNoiseRestored event,
			int startTime, MediaPlayerSequence output);

	protected abstract void visitIncomingData(IncomingData event,
			int startTime, MediaPlayerSequence output);

	protected abstract void visitDataTransfer(DataTransfer event,
			int startTime, MediaPlayerSequence output);

	protected abstract void visitThreat(Threat event, int startTime,
			MediaPlayerSequence output);

	protected abstract void visitWhiteNoise(WhiteNoise event, int startTime,
			MediaPlayerSequence output);

	protected abstract void visitAnnouncement(Announcement event,
			int startTime, MediaPlayerSequence output);
}
