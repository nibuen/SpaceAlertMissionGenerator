package com.boarbeard.ui

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boarbeard.R
import com.boarbeard.audio.MediaPlayerMainMission
import com.boarbeard.audio.MediaPlayerSequence
import com.boarbeard.audio.MissionLog
import com.boarbeard.audio.parser.EventListParserFactory
import com.boarbeard.ui.MissionType.Companion.toStringValues
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MissionActivity : AppCompatActivity() {
    private var systemUiMode = View.SYSTEM_UI_FLAG_VISIBLE
    private var sequence: MediaPlayerSequence? = null
    private lateinit var togglebutton: ToggleButton
    private var stopWatch: StopWatch? = null
    private var missionType = MissionType.Random
    private var missionTypeTextView: TextView? = null
    private val missionLogs = mutableListOf<MissionLog>()

    private lateinit var missionLogsRecyclerView: RecyclerView
    private lateinit var mAdapter: MissionCardsAdapter
    private val menuTypeMission: MenuItem? = null

    private val notificationRequestCode = 1001

    private val eventParserDispatcher = HandlerThread("EventParserDispatcher")
        .apply { start() }
        .looper.let { Handler(it) }
        .asCoroutineDispatcher()

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        //  When people hit the volume buttons, we want to change the media
        //  volume, not the ringtone volume.
        volumeControlStream = AudioManager.STREAM_MUSIC
        stopWatch = StopWatch(
            findViewById(R.id.missionClockTextView)
        )
        missionTypeTextView = findViewById(R.id.mission_type_text_view)
        missionTypeTextView?.text = missionType
            .toString(this@MissionActivity)

        missionLogsRecyclerView = findViewById(R.id.mission_cards_recycler_view)
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        missionLogsRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(this)
        missionLogsRecyclerView.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = MissionCardsAdapter(missionLogs)
        missionLogsRecyclerView.adapter = mAdapter
        togglebutton = findViewById(R.id.togglePlayMission)
        togglebutton.setOnClickListener {
            GlobalScope.launch {
                toggleMission(
                    togglebutton.isChecked
                )
            }
        }

        //  This is a pretty bad kludge.  When the app first starts up, Random
        //  is selected, but configureMission() hasn't been called, so we don't
        //  see the mission introduction messages printed by configureMission().
        //  I tried putting configureMission(true) here instead, but it caused a
        //  weird delay at startup (maybe just because I was running it through
        //  Android Studio?), and I wasn't sure it was the right thing to do
        //  anyway.  So, instead, duplicate stuff from configureMission().
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean("stompUnconfirmedReportsPreference", true)) {
            printMissionIntroduction(
                resources.getString(
                    R.string.player_count_message, prefs.getInt("playerCount", 5)
                )
            )
        }
        if (prefs.getBoolean("compressTimePreference", false)) {
            printMissionIntroduction(
                resources.getString(
                    R.string.time_compressed_message
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        // Prepare parser
        object : AsyncTask<Context?, Void?, Void?>() {
            override fun doInBackground(vararg params: Context?): Void? {
                EventListParserFactory.getInstance().getParser(params[0])
                return null
            }
        }.execute(this)
        setSystemUiVisibility(systemUiMode)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (MEDIA_ACTION == intent.action) {
            GlobalScope.launch {
                toggleMission(intent.getBooleanExtra(Intent.EXTRA_SUBJECT, false))
            }
        }
    }

    /**
     * Request that the visibility of the status bar of the main view be changed
     *
     * @param visibility Bitwise-or of flags [View.SYSTEM_UI_FLAG_LOW_PROFILE] or
     * [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION].
     */
    private fun setSystemUiVisibility(visibility: Int) = runOnUiThread {
            window.decorView.systemUiVisibility = visibility
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menuNewMission -> {
                GlobalScope.launch {
                    configureMission(true)
                }
                true
            }
            R.id.menuResetMission -> {
                GlobalScope.launch {
                    configureMission(false)
                }
                true
            }
            R.id.menuMissionOptions -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true
            }
            R.id.menuMissionAbout -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.menuMissionHelp -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            R.id.menuTypeMissionIcon -> {
                showMissionTypeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun configureMission(newGame: Boolean) {
        stopMission()
        val preferences = PreferenceManager
            .getDefaultSharedPreferences(this)
        // Displays the log texts, so needs the log color preferences
        if (sequence == null || newGame) {
            // TODO replace with some real scopes and error handling
            sequence = MediaPlayerMainMission(this@MissionActivity,
                stopWatch!!, preferences)
            withContext(eventParserDispatcher) {
                EventListParserFactory.getInstance().getParser(this@MissionActivity)
                    .parse(missionType.buildEvents(preferences), sequence)
            }
        } else {
            sequence!!.reset()
            stopWatch!!.reset()
        }

        runOnUiThread {
            missionLogs.clear()
            mAdapter.notifyDataSetChanged()
        }

        if (missionType.missionIntroductionResId != 0) {
            (sequence as MediaPlayerMainMission).printMissionIntroduction(
                resources.getString(
                    missionType.missionIntroductionResId
                )
            )
        }

        //  If we're stomping unconfirmed reports, display the player count to
        //  help keep them from discarding unconfirmed reports for five
        //  players, or getting unconfirmed reports as normal threats for four
        //  players.
        if (preferences.getBoolean("stompUnconfirmedReportsPreference", true)) {
            printMissionIntroduction(
                resources.getString(
                    R.string.player_count_message, preferences.getInt("playerCount", 5)
                )
            )
        }
        if (preferences.getBoolean("compressTimePreference", false)) {
            printMissionIntroduction(
                resources.getString(
                    R.string.time_compressed_message
                )
            )
        }
        if (preferences.getBoolean("dumpMissionTreePreference", false)) {
            (sequence as? MediaPlayerMainMission)?.dumpMissionTreeToLog()
        }
    }

    private fun printMissionIntroduction(message: String) = runOnUiThread {
        if (sequence != null) {
            (sequence as? MediaPlayerMainMission)?.printMissionIntroduction(message)
        } else {
            //  sequence == null during onCreate(), before configureMission()
            //  is called.
            missionLogs.add(MissionLog.formatIntro(resources, message))
        }
    }

    private fun showMissionTypeDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.pref_choose_mission))
            .setItems(
            toStringValues(this)
        ) { dialog, item ->
            missionType = MissionType.values()[item]
            val missionName = missionType
                .toString(this@MissionActivity)
            missionTypeTextView!!.text = missionName
            GlobalScope.launch {
                configureMission(true)
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private suspend fun toggleMission(start: Boolean) {
        if (start) {
            if (sequence == null) {
                configureMission(true)
            }
            startMission()

        } else {
            pauseMission()
        }
    }

    private fun startMission() {
        sequence?.apply {
            start()
            toggleOn()
            setKeepScreenOn(true)
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE.also { systemUiMode = it })
        }
    }

    private fun pauseMission() {
        sequence?.apply {
            pause()
            toggleOff()
            setKeepScreenOn(false)
        }
        setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE.also { systemUiMode = it })
    }

    private fun stopMission() {
        sequence?.apply {
            stop()
            toggleOff()
            setKeepScreenOn(false)
        }
        setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE.also { systemUiMode = it })
    }

    private fun setKeepScreenOn(keepScreenOn: Boolean) = runOnUiThread {
        if (keepScreenOn) {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE.also { systemUiMode = it })
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun updateMissionLog(missionLog: MissionLog) = runOnUiThread {
            missionLogs.add(missionLog)
            mAdapter.notifyItemInserted(missionLogs.size - 1)
            missionLogsRecyclerView.post { missionLogsRecyclerView.smoothScrollToPosition(mAdapter.itemCount) }
    }

    fun clearMissionLog() = runOnUiThread {
        missionLogs.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun toggleOn() = runOnUiThread {
        togglebutton.isChecked = true
        notificationUpdate(true)
    }

    fun toggleOff() = runOnUiThread {
        togglebutton.isChecked = false
        notificationUpdate(false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GlobalScope.launch {
                configureMission(false)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    public override fun onDestroy() {
        super.onDestroy()

        // Get an instance of the NotificationManager service
        val notificationManager = NotificationManagerCompat.from(this)

        // Cancel any ongoing notifications
        notificationManager.cancelAll()
    }

    private fun notificationUpdate(isRunning: Boolean) {
        val notificationId = 1
        val id = "mission_channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val name = "Mission Updates"
        val description = "Space alert mission updates"

        // Intent to bring you back to app
        val viewIntent = Intent(this, MissionActivity::class.java)
        val viewPendingIntent =
            PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

        val notificationBuilder = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.space_alert_logo
                )
            )
            .setContentText(missionType.toString(this))
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(viewPendingIntent)
            .setOngoing(true)

        // Intent to stop/start the mission
        val mediaIntent = Intent(this, MissionActivity::class.java)
        mediaIntent.action = MEDIA_ACTION
        mediaIntent.putExtra(Intent.EXTRA_SUBJECT, java.lang.Boolean.valueOf(!isRunning))
        val startStopIntent = PendingIntent.getActivity(
            this,
            0,
            mediaIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (isRunning) {
            notificationBuilder.addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.button_playing), startStopIntent
            )
        } else {
            notificationBuilder.addAction(
                android.R.drawable.ic_media_play,
                getString(R.string.button_paused), startStopIntent
            )
        }

        // Get an instance of the NotificationManager service
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannel(channel)

        // Build the notification and issues it with notification manager.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Notifications are broken, Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), notificationRequestCode)
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val MEDIA_ACTION = "com.boarbeard.spacealert.media.action"
    }
}