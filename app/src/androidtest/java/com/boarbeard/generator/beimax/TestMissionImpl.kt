package com.boarbeard.generator.beimax

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boarbeard.generator.beimax.event.IncomingData
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestMissionImpl {
    /**
     * Assert incoming data stays between the preferences.
     */
    @Test
    fun testGenerateIncomingDataBetweenValues() {
        val missionPreferences = MissionPreferences()
        missionPreferences.maxIncomingData = 4
        missionPreferences.minIncomingData = 2

        for (i in 0..9) {
            val missionImpl = MissionImpl(missionPreferences)
            Assert.assertTrue(missionImpl.generateMission())

            var incomingDataCount = 0
            for ((_, value) in missionImpl.missionEvents.entrySet) {
                if (value is IncomingData) {
                    incomingDataCount++
                }
            }
            Assert.assertTrue(incomingDataCount in 2..4)
        }
    }
}