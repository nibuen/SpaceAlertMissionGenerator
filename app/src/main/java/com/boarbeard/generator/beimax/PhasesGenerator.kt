package com.boarbeard.generator.beimax

import com.boarbeard.generator.beimax.event.DataTransfer
import com.boarbeard.generator.beimax.event.IncomingData
import com.boarbeard.generator.beimax.event.Threat
import timber.log.Timber
import java.util.*
import kotlin.math.max

/**
 * chance for ambush in phases 4/8 in %
 */
private val chanceForAmbush = intArrayOf(40, 40)

/**
 * times for first threats to appear
 */
private val minTimeForFirst = intArrayOf(10, 10)
private val maxTimeForFirst = intArrayOf(20, 40)

/**
 * "middle" threats (2+3/5+6) should appear with % of phase length
 */
private const val threatsWithInPercent = 70.0f


class PhasesGenerator(
    private val threats: Array<ThreatGroup>,
    private val generator: Random,
    private val phaseTimes: IntArray,
    private val dataOperationsBundle: DataOperationsBundle,
    private val whiteNoiseChunksGlob: ArrayList<Int>,
) {

    var eventList = EventList()

    /**
     * generate phase stuff from data above
     *
     * @return true if phase generation succeeded
     */
    fun generatePhases(): Boolean {
        eventList = EventList()

        Timber.i("Data gathered: Generating phases.")

        // Deep copy as we modify the groups when attempting to fit
        val threats =
            this.threats.map { original -> ThreatGroup(original.internal, original.external) }

        // add fixed events: announcements
        eventList.addPhaseEvents(phaseTimes[0], phaseTimes[1], phaseTimes[2])

        var ambushOccurred = false
        // add threats in first phase
        // ambush handling - is there a phase 4, and it is a normal external threat? ... and chance is taken?
        var maybeAmbush = threats[3].external
        if (maybeAmbush != null && maybeAmbush.threatLevel == Threat.THREAT_LEVEL_NORMAL && generator.nextInt(
                100
            ) + 1 < chanceForAmbush[0]
        ) {
            //...then add an "ambush" threat between 1 minute and 20 secs warnings
            eventList.fit {
                // TODO: remove hardcoded length here:
                val ambushTime: Int = generator.nextInt(35) + phaseTimes[0] - 59
                Timber.i("Ambush in phase 1 at time: %d", ambushTime)
                eventList.addEvent(ambushTime, maybeAmbush)
            }
            threats[3].removeExternal()
            ambushOccurred = true // to disallow two ambushes in one game
        }

        // to be used further down
        val lastThreatTime = intArrayOf(0, 0)

        // add the rest of the threats
        var currentTime: Int =
            generator.nextInt(maxTimeForFirst[0] - minTimeForFirst[0] + 1) + minTimeForFirst[0]
        // threats should appear within this time
        var lastTime = (phaseTimes[0] * (threatsWithInPercent / 100)).toInt()
        var first = true
        // look for first threat
        run {
            var i = 0
            while (i <= 3) {
                val now = threats[i]
                val activeThreat: Threat? = now.pop()
                if (activeThreat != null) {
                    i-- //check again
                } else {
                    i++
                    continue
                }

                // first event?
                if (first) {
                    if (!eventList.addEvent(
                            currentTime,
                            activeThreat
                        )
                    ) Timber.w("Could not add first event to list (time $currentTime) - arg!") else Timber.i(
                        "adding first threat %s",
                        activeThreat
                    )
                    first = false
                } else {
                    var nextTime = 0
                    if (lastTime > currentTime) {
                        val success = eventList.fit(attempts = 30) { currentAttempt ->
                            // next element occurs
                            val divisor = if (currentAttempt > 10) 3 else 2
                            nextTime =
                                generator.nextInt(max(1, (lastTime - currentTime) / divisor)) + 5
                            eventList.addEvent(currentTime + nextTime, activeThreat)
                        }
                        if (!success) return false
                    }
                    currentTime += nextTime
                    // save lastThreatTime for data transfers further down
                    if (i < 3) lastThreatTime[0] = currentTime
                }
                // add to time
                currentTime += activeThreat.lengthInSeconds
                i++
            }
        }

        // add threats in second phase
        // ambush handling - is there a phase 8, and it is a normal external threat? ... and chance is taken?
        maybeAmbush = threats[7].external
        if (!ambushOccurred && maybeAmbush != null && maybeAmbush.threatLevel == Threat.THREAT_LEVEL_NORMAL && generator.nextInt(
                100
            ) + 1 < chanceForAmbush[1]
        ) {
            //...then add an "ambush" threat between 1 minute and 20 secs warnings
            eventList.fit {
                // TODO: remove hardcoded length here:
                val ambushTime: Int =
                    generator.nextInt(35) + phaseTimes[0] + phaseTimes[1] - 59
                val done = eventList.addEvent(ambushTime, maybeAmbush)
                if (done) {
                    Timber.i("Ambush in phase 2 at time: %d", ambushTime)
                }
                done
            }
            threats[7].removeExternal()
        }

        // add the rest of the threats
        currentTime =
            phaseTimes[0] + generator.nextInt(maxTimeForFirst[1] - minTimeForFirst[1] + 1) + minTimeForFirst[1]
        // threats should appear within this time
        lastTime =
            phaseTimes[0] + (phaseTimes[1] * (threatsWithInPercent / 100f)).toInt()
        first = true
        // look for first threat
        run {
            var i = 4
            while (i <= 7) {
                val now = threats[i]
                val activeThreat: Threat? = now.pop()
                if (activeThreat != null) {
                    i-- //check again
                } else {
                    i++
                    continue
                }

                // first event?
                if (first) {
                    if (!eventList.addEvent(
                            currentTime,
                            activeThreat
                        )
                    ) Timber.w("Could not add first event to list in second phase (time $currentTime) - arg!")
                    first = false
                } else {
                    var nextTime = 0
                    if (lastTime > currentTime) {
                        val success = eventList.fit(attempts = 30) { currentAttempt ->
                            // next element occurs
                            val divisor = if (currentAttempt > 10) 3 else 2
                            nextTime =
                                generator.nextInt(max(1, (lastTime - currentTime) / divisor)) + 5
                            eventList.addEvent(currentTime + nextTime, activeThreat)
                        }
                        if (!success) return false
                    }
                    currentTime += nextTime
                    // save lastThreatTime for data transfers further down
                    if (i < 7) lastThreatTime[1] = currentTime
                }
                // add to time
                currentTime += activeThreat.lengthInSeconds
                i++
            }
        }

        fitDataOperations(lastThreatTime)
        fitWhiteNoise()

        return true
    }

    private fun fitDataOperations(lastThreatTime: IntArray): Boolean {
        var startTime = 0
        var endTime = 0
        // special balance: first data transfers in phase 1 and 2 should occur shortly after first threat wave
        for (i in 0..1) {
            endTime += phaseTimes[i]
            if (dataOperationsBundle.dataTransfers[i] > 0) { // if there is a data transfer
                startTime = lastThreatTime[i]
                var done = false // try until it fits
                do { // try to add incoming data within 30 seconds of event
                    startTime += generator.nextInt(31) + 1
                    done = eventList.addEvent(startTime, DataTransfer())
                } while (!done && startTime < endTime)
                if (done) {
                    // reduce data transfers below
                    dataOperationsBundle.dataTransfers[i]--
                }
            }
        }
        startTime = 0
        endTime = 0

        // distribute rest of data transfers and incoming data randomly within the phases
        for (phase in 0..2) {
            // recalculate phase times
            startTime = endTime
            endTime += phaseTimes[phase]
            // data transfer first, since these are fairly long
            for (dataTransferIndex in 0 until dataOperationsBundle.dataTransfers[phase]) {
                eventList.fit {
                    // white noise can pretty much occur everywhere
                    val time: Int =
                        generator.nextInt(endTime - startTime) + startTime - 5 // to fend off events after mission ends
                    eventList.addEvent(time, DataTransfer())
                }
            }
            // incoming data second
            for (incomingDataIndex in 0 until dataOperationsBundle.incomingData[phase]) {
                eventList.fit {
                    // white noise can pretty much occur everywhere
                    val time: Int =
                        generator.nextInt(endTime - startTime) + startTime - 5 // to fend off events after mission ends
                    eventList.addEvent(time, IncomingData())
                }
            }
        }

        return true
    }

    //add white noise at random times
    private fun fitWhiteNoise() {
        for (i in whiteNoiseChunksGlob.indices) {
            eventList.fit {
                val time: Int =
                    generator.nextInt(phaseTimes[0] + phaseTimes[1] + phaseTimes[2] - 30) + 10
                eventList.addWhiteNoiseEvents(time, whiteNoiseChunksGlob[i])
            }
        }
    }

}