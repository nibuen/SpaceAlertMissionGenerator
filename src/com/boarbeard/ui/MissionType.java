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

			MissionImpl mission = new MissionImpl(preferences);
			mission.generateMission();
			return mission.getMissionEvents();
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
			ConstructedMissions.doubleActionEasierMission1()),
	DoubleActionEasyMission2(R.string.double_action_easy_mission_2,
			ConstructedMissions.doubleActionEasierMission2()), 
	DoubleActionMission1(R.string.double_action_mission_1,
			ConstructedMissions.doubleActionMission1()),
	DoubleActionMission2(R.string.double_action_mission_2,
			ConstructedMissions.doubleActionMission2()),
	DoubleActionMission3(R.string.double_action_mission_3,
			ConstructedMissions.doubleActionMission4()),
	DoubleActionMission4(R.string.double_action_mission_4, ConstructedMissions.doubleActionMission4());


	private int resId;
	private EventList eventList;

	MissionType(int resId, EventList eventList) {
		this.eventList = eventList;
		this.resId = resId;
	}

	public EventList getEventList(SharedPreferences preferences) {
		return eventList;
	}

	public int getResId() {
		return resId;
	}

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
