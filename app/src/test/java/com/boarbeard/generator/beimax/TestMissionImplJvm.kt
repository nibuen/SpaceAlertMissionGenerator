package com.boarbeard.generator.beimax

import android.content.SharedPreferences
import com.boarbeard.generator.beimax.event.*
import com.boarbeard.ui.MissionType
import org.junit.Assert
import org.junit.Test
import timber.log.Timber


class JvmDebugTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("${if(tag != null) "[$tag]" else ""}[${parsePriority(priority)}] --- $message")
    }

    /*
    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;
     */
    private fun parsePriority(priority: Int): String {
        return when(priority) {
            2 -> "V"
            3 -> "D"
            4 -> "W"
            5 -> "W"
            6 -> "E"
            7 -> "ASSERT"
            else -> ""
        }
    }
}

/**
 * TestMissionImplJvm allows to run without Android/emulators.
 */
class TestMissionImplJvm {

    @Test
    fun testParsePreferences() {
        var mp: MissionPreferences
        val sp = BogoSharedPreferences()

        sp["stompUnconfirmedReportsPreference"] = false
        mp = MissionImpl.parsePreferences(sp)
        check(mp, 5, 8, 1, true, 1, 6, 240, 210, 150, 255, 225, 165)

        sp["stompUnconfirmedReportsPreference"] = true
        sp["playerCount"] = 5
        mp = MissionImpl.parsePreferences(sp)
        check(mp, 5, 8, 0, false, 1, 6, 240, 210, 150, 255, 225, 165)

        sp["playerCount"] = 4
        mp = MissionImpl.parsePreferences(sp)
        check(mp, 4, 7, 0, false, 1, 6, 240, 210, 150, 255, 225, 165)
    }

    /**
     * Assert incoming data stays between the preferences for incoming data.
     */
    @Test
    fun testGenerateIncomingDataBetweenValues() {
        val missionPreferences = MissionPreferences()
        missionPreferences.incomingDataRange = 2..4
        Assert.assertEquals(2, missionPreferences.getMinIncomingData())
        Assert.assertEquals(4, missionPreferences.getMaxIncomingData())

        for (i in 0..25) {
            val missionImpl = MissionImpl(missionPreferences)
            Assert.assertTrue(missionImpl.generateMission())

            val foundIncomingDataList =
                missionImpl.missionEvents.entrySet.filter { it.value is IncomingData }
            val incomingDataCount = foundIncomingDataList.size
            Assert.assertTrue(
                "found $incomingDataCount instead \n $foundIncomingDataList on test $i",
                incomingDataCount in 2..4
            )
        }
    }

    /**
     * Assert incoming data stays between the preferences for incoming data.
     */
    @Test
    fun testStompUnconfirmedRemovesAllUnconfirmedReports() {
        Timber.plant(JvmDebugTree())
        val sp = BogoSharedPreferences()
        sp["stompUnconfirmedReportsPreference"] = true
        sp["playerCount"] = 4
        val missionPreferences = MissionImpl.parsePreferences(sp)
        Assert.assertEquals(4, missionPreferences.players)
        Assert.assertEquals(7, missionPreferences.threatLevel)
        Assert.assertEquals(0, missionPreferences.threatUnconfirmed)

        for (i in 0..99) {
            val missionImpl = MissionImpl(missionPreferences)
            Assert.assertTrue(missionImpl.generateMission())

            val foundUnconfirmedReports = missionImpl.missionEvents.entrySet.filter {
                val checkIfThreat = it.value
                checkIfThreat is Threat && !checkIfThreat.isConfirmed
            }
            val foundConfirmedReports = missionImpl.missionEvents.entrySet.filter {
                val checkIfThreat = it.value
                checkIfThreat is Threat && checkIfThreat.isConfirmed
            }
            val unconfirmedReportsCount = foundUnconfirmedReports.size
            Assert.assertTrue(
                "found $unconfirmedReportsCount instead \n $foundUnconfirmedReports on test $i",
                unconfirmedReportsCount == 0
            )

            // All Random missions should have at least some number of reports
            val confirmedReportsCount = foundConfirmedReports.size

            Assert.assertTrue(
                "found $confirmedReportsCount instead \n $foundConfirmedReports on test $i",
                confirmedReportsCount > 0
            )
        }
    }

    @Test
    fun testMission1UnconfirmedReportPrefs() {
        //  showing unconfirmed reports
        testMission1UnconfirmedReports(true, 5, false)
        //  Same thing again, but this time with unconfirmed reports treated as
        //  normal threats
        testMission1UnconfirmedReports(false, 5, false)
        //  Same thing again, but this time with no unconfirmed reports
        testMission1UnconfirmedReports(false, 4, false)

        //  Same things, compressed (I know, debugging a debug option, silly,
        //  but I really did want to know whether it worked!)
        testMission1UnconfirmedReports(true, 5, true)
        testMission1UnconfirmedReports(false, 5, true)
        testMission1UnconfirmedReports(false, 4, true)

        //  The first one again to confirm we didn't mess with the original list
        testMission1UnconfirmedReports(true, 5, false)
    }

    private fun testMission1UnconfirmedReports(
        showUnconfirmed: Boolean,
        players: Int, compressed: Boolean
    ) {
        val sp = mockPreferences(showUnconfirmed, players, compressed, 0)
        val eventList = MissionType.RealMission1.getEventList(sp)

        //  if you run into problems
        //dumpEvents(eventList);
        val tl = eventList.entryList
        Assert.assertEquals(
            if (showUnconfirmed || players == 5) 30 else 29.toLong(),
            tl.size.toLong()
        )
        //  just a shorter variable name for the conditionals below
        val at = !compressed
        //  ugh, and compressed times after the removed unconfirmed report are
        //  different, so... if times are compressed, adjust the time of every
        //  event *after* the unconfirmed report happened, or would have
        //  happened.  This is a long way to go for testing a goofy debug flag!
        var off = 0
        var idx = 0
        check(tl, idx++, 0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
        check(tl, idx++, 10, Threat(2, true, true, Threat.Zone.White))
        if (showUnconfirmed) {
            check(tl, idx++, if (at) 55 else 25, Threat(3, false, false))
        } else if (players == 5) {
            check(tl, idx++, if (at) 55 else 25, Threat(3, true, false))
        } else {
            //  unconfirmed report should've been removed
            if (compressed) off = -15
        }
        check(tl, idx++, if (at) 110 else off + 40, Threat(4, true, false, Threat.Zone.Blue))
        check(tl, idx++, if (at) 140 else off + 55, IncomingData())
        check(
            tl,
            idx++,
            if (at) 165 else off + 65,
            Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE)
        )
        check(tl, idx++, if (at) 170 else off + 70, WhiteNoise(if (at) 10 else 4))
        check(tl, idx++, if (at) 180 else off + 74, WhiteNoiseRestored())
        check(tl, idx++, if (at) 185 else off + 79, DataTransfer())
        check(
            tl,
            idx++,
            if (at) 205 else off + 94,
            Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
        )
        check(
            tl,
            idx++,
            if (at) 220 else off + 99,
            Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS)
        )
        check(tl, idx++, if (at) 230 else off + 109, IncomingData())
        check(tl, idx++, if (at) 240 else off + 119, Threat(5, true, false))
        check(tl, idx++, if (at) 265 else off + 134, DataTransfer())
        check(tl, idx++, if (at) 290 else off + 149, Threat(6, true, false, Threat.Zone.Blue))
        check(tl, idx++, if (at) 320 else off + 164, WhiteNoise(if (at) 15 else 4))
        check(tl, idx++, if (at) 335 else off + 168, WhiteNoiseRestored())
        check(tl, idx++, if (at) 350 else off + 173, Threat(7, true, true, Threat.Zone.Red))
        check(
            tl,
            idx++,
            if (at) 390 else off + 188,
            Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE)
        )
        check(tl, idx++, if (at) 395 else off + 193, DataTransfer())
        check(
            tl,
            idx++,
            if (at) 430 else off + 208,
            Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS)
        )
        check(
            tl,
            idx++,
            if (at) 445 else off + 213,
            Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS)
        )
        check(tl, idx++, if (at) 470 else off + 223, WhiteNoise(if (at) 20 else 4))
        check(tl, idx++, if (at) 490 else off + 227, WhiteNoiseRestored())
        check(tl, idx++, if (at) 500 else off + 232, DataTransfer())
        check(
            tl,
            idx++,
            if (at) 540 else off + 247,
            Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
        )
        check(tl, idx++, if (at) 555 else off + 252, WhiteNoise(if (at) 10 else 4))
        check(tl, idx++, if (at) 565 else off + 256, WhiteNoiseRestored())
        check(
            tl,
            idx++,
            if (at) 580 else off + 261,
            Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
        )
        check(
            tl,
            idx++,
            if (at) 595 else off + 266,
            Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS)
        )
        Assert.assertEquals(tl.size.toLong(), idx.toLong()) //  make sure we checked them all
    }

    /**
     * Removing random gold standard check as it's too particular in the exact order of random generator and leaves it easily breakable with refactors, improvements.
     * New tests above use generational checks to attempt to account for stability instead.
     */
    /*
    private fun testRandomUnconfirmedReports(
        showUnconfirmed: Boolean,
        players: Int,
        compressed: Boolean
    ) {
        val sp = mockPreferences(showUnconfirmed, players, compressed, 666)
        val eventList = MissionType.Random.getEventList(sp)

        //  if you run into problems
        dumpEvents(eventList)
        val tl = eventList.entryList
        //  just a shorter variable name for the conditionals below
        val at = !compressed
        var idx = 0
        //  well, the random lists of events wind up being completely different
        //  when you stomp unconfirmed reports, so making this one method
        //  instead of three was maybe a little silly.
        if (showUnconfirmed) {
            Assert.assertEquals(32, tl.size.toLong())
            check(tl, idx++, if (at) 0 else 0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
            check(tl, idx++, if (at) 14 else 10, Threat(2, true, true, Threat.Zone.Red))
            check(tl, idx++, if (at) 47 else 25, WhiteNoise(if (at) 12 else 4))
            check(tl, idx++, if (at) 59 else 29, WhiteNoiseRestored())
            check(tl, idx++, if (at) 64 else 34, Threat(2, true, false, Threat.Zone.Red))
            check(tl, idx++, if (at) 95 else 49, DataTransfer())
            check(tl, idx++, if (at) 115 else 64, Threat(4, true, false))
            check(tl, idx++, if (at) 140 else 79, IncomingData())
            check(tl, idx++, if (at) 164 else 89, IncomingData())
            check(
                tl,
                idx++,
                if (at) 183 else 99,
                Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE)
            )
            check(
                tl,
                idx++,
                if (at) 223 else 104,
                Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
            )
            check(tl, idx++, if (at) 238 else 109, Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS))
            check(tl, idx++, if (at) 275 else 119, Threat(5, true, false))
            check(tl, idx++, if (at) 299 else 134, Threat(6, true, true, Threat.Zone.Blue))
            check(tl, idx++, if (at) 335 else 149, WhiteNoise(if (at) 19 else 4))
            check(tl, idx++, if (at) 354 else 153, WhiteNoiseRestored())
            check(tl, idx++, if (at) 362 else 158, Threat(7, true, false, Threat.Zone.White))
            check(tl, idx++, if (at) 390 else 173, IncomingData())
            check(
                tl,
                idx++,
                if (at) 406 else 183,
                Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE)
            )
            check(tl, idx++, if (at) 415 else 188, Threat(8, false, false, Threat.Zone.Blue))
            check(
                tl,
                idx++,
                if (at) 446 else 203,
                Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS)
            )
            check(tl, idx++, if (at) 461 else 208, Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS))
            check(tl, idx++, if (at) 474 else 218, DataTransfer())
            check(tl, idx++, if (at) 534 else 233, WhiteNoise(if (at) 16 else 4))
            check(tl, idx++, if (at) 550 else 237, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 568 else 242,
                Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
            )
            check(tl, idx++, if (at) 575 else 247, WhiteNoise(if (at) 12 else 4))
            check(tl, idx++, if (at) 587 else 251, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 608 else 256,
                Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
            )
            check(tl, idx++, if (at) 623 else 261, Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS))
        } else if (players == 5) {
            Assert.assertEquals(28, tl.size.toLong())
            check(tl, idx++, if (at) 0 else 0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
            check(tl, idx++, if (at) 18 else 10, Threat(1, true, false, Threat.Zone.Blue))
            check(tl, idx++, if (at) 61 else 25, Threat(2, true, false, Threat.Zone.Red))
            check(tl, idx++, if (at) 95 else 40, Threat(3, true, true, Threat.Zone.White))
            check(tl, idx++, if (at) 140 else 55, DataTransfer())
            check(
                tl,
                idx++,
                if (at) 179 else 70,
                Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE)
            )
            check(tl, idx++, if (at) 192 else 75, IncomingData())
            check(
                tl,
                idx++,
                if (at) 219 else 85,
                Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
            )
            check(tl, idx++, if (at) 234 else 90, Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS))
            check(tl, idx++, if (at) 282 else 100, Threat(5, true, false))
            check(tl, idx++, if (at) 308 else 115, WhiteNoise(if (at) 9 else 4))
            check(tl, idx++, if (at) 317 else 119, WhiteNoiseRestored())
            check(tl, idx++, if (at) 323 else 124, Threat(6, true, true))
            check(tl, idx++, if (at) 343 else 139, IncomingData())
            check(tl, idx++, if (at) 356 else 149, Threat(7, true, false, Threat.Zone.Blue))
            check(
                tl,
                idx++,
                if (at) 399 else 164,
                Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE)
            )
            check(tl, idx++, if (at) 410 else 169, DataTransfer())
            check(
                tl,
                idx++,
                if (at) 439 else 184,
                Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS)
            )
            check(tl, idx++, if (at) 454 else 189, Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS))
            check(tl, idx++, if (at) 499 else 199, WhiteNoise(if (at) 18 else 4))
            check(tl, idx++, if (at) 517 else 203, WhiteNoiseRestored())
            check(tl, idx++, if (at) 526 else 208, WhiteNoise(if (at) 12 else 4))
            check(tl, idx++, if (at) 538 else 212, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 554 else 217,
                Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
            )
            check(tl, idx++, if (at) 563 else 222, WhiteNoise(if (at) 7 else 4))
            check(tl, idx++, if (at) 570 else 226, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 594 else 231,
                Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
            )
            check(tl, idx++, if (at) 609 else 236, Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS))
        } else {
            Assert.assertEquals(29, tl.size.toLong())
            check(tl, idx++, if (at) 0 else 0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
            check(tl, idx++, if (at) 13 else 10, Threat(1, true, false, Threat.Zone.Red))
            check(tl, idx++, if (at) 38 else 25, IncomingData())
            check(tl, idx++, if (at) 65 else 35, IncomingData())
            check(tl, idx++, if (at) 95 else 45, Threat(2, true, false, Threat.Zone.White))
            check(tl, idx++, if (at) 129 else 60, Threat(4, true, true, Threat.Zone.Blue))
            check(
                tl,
                idx++,
                if (at) 178 else 75,
                Announcement(Announcement.ANNOUNCEMENT_PH1_ONEMINUTE)
            )
            check(tl, idx++, if (at) 188 else 80, WhiteNoise(if (at) 20 else 4))
            check(tl, idx++, if (at) 208 else 84, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 218 else 89,
                Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
            )
            check(tl, idx++, if (at) 233 else 94, Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS))
            check(tl, idx++, if (at) 257 else 104, IncomingData())
            check(tl, idx++, if (at) 276 else 114, Threat(5, true, false, Threat.Zone.Red))
            check(tl, idx++, if (at) 305 else 129, Threat(6, true, false, Threat.Zone.Blue))
            check(tl, idx++, if (at) 324 else 144, IncomingData())
            check(tl, idx++, if (at) 342 else 154, Threat(7, true, false))
            check(tl, idx++, if (at) 362 else 169, DataTransfer())
            check(
                tl,
                idx++,
                if (at) 389 else 184,
                Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE)
            )
            check(
                tl,
                idx++,
                if (at) 429 else 189,
                Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS)
            )
            check(tl, idx++, if (at) 444 else 194, Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS))
            check(tl, idx++, if (at) 504 else 204, WhiteNoise(if (at) 8 else 4))
            check(tl, idx++, if (at) 512 else 208, WhiteNoiseRestored())
            check(tl, idx++, if (at) 523 else 213, WhiteNoise(if (at) 19 else 4))
            check(tl, idx++, if (at) 542 else 217, WhiteNoiseRestored())
            check(
                tl,
                idx++,
                if (at) 550 else 222,
                Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
            )
            check(tl, idx++, if (at) 560 else 227, IncomingData())
            check(tl, idx++, if (at) 571 else 237, IncomingData())
            check(
                tl,
                idx++,
                if (at) 590 else 247,
                Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
            )
            check(tl, idx++, if (at) 605 else 252, Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS))
        }
        Assert.assertEquals(tl.size.toLong(), idx.toLong()) //  make sure we checked them all
    }
     */

    private fun mockPreferences(
        showUnconfirmed: Boolean,
        players: Int, compressed: Boolean,
        randomSeed: Int
    ): SharedPreferences {
        val sp = BogoSharedPreferences()
        sp["stompUnconfirmedReportsPreference"] = !showUnconfirmed
        if (!showUnconfirmed) {
            sp["playerCount"] = players
        }
        if (compressed) {
            sp["compressTimePreference"] = true
        }
        if (randomSeed != 0) {
            sp["randomSeed"] = randomSeed
        }
        return sp
    }

    private fun dumpEvents(el: EventList) {
        val tl = el.entryList
        for (ii in tl.indices) {
            System.err.println("  " + ii + ": " + tl[ii].key + ": " + tl[ii].value)
        }
    }

    private fun check(
        tl: List<Map.Entry<Int, Event>>, idx: Int,
        expectTime: Int, expectEvent: Event
    ) {
        Assert.assertTrue("idx " + idx + ", length " + tl.size, idx < tl.size)
        val (key, ev) = tl[idx]
        //Assert.assertEquals(expectTime.toLong(), key.toLong())

        //System.err.println("expect: " + expectEvent + "\n   got: " + ev);

        //  arguably would've been better to put the logic below in each Event
        //  implementation's equals(), and just assertEquals(expectEvent, ev)
        //  here.
        Assert.assertEquals(expectEvent.javaClass, ev.javaClass)
        Assert.assertEquals(expectEvent.lengthInSeconds.toLong(), ev.lengthInSeconds.toLong())
        //Assert.assertEquals(expectEvent.textColor, ev.textColor)
        //Assert.assertEquals(expectEvent.timeColor, ev.timeColor)
        if (ev is Announcement) {
            val te = expectEvent as Announcement
            Assert.assertEquals(te.type.toLong(), ev.type.toLong())
        } else if (ev is Threat) {
            val te = expectEvent as Threat
            Assert.assertEquals(te.threatLevel.toLong(), ev.threatLevel.toLong())
            Assert.assertEquals(te.threatPosition.toLong(), ev.threatPosition.toLong())
            if (ev.threatPosition == Threat.THREAT_POSITION_EXTERNAL) {
                Assert.assertEquals(te.sector.toLong(), ev.sector.toLong())
            }
            Assert.assertEquals(te.time.toLong(), ev.time.toLong())
            Assert.assertEquals(te.isConfirmed, ev.isConfirmed)
        }
    }

    private fun check(
        got: MissionPreferences,
        expectPlayers: Int, expectThreatLevel: Int,
        expectThreatUnconfirmed: Int,
        expectShowUnconfirmedReports: Boolean,
        expectMinIncomingData: Int, expectMaxIncomingData: Int,
        expectMinPhaseTime0: Int, expectMinPhaseTime1: Int, expectMinPhaseTime2: Int,
        expectMaxPhaseTime0: Int, expectMaxPhaseTime1: Int, expectMaxPhaseTime2: Int
    ) {
        Assert.assertEquals(expectPlayers.toLong(), got.players.toLong())
        Assert.assertEquals(expectThreatLevel.toLong(), got.threatLevel.toLong())
        Assert.assertEquals(expectThreatUnconfirmed.toLong(), got.threatUnconfirmed.toLong())
        Assert.assertEquals(expectShowUnconfirmedReports, got.showUnconfirmed)
        Assert.assertEquals(expectMinIncomingData.toLong(), got.getMinIncomingData().toLong())
        Assert.assertEquals(expectMaxIncomingData.toLong(), got.getMaxIncomingData().toLong())
        Assert.assertEquals(expectMinPhaseTime0.toLong(), got.minPhaseTime[0].toLong())
        Assert.assertEquals(expectMinPhaseTime1.toLong(), got.minPhaseTime[1].toLong())
        Assert.assertEquals(expectMinPhaseTime2.toLong(), got.minPhaseTime[2].toLong())
        Assert.assertEquals(expectMaxPhaseTime0.toLong(), got.maxPhaseTime[0].toLong())
        Assert.assertEquals(expectMaxPhaseTime1.toLong(), got.maxPhaseTime[1].toLong())
        Assert.assertEquals(expectMaxPhaseTime2.toLong(), got.maxPhaseTime[2].toLong())
    }
}