package stu.cn.ua.clock.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*

/**
 * TimerService manages timer operations: starting, pausing, resuming, stopping, and recording laps.
 * It runs independently in the background and provides updates through a listener.
 */
class TimerService : Service() {

    private val binder = TimerBinder()

    // Timer states
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var lastLapTime = 0L
    private var lapCount = 0

    private var timerJob: Job? = null
    private var listener: TimerServiceListener? = null

    /**
     * Binder class for clients to interact with the service.
     */
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    /**
     * Returns the binder to interact with the service.
     */
    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Starts the timer and initiates background updates.
     */
    fun startTimer() {
        if (!isRunning) {
            isRunning = true
            startTime = System.currentTimeMillis() - elapsedTime
            runTimer()
        }
    }

    /**
     * Pauses the timer and stops the background updates.
     */
    fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            elapsedTime = System.currentTimeMillis() - startTime
            timerJob?.cancel()
        }
    }

    /**
     * Resumes the timer from a paused state.
     */
    fun continueTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            runTimer()
        }
    }

    /**
     * Stops the timer, resets the state, and notifies the listener.
     */
    fun stopTimer() {
        isRunning = false
        elapsedTime = 0L
        lastLapTime = 0L
        lapCount = 0
        timerJob?.cancel()
        listener?.onTimerStopped()
    }

    /**
     * Records a lap time and notifies the listener.
     */
    fun recordLap() {
        if (isRunning) {
            lapCount++
            val lapTime = (System.currentTimeMillis() - startTime) - lastLapTime
            lastLapTime = System.currentTimeMillis() - startTime
            listener?.onLapRecorded(formatTime(lapTime), lapCount)
        }
    }

    /**
     * Assigns a listener to receive timer updates.
     */
    fun setTimerListener(listener: TimerServiceListener) {
        this.listener = listener
    }

    /**
     * Formats a given time in milliseconds into a string (mm:ss:SS).
     */
    private fun formatTime(time: Long): String {
        val minutes = (time / 1000) / 60
        val seconds = (time / 1000) % 60
        val milliseconds = time % 1000
        return String.format("%02d:%02d:%02d", minutes, seconds, milliseconds / 10)
    }

    /**
     * Runs the timer in the background and updates the listener every 10 milliseconds.
     */
    private fun runTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRunning) {
                delay(10)
                listener?.onTimerUpdated(formatTime(System.currentTimeMillis() - startTime))
            }
        }
    }

    /**
     * Listener interface for clients to receive timer updates and events.
     */
    interface TimerServiceListener {
        fun onTimerUpdated(time: String)
        fun onLapRecorded(lapTime: String, lapCount: Int)
        fun onTimerStopped()
    }

    /**
     * Returns an intent for binding the service.
     */
    companion object {
        fun getIntent(context: Context) = Intent(context, TimerService::class.java)
    }
}