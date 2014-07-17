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
public class Announcement implements Event {
	public static final int ANNOUNCEMENT_PH1_START = 1;
	public static final int ANNOUNCEMENT_PH1_ONEMINUTE = 2;
	public static final int ANNOUNCEMENT_PH1_TWENTYSECS = 3;
	public static final int ANNOUNCEMENT_PH1_ENDS = 4;
	public static final int ANNOUNCEMENT_PH2_ONEMINUTE = 11;
	public static final int ANNOUNCEMENT_PH2_TWENTYSECS = 12;
	public static final int ANNOUNCEMENT_PH2_ENDS = 13;
	public static final int ANNOUNCEMENT_PH3_ONEMINUTE = 21;
	public static final int ANNOUNCEMENT_PH3_TWENTYSECS = 22;
	public static final int ANNOUNCEMENT_PH3_ENDS = 23;

	/**
	 * announcement type
	 */
	private int type;

	public Announcement(int type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * length in seconds depends on type
	 */
	public int getLengthInSeconds() {
		switch (type) {
		case ANNOUNCEMENT_PH1_START:
			return 10; // if you make this higher, also check "- arg" stuff in
						// missionImpl
		case ANNOUNCEMENT_PH1_ONEMINUTE:
			return 5;
		case ANNOUNCEMENT_PH1_TWENTYSECS:
			return 5;
		case ANNOUNCEMENT_PH1_ENDS:
			return 10;
		case ANNOUNCEMENT_PH2_ONEMINUTE:
			return 5;
		case ANNOUNCEMENT_PH2_TWENTYSECS:
			return 5;
		case ANNOUNCEMENT_PH2_ENDS:
			return 10; // if you make this higher, also check "- arg" stuff in
						// missionImpl
		case ANNOUNCEMENT_PH3_ONEMINUTE:
			return 5;
		case ANNOUNCEMENT_PH3_TWENTYSECS:
			return 5;
		case ANNOUNCEMENT_PH3_ENDS:
			return 12;
		}
		return -1; // error
	}

	public String getTimeColor() {
		return "#80A9BC";
	}

	public String getTextColor() {
		return "#C7EBFC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Announcement [getType()=" + getType()
				+ ", getLengthInSeconds()=" + getLengthInSeconds()
				+ ", getTimeColor()=" + getTimeColor() + ", getTextColor()="
				+ getTextColor() + "]";
	}
}
