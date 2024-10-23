package com.boarbeard.ui

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.util.concurrent.TimeUnit

class StopWatch(private val timeTextView: TextView) {
    @Volatile
    private var startTime: Long = 0

    @Volatile
    private var pauseTime: Long = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            updateClock()
            timerHandler.postDelayed(this, CLOCK_UPDATE_IN_MILLIS.toLong())
        }
    }

    init {
        reset()
    }

    fun start() {
        startTime = System.nanoTime() - missionTimeInNanos()
        pauseTime = Long.MIN_VALUE
        timerHandler.post(mUpdateTimeTask)
    }

    fun pause() {
        pauseTime = System.nanoTime()
        timerHandler.removeCallbacks(mUpdateTimeTask)
    }

    fun stop() {
        pause()
    }

    fun reset() {
        startTime = Long.MIN_VALUE
        pauseTime = Long.MIN_VALUE
        updateClock()
    }

    fun missionTimeInNanos(): Long {
        return if (isPaused) {
            pauseTime - startTime
        } else if (isRunning) {
            System.nanoTime() - startTime
        } else {
            0
        }
    }

    val isPaused: Boolean
        get() = pauseTime > startTime
    val isRunning: Boolean
        get() = pauseTime < startTime

    private fun updateClock() {
        val now = missionTimeInNanos()
        timeTextView.text = formatTime(now)
    }

    override fun toString(): String {
        return formatTime(missionTimeInNanos()).toString()
    }

    companion object {
        // Update clock twice a second
        const val CLOCK_UPDATE_IN_MILLIS = 500

        fun formatTime(nanos: Long): CharSequence {
            var seconds = TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS)
            val minutes = seconds / 60
            seconds %= 60
            val sb = StringBuilder().append(minutes).append(':')
            if (seconds < 10) {
                sb.append(0)
            }
            return sb.append(seconds)
        }
    }
}