package stu.cn.ua.clock.fragments

import android.os.Bundle
import android.view.View
import stu.cn.ua.clock.R

/**
 * Fragment for displaying information about the application.
 */
class AboutFragment : BaseFragment(R.layout.fragment_about) {

    /**
     * Called when the fragment's view is created.
     * Sets up the screen title to "About".
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState A Bundle containing the fragment's previously saved state, if any.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setScreenTitle(R.string.about_screen_title)
    }
}