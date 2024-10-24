package com.boarbeard.generator.beimax

import com.boarbeard.generator.beimax.event.Threat
import timber.log.Timber
import java.util.*
import kotlin.math.abs


/**
 * ...of which x levels are internal
 */
private const val minInternalThreats = 1
private const val maxInternalThreats = 3
private const val maxInternalThreatsNumber = 2 // number of internal threats max

/**
 * minimum and maximum time in which normal threats can occur
 */
private const val minTNormalExternalThreat = 1
private const val maxTNormalExternalThreat = 8

/**
 * minimum and maximum time in which serious threats can occur
 */
private const val minTSeriousExternalThreat = 2
private const val maxTSeriousExternalThreat = 7

/**
 * minimum and maximum time in which normal threats can occur
 */
private const val minTNormalInternalThreat = 2
private const val maxTNormalInternalThreat = 7

/**
 * minimum and maximum time in which serious threats can occur
 */
private const val minTSeriousInternalThreat = 3
private const val maxTSeriousInternalThreat = 6


class ThreatsGenerator(
    private val missionPreferences: MissionPreferences,
    val generator: Random
) {
    /**
     * "sane" generator method for threats
     *
     * @return true if generation was successful
     */
    fun generateThreats(): Array<ThreatGroup> {
        val tg = BasicThreatGenerator(missionPreferences, generator)

        // initialize numbers - might fail, then we return false to try again
        if (!tg.initializeThreatNumbers()) {
            Timber.i("Threat initialization failed. Retrying.")
            return emptyArray()
        }

        // generate the basic threats
        val threats = tg.generateThreats()

        // keeps number of threats each phase - used to check sanity further down
        var threatsFirstPhase = 0
        var threatsSecondPhase = 0

        // generate phases and distribute threats
        val sortedThreats = arrayOfNulls<ThreatGroup>(8)
        for (threatGroup in threats) {
            // for each threat group, set min and max phases
            var minPhase = 1
            var maxPhase = 8
            val externalThreat = threatGroup.external
            if (externalThreat != null) {
                if (externalThreat.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
                    if (minPhase < minTSeriousExternalThreat) minPhase =
                        minTSeriousExternalThreat
                    if (maxPhase > maxTSeriousExternalThreat) maxPhase =
                        maxTSeriousExternalThreat
                } else {
                    if (minPhase < minTNormalExternalThreat) minPhase = minTNormalExternalThreat
                    if (maxPhase > maxTNormalExternalThreat) maxPhase = maxTNormalExternalThreat
                }
            }
            val internalThreat = threatGroup.internal
            if (internalThreat != null) {
                if (internalThreat.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
                    if (minPhase < minTSeriousInternalThreat) minPhase =
                        minTSeriousInternalThreat
                    if (maxPhase > maxTSeriousInternalThreat) maxPhase =
                        maxTSeriousInternalThreat
                } else {
                    if (minPhase < minTNormalInternalThreat) minPhase = minTNormalInternalThreat
                    if (maxPhase > maxTNormalInternalThreat) maxPhase = maxTNormalInternalThreat
                }
            }

            // create list of possible phases - find remaining possible phases and pick one
            val possiblePhases = mutableListOf<Int>()
            for (i in minPhase..maxPhase) {
                if (sortedThreats[i - 1] == null) possiblePhases.add(i)
            }

            // no possible phases left - giving up to continue again
            if (possiblePhases.size == 0) {
                Timber.i("Threat distribution failed - no possible phases left to put created threat into. Retrying.")
                return emptyArray()
            }

            // pick random phase
            val phase = possiblePhases[generator.nextInt(possiblePhases.size)]

            // set stuff
            if (externalThreat != null) externalThreat.time = phase
            if (internalThreat != null) internalThreat.time = phase
            sortedThreats[phase - 1] = threatGroup

            // add threat score
            if (externalThreat != null && internalThreat != null) {
                if (phase <= 4) threatsFirstPhase += 2 else threatsSecondPhase += 2
            } else {
                if (phase <= 4) threatsFirstPhase++ else threatsSecondPhase++
            }
        }

        // check sanity of distributions of threats among phase 1 and 2
        if (abs(threatsFirstPhase - threatsSecondPhase) > 1) {
            Timber.i("Threat distribution failed - not balanced enough. Retrying.")
            return emptyArray() // the distribution should be equal
        }

        // generate attack sectors
        var lastSector = -1 // to not generate same sectors twice
        var lastThreatWasInternal =
            false // sanity check if there are two internal threats in a row - if there are, retry mission
        sortedThreats.forEachIndexed { index, threatGroup ->
            val currentThreatGroup = sortedThreats[index]
            if (currentThreatGroup != null) {
                var t: Threat? = currentThreatGroup.external
                if (t != null) {
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
                    if (lastThreatWasInternal) {
                        Timber.i("Two internal threats in a row. Retrying.")
                        return emptyArray()
                    }
                    lastThreatWasInternal = true
                }
            } else {
                // add empty group to not have NPEs later on - this is not so elegant and might be subject to refactoring at some time...
                sortedThreats[index] = ThreatGroup()
            }
        }
        return sortedThreats.filterNotNull().toTypedArray()
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
    var enableDoubleThreats: Boolean = missionPreferences.enableDoubleThreats

    // counters for threats by level, class, type, etc.
    var internalThreats = 0
    var externalThreats = 0
    var seriousThreats = 0
    var normalThreats = 0
    var seriousUnconfirmed = 0
    var normalUnconfirmed = 0
    var threatsSum = 0

    /**
     * Initialize threat numbers
     *
     * @return false if something goes wrong
     */
    fun initializeThreatNumbers(): Boolean {
        internalThreats =
            generator.nextInt(maxInternalThreats - minInternalThreats + 1) + minInternalThreats
        externalThreats = threatLevel - internalThreats
        Timber.v("Threat Level: $threatLevel; internal = $internalThreats, external = $externalThreats")

        // generate number of serious threats
        seriousThreats = generator.nextInt(threatLevel / 2 + 1)
        // if we only have serious threats and normal unconfirmed reports: reduce number of threats by 1
        if (threatUnconfirmed % 2 == 1 && seriousThreats * 2 == threatLevel) seriousThreats--
        normalThreats = threatLevel - seriousThreats * 2
        Timber.v("Normal Threats: $normalThreats; Serious Threats: $seriousThreats")

        // if there are 8 normal threats - check again, if we really want this
        if (normalThreats >= 8 && generator.nextInt(3) != 0) {
            Timber.i("8 or more normal threats unlikely. Redoing.")
            return false
        }
        if ((seriousThreats == threatLevel / 2 || seriousThreats >= 5) && generator.nextInt(3) != 0) {
            Timber.i("all (or 5 or more) serious threats unlikely. Redoing.")
            return false
        }

        // get sums
        threatsSum = normalThreats + seriousThreats

        // if threat level is higher than 8, create serious threats until we have a threat level of 8 or lower
        // thanks to Leif Norcott from BoardGameGeek
        while (threatsSum > 8) {
            normalThreats -= 2
            seriousThreats++
            threatsSum = normalThreats + seriousThreats
        }

        // special case: if we have enableDoubleThreats and only have serious threats -> convert one of them to 2 normal threats
        if (enableDoubleThreats && normalThreats == 0) {
            seriousThreats -= 1
            normalThreats += 2
            threatsSum = normalThreats + seriousThreats
        }

        // distribute unconfirmed
        seriousUnconfirmed = generator.nextInt(threatUnconfirmed / 2 + 1)
        normalUnconfirmed = threatUnconfirmed - seriousUnconfirmed * 2
        if (normalUnconfirmed > normalThreats) { // adjust, if there are not enough threats
            normalUnconfirmed -= 2
            seriousUnconfirmed++
        } else if (seriousUnconfirmed > seriousThreats) { // adjust, if there are not enough serious threats
            normalUnconfirmed += 2
            seriousUnconfirmed--
        }

        Timber.v("Normal unconfirmed Threats: $normalUnconfirmed; Serious unconfirmed Threats: $seriousUnconfirmed")
        return true
    }

    /**
     * helper to add normal threat
     *
     * @param t Threat
     */
    fun normalThreatAdded(t: Threat) {
        normalThreats--
        t.threatLevel = Threat.THREAT_LEVEL_NORMAL
        t.isConfirmed = true
    }

    /**
     * helper to add normal unconfirmed threat
     *
     * @param t Threat
     */
    fun normalUnconfirmedThreatAdded(t: Threat) {
        normalUnconfirmed--
        normalThreats--
        t.threatLevel = Threat.THREAT_LEVEL_NORMAL
    }

    /**
     * helper to add serious threat
     *
     * @param t Threat
     */
    fun seriousThreatAdded(t: Threat) {
        seriousThreats--
        t.threatLevel = Threat.THREAT_LEVEL_SERIOUS
        t.isConfirmed = true
    }

    /**
     * helper to add serious unconfirmed threat
     *
     * @param t Threat
     */
    fun seriousUnconfirmedThreatAdded(t: Threat) {
        seriousUnconfirmed--
        seriousThreats--
        t.threatLevel = Threat.THREAT_LEVEL_SERIOUS
    }

    /**
     * helper to add internal threat
     *
     * @param t Threat
     */
    fun internalThreatAdded(t: Threat) {
        internalThreats -= if (t.threatLevel == Threat.THREAT_LEVEL_SERIOUS) 2 else 1
        t.threatPosition = Threat.THREAT_POSITION_INTERNAL
    }

    /**
     * helper to add external threat
     *
     * @param t Threat
     */
    fun externalThreatAdded(t: Threat) {
        externalThreats -= if (t.threatLevel == Threat.THREAT_LEVEL_SERIOUS) 2 else 1
        t.threatPosition = Threat.THREAT_POSITION_EXTERNAL
    }

    /**
     * Actually generate threats
     *
     * @return generated threats
     */
    fun generateThreats(): Array<ThreatGroup> {
        val enableDoubleThreats: Boolean = missionPreferences.enableDoubleThreats
        val threats =
            arrayOfNulls<ThreatGroup>(if (enableDoubleThreats) threatsSum - 1 else threatsSum)
        var threatIdx = 0 // current id in above array

        // if we have a double threat, create this first
        if (enableDoubleThreats) {
            val newThreat = Threat() // new threat created
            // confirmed or unconfirmed?
            if (missionPreferences.showUnconfirmed && generator.nextInt(threatsSum) + 1 > missionPreferences.threatUnconfirmed) {
                if (generator.nextInt(normalUnconfirmed + seriousUnconfirmed) + 1 <= normalUnconfirmed) {
                    normalUnconfirmedThreatAdded(newThreat)
                } else {
                    seriousUnconfirmedThreatAdded(newThreat)
                }
            } else { // normal threats aka confirmed
                // serious or not?
                if (generator.nextInt(normalThreats + seriousThreats - normalUnconfirmed - seriousUnconfirmed) + 1 <= normalThreats - normalUnconfirmed) {
                    normalThreatAdded(newThreat)
                } else {
                    seriousThreatAdded(newThreat)
                }
            }

            // internal or external?
            if (internalThreats > 1 && newThreat.threatLevel == Threat.THREAT_LEVEL_SERIOUS) { // number must be greater to work
                // internal/external?
                if (generator.nextInt(externalThreats + internalThreats) + 1 <= externalThreats) {
                    externalThreatAdded(newThreat)
                } else {
                    internalThreatAdded(newThreat)
                }
            } else {
                // create external
                newThreat.threatLevel = Threat.THREAT_LEVEL_NORMAL
                externalThreatAdded(newThreat)
            }

            // create second threat
            val newThreat2 = Threat() // new threat created
            newThreat2.isConfirmed = true // second threat is always confirmed

            // add new threat group with two elements
            val g = ThreatGroup(newThreat)
            newThreat2.threatPosition =
                if (newThreat.threatPosition == Threat.THREAT_POSITION_INTERNAL) Threat.THREAT_POSITION_EXTERNAL else Threat.THREAT_POSITION_INTERNAL
            g.set(newThreat2)

            // now check second threat level
            if (newThreat.threatLevel != Threat.THREAT_LEVEL_SERIOUS && seriousThreats > 0) {
                // not serious and serious threats left -> second might be serious
                if (generator.nextInt(normalThreats + seriousThreats - normalUnconfirmed - seriousUnconfirmed) + 1 <= normalThreats - normalUnconfirmed) {
                    newThreat2.threatLevel = Threat.THREAT_LEVEL_NORMAL
                } else {
                    newThreat2.threatLevel = Threat.THREAT_LEVEL_SERIOUS
                }
            } else {
                // second is always normal
                newThreat2.threatLevel = Threat.THREAT_LEVEL_NORMAL
            }

            // adjust levels
            if (newThreat2.threatLevel == Threat.THREAT_LEVEL_SERIOUS) {
                seriousThreatAdded(newThreat2)
            } else {
                normalThreatAdded(newThreat2)
            }
            if (newThreat2.threatPosition == Threat.THREAT_POSITION_INTERNAL) {
                internalThreatAdded(newThreat2)
            } else {
                externalThreatAdded(newThreat2)
            }
            threats[threatIdx++] = g
        }

        // create serious threats
        for (i in 0 until seriousThreats) {
            val newThreat = Threat() // new threat created
            // unconfirmed or confirmed?
            newThreat.isConfirmed = i >= seriousUnconfirmed
            newThreat.threatLevel = Threat.THREAT_LEVEL_SERIOUS

            // internal or external threat?
            if (internalThreats > 1 && generator.nextInt(externalThreats + internalThreats) + 1 > externalThreats) {
                internalThreatAdded(newThreat)
            } else {
                externalThreatAdded(newThreat)
            }
            threats[threatIdx++] = ThreatGroup(newThreat)
        }

        // create normal threats
        for (i in 0 until normalThreats) {
            val newThreat = Threat() // new threat created
            // unconfirmed or confirmed?
            newThreat.isConfirmed = i >= normalUnconfirmed
            newThreat.threatLevel = Threat.THREAT_LEVEL_NORMAL

            // internal/external?
            if (generator.nextInt(externalThreats + internalThreats) + 1 > externalThreats) {
                internalThreatAdded(newThreat)
            } else {
                externalThreatAdded(newThreat)
            }
            threats[threatIdx++] = ThreatGroup(newThreat)
        }

        return threats.filterNotNull().toTypedArray()
    }
}

