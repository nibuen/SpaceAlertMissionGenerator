package com.boarbeard.ui

import android.content.Context
import android.content.SharedPreferences
import com.boarbeard.R
import com.boarbeard.generator.beimax.ConstructedMissions
import com.boarbeard.generator.beimax.EventList
import com.boarbeard.generator.beimax.MissionImpl

enum class MissionType {
    /**
     * Constructs a new random mission each time
     * [.buildEvents] is called
     *
     * @author Chris
     */
    Random(R.string.mission_random, null) {
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
            .firstTestRun()
    ),  //
    SecondTestRun(
        R.string.mission_second_test_run, ConstructedMissions
            .secondTestRun()
    ),  //
    Simulation1(
        R.string.mission_simulation_1, ConstructedMissions
            .simulation1()
    ),  //
    Simulation2(
        R.string.mission_simulation_2, ConstructedMissions
            .simulation2()
    ),  //
    Simulation3(
        R.string.mission_simulation_3, ConstructedMissions
            .simulation3()
    ),  //
    AdvancedSimulation1(
        R.string.mission_advanced_simulation_1,
        ConstructedMissions.advancedsimulation1()
    ),  //
    AdvancedSimulation2(
        R.string.mission_advanced_simulation_2,
        ConstructedMissions.advancedsimulation2()
    ),  //
    AdvancedSimulation3(
        R.string.mission_advanced_simulation_3,
        ConstructedMissions.advancedsimulation3()
    ),  //
    RealMission1(
        R.string.mission_real_mission_1, ConstructedMissions
            .realmission1()
    ),  //
    RealMission2(
        R.string.mission_real_mission_2, ConstructedMissions
            .realmission2()
    ),  //
    RealMission3(
        R.string.mission_real_mission_3, ConstructedMissions
            .realmission3()
    ),  //
    RealMission4(
        R.string.mission_real_mission_4, ConstructedMissions
            .realmission4()
    ),  //
    RealMission5(
        R.string.mission_real_mission_5, ConstructedMissions
            .realmission5()
    ),  //
    RealMission6(
        R.string.mission_real_mission_6, ConstructedMissions
            .realmission6()
    ),  //
    RealMission7(
        R.string.mission_real_mission_7, ConstructedMissions
            .realmission7()
    ),  //
    RealMission8(
        R.string.mission_real_mission_8, ConstructedMissions
            .realmission8()
    ),
    DoubleActionEasyMission1(
        R.string.double_action_easy_mission_1,
        ConstructedMissions.doubleActionEasierMission1(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission2(
        R.string.double_action_easy_mission_2,
        ConstructedMissions.doubleActionEasierMission2(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission3(
        R.string.double_action_easy_mission_3,
        ConstructedMissions.doubleActionEasierMission3(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission4(
        R.string.double_action_easy_mission_4,
        ConstructedMissions.doubleActionEasierMission4(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission5(
        R.string.double_action_easy_mission_5,
        ConstructedMissions.doubleActionEasierMission5(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionEasyMission6(
        R.string.double_action_easy_mission_6,
        ConstructedMissions.doubleActionEasierMission6(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission1(
        R.string.double_action_mission_1,
        ConstructedMissions.doubleActionMission1(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission2(
        R.string.double_action_mission_2,
        ConstructedMissions.doubleActionMission2(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission3(
        R.string.double_action_mission_3,
        ConstructedMissions.doubleActionMission3(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission4(
        R.string.double_action_mission_4,
        ConstructedMissions.doubleActionMission4(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission5(
        R.string.double_action_mission_5,
        ConstructedMissions.doubleActionMission5(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission6(
        R.string.double_action_mission_6,
        ConstructedMissions.doubleActionMission6(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission7(
        R.string.double_action_mission_7,
        ConstructedMissions.doubleActionMission7(), R.string.double_action_mission_introduction
    ),
    DoubleActionMission8(
        R.string.double_action_mission_8, ConstructedMissions.doubleActionMission8(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission9(
        R.string.double_action_mission_9, ConstructedMissions.doubleActionMission9(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission10(
        R.string.double_action_mission_10, ConstructedMissions.doubleActionMission10(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission11(
        R.string.double_action_mission_11, ConstructedMissions.doubleActionMission11(),
        R.string.double_action_mission_introduction
    ),
    DoubleActionMission12(
        R.string.double_action_mission_12, ConstructedMissions.doubleActionMission12(),
        R.string.double_action_mission_introduction
    );

    var resId: Int
        private set
    private var eventList: EventList?
    var missionIntroductionResId: Int
        private set

    constructor(resId: Int, eventList: EventList?) {
        this.eventList = eventList
        this.resId = resId
        missionIntroductionResId = 0
    }

    constructor(resId: Int, eventList: EventList, missionIntroductionResId: Int) {
        this.eventList = eventList
        this.resId = resId
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

    companion object {
        @JvmStatic
		fun toStringValues(context: Context): Array<String?> {
            val values = values()
            val names = arrayOfNulls<String>(values.size)
            for (i in values.indices) {
                names[i] = context.getString(values[i].resId)
            }
            return names
        }
    }
}