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
package com.boarbeard.generator.beimax.event;

/**
 * @author mkalus
 * 
 */
public class IncomingData implements Event {
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.beimax.spacealert.Event#getLengthInSeconds()
	 */
	public int getLengthInSeconds() {
		return 10;
	}

	public String getTimeColor() {
		return "#80A9BC";
	}

	public String getTextColor() {
		return "#FFF9AD";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IncomingData [getLengthInSeconds()=" + getLengthInSeconds()
				+ ", getTimeColor()=" + getTimeColor() + ", getTextColor()="
				+ getTextColor() + "]";
	}
}
