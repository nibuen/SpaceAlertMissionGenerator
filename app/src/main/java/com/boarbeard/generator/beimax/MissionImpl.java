/**
 * This file is part of the JSpaceAlertMissionGenerator software.
 * Copyright (C) 2011 Maximilian Kalus
 * See http://www.beimax.de/ and https://github.com/mkalus/JSpaceAlertMissionGenerator
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.boarbeard.generator.beimax;

import static com.boarbeard.ui.widget.SeekBarPreference.RIGHT_VALUE_SUFFIX;

import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

import kotlin.ranges.IntRange;
import timber.log.Timber;

/**
 * Default Mission Generator
 *
 * @author mkalus
 */
public class MissionImpl implements IMission {

    private static final int CONSTANT_THREAT_UNCONFIRMED = 1; // You have to subtract a value from the threatLevel for 4-player-games, and this is that value
    private static final int CONSTANT_THREAT_UNCONFIRMED_LARGE = 2; // use a larger value if the threat count is high (normally for double actions)

    /**
     * minimum and maximum time for white noise
     */
    private int minWhiteNoise = 45;
    private int maxWhiteNoise = 60;
    private int minWhiteNoiseTime = 9;
    private int maxWhiteNoiseTime = 20;

    /**
     * keeps threats
     */
    ThreatGroup[] threats;

    /**
     * keeps incoming and data transfers
     */
    DataOperationsBundle dataOperationsBundle;

    /**
     * white noise chunks in seconds (to distribute)
     */
    //private WhiteNoise[] whiteNoise;
    ArrayList<Integer> whiteNoiseChunksGlob;

    /**
     * phase times in seconds
     */
    private int[] phaseTimes;

    /**
     * event list
     */
    private EventList eventList;

    /**
     * random number generator
     */
    Random generator;


    private final MissionPreferences missionPreferences;

    /**
     * @param preferences for the mission.
     */
    public MissionImpl(MissionPreferences preferences) {
        long seed = 0;
        if (preferences.getSeed() != 0) {
            seed = preferences.getSeed();
        } else {
            seed = System.nanoTime() + 8682522807148012L;
        }
        generator = new Random(seed);
        missionPreferences = preferences;
    }

    public static MissionPreferences parsePreferences(SharedPreferences preferences) {
        MissionPreferences prefs = new MissionPreferences();

        prefs.setPlayers(preferences.getInt("playerCount", 5));
        prefs.setThreatLevel(preferences.getInt("threatDifficultyPreference", 8)); // The threat level for 5 players!
        prefs.setEnableDoubleThreats(preferences.getBoolean("enable_double_threats", false));

        int unconfirmedValue = CONSTANT_THREAT_UNCONFIRMED;
        if(prefs.getThreatLevel() > 10){
            unconfirmedValue = CONSTANT_THREAT_UNCONFIRMED_LARGE;
        }

        if (!preferences.getBoolean("stompUnconfirmedReportsPreference", true)) {
            //  They want Unconfirmed Reports.
            prefs.setShowUnconfirmed(true);
            prefs.setThreatUnconfirmed(unconfirmedValue); // if 5 players - then this is the unconfirmed part of the base threat level
        } else {
            //  They want Unconfirmed Reports to appear as normal threats in
            //  5-player games, and to not appear at all in 4-or-fewer-player
            //  games.
            prefs.setShowUnconfirmed(false);
            //  One way we *could* do this is is to generate the missions
            //  normally (including unconfirmed reports), and then go back and
            //  call EventList.stompUnconfirmedReports() in
            //  MissionType.getEventList() the way we do for the standard
            //  ConstructedMissions, but instead, let's just adjust the
            //  difficulty of the mission.
            prefs.setThreatUnconfirmed(0);
            if (prefs.getPlayers() != 5) {
                prefs.setThreatLevel(prefs.getThreatLevel() - unconfirmedValue); // if 4 players, then you have to subtract the unconfirmed part from the base level (8 - 1 = 7 in normal missions)
            }
        }

        prefs.setIncomingDataRange(
                new IntRange(
                        preferences.getInt("numberIncomingData", prefs.getMinIncomingData()),
                        preferences.getInt("numberIncomingData" + RIGHT_VALUE_SUFFIX, prefs.getMaxIncomingData())
                )
        );
        prefs.setDataTransferRange(
                new IntRange(
                        preferences.getInt("numberDataTransfer", prefs.getMinDataTransfer()),
                        preferences.getInt("numberDataTransfer" + RIGHT_VALUE_SUFFIX, prefs.getMaxDataTransfer())
                )
        );


        int missionLength = preferences.getInt("missionLengthPreference", 600);
        prefs.getMinPhaseTime()[0] = (int) (missionLength * .4);
        prefs.getMinPhaseTime()[1] = (int) (missionLength * .35);
        prefs.getMinPhaseTime()[2] = (int) (missionLength * .25);

        prefs.getMaxPhaseTime()[0] = (int) (missionLength * .4 + 15);
        prefs.getMaxPhaseTime()[1] = (int) (missionLength * .35 + 15);
        prefs.getMaxPhaseTime()[2] = (int) (missionLength * .25 + 15);

        //  For unit tests, we'd like to be able to repeatably generate "random"
        //  missions, so... look for an optional random number seed.  (I know
        //  we're looking for an int instead of a long here, but it doesn't
        //  matter; int is fine for unit tests.)
        prefs.setSeed(preferences.getInt("randomSeed", 0));
        prefs.setCompressTime(preferences.getBoolean("compressTimePreference", false));

        return prefs;
    }

    /**
     * Return event list of mission
     *
     * @return ordered event list of mission
     */
    public EventList getMissionEvents() {
        return eventList;
    }

    /**
     * Return length of a phase in seconds
     *
     * @param phase 1-3
     * @return phase length of mission or -1
     */
    public int getMissionPhaseLength(int phase) {
        if (phase < 1 || phase > phaseTimes.length) return -1;
        return phaseTimes[phase - 1];
    }

    /**
     * Generate new mission
     *
     * @return true if mission creation succeeded
     */
    public boolean generateMission() {
        // generate threats
        boolean generated = false;
        int tries = 500; //maximum number of tries to generate mission
        do {
            threats = new ThreatsGenerator(missionPreferences, generator).generateThreats();
            generated = threats.length > 0;
        } while (!generated && tries-- > 0);
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating threats.");
            return false; //fail
        }

        // generate data transfer and incoming data
        generated = false;
        tries = 100;
        do {
            dataOperationsBundle = new DataOperationGenerator(missionPreferences, generator).generateDataOperations();
            generated = dataOperationsBundle.getSuccess();
        } while (!generated && tries-- > 0);
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating data operations.");
            return false; //fail
        }

        //generate times
        generateTimes();

        //generate phases
        generated = false;
        tries = 100;
        do {
            PhasesGenerator phasesGenerator = new PhasesGenerator(threats, generator, phaseTimes, dataOperationsBundle, whiteNoiseChunksGlob);
            generated = phasesGenerator.generatePhases();
            eventList = phasesGenerator.getEventList();
        } while (!generated && tries-- > 0);
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating phase details.");
            return false; //fail
        }

        return true;
    }


    /**
     * simple generation of times for phases, white noise etc.
     */
    protected void generateTimes() {
        // generate white noise
        int whiteNoiseTime = generator.nextInt(maxWhiteNoise - minWhiteNoise + 1) + minWhiteNoise;
        Timber.tag("generateTimes()").v("White noise time: %d", whiteNoiseTime);

        // create chunks
        ArrayList<Integer> whiteNoiseChunks = new ArrayList<>();
        while (whiteNoiseTime > 0) {
            // create random chunk
            int chunk = generator.nextInt(maxWhiteNoiseTime - minWhiteNoiseTime + 1) + minWhiteNoiseTime;
            // check if there is enough time left
            if (chunk > whiteNoiseTime) {
                // hard case: smaller than minimum time
                if (chunk < minWhiteNoiseTime) {
                    // add to last chunk that fits
                    for (int i = whiteNoiseChunks.size() - 1; i >= 0; i--) {
                        int sumChunk = whiteNoiseChunks.get(i) + chunk;
                        // if smaller than maximum time: add to this chunk
                        if (sumChunk <= maxWhiteNoiseTime) {
                            whiteNoiseChunks.set(i, sumChunk);
                            whiteNoiseTime = 0;
                            break;
                        }
                    }
                    // still not zeroed
                    if (whiteNoiseTime > 0) { // add to last element, regardless - quite unlikely though
                        int lastIdx = whiteNoiseChunks.size() - 1;
                        whiteNoiseChunks.set(lastIdx, whiteNoiseChunks.get(lastIdx) + chunk);
                        whiteNoiseTime = 0;
                    }
                } else { // easy case: create smaller rest chunk
                    whiteNoiseChunks.add(whiteNoiseTime);
                    whiteNoiseTime = 0;
                }
            } else { // add new chunk
                whiteNoiseChunks.add(chunk);
                whiteNoiseTime -= chunk;
            }
        }

        // ok, add chunks to mission
        //whiteNoise = new WhiteNoise[whiteNoiseChunks.size()];
        //for (int i = 0; i < whiteNoiseChunks.size(); i++) whiteNoise[i] = new WhiteNoise(whiteNoiseChunks.get(i));
        whiteNoiseChunksGlob = whiteNoiseChunks; // White noise consists of two events, which will be constructed later

        // add mission lengths
        phaseTimes = new int[3];
        for (int i = 0; i < 3; i++) {
            phaseTimes[i] = generator.nextInt(missionPreferences.getMaxPhaseTime()[i] - missionPreferences.getMinPhaseTime()[i] + 1) + missionPreferences.getMinPhaseTime()[i];
        }
    }

    /**
     * Prints list of missions
     */
    @NotNull
    @Override
    public String toString() {
        return eventList.toString();
    }
}
