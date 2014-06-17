package com.boarbeard.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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

	private int systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE;

	private MediaPlayerSequence sequence;

	private TextView logTextView;
	private TextView missionTypeTextView;

	private ScrollView scrollView;
	private ToggleButton togglebutton;

	private PowerManager.WakeLock wakeLock;

	private StopWatch stopWatch;

	private MissionType missionType = MissionType.Random;

	// Enable to dump the entire mission to the mission log immediately after the mission is 
	// selected. Usable for debugging.
	private static final boolean DUMP_MISSON_TREE = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"Mission Wake Lock");

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
				if (togglebutton.isChecked()) {
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
		
		if (DUMP_MISSON_TREE) {
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

	private void startMission() {
		if (sequence != null) {
			if (!wakeLock.isHeld())
				wakeLock.acquire();
			sequence.start();
			setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	private void pauseMission() {
		if (sequence != null) {
			sequence.pause();
			if (wakeLock.isHeld())
				wakeLock.release();
		}
		setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE);
	}

	private void stopMission() {
		if (sequence != null) {
			sequence.stop();
			if (wakeLock.isHeld())
				wakeLock.release();
		}
		setSystemUiVisibility(systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE);
	}

	public void updateMissionLog(String missionLog) {
		logTextView.setText(Html.fromHtml(missionLog));
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	public void toggleOff() {
		togglebutton.setChecked(false);
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
}