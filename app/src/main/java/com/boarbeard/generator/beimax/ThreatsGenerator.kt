package com.boarbeard.generator.beimax

import com.boarbeard.generator.beimax.event.Threat
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min


/**
 * ...of which x levels are internal
 */
private const val minInternalThreatsSingle = 1 // minimum internal threats for single actions
private const val maxInternalThreatsSingle = 3 // maximum internal threats for single actions
private const val minInternalThreatsDouble = 2 // minimum internal threats for double actions
private const val maxInternalThreatsDouble = 5 // maximum internal threats for double actions
private const val maxNormalInternalThreats = 4 // maximum normal internal threats


/**
 * minimum and maximum time in which normal threats can occur
 */
private const val minTNormalExternalThreat = 1
private const val maxTNormalExternalThreat = 8

/**
 * minimum and maximum time in which serious threats can occur
 */
private const val minTSeriousExternalThreat = 2
private const val maxTSeriousExternalThreat = 8

/**
 * minimum and maximum time in which normal threats can occur
 */
private const val minTNormalInternalThreat = 2
private const val maxTNormalInternalThreat = 8

/**
 * minimum and maximum time in which serious threats can occur
 */
private const val minTSeriousInternalThreat = 3
private const val maxTSeriousInternalThreat = 7


class ThreatsGenerator(
    private val missionPreferences: MissionPreferences,
    val generator: Random
) {
    var sortedThreats = Array<ThreatGroup?>(8) { null }
    var threatsFirstPhase = 0
    var threatsSecondPhase = 0
    val firstPhase: IntArray = intArrayOf(0,1,2)
    val secondPhase: IntArray = intArrayOf(3,4,5,6,7)
    val firstSecondPhase: IntArray = intArrayOf(0,1,2,3,4,5,6,7)
    var internalThreatTurns = ArrayList<Int>()
    var unconfirmedThreatsFirstPhase = 0
    var unconfirmedThreatsSecondPhase = 0
    fun insertThreat(threat: Threat): Boolean {
        // set min and max phases depending on the threat position and level
        var minTurn = 1
        var maxTurn = 8
        if (threat.threatPosition == Threat.THREAT_POSITION_EXTERNAL) {
            if (threat.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
                if (minTurn < minTSeriousExternalThreat) minTurn =
                    minTSeriousExternalThreat
                if (maxTurn > maxTSeriousExternalThreat) maxTurn =
                    maxTSeriousExternalThreat
            } else {
                if (minTurn < minTNormalExternalThreat) minTurn = minTNormalExternalThreat
                if (maxTurn > maxTNormalExternalThreat) maxTurn = maxTNormalExternalThreat
            }
        }
        if (threat.threatPosition == Threat.THREAT_POSITION_INTERNAL) {
            if (threat.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
                if (minTurn < minTSeriousInternalThreat) minTurn =
                    minTSeriousInternalThreat
                if (maxTurn > maxTSeriousInternalThreat) maxTurn =
                    maxTSeriousInternalThreat
            } else {
                if (minTurn < minTNormalInternalThreat) minTurn = minTNormalInternalThreat
                if (maxTurn > maxTNormalInternalThreat) maxTurn = maxTNormalInternalThreat
            }
        }

        // create list of possible phases - find remaining possible phases and pick one
        val possibleTurns: MutableList<Int> = ArrayList()
        for (i in minTurn..maxTurn) {
            // if intenral threat and previous turn was internal, skip, reduce number of retries creating the mission
            if (threat.threatPosition == Threat.THREAT_POSITION_INTERNAL && (i - 1) in internalThreatTurns) {
                continue
            }

            // Add phase if no threats in this phase yet
            if (sortedThreats[i - 1] == null) {
                possibleTurns.add(i)
            }
            // add phase if double threats, and threat type not yet in phase
            else if (missionPreferences.enableDoubleThreats) {
                // don't allow two serious threats on the same turn
                if(threat.threatLevel == Threat.THREAT_LEVEL_SERIOUS && sortedThreats[i - 1]!!.hasSerious())
                    continue

                if (threat.threatPosition == Threat.THREAT_POSITION_INTERNAL && !sortedThreats[i - 1]!!.hasInternal()) {
                    possibleTurns.add(i)
                }
                if (threat.threatPosition == Threat.THREAT_POSITION_EXTERNAL && !sortedThreats[i - 1]!!.hasExternal()) {
                    possibleTurns.add(i)
                }
            }

        }

        // no possible phases left - giving up to continue again
        if (possibleTurns.size == 0) {
            Timber.i("Threat distribution failed - no possible phases left to put created threat into. Retrying.")
            return false
        }

        // pick random turn
        val turn = possibleTurns[generator.nextInt(possibleTurns.size)]

        // set threat time
        threat.time = turn
        // create threat Group is one does not exist for this phase, else add it
        if (sortedThreats[turn - 1] == null) {
            sortedThreats[turn - 1] = ThreatGroup(threat)
        } else {
            sortedThreats[turn - 1]!!.set(threat)
        }

        // if internal add to internal times
        if (threat.threatPosition == Threat.THREAT_POSITION_INTERNAL) {
            internalThreatTurns.add(turn)
        }

        // add threat score
        if (turn <= 4) threatsFirstPhase++ else threatsSecondPhase++
        return true
    }

    fun placeAndCheckThreats(
        internalThreats: ArrayList<Threat>,
        externalThreats: ArrayList<Threat>
    ): Boolean {
        // generate phases and distribute threats
        for (threat in internalThreats) {
            if (!insertThreat(threat)) return false
        }
        for (threat in externalThreats) {
            if (!insertThreat(threat)) return false
        }

        // check sanity of distributions of threats among phase 1 and 2
        if (abs(threatsFirstPhase - threatsSecondPhase) > 1) {
            Timber.i("Threat distribution failed - not balanced enough. Retrying.")
            return false // the distribution should be equal
        }
        // generate attack sectors
        var lastSector = -1 // to not generate same sectors twice
        var lastThreatWasInternal =
            false // sanity check if there are two internal threats in a row - if there are, retry mission

        // sanity check if there are two unconfirmed threats in a row - if there are, retry mission.
        // This only checks if there are two single unconfirmed in a row, a double threat with one unconfirmed does not count
        var lastThreatHasConfirmed = true

        for (i in 0..7) {
            val currentThreatGroup = sortedThreats[i]
            var hasConfirmed = false
            var hasInternal = false
            if (currentThreatGroup != null) {
                var t: Threat? = currentThreatGroup.external
                if (t != null) {
                    if (t.isConfirmed) hasConfirmed = true
                    when (generator.nextInt(3)) {
                        0 -> if (lastSector != Threat.THREAT_SECTOR_BLUE) t.sector =
                            Threat.THREAT_SECTOR_BLUE else t.sector = Threat.THREAT_SECTOR_WHITE

                        1 -> if (lastSector != Threat.THREAT_SECTOR_WHITE) t.sector =
                            Threat.THREAT_SECTOR_WHITE else t.sector = Threat.THREAT_SECTOR_RED

                        2 -> if (lastSector != Threat.THREAT_SECTOR_RED) t.sector =
                            Threat.THREAT_SECTOR_RED else t.sector = Threat.THREAT_SECTOR_BLUE
                    }
                    lastSector = t.sector
                }
                t = currentThreatGroup.internal
                if (t != null) {
                    if (t.isConfirmed) hasConfirmed = true
                    hasInternal = true
                    if (lastThreatWasInternal) {
                        Timber.i("Two internal threats in a row. Retrying.")
                        return false
                    }
                }
                lastThreatWasInternal = hasInternal
                // Do check for two unconfirmed in a row
                if (!hasConfirmed && !lastThreatHasConfirmed) {
                    Timber.i("Two unconfirmed threats in a row. Retrying.")
                    return false
                }
                lastThreatHasConfirmed = hasConfirmed
            } else {
                // add empty group to not have NPEs later on - this is not so elegant and might be subject to refactoring at some time...
                sortedThreats[i] = ThreatGroup()
            }
        }
        return true
    }

    fun getRandomSeriousThreatIndex(): Int? {
        var idx = firstSecondPhase[generator.nextInt(firstSecondPhase.size)]
        var loopArray = firstSecondPhase

        if(unconfirmedThreatsFirstPhase > 0){
            idx = generator.nextInt(secondPhase.size)
            loopArray = secondPhase
        }
        else if(unconfirmedThreatsSecondPhase > 0){
            idx = generator.nextInt(firstPhase.size)
            loopArray = firstPhase
        }

        var tries = 10
        do {
            if (sortedThreats[loopArray[idx % loopArray.size]]?.hasSerious() == true)
                return loopArray[idx % loopArray.size]
            idx++
        } while (tries-- > 0)
        return null
    }

    fun getRandomNormalThreatIndex(): Int? {
        var idx = firstSecondPhase[generator.nextInt(firstSecondPhase.size)]
        var loopArray = firstSecondPhase

        if(unconfirmedThreatsFirstPhase > 0){
            idx = generator.nextInt(secondPhase.size)
            loopArray = secondPhase
        }
        else if(unconfirmedThreatsSecondPhase > 0){
            idx = generator.nextInt(firstPhase.size)
            loopArray = firstPhase
        }

        var tries = 10
        do {
            if (sortedThreats[loopArray[idx % loopArray.size]]?.hasNormal() == true)
                return loopArray[idx % loopArray.size]
            idx++
        } while (tries-- > 0)
        return null
    }

    fun makeSeriousUnconfirmed(): Boolean {
        val idx = getRandomSeriousThreatIndex() ?: return false
        if (idx <= 3) unconfirmedThreatsFirstPhase++ else unconfirmedThreatsSecondPhase++
        if (sortedThreats[idx]?.internal?.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
            sortedThreats[idx]!!.internal!!.isConfirmed = false
            return true
        } else if (sortedThreats[idx]?.external?.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
            sortedThreats[idx]!!.external!!.isConfirmed = false
            return true
        }
        return false
    }

    fun makeNormalUnconfirmed(): Boolean {
        val idx = getRandomNormalThreatIndex() ?: return false
        if (idx <= 3) unconfirmedThreatsFirstPhase++ else unconfirmedThreatsSecondPhase++
        // if threat turn has two normal threats, pick one at random
        if (sortedThreats[idx]?.hasInternal() == true
            && sortedThreats[idx]?.hasExternal() == true
            && sortedThreats[idx]?.internal?.threatLevel == Threat.THREAT_LEVEL_NORMAL
            && sortedThreats[idx]?.external?.threatLevel == Threat.THREAT_LEVEL_NORMAL) {
            if (generator.nextBoolean()) {
                sortedThreats[idx]!!.internal!!.isConfirmed = false
                return true
            } else {
                sortedThreats[idx]!!.external!!.isConfirmed = false
                return true
            }
        }
        // else, find the normal threat, and set it to unconfirmed
        if (sortedThreats[idx]?.internal?.threatLevel == Threat.THREAT_LEVEL_NORMAL) {
            sortedThreats[idx]!!.internal!!.isConfirmed = false
            return true
        } else if (sortedThreats[idx]?.external?.threatLevel == Threat.THREAT_LEVEL_NORMAL) {
            sortedThreats[idx]!!.external!!.isConfirmed = false
            return true
        }
        return false
    }

    /**
     * "sane" generator method for threats
     *
     * @return true if generation was successful
     */
    fun generateThreats(): Array<ThreatGroup?> {
        val tg = BasicThreatGenerator(missionPreferences, generator)

        // initialize numbers - might fail, then we return false to try again
        if (!tg.initializeThreatNumbers()) {
            Timber.i("Threat initialization failed. Retrying.")
            return emptyArray()
        }

        // generate the basic threats
        var internalThreats: ArrayList<Threat> = tg.generateThreats(Threat.THREAT_POSITION_INTERNAL)
        var externalThreats: ArrayList<Threat> = tg.generateThreats(Threat.THREAT_POSITION_EXTERNAL)

        var tries = 20 //maximum number of tries to place threats
        do {
            sortedThreats = Array<ThreatGroup?>(8) { null }
            threatsFirstPhase = 0
            threatsSecondPhase = 0
            unconfirmedThreatsFirstPhase = 0
            unconfirmedThreatsSecondPhase = 0
            internalThreatTurns = ArrayList<Int>()
            if (placeAndCheckThreats(internalThreats, externalThreats)) {
                // mark some of the threats unconfirmed
                for (i in 0 until tg.seriousUnconfirmed) {
                    makeSeriousUnconfirmed()
                }
                for (i in 0 until tg.normalUnconfirmed) {
                    makeNormalUnconfirmed()
                }
                return sortedThreats
            }
        } while (tries-- > 0)

        return emptyArray()
    }

}


/**
 * Inner class to facilitate basic threat generation
 */
class BasicThreatGenerator(
    private val missionPreferences: MissionPreferences,
    val generator: Random
) {
    var threatLevel: Int = missionPreferences.threatLevel
    var threatUnconfirmed: Int = missionPreferences.threatUnconfirmed

    // counters for threats by level, class, type, etc.
    var internalThreatsValue = 0
    var externalThreatsValue = 0
    var seriousInternalThreats = 0
    var seriousExternalThreats = 0
    var normalInternalThreats = 0
    var normalExternalThreats = 0
    var seriousUnconfirmed = 0
    var normalUnconfirmed = 0
    var maxExternalSerious = 0

    /**
     * Initialize threat numbers
     *
     * @return false if something goes wrong
     */
    fun initializeThreatNumbers(): Boolean {
        if (threatLevel > 10)
            internalThreatsValue =
                generator.nextInt(maxInternalThreatsDouble - minInternalThreatsDouble + 1) + minInternalThreatsDouble
        else
            internalThreatsValue =
                generator.nextInt(maxInternalThreatsSingle - minInternalThreatsSingle + 1) + minInternalThreatsSingle

        externalThreatsValue = threatLevel - internalThreatsValue


        // generate number of serious/normal threats
        seriousInternalThreats = generator.nextInt(internalThreatsValue / 2 + 1)
        normalInternalThreats = internalThreatsValue - seriousInternalThreats * 2

        // ensure maxNormalInternalThreats is adhered to
        while (normalInternalThreats > maxNormalInternalThreats) {
            normalInternalThreats -= 2
            seriousInternalThreats += 1
        }

        // calculate the maximum number of serious external, given the fact, that the total serious threats should only make up, maximum, half the threat count
        while ((maxExternalSerious + seriousInternalThreats) <
            internalThreats() + maxExternalSerious + (externalThreatsValue - maxExternalSerious * 2)
        ) {
            maxExternalSerious++
        }
        // ensure that the maximum external serious threats, does not exceed our threat value
        maxExternalSerious = min(maxExternalSerious, externalThreatsValue / 2)
        seriousExternalThreats = generator.nextInt(maxExternalSerious)
        normalExternalThreats = externalThreatsValue - seriousExternalThreats * 2

        // If we have more than 8 external threats, then make some of them serious
        while (normalExternalThreats + seriousExternalThreats > 8) {
            normalExternalThreats -= 2
            seriousExternalThreats += 1
        }

        Timber.v("Threat Level: $threatLevel; normal internal = $normalInternalThreats, serious internal = $seriousInternalThreats, normal external = $normalExternalThreats, serious external = $seriousExternalThreats")

        // Ensure the ratio of serious to normal threats is ok (at most half serious threats, rounded up)
        // This shouldn't happen due to how we generate threat numbers
        while (seriousThreats() > ceil(threatSum().toDouble() / 2)) {
            Timber.i("Too many serious threats. Redoing.")
            return false
        }

        // distribute unconfirmed
        seriousUnconfirmed = generator.nextInt(threatUnconfirmed / 2 + 1)
        normalUnconfirmed = threatUnconfirmed - seriousUnconfirmed * 2

        Timber.v("Normal unconfirmed Threats: $normalUnconfirmed; Serious unconfirmed Threats: $seriousUnconfirmed")
        return true
    }

    fun seriousThreats(): Int {
        return seriousExternalThreats + seriousInternalThreats
    }

    fun normalThreats(): Int {
        return normalExternalThreats + normalInternalThreats
    }

    fun internalThreats(): Int {
        return normalInternalThreats + seriousInternalThreats
    }

    fun externalThreats(): Int {
        return normalExternalThreats + seriousExternalThreats
    }

    fun threatSum(): Int {
        return internalThreats() + externalThreats()
    }

    /**
     * Actually generate threats
     *
     * @return generated threats
     */
    fun generateThreats(threatPosition: Int): ArrayList<Threat> {
        val threats = ArrayList<Threat>()
        var seriousThreatsGenerate = seriousInternalThreats
        var normalThreatsGenerate = normalInternalThreats

        if (threatPosition == Threat.THREAT_POSITION_EXTERNAL) {
            seriousThreatsGenerate = seriousExternalThreats
            normalThreatsGenerate = normalExternalThreats
        }

        // create serious threats
        for (i in 0 until seriousThreatsGenerate) {
            val newThreat = Threat() // new threat created
            newThreat.isConfirmed = true
            newThreat.threatLevel = Threat.THREAT_LEVEL_SERIOUS
            newThreat.threatPosition = threatPosition

            threats.add(newThreat)
        }

        // create normal threats
        for (i in 0 until normalThreatsGenerate) {
            val newThreat = Threat() // new threat created
            newThreat.isConfirmed = true
            newThreat.threatLevel = Threat.THREAT_LEVEL_NORMAL
            newThreat.threatPosition = threatPosition

            threats.add(newThreat)
        }
        return threats
    }
}

