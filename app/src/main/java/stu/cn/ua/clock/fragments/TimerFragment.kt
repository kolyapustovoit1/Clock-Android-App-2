package stu.cn.ua.clock.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import stu.cn.ua.clock.R
import stu.cn.ua.clock.services.TimerService

/**
 * TimerFragment handles the timer UI and provides user interactions for starting, pausing, stopping,
 * and recording lap times. It communicates with TimerService to handle the background operations.
 */
class TimerFragment : BaseFragment(R.layout.fragment_timer) {

    // UI components
    private lateinit var textTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnLap: Button
    private lateinit var listLaps: ListView

    private val lapTimes = mutableListOf<String>()

    // Service and timer states
    private var timerService: TimerService? = null
    private var isTimerRunning = false
    private var isTimerPaused = false

    // Connection to TimerService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            setupTimerServiceListener()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
        }
    }

    /**
     * Sets up the listener for TimerService to update the UI based on timer events.
     */
    private fun setupTimerServiceListener() {
        timerService?.setTimerListener(object : TimerService.TimerServiceListener {
            override fun onTimerUpdated(time: String) {
                textTimer.text = time
            }

            override fun onLapRecorded(lapTime: String, lapCount: Int) {
                lapTimes.add("Lap $lapCount - $lapTime")
                (listLaps.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onTimerStopped() {
                resetTimerState()
            }
        })
    }

    /**
     * Initializes the UI components, sets up listeners, and binds to TimerService.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenTitle(R.string.timer_screen_title)

        bindTimerService()
        initializeUI(view)
        setupButtonListeners()
    }

    /**
     * Binds the fragment to TimerService.
     */
    private fun bindTimerService() {
        val intent = TimerService.getIntent(requireContext())
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Initializes UI components and sets up the lap times adapter.
     */
    private fun initializeUI(view: View) {
        textTimer = view.findViewById(R.id.text_timer)
        btnStart = view.findViewById(R.id.btn_start)
        btnPause = view.findViewById(R.id.btn_pause)
        btnStop = view.findViewById(R.id.btn_stop)
        btnLap = view.findViewById(R.id.btn_lap)
        listLaps = view.findViewById(R.id.list_laps)

        listLaps.adapter = ArrayAdapter(requireContext(), R.layout.timer_list_item, lapTimes)
    }

    /**
     * Sets up listeners for all timer control buttons.
     */
    private fun setupButtonListeners() {
        btnStart.setOnClickListener { startTimer() }
        btnPause.setOnClickListener { togglePauseResumeTimer() }
        btnStop.setOnClickListener { stopTimer() }
        btnLap.setOnClickListener { handleLapOrClearAction() }
    }

    /**
     * Starts the timer and updates the UI states.
     */
    private fun startTimer() {
        timerService?.startTimer()
        isTimerRunning = true
        isTimerPaused = false
        updateButtonStates()
    }

    /**
     * Toggles between pausing and resuming the timer, updating UI states accordingly.
     */
    private fun togglePauseResumeTimer() {
        isTimerPaused = if (isTimerPaused) {
            timerService?.continueTimer()
            false
        } else {
            timerService?.pauseTimer()
            true
        }
        updateButtonStates()
    }

    /**
     * Stops the timer and resets UI states.
     */
    private fun stopTimer() {
        timerService?.stopTimer()
        resetTimerState()
    }

    /**
     * Handles lap recording or clearing all lap times based on the current button state.
     */
    private fun handleLapOrClearAction() {
        if (btnLap.text == "Lap") {
            timerService?.recordLap()
        } else if (btnLap.text == "Clear") {
            clearLaps()
        }
    }

    /**
     * Updates the states of control buttons based on the timer status.
     */
    @SuppressLint("SetTextI18n")
    private fun updateButtonStates() {
        btnPause.text = if (isTimerRunning && isTimerPaused) "Continue" else "Pause"
        btnLap.text = if (isTimerRunning) "Lap" else "Clear"

        btnPause.isEnabled = isTimerRunning
        btnStart.isEnabled = !isTimerRunning
        btnStop.isEnabled = isTimerRunning
    }

    /**
     * Clears all lap times and resets the UI.
     */
    @SuppressLint("SetTextI18n")
    private fun clearLaps() {
        lapTimes.clear()
        textTimer.text = "00:00:00"
        (listLaps.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        resetTimerState()
    }

    /**
     * Resets the timer state and updates the UI.
     */
    private fun resetTimerState() {
        isTimerRunning = false
        isTimerPaused = false
        updateButtonStates()
    }
}