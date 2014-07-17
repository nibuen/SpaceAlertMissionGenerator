package com.boarbeard.audio.parser;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.boarbeard.audio.MediaPlayerSequence;
import com.boarbeard.audio.parser.Grammar.Element;
import com.boarbeard.generator.beimax.event.Threat;

/**
 * Parser which supports German Grammars.
 * 
 * @author Chris
 * 
 */
public class GermanParser extends EnglishParser {

	public static final Set<Element> REQUIRED_ELEMENTS = Collections
			.unmodifiableSet(EnumSet.<Element> of(
					Element.SeriousThreatZoneBlue,
					Element.SeriousThreatZoneRed,
					Element.SeriousThreatZoneWhite, Element.ThreatZoneBlue,
					Element.ThreatZoneRed, Element.ThreatZoneWhite));

	public GermanParser(Grammar grammar) {
		super(grammar);
	}

	@Override
	protected void appendThreatLocation(StringBuilder sb, Threat event,
			int startTime, MediaPlayerSequence output) {
		final Element location;

		if (event.getThreatLevel() == Threat.THREAT_LEVEL_SERIOUS) {
			if (event.getThreatPosition() == Threat.THREAT_POSITION_EXTERNAL) {
				switch (event.getSector()) {
				case Threat.THREAT_SECTOR_RED:
					location = Element.SeriousThreatZoneRed;
					break;
				case Threat.THREAT_SECTOR_WHITE:
					location = Element.SeriousThreatZoneWhite;
					break;
				case Threat.THREAT_SECTOR_BLUE:
					location = Element.SeriousThreatZoneBlue;
					break;
				default:
					throw new IllegalArgumentException(
							"Unknown Threat Sector: " + event.getSector());
				}
			} else {
				location = Element.SeriousInternalThreat;
			}
		} else {
			if (event.getThreatPosition() == Threat.THREAT_POSITION_EXTERNAL) {
				switch (event.getSector()) {
				case Threat.THREAT_SECTOR_RED:
					location = Element.ThreatZoneRed;
					break;
				case Threat.THREAT_SECTOR_WHITE:
					location = Element.ThreatZoneWhite;
					break;
				case Threat.THREAT_SECTOR_BLUE:
					location = Element.ThreatZoneBlue;
					break;
				default:
					throw new IllegalArgumentException(
							"Unknown Threat Sector: " + event.getSector());
				}
			} else {
				location = Element.InternalThreat;
			}
		}
		appendMedia(sb, location, startTime, output);
	}

}
