package com.boarbeard.audio.parser;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Xml;

import com.boarbeard.audio.MediaInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public abstract class Grammar {
    private static final String LOG_TAG = Grammar.class.getSimpleName();
    private static final String GRAMMAR_XML_ROOT_TAG = "Settings";

    /**
     * Known Grammar elements
     *
     * @author Chris
     */
    public enum Element {

        Grammar(null), //

        CommunicationsDownHeader("communications_down"), //
        CommunicationsDownNoise("white_noise"), //
        CommunicationsDownFooter("communications_restored"), //

        AnnounceBeginFirstPhase("begin_first_phase"), //
        AnnounceFirstPhaseEndsInOneMinute("first_phase_ends_in_1_minute"), //
        AnnounceFirstPhaseEndsInTwentySeconds("first_phase_ends_in_20_seconds"), //
        AnnounceFirstPhaseEnds("first_phase_ends"), //
        AnnounceSecondPhaseBegins("second_phase_begins"), //
        AnnounceSecondPhaseEndsInOneMinute("second_phase_ends_in_1_minute"), //
        AnnounceSecondPhaseEndsInTwentySeconds(
                "second_phase_ends_in_20_seconds"), //
        AnnounceSecondPhaseEnds("second_phase_ends"), //
        AnnounceThirdPhaseBegins("third_phase_begins"), //
        AnnounceThirdPhaseEndsInOneMinute("operation_ends_in_1_minute"), //
        AnnounceThirdPhaseEndsInTwentySeconds("operation_ends_in_20_seconds"), //
        AnnounceThirdPhaseEnds("operation_ends"), //

        IncomingData("incoming_data"), //
        DataTransfer("data_transfer"), //

        AlertHeader("alert"), //
        InternalThreat("internal_threat"), //
        SeriousInternalThreat("serious_internal_threat"), //
        Repeat("repeat"), //
        UnconfirmedReport("unconfirmed_report"), //

        TimeTPlus1("time_t_plus_1"), //
        TimeTPlus2("time_t_plus_2"), //
        TimeTPlus3("time_t_plus_3"), //
        TimeTPlus4("time_t_plus_4"), //
        TimeTPlus5("time_t_plus_5"), //
        TimeTPlus6("time_t_plus_6"), //
        TimeTPlus7("time_t_plus_7"), //
        TimeTPlus8("time_t_plus_8"), //

        RedAlertLevel1("red_alert_1"), //

        // English specific elements
        Threat("threat"), //
        SeriousThreat("serious_threat"), //
        ZoneBlue("zone_blue"), //
        ZoneRed("zone_red"), //
        ZoneWhite("zone_white"), //

        // German specific elements
        ThreatZoneBlue("threat_zone_blue"), //
        ThreatZoneRed("threat_zone_red"), //
        ThreatZoneWhite("threat_zone_white"), //
        SeriousThreatZoneBlue("serious_threat_zone_blue"), //
        SeriousThreatZoneRed("serious_threat_zone_red"), //
        SeriousThreatZoneWhite("serious_threat_zone_white");//

        public static Element valueOfIC(String name) {
            for (Element elem : values()) {
                if (elem.name().equalsIgnoreCase(name)) {
                    return elem;
                }
            }
            throw new IllegalArgumentException("Unknown element: " + name);
        }

        private final String fileName;

        Element(String fileName) {

            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

    }

    private final Map<Element, MediaInfo> elemTable;
    private final Set<Element> elements;

    /**
     * Constructs an empty grammar
     */
    public Grammar() {
        super();
        elemTable = new EnumMap<>(Element.class);
        elements = Collections.unmodifiableSet(elemTable.keySet());
    }

    public Grammar(Grammar parent) {
        this();
        if (parent != null) {
            elemTable.putAll(parent.elemTable);
        }
    }

    public MediaInfo getMediaInfo(Element elem) {
        MediaInfo mediaInfo = elemTable.get(elem);
        return mediaInfo == null ? null : mediaInfo.copy();
    }

    public MediaInfo getAudioOnly(Element elem) {
        MediaInfo mediaInfo = getMediaInfo(elem);
        if (mediaInfo != null) {
            mediaInfo.setDescription("");
        }
        return mediaInfo;
    }

    public String getText(Element elem) {
        MediaInfo mediaInfo = elemTable.get(elem);
        return mediaInfo == null ? "" : mediaInfo.toString();
    }

    public Set<Element> getSupportedElements() {
        return elements;
    }

    /**
     * Adds support for an Element to this Grammar.
     *
     * @param elem        the supported Element
     * @param description Description of the Element
     * @param mediaUri    Uri of the media file belonging to this Element. If
     *                    {@code null} a zero-length MediaInfo will be created.
     * @param context     Context to load the media file
     * @throws IllegalArgumentException If playback of mediaUri is not supported
     */
    protected void addElement(Element elem, String description, Uri mediaUri,
                              Context context) {
        int duration;

        if (mediaUri == null) {
            duration = 0;
        } else {
            MediaPlayer mp = MediaPlayer.create(context, mediaUri);
            if (mp == null) {
                throw new IllegalArgumentException("Media cannot be played");
            }
            duration = mp.getDuration();
            mp.reset();
            mp.release();
        }

        MediaInfo mediaInfo = new MediaInfo(mediaUri, duration);
        mediaInfo.setDescription(description);

        elemTable.put(elem, mediaInfo);

    }

    /**
     * Adds support for an Element to this Grammar.
     *
     * @param elem        the supported Element
     * @param description Description of the Element
     * @param mediaResId  Ressource id of the media file belonging to this Element
     * @param context     Context to load the media file
     */
    protected void addElement(Element elem, String description, int mediaResId,
                              Context context) {
        addElement(elem, description, MediaInfo.convertResIdToUri(mediaResId),
                context);
    }

    public abstract String getName();

    protected static Map<Element, String> parseGrammarXml(InputStream in)
            throws XmlPullParserException, IOException {

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, null, GRAMMAR_XML_ROOT_TAG);

        Map<Element, String> result = new EnumMap<>(
                Element.class);
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            try {
                result.put(Element.valueOfIC(parser.getName()),
                        parser.nextText());
            } catch (IllegalArgumentException e) {
                Timber.tag(LOG_TAG).e(e);
                skipTag(parser);
            }

        }
        parser.require(XmlPullParser.END_TAG, null, GRAMMAR_XML_ROOT_TAG);

        return result;
    }

    private static void skipTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, null);
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
