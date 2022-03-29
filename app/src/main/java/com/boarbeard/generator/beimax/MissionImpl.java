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

import com.boarbeard.generator.beimax.event.DataTransfer;
import com.boarbeard.generator.beimax.event.IncomingData;
import com.boarbeard.generator.beimax.event.Threat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
    //private int constantThreatUnconfirmedExpansion = 2;

    /**
     * minimum data operations (either data transfer or incoming data)
     */
    private int[] minDataOperations = {2, 2, 0};
    private int[] maxDataOperations = {4, 4, 3};

    /**
     * minimum and maximum incoming data by phases
     */
    private int minIncomingDataTotal = 1;

    /**
     * minimum and maximum data transfers by phases
     */
    private int[] minDataTransfer = {0, 1, 1};
    private int[] maxDataTransfer = {1, 2, 1};
    private int minDataTransferTotal = 3;

    /**
     * minimum and maximum time for white noise
     */
    private int minWhiteNoise = 45;
    private int maxWhiteNoise = 60;
    private int minWhiteNoiseTime = 9;
    private int maxWhiteNoiseTime = 20;

    /**
     * times for first threats to appear
     */
    private int[] minTimeForFirst = {10, 10};
    private int[] maxTimeForFirst = {20, 40};

    /**
     * chance for ambush in phases 4/8 in %
     */
    private int[] chanceForAmbush = {40, 40};

    /**
     * "middle" threats (2+3/5+6) should appear with % of phase length
     */
    private int threatsWithInPercent = 70;

    /**
     * keeps threats
     */
    ThreatGroup[] threats;

    /**
     * keeps incoming and data transfers
     */
    private int[] incomingData;
    private int[] dataTransfers;

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

        if (!preferences.getBoolean("stompUnconfirmedReportsPreference", true)) {
            //  They want Unconfirmed Reports.
            prefs.setShowUnconfirmed(true);
            prefs.setThreatUnconfirmed(CONSTANT_THREAT_UNCONFIRMED); // if 5 players - then this is the unconfirmed part of the base threat level
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
                prefs.setThreatLevel(prefs.getThreatLevel() - CONSTANT_THREAT_UNCONFIRMED); // if 4 players, then you have to subtract the unconfirmed part from the base level (8 - 1 = 7 in normal missions)
            }
        }

        prefs.setIncomingDataRange(
                new IntRange(
                        preferences.getInt("numberIncomingData", prefs.getMinIncomingData()),
                        preferences.getInt("numberIncomingData" + RIGHT_VALUE_SUFFIX, prefs.getMaxIncomingData())
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
        int tries = 100; //maximum number of tries to generate mission
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
            generated = generateDataOperations();
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
            generated = generatePhases();
        } while (!generated && tries-- > 0);
        if (!generated) {
            Timber.tag("generateMission()").w("Giving up creating phase details.");
            return false; //fail
        }

        return true;
    }


    /**
     * Generate data operations (either data transfer or incoming data)
     *
     * @return true, if data creation could be generated
     */
    protected boolean generateDataOperations() {
        // clear data
        incomingData = new int[3];
        dataTransfers = new int[3];

        int incomingSum = 0;
        int transferSum = 0;

        int randomIncomingData = generator.nextInt(missionPreferences.getMaxIncomingData() - missionPreferences.getMinIncomingData() + 1) + missionPreferences.getMinIncomingData();
        Timber.tag("generateData").v("randomIncomingData: %d", randomIncomingData);

        // start with a random in one of the first two phases
        incomingData[generator.nextInt(2)]++;
        randomIncomingData--;
        assert (Arrays.stream(incomingData).sum() <= missionPreferences.getMaxIncomingData());

        // split evenly until we can't
        while ((randomIncomingData / 3) >= 1) {
            incomingData[0]++;
            incomingData[1]++;
            incomingData[2]++;
            randomIncomingData -= 3;
        }
        assert (Arrays.stream(incomingData).sum() <= missionPreferences.getMaxIncomingData());

        // finally just fit stuff randomly
        int lastPlaced = -1;
        while (randomIncomingData > 0) {
            int value;
            do {
                value = generator.nextInt(3);
            } while (lastPlaced == value);
            lastPlaced = value;
            incomingData[value]++;
            randomIncomingData--;
        }
        assert (Arrays.stream(incomingData).sum() <= missionPreferences.getMaxIncomingData());

        // generate stuff by phase
        for (int i = 0; i < 3; i++) {
            dataTransfers[i] = generator.nextInt(maxDataTransfer[i] - minDataTransfer[i] + 1) + minDataTransfer[i];

            // check minimums
            if (incomingData[i] + dataTransfers[i] < minDataOperations[i] ||
                    incomingData[i] + dataTransfers[i] > maxDataOperations[i]) return false;

            incomingSum += incomingData[i];
            transferSum += dataTransfers[i];
        }
        assert (Arrays.stream(incomingData).sum() <= missionPreferences.getMaxIncomingData());

        // check minimums
        if (incomingSum < minIncomingDataTotal || transferSum < minDataTransferTotal) return false;

        // debugging information
        for (int i = 0; i < 3; i++) {
            Timber.tag("generateData").v("Phase " + (i + 1) + ": Incoming Data = " + incomingData[i] + "; Data Transfers = " + dataTransfers[i]);
        }
        assert (Arrays.stream(incomingData).sum() <= missionPreferences.getMaxIncomingData());

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
     * generate phase stuff from data above
     *
     * @return true if phase generation succeeded
     */
    protected boolean generatePhases() {
        Timber.i("Data gathered: Generating phases.");

        // Deep copy as we modify the groups when attempting to fit
        ThreatGroup[] threats = new ThreatGroup[this.threats.length];
        for (int i = 0; i < threats.length; i++) {
            ThreatGroup original = this.threats[i];
            threats[i] = new ThreatGroup(original.getInternal(), original.getExternal());
        }

        // create events
        eventList = new EventList();

        // add fixed events: announcements
        eventList.addPhaseEvents(phaseTimes[0], phaseTimes[1], phaseTimes[2]);

        boolean ambushOccurred = false;
        // add threats in first phase
        // ambush handling - is there a phase 4, and it is a normal external threat? ... and chance is taken?
        Threat maybeAmbush = threats[3].getExternal();
        if (maybeAmbush != null && maybeAmbush.getThreatLevel() == Threat.THREAT_LEVEL_NORMAL && generator.nextInt(100) + 1 < chanceForAmbush[0]) {
            //...then add an "ambush" threat between 1 minute and 20 secs warnings
            boolean done = false; // try until it fits
            do {
                // TODO: remove hardcoded length here:
                int ambushTime = generator.nextInt(35) + phaseTimes[0] - 59;
                Timber.i("Ambush in phase 1 at time: %d", ambushTime);
                done = eventList.addEvent(ambushTime, maybeAmbush);
            } while (!done);

            threats[3].removeExternal();
            ambushOccurred = true; // to disallow two ambushes in one game
        }

        // to be used further down
        int[] lastThreatTime = {0, 0};

        // add the rest of the threats
        int currentTime = generator.nextInt(maxTimeForFirst[0] - minTimeForFirst[0] + 1) + minTimeForFirst[0];
        // threats should appear within this time
        int lastTime = (int) (phaseTimes[0] * (((float) threatsWithInPercent) / 100));
        boolean first = true;
        // look for first threat
        for (int i = 0; i <= 3; i++) {
            ThreatGroup now = threats[i];
            Threat activeThreat;
            if (now.hasExternal()) {
                activeThreat = now.removeExternal();
                i--; //check again
            } else if (now.hasInternal()) {
                activeThreat = now.removeInternal();
                i--; //check again
            } else {
                continue;
            }

            // first event?
            if (first) {
                if (!eventList.addEvent(currentTime, activeThreat))
                    Timber.w("Could not add first event to list (time " + currentTime + ") - arg!");
                else
                    Timber.i("adding first threat %s", activeThreat);
                first = false;
            } else {
                boolean done = false; // try until it fits
                int nextTime = 0;
                int tries = 0; // number of tries
                do {
                    // next threat appears
                    // next element occurs
                    int divisor = 2;
                    if (++tries > 10) divisor = 3;
                    else if (tries > 20) divisor = 4;
                    if (lastTime <= currentTime) return false;
                    nextTime = generator.nextInt((lastTime - currentTime) / divisor) + 5;
                    if (tries > 30) return false;
                    done = eventList.addEvent(currentTime + nextTime, activeThreat);
                } while (!done);
                currentTime += nextTime;
                // save lastThreatTime for data transfers further down
                if (i < 3) lastThreatTime[0] = currentTime;
            }
            // add to time
            currentTime += activeThreat.getLengthInSeconds();
        }

        // add threats in second phase
        // ambush handling - is there a phase 8, and it is a normal external threat? ... and chance is taken?
        maybeAmbush = threats[7].getExternal();
        if (!ambushOccurred && maybeAmbush != null && maybeAmbush.getThreatLevel() == Threat.THREAT_LEVEL_NORMAL && generator.nextInt(100) + 1 < chanceForAmbush[1]) {
            //...then add an "ambush" threat between 1 minute and 20 secs warnings
            boolean done = false; // try until it fits
            do {
                // TODO: remove hardcoded length here:
                int ambushTime = generator.nextInt(35) + phaseTimes[0] + phaseTimes[1] - 59;
                done = eventList.addEvent(ambushTime, maybeAmbush);
                if (done) {
                    Timber.i("Ambush in phase 2 at time: %d", ambushTime);
                }
            } while (!done);

            threats[7].removeExternal();
        }

        // add the rest of the threats
        currentTime = phaseTimes[0] + generator.nextInt(maxTimeForFirst[1] - minTimeForFirst[1] + 1) + minTimeForFirst[1];
        // threats should appear within this time
        lastTime = phaseTimes[0] + (int) (phaseTimes[1] * (((float) threatsWithInPercent) / 100));
        first = true;
        // look for first threat
        for (int i = 4; i <= 7; i++) {
            ThreatGroup now = threats[i];
            Threat activeThreat;
            if (now.hasExternal()) {
                activeThreat = now.removeExternal();
                i--; //check again
            } else if (now.hasInternal()) {
                activeThreat = now.removeInternal();
                i--; //check again
            } else {
                continue;
            }
            // first event?
            if (first) {
                if (!eventList.addEvent(currentTime, activeThreat))
                    Timber.w("Could not add first event to list in second phase (time " + currentTime + ") - arg!");
                first = false;
            } else {
                boolean done = false; // try until it fits
                int nextTime = 0;
                int tries = 0; // number of tries
                do {
                    // next element occurs
                    int divisor = 2;
                    if (++tries > 10) divisor = 3;
                    if (lastTime <= currentTime) return false;
                    nextTime = generator.nextInt((lastTime - currentTime) / divisor) + 5;
                    if (tries > 30) return false;
                    done = eventList.addEvent(currentTime + nextTime, activeThreat);
                } while (!done);
                currentTime += nextTime;
                // save lastThreatTime for data transfers further down
                if (i < 7) lastThreatTime[1] = currentTime;
            }
            // add to time
            currentTime += activeThreat.getLengthInSeconds();
        }

        //add data transfers
        // get start and end times
        int startTime = 0;
        int endTime = 0;
        // special balance: first data transfers in phase 1 and 2 should occur shortly after first threat wave
        for (int i = 0; i < 2; i++) {
            startTime = endTime;
            endTime += phaseTimes[i];
            if (dataTransfers[i] > 0) { // if there is a data transfer
                startTime = lastThreatTime[i];
                boolean done = false; // try until it fits
                do { // try to add incoming data within 30 seconds of event
                    startTime = generator.nextInt(31) + startTime + 1;
                    done = eventList.addEvent(startTime, new DataTransfer());
                } while (!done && startTime < endTime);
                if (done) {
                    // reduce data transfers below
                    dataTransfers[i]--;
                }
            }
        }

        startTime = 0;
        endTime = 0;
        // distribute rest of data transfers and incoming data randomly within the phases
        for (int phase = 0; phase < 3; phase++) {
            // recalculate phase times
            startTime = endTime;
            endTime += phaseTimes[phase];
            // data transfer first, since these are fairly long
            for (int dataTransferIndex = 0; dataTransferIndex < dataTransfers[phase]; dataTransferIndex++) {
                boolean done = false; // try until it fits
                do {
                    // white noise can pretty much occur everywhere
                    int time = generator.nextInt(endTime - startTime) + startTime - 5; // to fend off events after mission ends
                    done = eventList.addEvent(time, new DataTransfer());
                } while (!done);
            }
            // incoming data second
            for (int incomingDataIndex = 0; incomingDataIndex < incomingData[phase]; incomingDataIndex++) {
                boolean done = false; // try until it fits
                do {
                    // white noise can pretty much occur everywhere
                    int time = generator.nextInt(endTime - startTime) + startTime - 5; // to fend off events after mission ends
                    done = eventList.addEvent(time, new IncomingData());
                } while (!done);
            }
        }

        //add white noise at random times
        for (int i = 0; i < whiteNoiseChunksGlob.size(); i++) {
            boolean done = false; // try until it fits
            do {
                // white noise can pretty much occur everywhere
                int time = generator.nextInt(phaseTimes[0] + phaseTimes[1] + phaseTimes[2] - 30) + 10;
                done = eventList.addWhiteNoiseEvents(time, whiteNoiseChunksGlob.get(i));
            } while (!done);
        }

        return true;
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
