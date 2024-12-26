package stu.cn.ua.clock.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button
import stu.cn.ua.clock.R
import java.util.TimeZone

/**
 * Fragment for handling app settings.
 * Allows the user to select a timezone and save preferences.
 */
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private lateinit var sharedPreferences: SharedPreferences

    /**
     * Called when the view is created.
     * Sets up the timezone dropdown and handles save button logic.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenTitle(R.string.settings_screen_title)

        // Initialize SharedPreferences for saving timezone selection
        sharedPreferences = requireContext().getSharedPreferences("ClockAppPreferences", Context.MODE_PRIVATE)

        val spinner: Spinner = view.findViewById(R.id.timezone_spinner)
        val saveButton: Button = view.findViewById(R.id.btn_save)

        setupTimezoneSpinner(spinner)
        setupSaveButton(spinner, saveButton)
    }

    /**
     * Configures the timezone spinner with available timezones.
     * @param spinner The Spinner UI element for selecting timezones.
     */
    private fun setupTimezoneSpinner(spinner: Spinner) {
        // Retrieve the list of available time zones with GMT offsets
        val timeZones = TimeZone.getAvailableIDs().map { id ->
            val offset = TimeZone.getTimeZone(id).rawOffset / 3600000
            Pair("GMT${if (offset >= 0) "+" else ""}$offset ($id)", offset)
        }

        // Sort the time zones by their GMT offset (negative offsets come first)
        val sortedTimeZones = timeZones.sortedBy { it.second }

        // Prepare a list of time zones for display in the spinner
        val displayList = sortedTimeZones.map { it.first }

        // Create an ArrayAdapter and set it to the spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, displayList)
        spinner.adapter = adapter

        // Retrieve the saved time zone from SharedPreferences and set it as the default selection
        val savedTimeZone = sharedPreferences.getString("selected_timezone", null)
        if (savedTimeZone != null) {
            val index = displayList.indexOfFirst { zone -> zone.contains(savedTimeZone) }
            if (index >= 0) {
                spinner.setSelection(index)
            }
        } else {
            // If no timezone is saved, set the system default time zone as the selection
            val defaultTimeZone = getSystemTimeZone()
            val index = displayList.indexOf(defaultTimeZone)
            if (index >= 0) {
                spinner.setSelection(index)
            }
        }
    }

    /**
     * Gets the system's default timezone in the "GMT+X (ID)" format.
     * @return The system's default timezone string.
     */
    private fun getSystemTimeZone(): String {
        val timeZone = TimeZone.getDefault()
        val offset = timeZone.rawOffset / 3600000
        val id = timeZone.id
        return "GMT${if (offset >= 0) "+" else ""}$offset ($id)"
    }

    /**
     * Sets up the save button to store the selected timezone in SharedPreferences.
     * @param spinner The Spinner containing timezone options.
     * @param button The Button for saving the selected timezone.
     */
    private fun setupSaveButton(spinner: Spinner, button: Button) {
        button.setOnClickListener {
            val selectedTimeZone = spinner.selectedItem.toString()
            // If no timezone is selected, use the system default timezone
            val timeZoneToSave = if (selectedTimeZone.isEmpty()) getSystemTimeZone() else selectedTimeZone

            sharedPreferences.edit()
                .putString("selected_timezone", timeZoneToSave)
                .apply()
        }
    }
}