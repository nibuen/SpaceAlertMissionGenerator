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

import timber.log.Timber

data class WhiteNoisePreferences(
    val whiteNoiseTotalRange: IntRange = 45..60,
    val whiteNoiseEventTimeRange: IntRange = 9..20
)

/**
 * @author mkalus, leifnorcott
 */
class WhiteNoise
/**
 * Constructor
 *
 * of the white noise in seconds
 */(
    /**
     * length in seconds
     */
    override val lengthInSeconds: Int
) : Event {
    override val timeColor: String = "#80A9BC"
    override val textColor: String = "#A8A9A8"

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

    companion object {
        fun generateEvent(prefs: WhiteNoisePreferences): List<Int> {
            // generate white noise
            var whiteNoiseTime = prefs.whiteNoiseTotalRange.random()
            Timber.tag("generateTimes()").v("White noise time: %d", whiteNoiseTime)

            // create chunks
            val whiteNoiseChunks = arrayListOf<Int>()
            while (whiteNoiseTime > 0) {
                // create random chunk
                val chunkLength = prefs.whiteNoiseEventTimeRange.random()
                // check if there is enough time left
                if (chunkLength > whiteNoiseTime) {
                    // hard case: smaller than minimum time
                    if (chunkLength < prefs.whiteNoiseTotalRange.min()) {
                        // add to last chunk that fits
                        for (i in whiteNoiseChunks.indices.reversed()) {
                            val sumChunk = whiteNoiseChunks[i] + chunkLength
                            // if smaller than maximum time: add to this chunk
                            if (sumChunk <= prefs.whiteNoiseTotalRange.max()) {
                                whiteNoiseChunks[i] = sumChunk
                                whiteNoiseTime = 0
                                break
                            }
                        }
                        // still not zeroed
                        if (whiteNoiseTime > 0) { // add to last element, regardless - quite unlikely though
                            val lastIdx = whiteNoiseChunks.size - 1
                            whiteNoiseChunks[lastIdx] = whiteNoiseChunks[lastIdx] + chunkLength
                            whiteNoiseTime = 0
                        }
                    } else { // easy case: create smaller rest chunk
                        whiteNoiseChunks.add(whiteNoiseTime)
                        whiteNoiseTime = 0
                    }
                } else { // add new chunk
                    whiteNoiseChunks.add(chunkLength)
                    whiteNoiseTime -= chunkLength
                }
            }

            return whiteNoiseChunks
        }
    }
}