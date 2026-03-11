package com.boarbeard.ui

import com.boarbeard.generator.beimax.BogoSharedPreferences
import com.boarbeard.generator.beimax.MissionImpl
import com.boarbeard.generator.beimax.MissionPreferences
import com.boarbeard.generator.beimax.event.IncomingData
import com.boarbeard.generator.beimax.event.Threat
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Verifies that MissionGroup assignments are correct and that the
 * NewMissionActivity's conditional option display (show random-only prefs
 * only when group.isRandom) matches the actual preference consumption in
 * the mission generation pipeline.
 *
 * The key invariant being tested: the UI gates random-only options on
 * [MissionGroup.isRandom]. These tests verify that:
 * 1. The group assignments themselves are correct
 * 2. Random-only preferences (threat difficulty, incoming data range, mission
 *    length, double threats) actually affect Random mission output
 * 3. Those same preferences do NOT affect constructed mission output
 * 4. Common preferences (stomp unconfirmed, compress time, player count)
 *    DO affect constructed missions
 */
class TestMissionType {

    /** Retry generation up to [maxAttempts] times to avoid flakes from tight constraints. */
    private fun generateUntilSuccess(
        prefs: MissionPreferences,
        label: String,
        maxAttempts: Int = 5,
    ): MissionImpl {
        repeat(maxAttempts) {
            val mission = MissionImpl(prefs)
            if (mission.generateMission()) return mission
        }
        throw AssertionError("$label failed to generate after $maxAttempts attempts")
    }

    // -- Group structure tests --

    @Test
    fun everyGroupHasAtLeastOneMissionType() {
        MissionGroup.entries.forEach { group ->
            val types = MissionType.entries.filter { it.group == group }
            types.shouldNotBeEmpty()
        }
    }

    @Test
    fun randomGroupOnlyContainsRandom() {
        val randomTypes = MissionType.entries.filter { it.group == MissionGroup.RANDOM }
        randomTypes.size shouldBe 1
        randomTypes.first() shouldBe MissionType.Random
    }

    @Test
    fun onlyRandomGroupReportsIsRandom() {
        MissionGroup.RANDOM.isRandom shouldBe true
        MissionGroup.entries.filter { it != MissionGroup.RANDOM }.forEach { group ->
            group.isRandom shouldBe false
        }
    }

    @Test
    fun doubleActionTypesAllHaveIntroduction() {
        // Double Action missions require the expansion, so they should all
        // have a mission introduction resource telling the player.
        MissionType.entries
            .filter { it.group == MissionGroup.DOUBLE_ACTION_EASY || it.group == MissionGroup.DOUBLE_ACTION_STANDARD }
            .forEach { type ->
                type.missionIntroductionResId shouldNotBe 0
            }
    }

    @Test
    fun nonDoubleActionTypesHaveNoIntroduction() {
        MissionType.entries
            .filter { it.group != MissionGroup.DOUBLE_ACTION_EASY && it.group != MissionGroup.DOUBLE_ACTION_STANDARD }
            .forEach { type ->
                type.missionIntroductionResId shouldBe 0
            }
    }

    // -- Preference flow: random-only prefs affect Random mission generation --
    // These test through MissionImpl directly (same path as MissionType.Random.buildEvents)
    // to avoid needing the full Android SharedPreferences stack.

    @Test
    fun randomMissionUsesIncomingDataRange() {
        // These ranges mirror what the UI allows (min 1, max 6).
        // Narrow range: 2-3 incoming data events
        val narrow = MissionPreferences().apply { incomingDataRange = 2..3 }
        // Wide range: 4-6 incoming data events
        val wide = MissionPreferences().apply { incomingDataRange = 4..6 }

        repeat(15) { i ->
            val mission = generateUntilSuccess(narrow, "narrow mission $i")
            val count = mission.missionEvents.entrySet.count { it.value is IncomingData }
            assert(count in 2..3) { "Expected 2-3 incoming data, got $count on iteration $i" }
        }

        repeat(15) { i ->
            val mission = generateUntilSuccess(wide, "wide mission $i")
            val count = mission.missionEvents.entrySet.count { it.value is IncomingData }
            assert(count in 4..6) { "Expected 4-6 incoming data, got $count on iteration $i" }
        }
    }

    @Test
    fun randomMissionUsesThreatDifficulty() {
        // Low difficulty: total threat should be small
        val low = MissionPreferences().apply {
            threatLevel = 2
            threatUnconfirmed = 0
            showUnconfirmed = false
        }
        // High difficulty: total threat should be large
        val high = MissionPreferences().apply {
            threatLevel = 14
            threatUnconfirmed = 0
            showUnconfirmed = false
        }

        repeat(10) { i ->
            val mission = generateUntilSuccess(low, "low difficulty mission $i")
            val total = mission.missionEvents.events.values.sumOf { (it as? Threat)?.threatLevel ?: 0 }
            assert(total <= 4) { "Low difficulty total threat $total > 4 on iteration $i" }
        }

        repeat(10) { i ->
            val mission = generateUntilSuccess(high, "high difficulty mission $i")
            val total = mission.missionEvents.events.values.sumOf { (it as? Threat)?.threatLevel ?: 0 }
            assert(total >= 10) { "High difficulty total threat $total < 10 on iteration $i" }
        }
    }

    @Test
    fun randomMissionUsesEnableDoubleThreats() {
        // Without double threats enabled, no time slot should have both
        // an internal and external threat simultaneously.
        val noDouble = MissionPreferences().apply { enableDoubleThreats = false }

        repeat(20) { i ->
            val mission = generateUntilSuccess(noDouble, "mission $i")
            val threats = mission.missionEvents.entrySet.filter { it.value is Threat }
            val byTime = threats.groupBy { it.key }
            byTime.values.forEach { threatsAtTime ->
                assert(threatsAtTime.size <= 1) {
                    "Without double threats, no time slot should have >1 threat (iteration $i)"
                }
            }
        }
    }

    // -- Preference flow: random-only prefs DON'T affect constructed missions --
    // These prove that the UI is correct to hide these options for non-Random types.

    @Test
    fun constructedMissionIgnoresThreatDifficulty(): Unit = runBlocking {
        val prefsLow = BogoSharedPreferences()
        prefsLow["threatDifficultyPreference"] = 1

        val prefsHigh = BogoSharedPreferences()
        prefsHigh["threatDifficultyPreference"] = 14

        // RealMission1 is a constructed mission — its threats are fixed
        val type = MissionType.RealMission1
        type.group.isRandom shouldBe false

        val eventsLow = type.buildEvents(prefsLow)
        val eventsHigh = type.buildEvents(prefsHigh)

        // Threats should be identical regardless of difficulty setting
        val threatsLow = eventsLow.entrySet.filter { it.value is Threat }
        val threatsHigh = eventsHigh.entrySet.filter { it.value is Threat }

        threatsLow.size shouldBe threatsHigh.size
        threatsLow.zip(threatsHigh).forEach { (a, b) ->
            val ta = a.value as Threat
            val tb = b.value as Threat
            ta.threatLevel shouldBe tb.threatLevel
            ta.isConfirmed shouldBe tb.isConfirmed
        }
    }

    @Test
    fun constructedMissionIgnoresIncomingDataRange(): Unit = runBlocking {
        val prefsNarrow = BogoSharedPreferences()
        prefsNarrow["numberIncomingData"] = 1
        prefsNarrow["numberIncomingDataRightValue"] = 1

        val prefsWide = BogoSharedPreferences()
        prefsWide["numberIncomingData"] = 6
        prefsWide["numberIncomingDataRightValue"] = 6

        val type = MissionType.RealMission1
        type.group.isRandom shouldBe false

        val eventsNarrow = type.buildEvents(prefsNarrow)
        val eventsWide = type.buildEvents(prefsWide)

        // Incoming data count should be identical — constructed missions have fixed events
        val countNarrow = eventsNarrow.entrySet.count { it.value is IncomingData }
        val countWide = eventsWide.entrySet.count { it.value is IncomingData }
        countNarrow shouldBe countWide
    }

    @Test
    fun constructedMissionIgnoresMissionLength(): Unit = runBlocking {
        val prefsShort = BogoSharedPreferences()
        prefsShort["missionLengthPreference"] = 540

        val prefsLong = BogoSharedPreferences()
        prefsLong["missionLengthPreference"] = 840

        val type = MissionType.Simulation1
        type.group.isRandom shouldBe false

        val eventsShort = type.buildEvents(prefsShort)
        val eventsLong = type.buildEvents(prefsLong)

        // Same number of events regardless of mission length setting
        eventsShort.entrySet.size shouldBe eventsLong.entrySet.size
    }

    // -- Preference flow: common prefs DO affect constructed missions --
    // These prove that the UI is correct to always show these options.

    @Test
    fun constructedMissionUsesStompUnconfirmed(): Unit = runBlocking {
        // Show unconfirmed reports as-is
        val prefsShow = BogoSharedPreferences()
        prefsShow["stompUnconfirmedReportsPreference"] = false

        // Stomp for 4 players (removes unconfirmed entirely)
        val prefsStomp4 = BogoSharedPreferences()
        prefsStomp4["stompUnconfirmedReportsPreference"] = true
        prefsStomp4["playerCount"] = 4

        val type = MissionType.RealMission1
        type.group.isRandom shouldBe false

        val eventsShow = type.buildEvents(prefsShow)
        val eventsStomp = type.buildEvents(prefsStomp4)

        val unconfirmedShow = eventsShow.entrySet.count {
            val e = it.value; e is Threat && !e.isConfirmed
        }
        val unconfirmedStomp = eventsStomp.entrySet.count {
            val e = it.value; e is Threat && !e.isConfirmed
        }

        // RealMission1 has at least one unconfirmed report
        assert(unconfirmedShow > 0) { "RealMission1 should have unconfirmed reports" }
        // With stomp for 4 players, all unconfirmed should be removed
        unconfirmedStomp shouldBe 0
    }

    @Test
    fun constructedMissionUsesCompressTime(): Unit = runBlocking {
        val prefsNormal = BogoSharedPreferences()
        prefsNormal["compressTimePreference"] = false

        val prefsCompressed = BogoSharedPreferences()
        prefsCompressed["compressTimePreference"] = true

        val type = MissionType.RealMission1
        type.group.isRandom shouldBe false

        val eventsNormal = type.buildEvents(prefsNormal)
        val eventsCompressed = type.buildEvents(prefsCompressed)

        // Same event count, but compressed timeline is shorter
        eventsNormal.entrySet.size shouldBe eventsCompressed.entrySet.size

        val lastTimeNormal = eventsNormal.entrySet.last().key
        val lastTimeCompressed = eventsCompressed.entrySet.last().key
        assert(lastTimeCompressed < lastTimeNormal) {
            "Compressed last time ($lastTimeCompressed) should be less than normal ($lastTimeNormal)"
        }
    }

    // -- Verify parsePreferences maps the same pref keys the UI writes --
    // This catches drift between NewMissionActivity's pref keys and
    // MissionImpl.parsePreferences().

    @Test
    fun parsePreferencesReadsAllRandomOnlyKeys() {
        // These are the same SharedPreferences keys that NewMissionActivity
        // saves when the user picks a Random mission.
        val sp = BogoSharedPreferences()
        sp["threatDifficultyPreference"] = 10
        sp["missionLengthPreference"] = 720
        sp["numberIncomingData"] = 3
        sp["numberIncomingDataRightValue"] = 5
        sp["enable_double_threats"] = true
        sp["stompUnconfirmedReportsPreference"] = false

        val prefs = MissionImpl.parsePreferences(sp)
        prefs.threatLevel shouldBe 10
        prefs.getMinIncomingData() shouldBe 3
        prefs.getMaxIncomingData() shouldBe 5
        prefs.enableDoubleThreats shouldBe true
        // Mission length 720 -> phase times based on 40/35/25% split
        prefs.minPhaseTime[0] shouldBe (720 * 0.4).toInt()
        prefs.minPhaseTime[1] shouldBe (720 * 0.35).toInt()
        prefs.minPhaseTime[2] shouldBe (720 * 0.25).toInt()
    }

    @Test
    fun parsePreferencesReadsCommonKeys() {
        // These are the same SharedPreferences keys that NewMissionActivity
        // saves for all mission types.
        val sp = BogoSharedPreferences()
        sp["stompUnconfirmedReportsPreference"] = true
        sp["playerCount"] = 4
        sp["compressTimePreference"] = true

        val prefs = MissionImpl.parsePreferences(sp)
        prefs.players shouldBe 4
        prefs.showUnconfirmed shouldBe false
        prefs.compressTime shouldBe true
    }
}
