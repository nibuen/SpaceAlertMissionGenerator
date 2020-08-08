package com.boarbeard.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.boarbeard.R;
import com.boarbeard.generator.beimax.ConstructedMissions;
import com.boarbeard.generator.beimax.EventList;
import com.boarbeard.generator.beimax.MissionImpl;

public enum MissionType {
	/**
	 * Constructs a new random mission each time
	 * {@link #getEventList(SharedPreferences)} is called
	 * 
	 * @author Chris
	 * 
	 */
	Random(R.string.mission_random, null) {

		@Override
		public EventList getEventList(SharedPreferences preferences) {

			MissionImpl mission = new MissionImpl(MissionImpl.parsePreferences(preferences));
			mission.generateMission();
			EventList rv = mission.getMissionEvents();
			if (preferences.getBoolean("compressTimePreference", false)) {
				rv.compressTime();
			}
			return rv;
		}
	},
	FirstTestRun(R.string.mission_first_test_run, ConstructedMissions
			.firstTestRun()), //
	SecondTestRun(R.string.mission_second_test_run, ConstructedMissions
			.secondTestRun()), //
	Simulation1(R.string.mission_simulation_1, ConstructedMissions
			.simulation1()), //
	Simulation2(R.string.mission_simulation_2, ConstructedMissions
			.simulation2()), //
	Simulation3(R.string.mission_simulation_3, ConstructedMissions
			.simulation3()), //
	AdvancedSimulation1(R.string.mission_advanced_simulation_1,
			ConstructedMissions.advancedsimulation1()), //
	AdvancedSimulation2(R.string.mission_advanced_simulation_2,
			ConstructedMissions.advancedsimulation2()), //
	AdvancedSimulation3(R.string.mission_advanced_simulation_3,
			ConstructedMissions.advancedsimulation3()), //
	RealMission1(R.string.mission_real_mission_1, ConstructedMissions
			.realmission1()), //
	RealMission2(R.string.mission_real_mission_2, ConstructedMissions
			.realmission2()), //
	RealMission3(R.string.mission_real_mission_3, ConstructedMissions
			.realmission3()), //
	RealMission4(R.string.mission_real_mission_4, ConstructedMissions
			.realmission4()), //
	RealMission5(R.string.mission_real_mission_5, ConstructedMissions
			.realmission5()), //
	RealMission6(R.string.mission_real_mission_6, ConstructedMissions
			.realmission6()), //
	RealMission7(R.string.mission_real_mission_7, ConstructedMissions
			.realmission7()), //
	RealMission8(R.string.mission_real_mission_8, ConstructedMissions
			.realmission8()),
	DoubleActionEasyMission1(R.string.double_action_easy_mission_1,
			ConstructedMissions.doubleActionEasierMission1(), R.string.double_action_mission_introduction),
	DoubleActionEasyMission2(R.string.double_action_easy_mission_2,
			ConstructedMissions.doubleActionEasierMission2(), R.string.double_action_mission_introduction),
    DoubleActionEasyMission3(R.string.double_action_easy_mission_3,
            ConstructedMissions.doubleActionEasierMission3(), R.string.double_action_mission_introduction),
    DoubleActionEasyMission4(R.string.double_action_easy_mission_4,
            ConstructedMissions.doubleActionEasierMission4(), R.string.double_action_mission_introduction),
    DoubleActionEasyMission5(R.string.double_action_easy_mission_5,
            ConstructedMissions.doubleActionEasierMission5(), R.string.double_action_mission_introduction),
    DoubleActionEasyMission6(R.string.double_action_easy_mission_6,
            ConstructedMissions.doubleActionEasierMission6(), R.string.double_action_mission_introduction),
    DoubleActionMission1(R.string.double_action_mission_1,
			ConstructedMissions.doubleActionMission1(),R.string.double_action_mission_introduction),
	DoubleActionMission2(R.string.double_action_mission_2,
			ConstructedMissions.doubleActionMission2(), R.string.double_action_mission_introduction),
	DoubleActionMission3(R.string.double_action_mission_3,
		    ConstructedMissions.doubleActionMission3(), R.string.double_action_mission_introduction),
	DoubleActionMission4(R.string.double_action_mission_4,
            ConstructedMissions.doubleActionMission4(), R.string.double_action_mission_introduction),
    DoubleActionMission5(R.string.double_action_mission_5,
            ConstructedMissions.doubleActionMission5(), R.string.double_action_mission_introduction),
    DoubleActionMission6(R.string.double_action_mission_6,
            ConstructedMissions.doubleActionMission6(), R.string.double_action_mission_introduction),
    DoubleActionMission7(R.string.double_action_mission_7,
            ConstructedMissions.doubleActionMission7(), R.string.double_action_mission_introduction),
    DoubleActionMission8(R.string.double_action_mission_8, ConstructedMissions.doubleActionMission8(),
            R.string.double_action_mission_introduction),
    DoubleActionMission9(R.string.double_action_mission_9, ConstructedMissions.doubleActionMission9(),
            R.string.double_action_mission_introduction),
    DoubleActionMission10(R.string.double_action_mission_10, ConstructedMissions.doubleActionMission10(),
            R.string.double_action_mission_introduction),
    DoubleActionMission11(R.string.double_action_mission_11, ConstructedMissions.doubleActionMission11(),
            R.string.double_action_mission_introduction),
    DoubleActionMission12(R.string.double_action_mission_12, ConstructedMissions.doubleActionMission12(),
            R.string.double_action_mission_introduction);

	private int resId;
	private EventList eventList;
    private int missionIntroductionResId;

    MissionType(int resId, EventList eventList) {
        this.eventList = eventList;
        this.resId = resId;
        this.missionIntroductionResId = 0;
    }

	MissionType(int resId, EventList eventList, int missionIntroductionResId) {
		this.eventList = eventList;
		this.resId = resId;
        this.missionIntroductionResId = missionIntroductionResId;
	}

	public EventList getEventList(SharedPreferences preferences) {
        EventList copyOfList = null;
        MissionImpl.MissionPreferences mp = MissionImpl.parsePreferences(preferences);
        if (!mp.showUnconfirmedReports()) {
            copyOfList = new EventList(eventList);
            copyOfList.stompUnconfirmedReports(mp.getPlayers() == 5);
        }
        if (preferences.getBoolean("compressTimePreference", false)) {
            if (copyOfList == null) copyOfList = new EventList(eventList);
            copyOfList.compressTime();
        }
        return (copyOfList != null) ? copyOfList : eventList;
	}

	public int getResId() {
		return resId;
	}
    public int getMissionIntroductionResId() { return missionIntroductionResId; }
	public String toString(Context context) {
		return context.getString(getResId());
	}

	public static CharSequence[] toStringValues(Context context) {
		MissionType[] values = values();
		String[] names = new String[values.length];

		for (int i = 0; i < values.length; ++i) {
			names[i] = context.getString(values[i].resId);
		}

		return names;
	}
}
