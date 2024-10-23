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
package com.boarbeard.generator.beimax

import com.boarbeard.generator.beimax.event.Announcement
import com.boarbeard.generator.beimax.event.Event
import com.boarbeard.generator.beimax.event.Threat
import com.boarbeard.generator.beimax.event.WhiteNoise
import com.boarbeard.generator.beimax.event.WhiteNoiseRestored
import timber.log.Timber
import java.util.TreeMap

fun fit(attempts: Int = 100000, runner: (currentAttempt: Int) -> Boolean): Boolean {
    var done: Boolean
    var currentAttempts = 0
    do {
        currentAttempts++
        done = runner.invoke(currentAttempts)
    } while (!done && currentAttempts < attempts)

    return done
}

/**
 * Keeps mission events in an ordered state and also checks for collisions and
 * the like
 *
 * @author mkalus, leifnorcott
 */
class EventList {
    /**
     * actual time table for this mission
     */
    var events: TreeMap<Int, Event>

    /**
     * Constructor
     */
    constructor() {
        events = TreeMap()
    }

    /**
     * Copy constructor.  This just does a shallow copy of the tree; the events
     * in the new tree will still be references to the events in the original
     * tree.
     *
     * @param other must not be null
     */
    constructor(other: EventList) {
        events = TreeMap(other.events.comparator())
        events.putAll(other.events)
    }

    /**
     * Add announcements
     *
     * @param phase1
     * - length of phase 1
     * @param phase2
     * - length of phase 2
     * @param phase3
     * - length of phase 3
     */
    fun addPhaseEvents(phase1: Int, phase2: Int, phase3: Int) {
        addEvent(0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
        var a = Announcement(
            Announcement.ANNOUNCEMENT_PH1_ONEMINUTE
        )
        addEvent(phase1 - 60 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
        addEvent(phase1 - 20 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS)
        addEvent(phase1 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE)
        addEvent(phase1 + phase2 - 60 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS)
        addEvent(phase1 + phase2 - 20 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS)
        addEvent(phase1 + phase2 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
        addEvent(phase1 + phase2 + phase3 - 60 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
        addEvent(phase1 + phase2 + phase3 - 20 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS)
        addEvent(phase1 + phase2 + phase3 - a.lengthInSeconds, a)
    }

    /**
     * Add announcements
     *
     * @param phase1
     * - length of phase 1
     * @param phase2
     * - length of phase 2
     */
    fun addPhaseEvents(phase1: Int, phase2: Int) {
        addEvent(0, Announcement(Announcement.ANNOUNCEMENT_PH1_START))
        var a = Announcement(
            Announcement.ANNOUNCEMENT_PH1_ONEMINUTE
        )
        addEvent(phase1 - 60 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS)
        addEvent(phase1 - 20 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS)
        addEvent(phase1 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE)
        addEvent(phase1 + phase2 - 60 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS)
        addEvent(phase1 + phase2 - 20 - a.lengthInSeconds, a)
        a = Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS)
        addEvent(phase1 + phase2 - a.lengthInSeconds, a)
    }

    // Attempts to add a White Noise Event
    // This adds TWO events, WhiteNoise and WhiteNoiseRestored
    fun addWhiteNoiseEvents(time: Int, length: Int): Boolean {
        val returnValue1: Boolean
        val returnValue2: Boolean
        val whiteNoise: Event = WhiteNoise(length)
        val whiteNoiseRestored: Event = WhiteNoiseRestored()

        if (!checkTime(
                time, whiteNoise.lengthInSeconds
                        + whiteNoiseRestored.lengthInSeconds
            )
        ) return false

        returnValue1 = addEvent(time, whiteNoise)

        returnValue2 = addEvent(
            time + whiteNoise.lengthInSeconds,
            whiteNoiseRestored
        )

        if (!returnValue1) {
            Timber.e("retunValue1 was false!")
        }
        if (!returnValue2) {
            Timber.e("retunValue1 was false!")
        }

        return returnValue1 && returnValue2
    }

    /**
     * attempts to add event at time
     *
     * @param time
     * @param event
     * @return false, if a collision was detected
     */
    fun addEvent(time: Int, event: Event): Boolean {
        if (!checkTime(time, event.lengthInSeconds)) {
            return false
        }

        events[time] = event
        return true
    }

    /**
     * Checks whether a certain time slot is free
     * @param time
     * @param length in seconds
     * @return false, if time slot is not free
     */
    private fun checkTime(time: Int, length: Int): Boolean {
        // if empty set, you can add stuff
        if (events.isEmpty()) return true

        // lowest or highest keys?
        val lowest = events.firstKey()
        if (lowest > time) { // there is no key before?
            return time + length <= lowest // too long
        }
        val highest = events.lastKey()
        if (highest < time + length) return true

        // ok, we are in between somewhere - check event before
        var after = -1
        events.floorEntry(time)?.let { before ->
            val endTime = before.key + before.value.lengthInSeconds
            if (endTime > time) return false

            // check event after
            after = events.ceilingKey(before.key + 1) ?: -1
        }

        return time + length <= after
    }

    /**
     * Remove Unconfirmed Reports, or replace them with normal (confirmed)
     * threats.
     *
     * @param replace true if they should be replaced with normal threats; false
     * if they should be removed from the list.
     */
    fun stompUnconfirmedReports(replace: Boolean) {
        val toRemove = if (replace) null else mutableListOf<Int>()
        events.entries.forEach { event ->
            val threat = event.value as? Threat
            if (threat?.isConfirmed == false) {
                if (replace) {
                    //  We don't want to fiddle with the existing Threat instance
                    //  itself (as it may be shared by another instance of this
                    //  MissionType), so copy it.
                    event.setValue(Threat(threat).apply { isConfirmed = true })
                } else {
                    toRemove?.add(event.key)
                }
            }
        }
        toRemove?.forEach { key -> events.remove(key) }
    }

    /**
     * This is a ridiculous debugging method which removes all gaps between
     * events, so that you can listen to a complete soundtrack in as little
     * time as possible.  Note that it doesn't attempt to shrink events below
     * their stated getLengthInSeconds(); if an event says it takes 15 seconds,
     * we'll give it the full 15 seconds, even if its sound file only takes 8
     * seconds to play.
     */
    fun compressTime() {
        if (events.isEmpty()) return

        val tl = TreeMap<Int, Event>(events.comparator())
        var nextKey: Int? = null
        for (ent in events.entries) {
            if (nextKey == null) {
                tl[0] = ent.value
                nextKey = ent.value.lengthInSeconds
            } else {
                var ev = ent.value
                //  just for fun, let's also clamp white noise at 4 seconds.
                if ((ev is WhiteNoise) && (ev.lengthInSeconds > 4)) {
                    ev = WhiteNoise(4)
                }
                tl[nextKey] = ev
                nextKey += ev.lengthInSeconds
            }
        }
        events = tl
    }

    val entrySet: Set<Map.Entry<Int, Event>>
        /**
         * returns the entry set itself
         *
         * @return Set<Map.Entry></Map.Entry><Integer></Integer>,Event>> set of events
         */
        get() = events.entries

    val entryList: List<Map.Entry<Int, Event>>
        /**
         * Returns a shallow copy of the list into a List.  This is just used in
         * unit tests.
         */
        get() {
            val rv: MutableList<Map.Entry<Int, Event>> = ArrayList(events.size)
            rv.addAll(events.entries)
            return rv
        }

    /**
     * Prints list of missions
     */
    override fun toString(): String {
        return events.entries.toString()
    }
}
