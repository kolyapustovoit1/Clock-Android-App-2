package stu.cn.ua.clock.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import stu.cn.ua.clock.contracts.NavContract

/**
 * Base fragment class that provides common functionality for all fragments in the app.
 * It handles navigation and screen title updating.
 */
open class BaseFragment(
    private val contentLayoutId: Int
) : Fragment() {

    private var navContract: NavContract? = null

    /**
     * Called to inflate the view for the fragment.
     * Inflates the layout resource specified by the contentLayoutId.
     * @param inflater The LayoutInflater object to inflate views.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle containing saved instance state, if available.
     * @return The view for the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(contentLayoutId, container, false)
    }

    /**
     * Called when the fragment is attached to its parent activity.
     * Ensures that the activity implements the NavContract interface.
     * @param context The context to which the fragment is attached.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavContract) {
            navContract = context
        } else {
            throw IllegalStateException("Activity must implement NavContract")
        }
    }

    /**
     * Called when the fragment is detached from its parent activity.
     * Clears the reference to the NavContract.
     */
    override fun onDetach() {
        super.onDetach()
        navContract = null
    }

    /**
     * Executes a navigation action provided by the NavContract.
     * Ensures that the NavContract is attached before calling the action.
     * @param action The navigation action to execute.
     */
    protected fun navigate(action: NavContract.() -> Unit) {
        navContract?.action() ?: throw IllegalStateException("NavContract is not attached")
    }

    /**
     * Updates the screen title by invoking the `updateScreenTitle` method from the navigation contract.
     * @param stringResourceId The resource ID of the string to be used as the screen title.
     */
    protected fun setScreenTitle(stringResourceId: Int) {
        navContract?.updateScreenTitle(getString(stringResourceId))
    }
}