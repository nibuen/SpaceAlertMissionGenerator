package com.boarbeard.audio.parser;

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

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public abstract class EventListParser {

	private static final String LOG_TAG = EventListParser.class.getName();

	public void parse(EventList input, MediaPlayerSequence output) {

		for (Entry<Integer, Event> entry : input.getEntrySet()) {
			dispatch(entry.getValue(), TimeUnit.NANOSECONDS.convert(entry.getKey(), TimeUnit.SECONDS), output);
		}
	}

	public abstract void createAmbiance(MediaPlayerSequence output);

	private void dispatch(Event event, long startTime, MediaPlayerSequence output) {
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
			long startTime, MediaPlayerSequence output);

	protected abstract void visitIncomingData(IncomingData event,
			long startTime, MediaPlayerSequence output);

	protected abstract void visitDataTransfer(DataTransfer event,
			long startTime, MediaPlayerSequence output);

	protected abstract void visitThreat(Threat event, long startTime,
			MediaPlayerSequence output);

	protected abstract void visitWhiteNoise(WhiteNoise event, long startTime,
			MediaPlayerSequence output);

	protected abstract void visitAnnouncement(Announcement event,
			long startTime, MediaPlayerSequence output);
}
