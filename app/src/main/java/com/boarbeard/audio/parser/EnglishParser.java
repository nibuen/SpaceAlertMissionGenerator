package com.boarbeard.audio.parser;

import android.util.Log;

import com.boarbeard.audio.MediaInfo;
import com.boarbeard.audio.MediaPlayerSequence;
import com.boarbeard.audio.parser.Grammar.Element;
import com.boarbeard.generator.beimax.event.Announcement;
import com.boarbeard.generator.beimax.event.DataTransfer;
import com.boarbeard.generator.beimax.event.Event;
import com.boarbeard.generator.beimax.event.IncomingData;
import com.boarbeard.generator.beimax.event.Threat;
import com.boarbeard.generator.beimax.event.WhiteNoise;
import com.boarbeard.generator.beimax.event.WhiteNoiseRestored;

/**
 * Parser which supports English grammars.
 * 
 * @author Chris
 * 
 */
public class EnglishParser extends EventListParser {

	private static final String LOG_TAG = EnglishParser.class.getName();
	private static final Element[] THREAT_TIMES = { Element.TimeTPlus1,
			Element.TimeTPlus2, Element.TimeTPlus3, Element.TimeTPlus4,
			Element.TimeTPlus5, Element.TimeTPlus6, Element.TimeTPlus7,
			Element.TimeTPlus8 };

	protected static Element getThreatTime(int time) {

		return THREAT_TIMES[time - 1];
	}

	private final Grammar grammar;

	public EnglishParser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}

	@Override
	protected void visitWhiteNoiseRestored(WhiteNoiseRestored event,
			long startTime, MediaPlayerSequence output) {

		output.addAudioClip(coloredLike(
				event,
				at(startTime,
						grammar.getMediaInfo(Element.CommunicationsDownFooter))));
	}

	private MediaInfo coloredLike(Event event, MediaInfo mediaInfo) {
		mediaInfo.setTextColor(event.getTextColor());
		mediaInfo.setTimeColor(event.getTimeColor());
		return mediaInfo;
	}

	private MediaInfo at(long startTime, MediaInfo mediaInfo) {
		mediaInfo.setStartTimeNanos(startTime);
		return mediaInfo;
	}

	@Override
	protected void visitIncomingData(IncomingData event, long startTime,
			MediaPlayerSequence output) {
		output.addAudioClip(coloredLike(event,
				at(startTime, grammar.getMediaInfo(Element.IncomingData))));
	}

	@Override
	protected void visitDataTransfer(DataTransfer event, long startTime,
			MediaPlayerSequence output) {
		output.addAudioClip(coloredLike(event,
				at(startTime, grammar.getMediaInfo(Element.DataTransfer))));
	}

	@Override
	protected void visitThreat(Threat event, long startTime,
			MediaPlayerSequence output) {

		MediaInfo alert = coloredLike(event,
				at(startTime, grammar.getMediaInfo(Element.AlertHeader)));
		output.addAudioClip(alert);

		alert.setDescription(buildThreatDetails(event, startTime, output));

		output.addAudioClip(at(startTime, grammar.getAudioOnly(Element.Repeat)));

		buildThreatDetails(event, startTime, output);
	}

	protected String buildThreatDetails(Threat event, long startTime,
			MediaPlayerSequence output) {
		StringBuilder sb = new StringBuilder();

		if (!event.isConfirmed()) {
			appendMedia(sb.append("<b>["), Element.UnconfirmedReport,
					startTime, output).append(":</b> ");
		}

		appendMedia(sb.append("<b>"), getThreatTime(event.getTime()),
				startTime, output).append("</b>. ");

		appendThreatLocation(sb, event, startTime, output);

		if (!event.isConfirmed()) {
			sb.append("<b>]</b>");
		}

		return sb.toString();
	}

	protected void appendThreatLocation(StringBuilder sb, Threat event,
			long startTime, MediaPlayerSequence output) {
		if (event.getThreatLevel() == Threat.THREAT_LEVEL_SERIOUS) {
			sb.append("<b>");
			if (event.getThreatPosition() == Threat.THREAT_POSITION_INTERNAL) {
				appendMedia(sb, Element.SeriousInternalThreat, startTime,
						output);
			} else {
				appendMedia(sb, Element.SeriousThreat, startTime, output);
			}
			sb.append("</b>");
		} else {
			if (event.getThreatPosition() == Threat.THREAT_POSITION_INTERNAL) {
				appendMedia(sb, Element.InternalThreat, startTime, output);
			} else {
				appendMedia(sb, Element.Threat, startTime, output);
			}
		}
		sb.append(". ");
		if (event.getThreatPosition() == Threat.THREAT_POSITION_EXTERNAL) {
			final Element sector;
			switch (event.getSector()) {
			case Threat.THREAT_SECTOR_RED:
				sector = Element.ZoneRed;
				break;
			case Threat.THREAT_SECTOR_WHITE:
				sector = Element.ZoneWhite;
				break;
			case Threat.THREAT_SECTOR_BLUE:
				sector = Element.ZoneBlue;
				break;
			default:
				throw new IllegalArgumentException("Unknown Threat Sector: "
						+ event.getSector());
			}
			appendMedia(sb, sector, startTime, output);
		}
	}

	protected StringBuilder appendMedia(StringBuilder text, Element element,
			long startTime, MediaPlayerSequence output) {
		text.append(grammar.getText(element));
		output.addAudioClip(at(startTime, grammar.getAudioOnly(element)));
		return text;
	}

	@Override
	protected void visitWhiteNoise(WhiteNoise event, long startTime,
			MediaPlayerSequence output) {
		output.addAudioClip(coloredLike(
				event,
				at(startTime,
						grammar.getMediaInfo(Element.CommunicationsDownHeader))));

		MediaInfo noise = at(startTime,
				grammar.getAudioOnly(Element.CommunicationsDownNoise));
		noise.setLoopUntilNext(true);
		output.addAudioClip(noise);

	}

	@Override
	protected void visitAnnouncement(Announcement event, long startTime,
			MediaPlayerSequence output) {

		final Element elem;
		switch (event.getType()) {
		case Announcement.ANNOUNCEMENT_PH1_START:
			elem = Element.AnnounceBeginFirstPhase;
			break;
		case Announcement.ANNOUNCEMENT_PH1_ONEMINUTE:
			elem = Element.AnnounceFirstPhaseEndsInOneMinute;
			break;
		case Announcement.ANNOUNCEMENT_PH1_TWENTYSECS:
			elem = Element.AnnounceFirstPhaseEndsInTwentySeconds;
			break;
		case Announcement.ANNOUNCEMENT_PH1_ENDS:
			output.addAudioClip(coloredLike(
					event,
					at(startTime, grammar
							.getMediaInfo(Element.AnnounceFirstPhaseEnds))));
			output.addAudioClip(at(startTime,
					grammar.getAudioOnly(Element.AnnounceSecondPhaseBegins)));
			return;
		case Announcement.ANNOUNCEMENT_PH2_ONEMINUTE:
			elem = Element.AnnounceSecondPhaseEndsInOneMinute;
			break;
		case Announcement.ANNOUNCEMENT_PH2_TWENTYSECS:
			elem = Element.AnnounceSecondPhaseEndsInTwentySeconds;
			break;
		case Announcement.ANNOUNCEMENT_PH2_ENDS:
			output.addAudioClip(coloredLike(
					event,
					at(startTime, grammar
							.getMediaInfo(Element.AnnounceSecondPhaseEnds))));
			output.addAudioClip(at(startTime,
					grammar.getAudioOnly(Element.AnnounceThirdPhaseBegins)));
			return;
		case Announcement.ANNOUNCEMENT_PH3_ONEMINUTE:
			elem = Element.AnnounceThirdPhaseEndsInOneMinute;
			break;
		case Announcement.ANNOUNCEMENT_PH3_TWENTYSECS:
			elem = Element.AnnounceThirdPhaseEndsInTwentySeconds;
			break;
		case Announcement.ANNOUNCEMENT_PH3_ENDS:
			elem = Element.AnnounceThirdPhaseEnds;
			break;
		default:
			Log.e(LOG_TAG, "Unknown announcement type: " + event.getType());
			return;
		}

		output.addAudioClip(coloredLike(event,
				at(startTime, grammar.getMediaInfo(elem))));

	}

	@Override
	public void createAmbiance(MediaPlayerSequence output) {
		output.addAudioClip(grammar.getAudioOnly(Element.RedAlertLevel1));
	}
}
