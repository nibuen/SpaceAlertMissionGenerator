package com.boarbeard.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.boarbeard.R;
import com.boarbeard.audio.MediaPlayerMainMission;
import com.boarbeard.audio.MediaPlayerSequence;
import com.boarbeard.audio.parser.EventListParserFactory;

public class MissionActivity extends Activity {

    private static final String MEDIA_ACTION = "com.boarbeard.spacealert.media.action";

    private int systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE;

	private MediaPlayerSequence sequence;

	private TextView logTextView;
	private TextView missionTypeTextView;

	private ScrollView scrollView;
	private ToggleButton togglebutton;

	private StopWatch stopWatch;

	private MissionType missionType = MissionType.Random;

	// Enable to dump the entire mission to the mission log immediately after the mission is
	// selected. Usable for debugging.
	private static final boolean DUMP_MISSION_TREE = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		missionTypeTextView = (TextView) findViewById(R.id.missionTypeTextView);
		if (missionTypeTextView != null) {
			// is null if we use action bar
			missionTypeTextView.setText(missionType
					.toString(MissionActivity.this));
		}

		stopWatch = new StopWatch(
				(TextView) findViewById(R.id.missionClockTextView));

		scrollView = (ScrollView) findViewById(R.id.missionScrollView);
		logTextView = (TextView) findViewById(R.id.missionTextView);
		logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

		togglebutton = (ToggleButton) findViewById(R.id.togglePlayMission);
		togglebutton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
                toggleMission(togglebutton.isChecked());
			}
		});
		setupActionBar();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			return;
		}

		final SpinnerAdapter spinner = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_dropdown_item,
				MissionType.toStringValues(this));

		final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(spinner,
				new OnNavigationListener() {

					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						missionType = MissionType.values()[itemPosition];
						if (missionTypeTextView != null) {
							missionTypeTextView.setText(missionType
									.toString(MissionActivity.this));
						}
						createMission();
						return true;
					}
				});

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onResume()
	 */
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
        if(MEDIA_ACTION.equals(intent.getAction())) {
            toggleMission(intent.getBooleanExtra(Intent.EXTRA_SUBJECT, false));
        }
    }

    /**
	 * Request that the visibility of the status bar of the main view be changed
	 *
	 * @param visibility
	 *            Bitwise-or of flags {@link View#SYSTEM_UI_FLAG_LOW_PROFILE} or
	 *            {@link View#SYSTEM_UI_FLAG_HIDE_NAVIGATION}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setSystemUiVisibility(int visibility) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			return;
		}
		getWindow().getDecorView().setSystemUiVisibility(visibility);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		menu.findItem(R.id.menuTypeMission).setVisible(
				Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuNewMission:
			createMission();
			return true;
		case R.id.menuResetMission:
			if (sequence != null) {
				sequence.reset();
			}
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
		case R.id.menuTypeMission:
			showMissionTypeDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void createMission() {
		stopMission();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Displays the log texts, so needs the log color preferences
		sequence = new MediaPlayerMainMission(this, stopWatch, preferences);

		EventListParserFactory.getInstance().getParser(this)
				.parse(missionType.getEventList(preferences), sequence);

        if (missionType.getMissionIntroductionResId() != 0) {
            ((MediaPlayerMainMission) sequence).
                    printMissionIntroduction(getResources().
                            getString(missionType.getMissionIntroductionResId()));
        }

        if (DUMP_MISSION_TREE) {
            ((MediaPlayerMainMission) sequence).dumpMissionTreeToLog();
        }

	}

	private void showMissionTypeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.pref_choose_mission));
		builder.setItems(MissionType.toStringValues(this),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						missionType = MissionType.values()[item];
						missionTypeTextView.setText(missionType
								.toString(MissionActivity.this));
						createMission();
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

    private void toggleMission(boolean start) {
        if (start) {
            if (sequence == null) {
                createMission();
                startMission();
            } else {
                startMission();
            }
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

	public void updateMissionLog(String missionLog) {
		logTextView.setText(Html.fromHtml(missionLog));
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
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
			if (sequence != null) {
				sequence.reset();
			}
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
                        .setSmallIcon(R.drawable.space_alert_logo)
                        .setContentText(missionType.toString(this))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentIntent(viewPendingIntent)
                        .setOngoing(true);

        // Intent to stop/start the mission
        Intent mediaIntent = new Intent(this, MissionActivity.class);
        mediaIntent.setAction(MEDIA_ACTION);
        mediaIntent.putExtra(Intent.EXTRA_SUBJECT, new Boolean(!isRunning));
        PendingIntent startStopIntent =
                PendingIntent.getActivity(this, 0, mediaIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(isRunning) {
            notificationBuilder.addAction(android.R.drawable.ic_media_pause,
                    getString(R.string.button_playing), startStopIntent);
        }
        else {
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