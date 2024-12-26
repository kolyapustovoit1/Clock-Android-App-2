package stu.cn.ua.clock.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import stu.cn.ua.clock.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying the current time, date, and timezone.
 * Updates in real-time based on the selected timezone in settings.
 */
class ClockFragment : BaseFragment(R.layout.fragment_clock) {
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 1000 // Update interval in milliseconds
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * Called when the view is created.
     * Sets up the clock display and starts periodic updates.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenTitle(R.string.clock_screen_title)

        // Initialize SharedPreferences for reading the selected timezone
        sharedPreferences = requireContext().getSharedPreferences("ClockAppPreferences", Context.MODE_PRIVATE)

        // Start the periodic clock updates
        startClockUpdates(view)
        setupButtonListeners(view)
    }

    /**
     * Set up the listeners for the timer and settings buttons.
     */
    private fun setupButtonListeners(view: View) {
        view.findViewById<Button>(R.id.btn_timer).setOnClickListener {
            navigate { toTimerScreen() }
        }

        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            navigate { toSettingsScreen() }
        }
    }

    /**
     * Starts periodic updates for the clock display.
     * @param view The root view of the fragment.
     */
    private fun startClockUpdates(view: View) {
        val updateRunnable = object : Runnable {
            override fun run() {
                updateClockDisplay(view)
                handler.postDelayed(this, updateInterval)
            }
        }
        handler.post(updateRunnable)
    }

    /**
     * Updates the displayed time, date, and timezone based on the selected settings.
     * @param view The root view of the fragment.
     */
    private fun updateClockDisplay(view: View) {
        val selectedTimeZone = sharedPreferences.getString("selected_timezone", null)
        val timeZone = if (selectedTimeZone != null) {
            val zoneId = selectedTimeZone.substringAfter("(").substringBefore(")")
            TimeZone.getTimeZone(zoneId)
        } else {
            TimeZone.getDefault()
        }

        view.findViewById<TextView>(R.id.text_time).text = formatTime(Date(), timeZone)
        view.findViewById<TextView>(R.id.text_date).text = formatDate(Date(), timeZone)
        view.findViewById<TextView>(R.id.text_time_zone).text = formatTimeZone(timeZone)
    }

    /**
     * Formats the given time into "HH:mm" format.
     * @param date The current date.
     * @param timeZone The timezone to format the time for.
     * @return The formatted time string.
     */
    private fun formatTime(date: Date, timeZone: TimeZone): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.timeZone = timeZone
        return timeFormat.format(date)
    }

    /**
     * Formats the given date into "EEEE, dd MMM yyyy" format.
     * @param date The current date.
     * @param timeZone The timezone to format the date for.
     * @return The formatted date string.
     */
    private fun formatDate(date: Date, timeZone: TimeZone): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

    /**
     * Formats the timezone into "GMT+/-X (ID)" format.
     * @param timeZone The timezone to format.
     * @return The formatted timezone string.
     */
    private fun formatTimeZone(timeZone: TimeZone): String {
        val gmtOffset = timeZone.rawOffset / 3600000 // Offset in hours
        val timeZoneID = timeZone.id // Get the timezone ID (e.g., Europe/Kiev)
        return "GMT${if (gmtOffset >= 0) "+" else ""}$gmtOffset ($timeZoneID)"
    }

    /**
     * Called when the fragment view is destroyed.
     * Stops periodic updates to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}