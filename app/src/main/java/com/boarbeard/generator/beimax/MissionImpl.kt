/**
 * This file is part of the JSpaceAlertMissionGenerator software.
 * Copyright (C) 2011 Maximilian Kalus
 * See http://www.beimax.de/ and https://github.com/mkalus/JSpaceAlertMissionGenerator
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.boarbeard.generator.beimax

import android.content.SharedPreferences
import com.boarbeard.ui.widget.SeekBarPreference
import timber.log.Timber
import java.util.Random

/**
 * Default Mission Generator
 *
 * @author mkalus
 */
class MissionImpl(preferences: MissionPreferences) : IMission {
    //private int constantThreatUnconfirmedExpansion = 2;
    /**
     * minimum and maximum time for white noise
     */
    private val minWhiteNoise = 45
    private val maxWhiteNoise = 60
    private val minWhiteNoiseTime = 9
    private val maxWhiteNoiseTime = 20

    /**
     * keeps threats
     */
    var threats: Array<ThreatGroup> = arrayOf()

    /**
     * keeps incoming and data transfers
     */
    var dataOperationsBundle: DataOperationsBundle? = null

    /**
     * white noise chunks in seconds (to distribute)
     */
    //private WhiteNoise[] whiteNoise;
    var whiteNoiseChunksGlob: List<Int>? = null

    /**
     * phase times in seconds
     */
    private var phaseTimes: IntArray = emptyArray<Int>().toIntArray()

    /**
     * event list
     */
    private var eventList: EventList? = null

    /**
     * random number generator
     */
    var generator: Random


    private val missionPreferences: MissionPreferences

    /**
     * @param preferences for the mission.
     */
    init {
        var seed: Long = 0
        seed = if (preferences.seed != 0L) {
            preferences.seed
        } else {
            System.nanoTime() + 8682522807148012L
        }
        generator = Random(seed)
        missionPreferences = preferences
    }

    /**
     * Return event list of mission
     *
     * @return ordered event list of mission
     */
    override fun getMissionEvents(): EventList {
        return eventList!!
    }

    /**
     * Return length of a phase in seconds
     *
     * @param phase 1-3
     * @return phase length of mission or -1
     */
    override fun getMissionPhaseLength(phase: Int): Int {
        if (phase < 1 || phase > phaseTimes.size) return -1
        return phaseTimes[phase - 1]
    }

    /**
     * Generate new mission
     *
     * @return true if mission creation succeeded
     */
    override fun generateMission(): Boolean {
        // generate threats
        var generated: Boolean
        var tries = 100 //maximum number of tries to generate mission
        do {
            threats = ThreatsGenerator(missionPreferences, generator).generateThreats()
            generated = threats.isNotEmpty()
        } while (!generated && tries-- > 0)
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating threats.")
            return false //fail
        }

        // generate data transfer and incoming data
        tries = 100
        do {
            dataOperationsBundle =
                DataOperationGenerator(missionPreferences, generator).generateDataOperations()
            generated = dataOperationsBundle!!.success
        } while (!generated && tries-- > 0)
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating data operations.")
            return false //fail
        }

        //generate times
        Companion.generateTimes(this)

        // generate phases
        eventList = PhasesGenerator(
            threats,
            generator,
            phaseTimes,
            dataOperationsBundle!!,
            whiteNoiseChunksGlob!!
        )
            .generatePhases(100)

        if (eventList == null) {
            Timber.tag("generateMission()").w("Giving up creating phase details.")
            return false //fail
        }

        return true
    }


    /**
     * Prints list of missions
     */
    override fun toString(): String {
        return eventList.toString()
    }

    companion object {
        private const val CONSTANT_THREAT_UNCONFIRMED =
            1 // You have to subtract a value from the threatLevel for 4-player-games, and this is that value

        fun parsePreferences(preferences: SharedPreferences): MissionPreferences {
            val prefs = MissionPreferences()

            prefs.players = preferences.getInt("playerCount", 5)
            prefs.threatLevel = preferences.getInt(
                "threatDifficultyPreference",
                8
            ) // The threat level for 5 players!
            prefs.enableDoubleThreats = preferences.getBoolean("enable_double_threats", false)

            if (!preferences.getBoolean("stompUnconfirmedReportsPreference", true)) {
                //  They want Unconfirmed Reports.
                prefs.showUnconfirmed = true
                prefs.threatUnconfirmed =
                    CONSTANT_THREAT_UNCONFIRMED // if 5 players - then this is the unconfirmed part of the base threat level
            } else {
                //  They want Unconfirmed Reports to appear as normal threats in
                //  5-player games, and to not appear at all in 4-or-fewer-player
                //  games.
                prefs.showUnconfirmed = false
                //  One way we *could* do this is is to generate the missions
                //  normally (including unconfirmed reports), and then go back and
                //  call EventList.stompUnconfirmedReports() in
                //  MissionType.getEventList() the way we do for the standard
                //  ConstructedMissions, but instead, let's just adjust the
                //  difficulty of the mission.
                prefs.threatUnconfirmed = 0
                if (prefs.players != 5) {
                    prefs.threatLevel =
                        prefs.threatLevel - CONSTANT_THREAT_UNCONFIRMED // if 4 players, then you have to subtract the unconfirmed part from the base level (8 - 1 = 7 in normal missions)
                }
            }

            prefs.incomingDataRange = IntRange(
                preferences.getInt("numberIncomingData", prefs.getMinIncomingData()),
                preferences.getInt(
                    "numberIncomingData" + SeekBarPreference.RIGHT_VALUE_SUFFIX,
                    prefs.getMaxIncomingData()
                )
            )


            val missionLength = preferences.getInt("missionLengthPreference", 600)
            prefs.minPhaseTime[0] = (missionLength * .4).toInt()
            prefs.minPhaseTime[1] = (missionLength * .35).toInt()
            prefs.minPhaseTime[2] = (missionLength * .25).toInt()

            prefs.maxPhaseTime[0] = (missionLength * .4 + 15).toInt()
            prefs.maxPhaseTime[1] = (missionLength * .35 + 15).toInt()
            prefs.maxPhaseTime[2] = (missionLength * .25 + 15).toInt()

            //  For unit tests, we'd like to be able to repeatably generate "random"
            //  missions, so... look for an optional random number seed.  (I know
            //  we're looking for an int instead of a long here, but it doesn't
            //  matter; int is fine for unit tests.)
            prefs.seed = preferences.getInt("randomSeed", 0).toLong()
            prefs.compressTime = preferences.getBoolean("compressTimePreference", false)

            return prefs
        }

        /**
         * simple generation of times for phases, white noise etc.
         */
        protected fun generateTimes(missionImpl: MissionImpl) {
            // generate white noise
            var whiteNoiseTime = missionImpl.generator.nextInt(
                missionImpl.maxWhiteNoise - missionImpl.minWhiteNoise + 1
            ) + missionImpl.minWhiteNoise
            Timber.tag("generateTimes()").v("White noise time: %d", whiteNoiseTime)
    
            // create chunks
            val whiteNoiseChunks = ArrayList<Int>()
            while (whiteNoiseTime > 0) {
                // create random chunk
                val chunk =
                    missionImpl.generator.nextInt(
                        missionImpl.maxWhiteNoiseTime - missionImpl.minWhiteNoiseTime + 1
                    ) + missionImpl.minWhiteNoiseTime
                // check if there is enough time left
                if (chunk > whiteNoiseTime) {
                    // hard case: smaller than minimum time
                    if (chunk < missionImpl.minWhiteNoiseTime) {
                        // add to last chunk that fits
                        for (i in whiteNoiseChunks.indices.reversed()) {
                            val sumChunk = whiteNoiseChunks[i] + chunk
                            // if smaller than maximum time: add to this chunk
                            if (sumChunk <= missionImpl.maxWhiteNoiseTime) {
                                whiteNoiseChunks[i] = sumChunk
                                whiteNoiseTime = 0
                                break
                            }
                        }
                        // still not zeroed
                        if (whiteNoiseTime > 0) { // add to last element, regardless - quite unlikely though
                            val lastIdx = whiteNoiseChunks.size - 1
                            whiteNoiseChunks[lastIdx] = whiteNoiseChunks[lastIdx] + chunk
                            whiteNoiseTime = 0
                        }
                    } else { // easy case: create smaller rest chunk
                        whiteNoiseChunks.add(whiteNoiseTime)
                        whiteNoiseTime = 0
                    }
                } else { // add new chunk
                    whiteNoiseChunks.add(chunk)
                    whiteNoiseTime -= chunk
                }
            }
    
            // ok, add chunks to mission
            //whiteNoise = new WhiteNoise[whiteNoiseChunks.size()];
            //for (int i = 0; i < whiteNoiseChunks.size(); i++) whiteNoise[i] = new WhiteNoise(whiteNoiseChunks.get(i));
            missionImpl.whiteNoiseChunksGlob =
                whiteNoiseChunks // White noise consists of two events, which will be constructed later
    
            // add mission lengths
            missionImpl.phaseTimes = IntArray(3)
            for (i in 0..2) {
                missionImpl.phaseTimes[i] =
                    missionImpl.generator.nextInt(missionImpl.missionPreferences.maxPhaseTime[i] - missionImpl.missionPreferences.minPhaseTime[i] + 1) + missionImpl.missionPreferences.minPhaseTime[i]
            }
        }
    }
}
