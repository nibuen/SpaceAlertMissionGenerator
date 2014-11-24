package com.boarbeard.generator.beimax;


import android.test.AndroidTestCase;

import com.boarbeard.generator.beimax.event.Event;
import com.boarbeard.generator.beimax.event.IncomingData;

import java.util.Map;

public class TestMissionImpl extends AndroidTestCase {

    /**
     * Assert incoming data stays between the preferences.
     */
    public void testGenerateIncomingDataBetweenValues() {
        MissionImpl.MissionPreferences missionPreferences = new MissionImpl.MissionPreferences();
        missionPreferences.setMaxIncomingData(4);
        missionPreferences.setMinIncomingData(2);

        for (int i = 0; i < 10; i++) {
            MissionImpl missionImpl = new MissionImpl(missionPreferences);
            assertTrue(missionImpl.generateMission());

            int incomingDataCount = 0;
            for (Map.Entry<Integer, Event> entry : missionImpl.getMissionEvents().getEntrySet()) {
                if (entry.getValue() instanceof IncomingData) {
                    incomingDataCount++;
                }
            }

            assertTrue(incomingDataCount >= 2 && incomingDataCount <= 4);
        }
    }
}
