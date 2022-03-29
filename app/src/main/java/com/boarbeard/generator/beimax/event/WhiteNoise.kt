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

/**
 * @author mkalus
 */
class WhiteNoise
/**
 * Constructor
 *
 * @param length
 * of the white noise in seconds
 */(
    /**
     * length in seconds
     */
    override val lengthInSeconds: Int
) : Event {
    /**
     * white noise time is the the time it takes...
     */
    override val timeColor: String
        get() = "#80A9BC"
    override val textColor: String
        get() = "#A8A9A8"

    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return ("WhiteNoise [getLengthInSeconds()=" + lengthInSeconds
                + ", getTimeColor()=" + timeColor + ", getTextColor()="
                + textColor + "]")
    }
}