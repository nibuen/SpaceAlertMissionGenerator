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

import androidx.annotation.NonNull;

/**
 * Threat class
 * 
 * @author mkalus
 */
public class Threat implements Event {
	public static final int THREAT_LEVEL_NORMAL = 1;
	public static final int THREAT_LEVEL_SERIOUS = 2;

	public static final int THREAT_POSITION_INTERNAL = 1;
	public static final int THREAT_POSITION_EXTERNAL = 2;

	public static final int THREAT_SECTOR_BLUE = 1;
	public static final int THREAT_SECTOR_WHITE = 2;
	public static final int THREAT_SECTOR_RED = 3;

	/** Enum wrapper around THREAT_SECTOR_* constants. */
	public enum Zone {
		Blue(THREAT_SECTOR_BLUE), White(THREAT_SECTOR_WHITE), Red(THREAT_SECTOR_RED);
		Zone(int sector) { this.sector = sector; }
		private final int sector;
	}

	/**
	 * assumed time in seconds: 10
	 */
	public int getLengthInSeconds() {
		return 15;
	}

	/**
	 * Threat level
	 */
	private int threatLevel = 0;

	/**
	 * internal/external threat
	 */
	private int threatPosition = 0;

	/**
	 * sector of threat
	 */
	private int sector = 0;

	/**
	 * time in which threat appears
	 */
	private int time = 0;

	/**
	 * confirmed threat?
	 */
	private boolean confirmed = false;

	public Threat() {
	}

	/**
	 * Copy constructor.
	 */
	public Threat(Threat other) {
		threatLevel = other.threatLevel;
		threatPosition = other.threatPosition;
		sector = other.sector;
		time = other.time;
		confirmed = other.confirmed;
	}

	/**
	 * Creates an internal threat.
	 */
	public Threat(int time, boolean confirmed, boolean serious) {
		this.time = time;
		this.confirmed = confirmed;
		this.threatPosition = THREAT_POSITION_INTERNAL;
		this.threatLevel = serious ? THREAT_LEVEL_SERIOUS : THREAT_LEVEL_NORMAL;
	}

	/**
	 * Creates an external threat.
	 *
	 * @param zone must not be null.
	 */
	public Threat(int time, boolean confirmed, boolean serious, @NonNull Zone zone) {
		this.time = time;
		this.confirmed = confirmed;
		this.threatPosition = THREAT_POSITION_EXTERNAL;
		this.threatLevel = serious ? THREAT_LEVEL_SERIOUS : THREAT_LEVEL_NORMAL;
		sector = zone.sector;
	}

	/**
	 * @return the threatLevel
	 */
	public int getThreatLevel() {
		return threatLevel;
	}

	/**
	 * @param threatLevel
	 *            the threatLevel to set
	 */
	public void setThreatLevel(int threatLevel) {
		this.threatLevel = threatLevel;
	}

	/**
	 * @return the threatPosition
	 */
	public int getThreatPosition() {
		return threatPosition;
	}

	/**
	 * @param threatPosition
	 *            the threatPosition to set
	 */
	public void setThreatPosition(int threatPosition) {
		this.threatPosition = threatPosition;
	}

	/**
	 * @return the sector
	 */
	public int getSector() {
		return sector;
	}

	/**
	 * @param sector
	 *            the sector to set
	 */
	public void setSector(int sector) {
		this.sector = sector;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * @return the confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	/**
	 * @param confirmed
	 *            the confirmed to set
	 */
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getTimeColor() {
		if (threatPosition == THREAT_POSITION_INTERNAL) {
			return "#4CB847";
		} else {
			switch (sector) {
			case THREAT_SECTOR_BLUE:
				return "#00ADEE";
			case THREAT_SECTOR_WHITE:
				return "#FFFFFF";
			case THREAT_SECTOR_RED:
				return "#EE1C25";
			default:
				return null;
			}
		}
	}

	public String getTextColor() {
		if (threatPosition == THREAT_POSITION_INTERNAL) {
			return "#4CB847";
		} else {
			return "#BC8CBF";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Threat [isConfirmed()=" + isConfirmed() + ", getTime()="
				+ getTime() + ", getThreatLevel()=" + getThreatLevel()
				+ ", getThreatPosition()=" + getThreatPosition()
				+ ", getSector()=" + getSector() + ", getLengthInSeconds()="
				+ getLengthInSeconds() + ", getTimeColor()=" + getTimeColor()
				+ ", getTextColor()=" + getTextColor() + "]";
	}
}
