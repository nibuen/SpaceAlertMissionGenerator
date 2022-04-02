package com.boarbeard.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.boarbeard.R;
import com.boarbeard.audio.parser.DefaultGrammar;
import com.boarbeard.audio.parser.EventListParserFactory;
import com.boarbeard.io.ExternalMedia;

import java.util.List;

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
    }

    private void onCreateVoicePreferences() {
        ListPreference listPreferenceCategory = (ListPreference) findPreference("voice_choices");
        if (listPreferenceCategory != null) {
            List<Uri> mediaFolderList = ExternalMedia.getMediaFolders(this);
            CharSequence[] entries = new String[mediaFolderList.size() + 1];
            CharSequence[] entryValues = new String[mediaFolderList.size() + 1];

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
        if (getApplicationContext().getString(R.string.voice_choices).equals(key)) {
            EventListParserFactory.getInstance().getParser(
                    sharedPreferences.getString(key, DefaultGrammar.NAME),
                    this, true);
        }
    }
}