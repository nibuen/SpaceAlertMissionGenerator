package com.boarbeard.audio

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import android.text.Html
import android.text.TextUtils
import android.widget.Toast
import com.boarbeard.R
import com.boarbeard.audio.parser.EventListParserFactory
import com.boarbeard.ui.MissionActivity
import com.boarbeard.ui.StopWatch
import kotlinx.coroutines.android.asCoroutineDispatcher
import java.util.concurrent.TimeUnit

class MediaPlayerMainMission(
    protected var missionActivity: MissionActivity,
    protected val missionLog: List<MissionLog>,
    protected var stopWatch: StopWatch, // Displays the log texts, so needs
    private val preferences: SharedPreferences
) : MediaPlayerSequence(missionActivity) {
    private val timerHandler = HandlerThread("TimerHandler")
        .apply { start() }
        .looper.let { Handler(it) }
    private var startTime: Long = 0
    private var timeWhenPaused: Long = 0

    private lateinit var mediaPlayerBackgroundSounds: MediaPlayerCycle

    // log color preferences
    private val mPlayNextAudioTask = Runnable {
        mediaPlayerBackgroundSounds.pause()
        playNextAudio()
    }

    init {
        // Displays the log texts, so needs log
        // color preferences
        init()
        mediaPlayerBackgroundSounds = MediaPlayerCycle(missionActivity)
        EventListParserFactory.getInstance().getParser(missionActivity)
            .createAmbiance(mediaPlayerBackgroundSounds)
    }

    @Synchronized
    override fun start() {
        // Clear the mission introduction when the mission is started (not resumed after a pause)
        if (stopWatch.missionTimeInNanos() == 0L) {
            missionActivity.clearMissionLog()
        }
        if (mediaPlayerList.size <= playerIndex) {
            init()
        }
        stopWatch.start()
        stopped = false
        if (paused) {
            paused = false

            // Even when a running sound was paused, you have to update the
            // startTime for the next file
            startTime += System.nanoTime() - timeWhenPaused
            if (activeMediaPlayer == null) {
                mediaPlayerBackgroundSounds.start()
            } else {
                activeMediaPlayer.start()
            }
            if (activeMediaPlayer == null || activeMediaPlayer.isLooping) {
                postAtTime(
                    mPlayNextAudioTask, startTime
                            + mediaPlayerList[playerIndex].startTimeNanos
                )
            }
            return
        }
        startTime = System.nanoTime()
        playNextAudio()
    }

    @Synchronized
    fun init() {
        playerIndex = 0
        activeMediaPlayer = null
        missionActivity.clearMissionLog()
        stopWatch.reset()
    }

    @Synchronized
    private fun playNextAudio() {
        if (activeMediaPlayer != null) {
            // TODO reuse player
            activeMediaPlayer.release()
        }
        val mediaInfo = mediaPlayerList[playerIndex]
        activeMediaPlayer = MediaPlayer.create(
            missionActivity,
            mediaInfo.resUri
        )
        updateMissionLog(mediaInfo)
        if (mediaInfo.isLoopUntilNext
            && playerIndex + 1 < mediaPlayerList.size
        ) {
            activeMediaPlayer.isLooping = true
            planNextAudioTask()
        } else {
            activeMediaPlayer
                .setOnCompletionListener {
                    activeMediaPlayer.stop()
                    activeMediaPlayer.release()
                    activeMediaPlayer = null
                    if (!stopped) {
                        planNextAudioTask()
                    }
                }
        }
        if (!paused && !stopped) {
            try {
                activeMediaPlayer.start()
            } catch (e: IllegalStateException) {
                Toast.makeText(
                    missionActivity, R.string.media_failed,
                    Toast.LENGTH_LONG
                ).show()
                stop()
            }
        }
    }

    private fun updateMissionLog(mediaInfo: MediaInfo) {
        if (!TextUtils.isEmpty(mediaInfo.toString())) {
            val bLogColors = preferences.getBoolean("logColorsPreference", true)
            val timeText = StringBuilder()
            val actionText = StringBuilder()

            // Display log in colors (if preference is set to true)
            // Note: Some parts are now bold, regardless of the color settings
            // (Time, Unconfirmed, Serious - like the printed cards)
            if (mediaInfo.timeColor == null || !bLogColors) {
                timeText.append(
                    "<b>" + stopWatch.toString()
                            + "</b>"
                )
            } else {
                timeText.append(
                    ("<b><font color=\""
                            + mediaInfo.timeColor + "\">"
                            + StopWatch.formatTime(mediaInfo.startTimeNanos)) + "</b></font>"
                )
            }
            if (mediaInfo.timeColor == null || !bLogColors) {
                actionText.append(mediaInfo.toString())
            } else {
                actionText.append(
                    "<font color=\""
                            + mediaInfo.textColor + "\">"
                            + mediaInfo.toString() + "</font>"
                )
            }
            missionActivity.updateMissionLog(
                missionLog = MissionLog(
                    Html.fromHtml(actionText.toString(), 0),
                    Html.fromHtml(timeText.toString(), 0)
                ), missionLog.size - 1
            )
        }
    }

    @Synchronized
    override fun pause() {
        super.pause()
        stopWatch.pause()
        mediaPlayerBackgroundSounds.pause()
        missionActivity.toggleOff()
        timeWhenPaused = System.nanoTime()
        timerHandler.removeCallbacks(mPlayNextAudioTask)
    }

    @Synchronized
    override fun stop() {
        super.stop()
        missionActivity.toggleOff()
        mediaPlayerBackgroundSounds.stop()
        stopWatch.stop()
        timerHandler.removeCallbacks(mPlayNextAudioTask)
    }

    /**
     *
     */
    private fun planNextAudioTask() {
        playerIndex++
        if (mediaPlayerList.size > playerIndex) {
            val nextTime = (startTime
                    + mediaPlayerList[playerIndex].startTimeNanos)
            if (activeMediaPlayer == null
                && nextTime > System.nanoTime()
            ) {
                mediaPlayerBackgroundSounds.start()
            }
            postAtTime(mPlayNextAudioTask, nextTime)
        } else {
            stop()
        }
    }

    private fun postAtTime(runnable: Runnable, timeInNanos: Long) {
        val timeInMillis = TimeUnit.MILLISECONDS.convert(timeInNanos, TimeUnit.NANOSECONDS)
        timerHandler.postAtTime(runnable, timeInMillis)
    }

    /**
     * Dumps the entire mission to the mission log.
     */
    fun dumpMissionTreeToLog() {
        mediaPlayerList.forEach { mediaInfo ->
            updateMissionLog(mediaInfo)
        }
    }

    /*
     * Prints the mission introduction to the log.
    */
    fun printMissionIntroduction(missionIntroduction: String?) {
        missionActivity.updateMissionLog(MissionLog.formatIntro(
            missionActivity.resources,
            missionIntroduction
        ), missionLog.size - 1)
    }
}