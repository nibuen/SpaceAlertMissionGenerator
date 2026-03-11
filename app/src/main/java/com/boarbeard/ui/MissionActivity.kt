package com.boarbeard.ui

import android.Manifest
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.boarbeard.R
import com.boarbeard.audio.MediaPlayerMainMission
import com.boarbeard.audio.MediaPlayerSequence
import com.boarbeard.audio.MissionLog
import com.boarbeard.audio.parser.EventListParserFactory
import com.boarbeard.databinding.MainBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MissionActivity : AppCompatActivity() {
    private var sequence: MediaPlayerSequence? = null
    private lateinit var togglebutton: ToggleButton
    private lateinit var stopWatch: StopWatch
    private var missionType = MissionType.Random

    private lateinit var missionTypeTextView: TextView

    private val missionLogs = mutableStateListOf<MissionLog>()

    private val newMissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val ordinal = result.data?.getIntExtra(
                NewMissionActivity.RESULT_MISSION_TYPE_ORDINAL, MissionType.Random.ordinal
            ) ?: MissionType.Random.ordinal
            missionType = MissionType.entries[ordinal]
            missionTypeTextView.text = missionType.toString(this@MissionActivity)
            lifecycleScope.launch {
                configureMission(true)
                startMission()
            }
        }
    }

    private lateinit var binding: MainBinding

    private fun hideSystemUI() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun showSystemUI() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    private val eventParserDispatcher = HandlerThread("EventParserDispatcher")
        .apply { start() }
        .looper.let { Handler(it) }
        .asCoroutineDispatcher()

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //  When people hit the volume buttons, we want to change the media
        //  volume, not the ringtone volume.
        volumeControlStream = AudioManager.STREAM_MUSIC
        stopWatch = StopWatch(
            binding.missionClockTextView,
        )
        missionTypeTextView = binding.missionTypeTextView
        missionTypeTextView.text = missionType
            .toString(this@MissionActivity)


        binding.missionCardsComposeView.setContent {
            val listState = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                itemsIndexed(missionLogs) { _, data ->
                    Box(modifier = Modifier.animateItem()) {
                        MissionCard(data)
                    }
                }
            }
            // Auto-scroll to bottom when new items are added
            if (missionLogs.isNotEmpty()) {
                val lastIndex = missionLogs.size - 1
                androidx.compose.runtime.LaunchedEffect(missionLogs.size) {
                    listState.animateScrollToItem(lastIndex)
                }
            }
        }

        togglebutton = binding.togglePlayMission
        togglebutton.setOnClickListener {
            lifecycleScope.launch {
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

        hideSystemUI()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (MEDIA_ACTION == intent.action) {
            lifecycleScope.launch {
                toggleMission(intent.getBooleanExtra(Intent.EXTRA_SUBJECT, false))
            }
        }
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
                val intent = Intent(this, NewMissionActivity::class.java).apply {
                    putExtra(NewMissionActivity.RESULT_MISSION_TYPE_ORDINAL, missionType.ordinal)
                }
                newMissionLauncher.launch(intent)
                true
            }

            R.id.menuRestartMission -> {
                lifecycleScope.launch {
                    configureMission(false)
                    android.widget.Toast.makeText(
                        this@MissionActivity,
                        getString(R.string.mission_restarted),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
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
            sequence = MediaPlayerMainMission(
                this@MissionActivity,
                stopWatch, preferences
            )
            withContext(eventParserDispatcher) {
                EventListParserFactory.getInstance().getParser(this@MissionActivity)
                    .parse(missionType.buildEvents(preferences), sequence)
            }
        } else {
            sequence!!.reset()
            stopWatch.reset()
        }

        runOnUiThread {
            missionLogs.clear()
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
            Timber.v("dumping full mission log")
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
            hideSystemUI()
        }
    }

    private fun pauseMission() {
        sequence?.apply {
            pause()
            toggleOff()
            setKeepScreenOn(false)
        }
        showSystemUI()
    }

    private fun stopMission() {
        sequence?.apply {
            stop()
            toggleOff()
            setKeepScreenOn(false)
        }
        showSystemUI()
    }

    private fun setKeepScreenOn(keepScreenOn: Boolean) = runOnUiThread {
        if (keepScreenOn) {
            hideSystemUI()
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun updateMissionLog(missionLog: MissionLog) = runOnUiThread {
        missionLogs.add(missionLog)
    }

    fun clearMissionLog() = runOnUiThread {
        missionLogs.clear()
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
            lifecycleScope.launch {
                configureMission(false)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    public override fun onDestroy() {
        super.onDestroy()

        // Cancel any ongoing notifications
        NotificationManagerCompat.from(this).apply {
            cancelAll()
        }
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

        // On Android 13+, POST_NOTIFICATIONS requires a runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_REQUEST_CODE
            )
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 1001

        private const val MEDIA_ACTION = "com.boarbeard.spacealert.media.action"
    }
}