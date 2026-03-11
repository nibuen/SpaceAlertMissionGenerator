package com.boarbeard.ui

import android.content.Context
import android.content.SharedPreferences
import com.boarbeard.R
import com.boarbeard.generator.beimax.ConstructedMissions
import com.boarbeard.generator.beimax.EventList
import com.boarbeard.generator.beimax.MissionImpl

enum class MissionGroup(val displayNameResId: Int) {
    RANDOM(R.string.group_random),
    TEST_RUNS(R.string.group_test_runs),
    SIMULATIONS(R.string.group_simulations),
    ADVANCED_SIMULATIONS(R.string.group_advanced_simulations),
    REAL_MISSIONS(R.string.group_real_missions),
    DOUBLE_ACTION_EASY(R.string.group_double_action_easy),
    DOUBLE_ACTION_STANDARD(R.string.group_double_action_standard);

    val isRandom: Boolean get() = this == RANDOM
}

enum class MissionType {
    /**
     * Constructs a new random mission each time
     * [.buildEvents] is called
     *
     * @author Chris
     */
    Random(R.string.mission_random, null, MissionGroup.RANDOM) {
        override suspend fun buildEvents(preferences: SharedPreferences): EventList {
            val mission = MissionImpl(MissionImpl.parsePreferences(preferences))

            mission.generateMission()

            val eventList = mission.missionEvents
            if (preferences.getBoolean("compressTimePreference", false)) {
                eventList.compressTime()
            }
            return eventList
        }
    },
    FirstTestRun(
        R.string.mission_first_test_run, ConstructedMissions
            .firstTestRun(), MissionGroup.TEST_RUNS
    ),
    SecondTestRun(
        R.string.mission_second_test_run, ConstructedMissions
            .secondTestRun(), MissionGroup.TEST_RUNS
    ),
    Simulation1(
        R.string.mission_simulation_1, ConstructedMissions
            .simulation1(), MissionGroup.SIMULATIONS
    ),
    Simulation2(
        R.string.mission_simulation_2, ConstructedMissions
            .simulation2(), MissionGroup.SIMULATIONS
    ),
    Simulation3(
        R.string.mission_simulation_3, ConstructedMissions
            .simulation3(), MissionGroup.SIMULATIONS
    ),
    AdvancedSimulation1(
        R.string.mission_advanced_simulation_1,
        ConstructedMissions.advancedsimulation1(), MissionGroup.ADVANCED_SIMULATIONS
    ),
    AdvancedSimulation2(
        R.string.mission_advanced_simulation_2,
        ConstructedMissions.advancedsimulation2(), MissionGroup.ADVANCED_SIMULATIONS
    ),
    AdvancedSimulation3(
        R.string.mission_advanced_simulation_3,
        ConstructedMissions.advancedsimulation3(), MissionGroup.ADVANCED_SIMULATIONS
    ),
    RealMission1(
        R.string.mission_real_mission_1, ConstructedMissions
            .realmission1(), MissionGroup.REAL_MISSIONS
    ),
    RealMission2(
        R.string.mission_real_mission_2, ConstructedMissions
            .realmission2(), MissionGroup.REAL_MISSIONS
    ),
    RealMission3(
        R.string.mission_real_mission_3, ConstructedMissions
            .realmission3(), MissionGroup.REAL_MISSIONS
    ),
    RealMission4(
        R.string.mission_real_mission_4, ConstructedMissions
            .realmission4(), MissionGroup.REAL_MISSIONS
    ),
    RealMission5(
        R.string.mission_real_mission_5, ConstructedMissions
            .realmission5(), MissionGroup.REAL_MISSIONS
    ),
    RealMission6(
        R.string.mission_real_mission_6, ConstructedMissions
            .realmission6(), MissionGroup.REAL_MISSIONS
    ),
    RealMission7(
        R.string.mission_real_mission_7, ConstructedMissions
            .realmission7(), MissionGroup.REAL_MISSIONS
    ),
    RealMission8(
        R.string.mission_real_mission_8, ConstructedMissions
            .realmission8(), MissionGroup.REAL_MISSIONS
    ),
    DoubleActionEasyMission1(
        R.string.double_action_easy_mission_1,
        ConstructedMissions.doubleActionEasierMission1(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission2(
        R.string.double_action_easy_mission_2,
        ConstructedMissions.doubleActionEasierMission2(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission3(
        R.string.double_action_easy_mission_3,
        ConstructedMissions.doubleActionEasierMission3(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission4(
        R.string.double_action_easy_mission_4,
        ConstructedMissions.doubleActionEasierMission4(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission5(
        R.string.double_action_easy_mission_5,
        ConstructedMissions.doubleActionEasierMission5(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission6(
        R.string.double_action_easy_mission_6,
        ConstructedMissions.doubleActionEasierMission6(),
        MissionGroup.DOUBLE_ACTION_EASY,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission1(
        R.string.double_action_mission_1,
        ConstructedMissions.doubleActionMission1(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission2(
        R.string.double_action_mission_2,
        ConstructedMissions.doubleActionMission2(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission3(
        R.string.double_action_mission_3,
        ConstructedMissions.doubleActionMission3(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission4(
        R.string.double_action_mission_4,
        ConstructedMissions.doubleActionMission4(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission5(
        R.string.double_action_mission_5,
        ConstructedMissions.doubleActionMission5(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission6(
        R.string.double_action_mission_6,
        ConstructedMissions.doubleActionMission6(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission7(
        R.string.double_action_mission_7,
        ConstructedMissions.doubleActionMission7(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission8(
        R.string.double_action_mission_8, ConstructedMissions.doubleActionMission8(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission9(
        R.string.double_action_mission_9, ConstructedMissions.doubleActionMission9(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission10(
        R.string.double_action_mission_10, ConstructedMissions.doubleActionMission10(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission11(
        R.string.double_action_mission_11, ConstructedMissions.doubleActionMission11(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission12(
        R.string.double_action_mission_12, ConstructedMissions.doubleActionMission12(),
        MissionGroup.DOUBLE_ACTION_STANDARD,
        R.string.double_action_mission_introduction
    );

    var resId: Int
        private set
    private var eventList: EventList?
    var group: MissionGroup
        private set
    var missionIntroductionResId: Int
        private set

    constructor(resId: Int, eventList: EventList?, group: MissionGroup) {
        this.eventList = eventList
        this.resId = resId
        this.group = group
        missionIntroductionResId = 0
    }

    constructor(resId: Int, eventList: EventList, group: MissionGroup, missionIntroductionResId: Int) {
        this.eventList = eventList
        this.resId = resId
        this.group = group
        this.missionIntroductionResId = missionIntroductionResId
    }

    open suspend fun buildEvents(preferences: SharedPreferences): EventList {
        val missionPreferences = MissionImpl.parsePreferences(preferences)
        var copyOfList: EventList? = null

        if (!missionPreferences.showUnconfirmed) {
            copyOfList = EventList(eventList!!)
            copyOfList.stompUnconfirmedReports(missionPreferences.players == 5)
        }
        if (preferences.getBoolean("compressTimePreference", false)) {
            if (copyOfList == null) copyOfList = EventList(eventList!!)
            copyOfList.compressTime()
        }
        return copyOfList ?: eventList!!
    }

    fun toString(context: Context): String {
        return context.getString(resId)
    }

    companion object
}