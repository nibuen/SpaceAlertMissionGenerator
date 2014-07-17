package com.boarbeard.audio.parser;

import android.content.Context;

import com.boarbeard.R;

public class DefaultGrammar extends Grammar {

	public static final String NAME = "Default";

	public DefaultGrammar(Context context) {
		super();
		// TODO externalize Strings

		addElement(Element.Grammar, "English", null, context);

		addElement(Element.CommunicationsDownHeader, "Communications Down",
				R.raw.communications_down, context);
		addElement(Element.CommunicationsDownNoise, "White Noise",
				R.raw.white_noise, context);
		addElement(Element.CommunicationsDownFooter, "Communications Restored",
				R.raw.communications_restored, context);

		addElement(Element.AnnounceBeginFirstPhase,
				"Enemy activity detected. Please begin 1st phase.",
				R.raw.begin_first_phase, context);
		addElement(Element.AnnounceFirstPhaseEndsInOneMinute,
				"1st phase ends in 60 seconds",
				R.raw.first_phase_ends_in_1_minute, context);
		addElement(Element.AnnounceFirstPhaseEndsInTwentySeconds,
				"1st phase ends in 20 seconds",
				R.raw.first_phase_ends_in_20_seconds, context);
		addElement(Element.AnnounceFirstPhaseEnds,
				"1st phase ends in 5, 4, 3, 2, 1... 2nd phase begins.",
				R.raw.first_phase_ends, context);
		addElement(Element.AnnounceSecondPhaseBegins, "2nd phase begins",
				R.raw.second_phase_begins, context);
		addElement(Element.AnnounceSecondPhaseEndsInOneMinute,
				"2nd phase ends in 60 seconds",
				R.raw.second_phase_ends_in_1_minute, context);
		addElement(Element.AnnounceSecondPhaseEndsInTwentySeconds,
				"Second phase ends in 20 seconds",
				R.raw.second_phase_ends_in_20_seconds, context);
		addElement(Element.AnnounceSecondPhaseEnds,
				"2nd phase ends in 5, 4, 3, 2, 1... 3rd phase begins.",
				R.raw.second_phase_ends, context);
		addElement(Element.AnnounceThirdPhaseBegins, "Third phase begins",
				R.raw.third_phase_begins, context);
		addElement(Element.AnnounceThirdPhaseEndsInOneMinute,
				"Operation ends in 60 seconds",
				R.raw.operation_ends_in_1_minute, context);
		addElement(Element.AnnounceThirdPhaseEndsInTwentySeconds,
				"Operation ends in 20 seconds",
				R.raw.operation_ends_in_20_seconds, context);
		addElement(Element.AnnounceThirdPhaseEnds,
				"Operation ends in 5, 4, 3, 2, 1. Mission complete.",
				R.raw.operation_ends, context);

		addElement(Element.IncomingData, "Incoming Data", R.raw.incoming_data,
				context);
		addElement(Element.DataTransfer,
				"Data transfer. Data transfer in 5, 4, 3, 2, 1.",
				R.raw.data_transfer, context);

		addElement(Element.AlertHeader, "Alert!!!", R.raw.alert, context);
		addElement(Element.InternalThreat, "Internal Threat",
				R.raw.internal_threat, context);
		addElement(Element.SeriousInternalThreat, "Serious Internal Threat",
				R.raw.serious_internal_threat, context);
		addElement(Element.Repeat, "Repeat", R.raw.repeat, context);
		addElement(Element.UnconfirmedReport, "Unconfirmed Report",
				R.raw.unconfirmed_report, context);

		addElement(Element.TimeTPlus1, "Time T+1", R.raw.time_t_plus_1, context);
		addElement(Element.TimeTPlus2, "Time T+2", R.raw.time_t_plus_2, context);
		addElement(Element.TimeTPlus3, "Time T+3", R.raw.time_t_plus_3, context);
		addElement(Element.TimeTPlus4, "Time T+4", R.raw.time_t_plus_4, context);
		addElement(Element.TimeTPlus5, "Time T+5", R.raw.time_t_plus_5, context);
		addElement(Element.TimeTPlus6, "Time T+6", R.raw.time_t_plus_6, context);
		addElement(Element.TimeTPlus7, "Time T+7", R.raw.time_t_plus_7, context);
		addElement(Element.TimeTPlus8, "Time T+8", R.raw.time_t_plus_8, context);

		addElement(Element.RedAlertLevel1, "", R.raw.red_alert_1, context);

		addElement(Element.Threat, "Threat", R.raw.threat, context);
		addElement(Element.SeriousThreat, "Serious Threat",
				R.raw.serious_threat, context);
		addElement(Element.ZoneBlue, "Zone Blue", R.raw.zone_blue, context);
		addElement(Element.ZoneWhite, "Zone White", R.raw.zone_white, context);
		addElement(Element.ZoneRed, "Zone Red", R.raw.zone_red, context);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
