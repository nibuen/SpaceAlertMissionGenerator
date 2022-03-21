package com.boarbeard.generator.beimax;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.SharedPreferences;

import com.boarbeard.R;
import com.boarbeard.generator.beimax.event.Announcement;
import com.boarbeard.generator.beimax.event.DataTransfer;
import com.boarbeard.generator.beimax.event.Event;
import com.boarbeard.generator.beimax.event.IncomingData;
import com.boarbeard.generator.beimax.event.Threat;
import com.boarbeard.generator.beimax.event.WhiteNoise;
import com.boarbeard.generator.beimax.event.WhiteNoiseRestored;
import com.boarbeard.ui.MissionType;

import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * TestMissionImpl2 because there's already a TestMissionImpl under
 * app/src/androidtest, but it's not getting picked up as a test by Android
 * Studio for me.  This runs un-instrumented using a mock Log implementation
 * under app/src/test.
 */
public class TestMissionImpl2 {

    @Test
    public void testParsePreferences() {
        BogoSharedPreferences sp;
        MissionPreferences mp;

        sp = new BogoSharedPreferences();
        sp.set("stompUnconfirmedReportsPreference", false);
        mp = MissionImpl.parsePreferences(sp);
        check(mp, 5, 8, 1, true, 1, 6, 240, 210, 150, 255, 225, 165);

        sp.set("stompUnconfirmedReportsPreference", true);
        sp.set("playerCount", 5);
        mp = MissionImpl.parsePreferences(sp);
        check(mp, 5, 8, 0, false, 1, 6, 240, 210, 150, 255, 225, 165);

        sp.set("playerCount", 4);
        mp = MissionImpl.parsePreferences(sp);
        check(mp, 4, 7, 0, false, 1, 6, 240, 210, 150, 255, 225, 165);
    }

    @Test
    public void testMission1UnconfirmedReportPrefs() {
        //  showing unconfirmed reports
        testMission1UnconfirmedReports(true, 5, false);
        //  Same thing again, but this time with unconfirmed reports treated as
        //  normal threats
        testMission1UnconfirmedReports(false, 5, false);
        //  Same thing again, but this time with no unconfirmed reports
        testMission1UnconfirmedReports(false, 4, false);

        //  Same things, compressed (I know, debugging a debug option, silly,
        //  but I really did want to know whether it worked!)
        testMission1UnconfirmedReports(true, 5, true);
        testMission1UnconfirmedReports(false, 5, true);
        testMission1UnconfirmedReports(false, 4, true);

        //  The first one again to confirm we didn't mess with the original list
        testMission1UnconfirmedReports(true, 5, false);
    }

    @Test
    public void testRandomUnconfirmedReportPrefs() {
        //  showing unconfirmed reports
        testRandomUnconfirmedReports(true, 5, false);
        //  again
        testRandomUnconfirmedReports(true, 5, false);
        //  Same thing again, but this time with unconfirmed reports treated as
        //  normal threats
        testRandomUnconfirmedReports(false, 5, false);
        //  Same thing again, but this time with no unconfirmed reports
        testRandomUnconfirmedReports(false, 4, false);

        //  Same things, compressed
        testRandomUnconfirmedReports(true, 5, true);
        testRandomUnconfirmedReports(false, 5, true);
        testRandomUnconfirmedReports(false, 4, true);

        //  The first one again
        testRandomUnconfirmedReports(true, 5, false);
    }

    private void testMission1UnconfirmedReports(boolean showUnconfirmed,
                                                int players, boolean compressed) {
        SharedPreferences sp = mockPreferences(showUnconfirmed, players, compressed, 0);
        EventList eventList = MissionType.RealMission1.getEventList(sp);

        //  if you run into problems
        //dumpEvents(eventList);

        List<Map.Entry<Integer, Event>> tl = eventList.getEntryList();
        assertEquals((showUnconfirmed || (players == 5)) ? 30 : 29, tl.size());
        //  just a shorter variable name for the conditionals below
        boolean at = !compressed;
        //  ugh, and compressed times after the removed unconfirmed report are
        //  different, so... if times are compressed, adjust the time of every
        //  event *after* the unconfirmed report happened, or would have
        //  happened.  This is a long way to go for testing a goofy debug flag!
        int off = 0;
        int idx = 0;
        check(tl, idx++, 0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
        check(tl, idx++, 10, new Threat(2, true, true, Threat.Zone.White));
        if (showUnconfirmed) {
            check(tl, idx++, at ? 55 : 25, new Threat(3, false, false));
        } else if (players == 5) {
            check(tl, idx++, at ? 55 : 25, new Threat(3, true, false));
        } else {
            //  unconfirmed report should've been removed
            if (compressed) off = -15;
        }
        check(tl, idx++, at ? 110 : off + 40, new Threat(4, true, false, Threat.Zone.Blue));
        check(tl, idx++, at ? 140 : off + 55, new IncomingData());
        check(tl, idx++, at ? 165 : off + 65, new Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE));
        check(tl, idx++, at ? 170 : off + 70, new WhiteNoise(at ? 10 : 4));
        check(tl, idx++, at ? 180 : off + 74, new WhiteNoiseRestored());
        check(tl, idx++, at ? 185 : off + 79, new DataTransfer());
        check(tl, idx++, at ? 205 : off + 94, new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS));
        check(tl, idx++, at ? 220 : off + 99, new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS));
        check(tl, idx++, at ? 230 : off + 109, new IncomingData());
        check(tl, idx++, at ? 240 : off + 119, new Threat(5, true, false));
        check(tl, idx++, at ? 265 : off + 134, new DataTransfer());
        check(tl, idx++, at ? 290 : off + 149, new Threat(6, true, false, Threat.Zone.Blue));
        check(tl, idx++, at ? 320 : off + 164, new WhiteNoise(at ? 15 : 4));
        check(tl, idx++, at ? 335 : off + 168, new WhiteNoiseRestored());
        check(tl, idx++, at ? 350 : off + 173, new Threat(7, true, true, Threat.Zone.Red));
        check(tl, idx++, at ? 390 : off + 188, new Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE));
        check(tl, idx++, at ? 395 : off + 193, new DataTransfer());
        check(tl, idx++, at ? 430 : off + 208, new Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS));
        check(tl, idx++, at ? 445 : off + 213, new Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS));
        check(tl, idx++, at ? 470 : off + 223, new WhiteNoise(at ? 20 : 4));
        check(tl, idx++, at ? 490 : off + 227, new WhiteNoiseRestored());
        check(tl, idx++, at ? 500 : off + 232, new DataTransfer());
        check(tl, idx++, at ? 540 : off + 247, new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE));
        check(tl, idx++, at ? 555 : off + 252, new WhiteNoise(at ? 10 : 4));
        check(tl, idx++, at ? 565 : off + 256, new WhiteNoiseRestored());
        check(tl, idx++, at ? 580 : off + 261, new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS));
        check(tl, idx++, at ? 595 : off + 266, new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS));
        assertEquals(tl.size(), idx);  //  make sure we checked them all
    }

    private void testRandomUnconfirmedReports(boolean showUnconfirmed, int players, boolean compressed) {
        SharedPreferences sp = mockPreferences(showUnconfirmed, players, compressed, 666);
        EventList eventList = MissionType.Random.getEventList(sp);

        //  if you run into problems
        //dumpEvents(eventList);

        List<Map.Entry<Integer, Event>> tl = eventList.getEntryList();
        //  just a shorter variable name for the conditionals below
        boolean at = !compressed;
        int idx = 0;
        //  well, the random lists of events wind up being completely different
        //  when you stomp unconfirmed reports, so making this one method
        //  instead of three was maybe a little silly.
        if (showUnconfirmed) {
            assertEquals(30, tl.size());
            check(tl, idx++, at ? 0 : 0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
            check(tl, idx++, at ? 14 : 10, new Threat(1, true, false, Threat.Zone.White));
            check(tl, idx++, at ? 47 : 25, new WhiteNoise(at ? 12 : 4));
            check(tl, idx++, at ? 59 : 29, new WhiteNoiseRestored());
            check(tl, idx++, at ? 64 : 34, new Threat(2, true, false, Threat.Zone.Red));
            check(tl, idx++, at ? 95 : 49, new DataTransfer());
            check(tl, idx++, at ? 115 : 64, new Threat(4, true, false));
            check(tl, idx++, at ? 140 : 79, new IncomingData());
            check(tl, idx++, at ? 164 : 89, new IncomingData());
            check(tl, idx++, at ? 183 : 99, new Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE));
            check(tl, idx++, at ? 223 : 104, new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS));
            check(tl, idx++, at ? 238 : 109, new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS));
            check(tl, idx++, at ? 275 : 119, new Threat(5, true, false));
            check(tl, idx++, at ? 299 : 134, new Threat(6, true, true, Threat.Zone.Blue));
            check(tl, idx++, at ? 335 : 149, new WhiteNoise(at ? 19 : 4));
            check(tl, idx++, at ? 354 : 153, new WhiteNoiseRestored());
            check(tl, idx++, at ? 362 : 158, new Threat(7, true, false, Threat.Zone.White));
            check(tl, idx++, at ? 390 : 173, new IncomingData());
            check(tl, idx++, at ? 406 : 183, new Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE));
            check(tl, idx++, at ? 415 : 188, new Threat(8, false, false, Threat.Zone.Blue));
            check(tl, idx++, at ? 446 : 203, new Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS));
            check(tl, idx++, at ? 461 : 208, new Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS));
            check(tl, idx++, at ? 474 : 218, new DataTransfer());
            check(tl, idx++, at ? 534 : 233, new WhiteNoise(at ? 16 : 4));
            check(tl, idx++, at ? 550 : 237, new WhiteNoiseRestored());
            check(tl, idx++, at ? 568 : 242, new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE));
            check(tl, idx++, at ? 575 : 247, new WhiteNoise(at ? 12 : 4));
            check(tl, idx++, at ? 587 : 251, new WhiteNoiseRestored());
            check(tl, idx++, at ? 608 : 256, new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS));
            check(tl, idx++, at ? 623 : 261, new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS));
        } else if (players == 5) {
            assertEquals(28, tl.size());
            check(tl, idx++, at ? 0 : 0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
            check(tl, idx++, at ? 18 : 10, new Threat(1, true, false, Threat.Zone.Blue));
            check(tl, idx++, at ? 61 : 25, new Threat(2, true, false, Threat.Zone.Red));
            check(tl, idx++, at ? 95 : 40, new Threat(3, true, true, Threat.Zone.White));
            check(tl, idx++, at ? 140 : 55, new DataTransfer());
            check(tl, idx++, at ? 179 : 70, new Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE));
            check(tl, idx++, at ? 192 : 75, new IncomingData());
            check(tl, idx++, at ? 219 : 85, new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS));
            check(tl, idx++, at ? 234 : 90, new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS));
            check(tl, idx++, at ? 282 : 100, new Threat(5, true, false));
            check(tl, idx++, at ? 308 : 115, new WhiteNoise(at ? 9 : 4));
            check(tl, idx++, at ? 317 : 119, new WhiteNoiseRestored());
            check(tl, idx++, at ? 323 : 124, new Threat(6, true, true));
            check(tl, idx++, at ? 343 : 139, new IncomingData());
            check(tl, idx++, at ? 356 : 149, new Threat(7, true, false, Threat.Zone.Blue));
            check(tl, idx++, at ? 399 : 164, new Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE));
            check(tl, idx++, at ? 410 : 169, new DataTransfer());
            check(tl, idx++, at ? 439 : 184, new Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS));
            check(tl, idx++, at ? 454 : 189, new Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS));
            check(tl, idx++, at ? 499 : 199, new WhiteNoise(at ? 18 : 4));
            check(tl, idx++, at ? 517 : 203, new WhiteNoiseRestored());
            check(tl, idx++, at ? 526 : 208, new WhiteNoise(at ? 12 : 4));
            check(tl, idx++, at ? 538 : 212, new WhiteNoiseRestored());
            check(tl, idx++, at ? 554 : 217, new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE));
            check(tl, idx++, at ? 563 : 222, new WhiteNoise(at ? 7 : 4));
            check(tl, idx++, at ? 570 : 226, new WhiteNoiseRestored());
            check(tl, idx++, at ? 594 : 231, new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS));
            check(tl, idx++, at ? 609 : 236, new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS));
        } else {
            assertEquals(29, tl.size());
            check(tl, idx++, at ? 0 : 0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
            check(tl, idx++, at ? 13 : 10, new Threat(1, true, false, Threat.Zone.Red));
            check(tl, idx++, at ? 38 : 25, new IncomingData());
            check(tl, idx++, at ? 65 : 35, new IncomingData());
            check(tl, idx++, at ? 95 : 45, new Threat(2, true, false, Threat.Zone.White));
            check(tl, idx++, at ? 129 : 60, new Threat(4, true, true, Threat.Zone.Blue));
            check(tl, idx++, at ? 178 : 75, new Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE));
            check(tl, idx++, at ? 188 : 80, new WhiteNoise(at ? 20 : 4));
            check(tl, idx++, at ? 208 : 84, new WhiteNoiseRestored());
            check(tl, idx++, at ? 218 : 89, new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS));
            check(tl, idx++, at ? 233 : 94, new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS));
            check(tl, idx++, at ? 257 : 104, new IncomingData());
            check(tl, idx++, at ? 276 : 114, new Threat(5, true, false, Threat.Zone.Red));
            check(tl, idx++, at ? 305 : 129, new Threat(6, true, false, Threat.Zone.Blue));
            check(tl, idx++, at ? 324 : 144, new IncomingData());
            check(tl, idx++, at ? 342 : 154, new Threat(7, true, false));
            check(tl, idx++, at ? 362 : 169, new DataTransfer());
            check(tl, idx++, at ? 389 : 184, new Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE));
            check(tl, idx++, at ? 429 : 189, new Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS));
            check(tl, idx++, at ? 444 : 194, new Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS));
            check(tl, idx++, at ? 504 : 204, new WhiteNoise(at ? 8 : 4));
            check(tl, idx++, at ? 512 : 208, new WhiteNoiseRestored());
            check(tl, idx++, at ? 523 : 213, new WhiteNoise(at ? 19 : 4));
            check(tl, idx++, at ? 542 : 217, new WhiteNoiseRestored());
            check(tl, idx++, at ? 550 : 222, new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE));
            check(tl, idx++, at ? 560 : 227, new IncomingData());
            check(tl, idx++, at ? 571 : 237, new IncomingData());
            check(tl, idx++, at ? 590 : 247, new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS));
            check(tl, idx++, at ? 605 : 252, new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS));
        }
        assertEquals(tl.size(), idx);  //  make sure we checked them all
    }

    private SharedPreferences mockPreferences(boolean showUnconfirmed,
                                              int players, boolean compressed,
                                              int randomSeed) {
        BogoSharedPreferences sp = new BogoSharedPreferences();
        sp.set("stompUnconfirmedReportsPreference", !showUnconfirmed);
        if (!showUnconfirmed) {
            sp.set("playerCount", players);
        }
        if (compressed) {
            sp.set("compressTimePreference", true);
        }
        if (randomSeed != 0) {
            sp.set("randomSeed", randomSeed);
        }
        return sp;
    }

    private void dumpEvents(EventList el) {
        List<Map.Entry<Integer, Event>> tl = el.getEntryList();
        for (int ii = 0; ii < tl.size(); ++ii) {
            System.err.println("  " + ii + ": " + tl.get(ii).getKey() + ": " + tl.get(ii).getValue());
        }
    }

    private void check(List<Map.Entry<Integer, Event>> tl, int idx,
                       int expectTime, Event expectEvent) {
        assertTrue("idx " + idx + ", length " + tl.size(), idx < tl.size());
        Map.Entry<Integer, Event> entry = tl.get(idx);
        assertEquals(expectTime, entry.getKey().intValue());
        Event ev = entry.getValue();

        //System.err.println("expect: " + expectEvent + "\n   got: " + ev);

        //  arguably would've been better to put the logic below in each Event
        //  implementation's equals(), and just assertEquals(expectEvent, ev)
        //  here.
        assertEquals(expectEvent.getClass(), ev.getClass());
        assertEquals(expectEvent.getLengthInSeconds(), ev.getLengthInSeconds());
        assertEquals(expectEvent.getTextColor(), ev.getTextColor());
        assertEquals(expectEvent.getTimeColor(), ev.getTimeColor());
        if (ev instanceof Announcement) {
            Announcement te = (Announcement) expectEvent;
            Announcement tg = (Announcement) ev;
            assertEquals(te.getType(), tg.getType());
        } else if (ev instanceof Threat) {
            Threat te = (Threat) expectEvent;
            Threat tg = (Threat) ev;
            assertEquals(te.getThreatLevel(), tg.getThreatLevel());
            assertEquals(te.getThreatPosition(), tg.getThreatPosition());
            if (tg.getThreatPosition() == Threat.THREAT_POSITION_EXTERNAL) {
                assertEquals(te.getSector(), tg.getSector());
            }
            assertEquals(te.getTime(), tg.getTime());
            assertEquals(te.isConfirmed(), tg.isConfirmed());
        }
    }

    private void check(MissionPreferences got,
                       int expectPlayers, int expectThreatLevel,
                       int expectThreatUnconfirmed,
                       boolean expectShowUnconfirmedReports,
                       int expectMinIncomingData, int expectMaxIncomingData,
                       int expectMinPhaseTime0, int expectMinPhaseTime1, int expectMinPhaseTime2,
                       int expectMaxPhaseTime0, int expectMaxPhaseTime1, int expectMaxPhaseTime2) {
        assertEquals(expectPlayers, got.getPlayers());
        assertEquals(expectThreatLevel, got.getThreatLevel());
        assertEquals(expectThreatUnconfirmed, got.getThreatUnconfirmed());
        assertEquals(expectShowUnconfirmedReports, got.getShowUnconfirmed());
        assertEquals(expectMinIncomingData, got.getMinIncomingData());
        assertEquals(expectMaxIncomingData, got.getMaxIncomingData());
        assertEquals(expectMinPhaseTime0, got.getMinPhaseTime()[0]);
        assertEquals(expectMinPhaseTime1, got.getMinPhaseTime()[1]);
        assertEquals(expectMinPhaseTime2, got.getMinPhaseTime()[2]);
        assertEquals(expectMaxPhaseTime0, got.getMaxPhaseTime()[0]);
        assertEquals(expectMaxPhaseTime1, got.getMaxPhaseTime()[1]);
        assertEquals(expectMaxPhaseTime2, got.getMaxPhaseTime()[2]);
    }
}
