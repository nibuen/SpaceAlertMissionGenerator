/**
 * This file is part of the JSpaceAlertMissionGenerator software.
 * Copyright (C) 2011 Maximilian Kalus
 * See http://www.beimax.de/ and https://github.com/mkalus/JSpaceAlertMissionGenerator
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.boarbeard.generator.beimax.event

import com.boarbeard.generator.beimax.event.Threat

/**
 * Threat class
 *
 * @author mkalus
 */
class Threat : Event {
    /** Enum wrapper around THREAT_SECTOR_* constants.  */
    enum class Zone(val sector: Int) {
        Blue(THREAT_SECTOR_BLUE), White(THREAT_SECTOR_WHITE), Red(THREAT_SECTOR_RED);
    }

    /**
     * assumed time in seconds: 10
     */
    override val lengthInSeconds: Int
        get() = 15
    /**
     * @return the threatLevel
     */
    /**
     * @param threatLevel
     * the threatLevel to set
     */
    /**
     * Threat level
     */
    var threatLevel = 0
    /**
     * @return the threatPosition
     */
    /**
     * @param threatPosition
     * the threatPosition to set
     */
    /**
     * internal/external threat
     */
    var threatPosition = 0
    /**
     * @return the sector
     */
    /**
     * @param sector
     * the sector to set
     */
    /**
     * sector of threat
     */
    var sector = 0
    /**
     * @return the time
     */
    /**
     * @param time
     * the time to set
     */
    /**
     * time in which threat appears
     */
    var time = 0
    /**
     * @return the confirmed
     */
    /**
     * @param confirmed
     * the confirmed to set
     */
    /**
     * confirmed threat?
     */
    var isConfirmed = false

    constructor()

    /**
     * Copy constructor.
     */
    constructor(other: Threat) {
        threatLevel = other.threatLevel
        threatPosition = other.threatPosition
        sector = other.sector
        time = other.time
        isConfirmed = other.isConfirmed
    }

    /**
     * Creates an internal threat.
     */
    constructor(time: Int, confirmed: Boolean, serious: Boolean) {
        this.time = time
        isConfirmed = confirmed
        threatPosition = THREAT_POSITION_INTERNAL
        threatLevel = if (serious) THREAT_LEVEL_SERIOUS else THREAT_LEVEL_NORMAL
    }

    /**
     * Creates an external threat.
     *
     * @param zone must not be null.
     */
    constructor(time: Int, confirmed: Boolean, serious: Boolean, zone: Zone) {
        this.time = time
        isConfirmed = confirmed
        threatPosition = THREAT_POSITION_EXTERNAL
        threatLevel = if (serious) THREAT_LEVEL_SERIOUS else THREAT_LEVEL_NORMAL
        sector = zone.sector
    }

    override val timeColor: String
        get() = if (threatPosition == THREAT_POSITION_INTERNAL) {
            "#4CB847"
        } else {
            when (sector) {
                THREAT_SECTOR_BLUE -> "#00ADEE"
                THREAT_SECTOR_WHITE -> "#FFFFFF"
                THREAT_SECTOR_RED -> "#EE1C25"
                else -> "#FFFFFF"
            }
        }
    override val textColor: String
        get() = if (threatPosition == THREAT_POSITION_INTERNAL) {
            "#4CB847"
        } else {
            "#BC8CBF"
        }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return ("Threat [isConfirmed()=" + isConfirmed + ", getTime()="
                + time + ", getThreatLevel()=" + threatLevel
                + ", getThreatPosition()=" + threatPosition
                + ", getSector()=" + sector + ", getLengthInSeconds()="
                + lengthInSeconds + ", getTimeColor()=" + timeColor
                + ", getTextColor()=" + textColor + "]")
    }

    companion object {
        const val THREAT_LEVEL_NORMAL = 1
        const val THREAT_LEVEL_SERIOUS = 2
        const val THREAT_POSITION_INTERNAL = 1
        const val THREAT_POSITION_EXTERNAL = 2
        const val THREAT_SECTOR_BLUE = 1
        const val THREAT_SECTOR_WHITE = 2
        const val THREAT_SECTOR_RED = 3
    }
}