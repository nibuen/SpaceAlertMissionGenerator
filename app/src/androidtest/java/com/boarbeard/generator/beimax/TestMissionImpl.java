package com.boarbeard.generator.beimax;


import android.test.AndroidTestCase;

public class TestMissionImpl extends AndroidTestCase {

    public void testGenerateDataOperations() {
        MissionImpl.MissionPreferences missionPreferences = new MissionImpl.MissionPreferences();
        missionPreferences.setMaxIncomingData(6);
        missionPreferences.setMinIncomingData(1);

        MissionImpl missionImpl = new MissionImpl(missionPreferences);

        assertTrue(missionImpl.generateDataOperations());
    }


}
