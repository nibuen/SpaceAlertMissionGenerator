package com.boarbeard.generator.beimax

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boarbeard.generator.beimax.event.IncomingData
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestMissionImpl {
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

            val foundIncomingDataList = missionImpl.missionEvents.entrySet.filter { it.value is IncomingData }
            val incomingDataCount = foundIncomingDataList.size
            Assert.assertTrue("found $incomingDataCount instead \n $foundIncomingDataList on test $i", incomingDataCount in 2..4)
        }
    }
}