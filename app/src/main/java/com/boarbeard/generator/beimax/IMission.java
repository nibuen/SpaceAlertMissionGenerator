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

/**
 * Generic Mission Interface
 * @author mkalus
 */
public interface IMission {
	/**
	 * Generate new mission
	 * 
	 * @return true if mission creation succeeded
	 */
	public boolean generateMission();
	
	/**
	 * Return event list of mission
	 * @return ordered event list of mission
	 */
	public EventList getMissionEvents();
	
	/**
	 * Return length of a phase in seconds
	 * @param phase 1-3
	 * @return phase length of mission or -1
	 */
	public int getMissionPhaseLength(int phase);
}
