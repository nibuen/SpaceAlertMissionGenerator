package com.boarbeard.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.boarbeard.R;
import com.boarbeard.audio.MediaPlayerMainMission;
import com.boarbeard.audio.MediaPlayerSequence;
import com.boarbeard.audio.MissionLog;
import com.boarbeard.audio.parser.EventListParserFactory;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MissionActivity extends AppCompatActivity {

    private static final String MEDIA_ACTION = "com.boarbeard.spacealert.media.action";

    private int systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE;

    private MediaPlayerSequence sequence;

    private ToggleButton togglebutton;

    private StopWatch stopWatch;

    private MissionType missionType = MissionType.Random;

    private TextView missionTypeTextView;

    private List<MissionLog> missionLogs = new ArrayList<MissionLog>();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MissionCardsAdapter mAdapter;
    private MenuItem menuTypeMission;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //  When people hit the volume buttons, we want to change the media
        //  volume, not the ringtone volume.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        stopWatch = new StopWatch(
                (TextView) findViewById(R.id.missionClockTextView));

        missionTypeTextView = (TextView) findViewById(R.id.mission_type_text_view);
        if (missionTypeTextView != null) {
            // is null if we use action bar
            missionTypeTextView.setText(missionType
                    .toString(MissionActivity.this));
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.mission_cards_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MissionCardsAdapter(missionLogs);
        mRecyclerView.setAdapter(mAdapter);

        togglebutton = (ToggleButton) findViewById(R.id.togglePlayMission);
        togglebutton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                toggleMission(togglebutton.isChecked());
            }
        });

        //  This is a pretty bad kludge.  When the app first starts up, Random
        //  is selected, but configureMission() hasn't been called, so we don't
        //  see the mission introduction messages printed by configureMission().
        //  I tried putting configureMission(true) here instead, but it caused a
        //  weird delay at startup (maybe just because I was running it through
        //  Android Studio?), and I wasn't sure it was the right thing to do
        //  anyway.  So, instead, duplicate stuff from configureMission().
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("stompUnconfirmedReportsPreference", true)) {
            printMissionIntroduction(getResources().getString(
                    R.string.player_count_message, prefs.getInt("playerCount", 5)));
        }
        if (prefs.getBoolean("compressTimePreference", false)) {
            printMissionIntroduction(getResources().getString(
                    R.string.time_compressed_message));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Prepare parser
        new AsyncTask<Context, Void, Void>() {

            @Override
            protected Void doInBackground(Context... params) {
                EventListParserFactory.getInstance().getParser(params[0]);
                return null;
            }
        }.execute(this);

        setSystemUiVisibility(systemUiMode);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (MEDIA_ACTION.equals(intent.getAction())) {
            toggleMission(intent.getBooleanExtra(Intent.EXTRA_SUBJECT, false));
        }
    }

    /**
     * Request that the visibility of the status bar of the main view be changed
     *
     * @param visibility Bitwise-or of flags {@link View#SYSTEM_UI_FLAG_LOW_PROFILE} or
     *                   {@link View#SYSTEM_UI_FLAG_HIDE_NAVIGATION}.
     */
    private void setSystemUiVisibility(int visibility) {
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuNewMission:
                configureMission(true);
                return true;
            case R.id.menuResetMission:
                configureMission(false);
                return true;
            case R.id.menuMissionOptions:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.menuMissionAbout:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menuMissionHelp:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.menuTypeMissionIcon:
                showMissionTypeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureMission(boolean newGame) {
        stopMission();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        // Displays the log texts, so needs the log color preferences
        if (sequence == null || newGame) {
            sequence = new MediaPlayerMainMission(this, missionLogs, stopWatch, preferences);
            EventListParserFactory.getInstance().getParser(this)
                    .parse(missionType.getEventList(preferences), sequence);
        } else {
            sequence.reset();
            stopWatch.reset();
        }

        missionLogs.clear();
        mAdapter.notifyDataSetChanged();

        if (missionType.getMissionIntroductionResId() != 0) {
            ((MediaPlayerMainMission) sequence).
                    printMissionIntroduction(getResources().
                            getString(missionType.getMissionIntroductionResId()));
        }

        //  If we're stomping unconfirmed reports, display the player count to
        //  help keep them from discarding unconfirmed reports for five
        //  players, or getting unconfirmed reports as normal threats for four
        //  players.
        if (preferences.getBoolean("stompUnconfirmedReportsPreference", true)) {
            printMissionIntroduction(getResources().getString(
                    R.string.player_count_message, preferences.getInt("playerCount", 5)));
        }
        if (preferences.getBoolean("compressTimePreference", false)) {
            printMissionIntroduction(getResources().getString(
                    R.string.time_compressed_message));
        }
        if (preferences.getBoolean("dumpMissionTreePreference", false)) {
            ((MediaPlayerMainMission) sequence).dumpMissionTreeToLog();
        }

    }

    private void printMissionIntroduction(String message) {
        if (sequence != null) {
            ((MediaPlayerMainMission) sequence).printMissionIntroduction(message);
        } else {
            //  sequence == null during onCreate(), before configureMission()
            //  is called.  This is kind of nasty; duplicate code from
            //  MediaPlayerMainMission.printMissionIntroduction().
            missionLogs.add(new MissionLog(Html.fromHtml(
                    "<b><i><span style=\"color:#C7EBFC;\"> " + message +
                    "</span></i></b>")));
        }
    }

    private void showMissionTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.pref_choose_mission));
        builder.setItems(MissionType.toStringValues(this),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        missionType = MissionType.values()[item];
                        String missionName = missionType
                                .toString(MissionActivity.this);
                        missionTypeTextView.setText(missionName);
                        configureMission(true);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void toggleMission(boolean start) {
        if (start) {
            if (sequence == null) {
                configureMission(true);
            }
            startMission();
        } else {
            pauseMission();
        }
    }

    private void startMission() {
        if (sequence != null) {
            setKeepScreenOn(true);
            sequence.start();
            toggleOn();

            setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    private void pauseMission() {
        if (sequence != null) {

            sequence.pause();
            toggleOff();
            setKeepScreenOn(false);
        }
        setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void stopMission() {
        if (sequence != null) {

            sequence.stop();
            toggleOff();
            setKeepScreenOn(false);
        }
        setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void setKeepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void updateMissionLog(int itemAddedPosition) {
        mAdapter.notifyItemInserted(itemAddedPosition);
        mRecyclerView.post(new Runnable() {
            public void run() {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });
    }

    public void clearMissionLog() {
        mAdapter.notifyDataSetChanged();
    }

    public void toggleOn() {
        togglebutton.setChecked(true);
        notificationUpdate(true);
    }

    public void toggleOff() {
        togglebutton.setChecked(false);
        notificationUpdate(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            configureMission(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Cancel any ongoing notifications
        notificationManager.cancelAll();
    }

    private void notificationUpdate(boolean isRunning) {
        int notificationId = 001;

        // Intent to bring you back to app
        Intent viewIntent = new Intent(this, MissionActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.drawable.space_alert_logo))
                        .setContentText(missionType.toString(this))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentIntent(viewPendingIntent)
                        .setOngoing(true);

        // Intent to stop/start the mission
        Intent mediaIntent = new Intent(this, MissionActivity.class);
        mediaIntent.setAction(MEDIA_ACTION);
        mediaIntent.putExtra(Intent.EXTRA_SUBJECT, Boolean.valueOf(!isRunning));
        PendingIntent startStopIntent =
                PendingIntent.getActivity(this, 0, mediaIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (isRunning) {
            notificationBuilder.addAction(android.R.drawable.ic_media_pause,
                    getString(R.string.button_playing), startStopIntent);
        } else {
            notificationBuilder.addAction(android.R.drawable.ic_media_play,
                    getString(R.string.button_paused), startStopIntent);

        }

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

}