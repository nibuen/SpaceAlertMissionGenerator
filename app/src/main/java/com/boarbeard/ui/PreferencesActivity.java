package com.boarbeard.ui;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;

import com.boarbeard.R;
import com.boarbeard.audio.parser.DefaultGrammar;
import com.boarbeard.audio.parser.EventListParserFactory;
import com.boarbeard.io.ExternalMedia;
import com.boarbeard.ui.widget.SeekBarPreference;

public class PreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		onCreateVoicePreferences();
        initRestoreDefaultButton();
	}

    /*
     * Initiates the button restore default settings button 
     */
    private void initRestoreDefaultButton() {
        Preference restoreButton = findPreference("restoreDefaultSettings");

            // Configure the behavior when the user pressed the restore default setting button.
        restoreButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                // Restore unconfirmed reports setting to default value
                CheckBoxPreference unconfirmedReports = (CheckBoxPreference)
                        findPreference("unconfirmedReportsPreference");
                unconfirmedReports.setChecked(getResources().
                    getBoolean(R.bool.pref_unconfirmed_reports_default_value));

                // Restore mission length setting to default value.
                SeekBarPreference missionLength = (SeekBarPreference)
                        findPreference("missionLengthPreference");
                missionLength.restoreDefaultValues();

                // Restore threat difficulty setting to default value.
                SeekBarPreference threatDifficulty = (SeekBarPreference)
                        findPreference("threatDifficultyPreference");
                threatDifficulty.restoreDefaultValues();
                
                // Restore Incoming data settings to default value
                SeekBarPreference nbrIncomingData = (SeekBarPreference)
                        findPreference("numberIncomingData");
                nbrIncomingData.restoreDefaultValues();
                return true;
            }
        });
    }

    private void onCreateVoicePreferences() {
		ListPreference listPreferenceCategory = (ListPreference) findPreference("voice_choices");
		if (listPreferenceCategory != null) {
			List<Uri> mediaFolderList = ExternalMedia.getMediaFolders(this);
			CharSequence entries[] = new String[mediaFolderList.size() + 1];
			CharSequence entryValues[] = new String[mediaFolderList.size() + 1];

			int i = 0;
			entries[i] = DefaultGrammar.NAME;
			entryValues[i] = entries[i];
			i++;

			for (Uri uri : mediaFolderList) {
				entries[i] = uri.getLastPathSegment();
				entryValues[i] = entries[i];
				i++;
			}

			listPreferenceCategory.setEntries(entries);
			listPreferenceCategory.setEntryValues(entryValues);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if ("voice_choices".equals(key)) {
			EventListParserFactory.getInstance().getParser(
					sharedPreferences.getString(key, DefaultGrammar.NAME),
					this, true);
		}
	}
}