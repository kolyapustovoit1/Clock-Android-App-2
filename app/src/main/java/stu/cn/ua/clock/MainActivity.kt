package stu.cn.ua.clock

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import stu.cn.ua.clock.contracts.NavContract
import stu.cn.ua.clock.fragments.AboutFragment
import stu.cn.ua.clock.fragments.ClockFragment
import stu.cn.ua.clock.fragments.SettingsFragment
import stu.cn.ua.clock.fragments.TimerFragment

class MainActivity : AppCompatActivity(), NavContract {

    /**
     * Called when the activity is created.
     * Initializes the UI, handles SplashScreen, system bars, and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display splash screen for 2 seconds
        Thread.sleep(2000)
        installSplashScreen()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupSystemBars()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initializeDefaultFragment(savedInstanceState)
        setupAboutButton()

        handleBackPressed()
    }

    /**
     * Sets up system bars (status bar and navigation bar).
     * Adjusts padding for system bars and sets the status bar color.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
            window.statusBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            window.statusBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
        }
    }

    /**
     * Sets up the About button click listener.
     * Navigates to the About screen when clicked.
     */
    private fun setupAboutButton() {
        val aboutButton: ImageButton = findViewById(R.id.btnAbout)
        aboutButton.setOnClickListener { toAboutScreen() }
    }

    /**
     * Registers a custom back button handler.
     * Handles back press behavior to navigate within the fragment stack or exit the activity.
     */
    private fun handleBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back()
            }
        })
    }

    /**
     * Initializes the default fragment (stu.cn.ua.clock.fragments.stu.cn.ua.clock.fragments.ClockFragment) when the activity is first created.
     * Loads the stu.cn.ua.clock.fragments.stu.cn.ua.clock.fragments.ClockFragment if no saved instance state exists.
     */
    private fun initializeDefaultFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            navigateTo(ClockFragment(), false)
        }
    }

    /**
     * Navigates to the specified fragment and optionally adds it to the back stack.
     * @param fragment The fragment to navigate to.
     * @param addToBackStack Whether the transaction should be added to the back stack.
     */
    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    /**
     * Navigates to the Clock fragment.
     */
    override fun toClockScreen() {
        navigateTo(ClockFragment(), false)
        updateScreenTitle("Clock")
    }

    /**
     * Navigates to the Timer fragment.
     */
    override fun toTimerScreen() {
        navigateTo(TimerFragment())
        updateScreenTitle("Timer")
    }

    /**
     * Navigates to the Settings fragment.
     */
    override fun toSettingsScreen() {
        navigateTo(SettingsFragment())
        updateScreenTitle("Settings")
    }

    /**
     * Navigates to the About fragment.
     */
    override fun toAboutScreen() {
        navigateTo(AboutFragment())
        updateScreenTitle("About")
    }

    /**
     * Handles the back button behavior.
     * If there are fragments in the back stack, pops the top fragment.
     * Otherwise, finishes the activity.
     */
    override fun back() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    /**
     * Updates the screen title displayed in the UI.
     * @param title The title to display.
     */
    override fun updateScreenTitle(title: String) {
        val titleTextView = findViewById<TextView>(R.id.tvScreenTitle)
        titleTextView.text = title
    }

    /**
     * Handles the back button click on the toolbar.
     * If the back button is clicked, it triggers the back navigation.
     * @param item The menu item that was selected.
     * @return True if the item selection is handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                back() // This handles the back navigation
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}