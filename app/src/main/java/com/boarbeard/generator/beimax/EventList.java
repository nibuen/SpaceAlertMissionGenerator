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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.boarbeard.generator.beimax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.boarbeard.generator.beimax.event.Announcement;
import com.boarbeard.generator.beimax.event.Event;
import com.boarbeard.generator.beimax.event.Threat;
import com.boarbeard.generator.beimax.event.WhiteNoise;
import com.boarbeard.generator.beimax.event.WhiteNoiseRestored;

import androidx.annotation.NonNull;

/**
 * Keeps mission events in an ordered state and also checks for collisions and
 * the like
 * 
 * @author mkalus
 * 
 */
public class EventList {
	public static String formatTime(int time) {
		int minute = time / 60;
		int seconds = time % 60;
		return String.format("%02d", minute) + ":"
				+ String.format("%02d", seconds);
	}

	/**
	 * actual time table for this mission
	 */
	TreeMap<Integer, Event> events;

	/**
	 * Constructor
	 */
	public EventList() {
		events = new TreeMap<Integer, Event>();
	}

    /**
     * Copy constructor.  This just does a shallow copy of the tree; the events
     * in the new tree will still be references to the events in the original
     * tree.
     *
     * @param other must not be null
     */
    public EventList(@NonNull EventList other) {
        events = new TreeMap<>(other.events.comparator());
        events.putAll(other.events);
    }

	/**
	 * Add announcements
	 * 
	 * @param phase1
	 *            - length of phase 1
	 * @param phase2
	 *            - length of phase 2
	 * @param phase3
	 *            - length of phase 3
	 */
	public void addPhaseEvents(int phase1, int phase2, int phase3) {
		addEvent(0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
		Announcement a = new Announcement(
				Announcement.ANNOUNCEMENT_PH1_ONEMINUTE);
		addEvent(phase1 - 60 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS);
		addEvent(phase1 - 20 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS);
		addEvent(phase1 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH2_ONEMINUTE);
		addEvent(phase1 + phase2 - 60 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH2_TWENTYSECS);
		addEvent(phase1 + phase2 - 20 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH2_ENDS);
		addEvent(phase1 + phase2 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE);
		addEvent(phase1 + phase2 + phase3 - 60 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS);
		addEvent(phase1 + phase2 + phase3 - 20 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS);
		addEvent(phase1 + phase2 + phase3 - a.getLengthInSeconds(), a);
	}

	/**
	 * Add announcements
	 * 
	 * @param phase1
	 *            - length of phase 1
	 * @param phase2
	 *            - length of phase 2
	 */
	public void addPhaseEvents(int phase1, int phase2) {
		addEvent(0, new Announcement(Announcement.ANNOUNCEMENT_PH1_START));
		Announcement a = new Announcement(
				Announcement.ANNOUNCEMENT_PH1_ONEMINUTE);
		addEvent(phase1 - 60 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH1_TWENTYSECS);
		addEvent(phase1 - 20 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH1_ENDS);
		addEvent(phase1 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_ONEMINUTE);
		addEvent(phase1 + phase2 - 60 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_TWENTYSECS);
		addEvent(phase1 + phase2 - 20 - a.getLengthInSeconds(), a);
		a = new Announcement(Announcement.ANNOUNCEMENT_PH3_ENDS);
		addEvent(phase1 + phase2 - a.getLengthInSeconds(), a);
	}

	// Attempts to add a White Noise Event
	// This adds TWO events, WhiteNoise and WhiteNoiseRestored
	public boolean addWhiteNoiseEvents(int time, int length) {
		boolean returnValue1 = false, returnValue2 = false;
		Event whitenoise = new WhiteNoise(length);
		Event whitenoiserestored = new WhiteNoiseRestored();

		if (!checkTime(time, whitenoise.getLengthInSeconds()
				+ whitenoiserestored.getLengthInSeconds()))
			return false;

		returnValue1 = addEvent(time, whitenoise);

		returnValue2 = addEvent(time + whitenoise.getLengthInSeconds(),
				whitenoiserestored);

		if (!returnValue1) {
			System.err.println("retunValue1 was false!");
		}
		if (!returnValue2) {
			System.err.println("retunValue2 was false!");
		}

		return (true);
	}

	/**
	 * attempts to add event at time
	 * 
	 * @param time
	 * @param event
	 * @return false, if a collision was detected
	 */
	public boolean addEvent(int time, Event event) {

		// check first
		if (!checkTime(time, event.getLengthInSeconds())) {
			return false;
		}

		// otherwise add event
		events.put(time, event);
		return true;
	}

	/**
	 * Checks whether a certain time slot is free
	 * 
	 * @param time
	 * @param length
	 *            in seconds
	 * @return false, if time slot is not free
	 */
	public boolean checkTime(int time, int length) {
		// if empty set, you can add stuff
		if (events.isEmpty())
			return true;

		// lowest or highest keys?
		int lowest = events.firstKey();
		if (lowest > time) { // there is no key before?
            return time + length <= lowest;
		}
		int highest = events.lastKey();
		int lengthOfLastEvent = events.get(highest).getLengthInSeconds();
		if (highest + lengthOfLastEvent < time)
			return true;

		// ok, we are in between somewhere - check event before
		Integer beforeKey = floorKey(time);
		int endTime = beforeKey + events.get(beforeKey).getLengthInSeconds();
		if (endTime > time)
			return false;

		// check event after
		int after = ceilingKey(beforeKey + 1); // next event after before key
        return time + length <= after;

    }

    /**
     * Remove Unconfirmed Reports, or replace them with normal (confirmed)
     * threats.
     *
     * @param replace true if they should be replaced with normal threats; false
     *                if they should be removed from the list.
     */
    public void stompUnconfirmedReports(boolean replace) {
        ArrayList<Integer> toRemove = replace ? null : new ArrayList<Integer>();
        for (Entry<Integer, Event> te : events.entrySet()) {
            if (!(te.getValue() instanceof Threat)) continue;
            Threat tt = (Threat)(te.getValue());
            if (tt.isConfirmed()) continue;
            if (replace) {
                //  We don't want to fiddle with the existing Threat instance
                //  itself (as it may be shared by another instance of this
                //  MissionType), so copy it.
                te.setValue(new Threat(tt));
                ((Threat) te.getValue()).setConfirmed(true);
            } else {
                //  can't actually remove the element here without a
                //  ConcurrentModificationException, so add it to the list (of
                //  probably one element) to remove afterward.
                toRemove.add(te.getKey());
            }
        }
        for (int ii = replace ? -1 : toRemove.size() - 1; ii >= 0; --ii) {
            events.remove(toRemove.get(ii));
        }
    }

    /**
     * This is a ridiculous debugging method which removes all gaps between
     * events, so that you can listen to a complete soundtrack in as little
     * time as possible.  Note that it doesn't attempt to shrink events below
     * their stated getLengthInSeconds(); if an event says it takes 15 seconds,
     * we'll give it the full 15 seconds, even if its sound file only takes 8
     * seconds to play.
     */
    public void compressTime() {
        if (events.isEmpty()) return;
        TreeMap<Integer, Event> tl = new TreeMap<>(events.comparator());
        Integer nextKey = null;
        for (Entry<Integer, Event> ent : events.entrySet()) {
            if (nextKey == null) {
                tl.put(0, ent.getValue());
                nextKey = ent.getValue().getLengthInSeconds();
            } else {
                Event ev = ent.getValue();
                //  just for fun, let's also clamp white noise at 4 seconds.
                if ((ev instanceof WhiteNoise) && (ev.getLengthInSeconds() > 4)) {
                    ev = new WhiteNoise(4);
                }
                tl.put(nextKey, ev);
                nextKey = nextKey + ev.getLengthInSeconds();
            }
        }
        events = tl;
    }

	/**
	 * returns the entry set itself
	 * 
	 * @return Set<Map.Entry<Integer,Event>> set of events
	 */
	public Set<Map.Entry<Integer, Event>> getEntrySet() {
		return events.entrySet();
	}

    /**
     * Returns a shallow copy of the list into a List.  This is just used in
     * unit tests.
     */
    public List<Entry<Integer, Event>> getEntryList() {
        List<Entry<Integer, Event>> rv = new ArrayList<>(events.size());
        rv.addAll(events.entrySet());
        return rv;
    }

	/**
	 * Prints list of missions
	 */
	@Override
	public String toString() {

		return events.entrySet().toString();
	}

	/**
	 * Gets the entry corresponding to the specified key; if no such entry
	 * exists, returns the entry for the greatest key less than the specified
	 * key; if no such entry exists, returns {@code null}.
	 */
	/*
	 * final Entry<Integer,Event> floorEntry(Integer time) {
	 * 
	 * // Since this is not in till API level 9 for android going this is a
	 * dirty version. for(Entry<Integer, Event> entry :
	 * events.headMap(time).entrySet()) { return entry; } return null; }
	 */

	/**
	 * Gets the entry corresponding to the specified key; if no such entry
	 * exists, returns the entry for the greatest key less than the specified
	 * key; if no such entry exists, returns {@code null}.
	 */
	final Integer floorKey(Integer time) {

		if (events.containsKey(time)) {
			return time;
		}

		SortedMap<Integer, Event> map = events.headMap(time);
		return map.isEmpty() ? null : map.lastKey();
	}

	/**
	 * Returns the least key greater than or equal to the given key, or null if
	 * there is no such key.
	 */
	final Integer ceilingKey(Integer time) {

		if (events.containsKey(time)) {
			return time;
		}

		SortedMap<Integer, Event> map = events.tailMap(time);
		return map.isEmpty() ? null : map.firstKey();
	}
}
