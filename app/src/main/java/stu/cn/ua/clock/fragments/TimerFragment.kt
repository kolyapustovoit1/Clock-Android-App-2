package stu.cn.ua.clock.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.ArrayAdapter
import kotlinx.coroutines.*

import stu.cn.ua.clock.R

/**
 * TimerFragment is responsible for handling the timer functionality in the app.
 * It allows users to start, pause, stop, and record lap times. The fragment
 * displays the elapsed time, including milliseconds, and maintains a list
 * of recorded lap times. It provides the following features:
 *
 * - Start: Begins the timer and starts tracking the elapsed time.
 * - Pause: Pauses the timer and allows it to be resumed.
 * - Continue: Resumes the timer from the paused state.
 * - Stop: Stops the timer, resets the time, and disables all buttons.
 * - Lap: Records the current time as a lap without stopping the timer.
 *
 * The timer updates every 10 milliseconds and displays the elapsed time
 * in the format: mm:ss:SS (minutes:seconds:milliseconds).
 * The lap times are stored and displayed in a ListView, showing the time
 * taken for each lap from the previous lap or start point.
 */
class TimerFragment : BaseFragment(R.layout.fragment_timer) {

    private lateinit var textTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnLap: Button
    private lateinit var listLaps: ListView

    private var timerRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var lastLapTime = 0L
    private var lapsCount = 0
    private val lapTimes = mutableListOf<String>()
    private var timerJob: Job? = null  // Coroutine job to handle the timer updates

    /**
     * Called when the fragment's view is created. Initializes UI elements,
     * sets up button listeners, and prepares the lap times ListView.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenTitle(R.string.timer_screen_title)

        // Initialize UI elements
        textTimer = view.findViewById(R.id.text_timer)
        btnStart = view.findViewById(R.id.btn_start)
        btnPause = view.findViewById(R.id.btn_pause)
        btnStop = view.findViewById(R.id.btn_stop)
        btnLap = view.findViewById(R.id.btn_lap)
        listLaps = view.findViewById(R.id.list_laps)

        // Set up Lap times list adapter
        val adapter = ArrayAdapter(requireContext(), R.layout.timer_list_item, lapTimes)
        listLaps.adapter = adapter

        // Set up button listeners
        setupButtonListeners(adapter)
    }

    /**
     * Sets up listeners for the timer control buttons.
     * Listens for start, pause, stop, and lap events and calls the
     * appropriate methods based on the user interaction.
     */
    private fun setupButtonListeners(adapter: ArrayAdapter<String>) {
        btnStart.setOnClickListener {
            startTimer()
        }

        btnPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                continueTimer()
            }
        }

        btnStop.setOnClickListener {
            stopTimer()
            adapter.notifyDataSetChanged()  // Refresh ListView if necessary
        }

        btnLap.setOnClickListener {
            if (timerRunning) {
                recordLap(adapter)
            } else {
                clearLaps(adapter)
            }
        }
    }

    /**
     * Starts the timer and initiates the time tracking.
     */
    private fun startTimer() {
        timerRunning = true
        startTime = System.currentTimeMillis() - elapsedTime
        runTimer()
        toggleButtonsOnStart()
    }

    /**
     * Pauses the timer and saves the elapsed time.
     */
    @SuppressLint("SetTextI18n")
    private fun pauseTimer() {
        timerRunning = false
        elapsedTime = System.currentTimeMillis() - startTime
        btnPause.text = "Continue"
        timerJob?.cancel()  // Cancel the ongoing coroutine
    }

    /**
     * Resumes the timer from the paused state.
     */
    @SuppressLint("SetTextI18n")
    private fun continueTimer() {
        timerRunning = true
        startTime = System.currentTimeMillis() - elapsedTime
        runTimer()  // Restart the coroutine
        btnPause.text = "Pause"
    }

    /**
     * Stops the timer, resets the time and updates the button states.
     */
    @SuppressLint("SetTextI18n")
    private fun stopTimer() {
        timerRunning = false
        elapsedTime = 0L
        lastLapTime = 0L
        updateTimerDisplay()
        toggleButtonsOnStop()
        timerJob?.cancel()  // Cancel the coroutine when the timer stops
        btnLap.text = "Clear"  // Change the button text to "Clear"
    }

    /**
     * Records the current lap time and updates the ListView.
     */
    private fun recordLap(adapter: ArrayAdapter<String>) {
        lapsCount += 1
        val lapTime = (System.currentTimeMillis() - startTime) - lastLapTime
        lapTimes.add("Lap $lapsCount - ${formatTime(lapTime)}" )
        lastLapTime = System.currentTimeMillis() - startTime
        adapter.notifyDataSetChanged()
    }

    /**
     * Clears the lap times list and updates the ListView.
     */
    @SuppressLint("SetTextI18n")
    private fun clearLaps(adapter: ArrayAdapter<String>) {
        lapTimes.clear()
        textTimer.text = "00:00:00"
        lapsCount = 0
        adapter.notifyDataSetChanged()
        btnLap.text = "Lap"  // Change the button text back to "Lap"
    }

    /**
     * Updates the displayed timer with the current elapsed time.
     */
    private fun updateTimerDisplay() {
        val currentTime = System.currentTimeMillis() - startTime
        val minutes = (currentTime / 1000) / 60
        val seconds = (currentTime / 1000) % 60
        val milliseconds = currentTime % 1000
        textTimer.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds / 10)  // Display milliseconds as 2 digits
    }

    /**
     * Formats the given time in milliseconds into a string in mm:ss:SS format.
     */
    private fun formatTime(time: Long): String {
        val minutes = (time / 1000) / 60
        val seconds = (time / 1000) % 60
        val milliseconds = time % 1000
        return String.format("%02d:%02d:%02d", minutes, seconds, milliseconds / 10)  // Display milliseconds as 2 digits
    }

    /**
     * Runs the timer and updates the UI every 10 milliseconds.
     */
    private fun runTimer() {
        // Launch the coroutine to update the timer every 10ms
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timerRunning) {
                delay(10)  // Delay for 10ms to update frequently
                updateTimerDisplay()
            }
        }
    }

    /**
     * Toggles the buttons when the timer is started.
     */
    private fun toggleButtonsOnStart() {
        btnStart.isEnabled = false
        btnPause.isEnabled = true
        btnStop.isEnabled = true
        btnLap.isEnabled = true
    }

    /**
     * Resets the buttons when the timer is stopped.
     */
    private fun toggleButtonsOnStop() {
        btnStart.isEnabled = true
        btnPause.isEnabled = false
        btnStop.isEnabled = false
        btnLap.isEnabled = true  // Keep Lap button enabled when stopped
    }
}