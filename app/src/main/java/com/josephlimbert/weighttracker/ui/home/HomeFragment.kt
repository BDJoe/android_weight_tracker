package com.josephlimbert.weighttracker.ui.home

import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.loadingindicator.LoadingIndicator
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.ui.sheet.AddWeightSheetFragment
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import com.josephlimbert.weighttracker.ui.sheet.SetGoalWeightFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        // Initialize variables
        val loadingLayout = rootView.findViewById<LoadingIndicator>(R.id.loading_layout)
        val homeLayout = rootView.findViewById<ConstraintLayout>(R.id.home_layout)
        val addWeightFab = rootView.findViewById<FloatingActionButton>(R.id.add_weight_fab)
        val startingWeightText = rootView.findViewById<TextView>(R.id.start_weight_text)
        val currentWeightText = rootView.findViewById<TextView>(R.id.current_weight_text)
        val goalWeightText = rootView.findViewById<TextView>(R.id.goal_weight_text)
        val percentageText = rootView.findViewById<TextView>(R.id.progress_percent_text)
        val progressBar = rootView.findViewById<CircularSeekBar>(R.id.progress_bar)
        val setGoalButton = rootView.findViewById<Button>(R.id.set_goal_button)
        val targetLossText = rootView.findViewById<TextView>(R.id.target_loss_text)
        val totalLossText = rootView.findViewById<TextView>(R.id.total_loss_text)
        val targetLeftText = rootView.findViewById<TextView>(R.id.target_left_text)
        val startDateText = rootView.findViewById<TextView>(R.id.start_date_text)

        val signInReminder = rootView.findViewById<ConstraintLayout>(R.id.sign_in_reminder)
        val signInButton = rootView.findViewById<Button>(R.id.sign_in_button)
        val weightUnits = getString(R.string.unit_pounds)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.auth.collect { user ->
                        if (user == null) {
                            loadingLayout.visibility = View.VISIBLE
                            homeLayout.visibility = View.GONE
                            when (val user = viewModel.createGuestAccount()) {
                                is AuthResult.Success -> {
                                    when (val result = viewModel.createUserProfile(user.data)) {
                                        is FirestoreResult.Success -> {
                                            Log.d("MAINAX", user.data.uid + "Success")
                                        }
                                        is FirestoreResult.Error -> {
                                            Log.d("MAINAX", result.message)
                                            Toast.makeText(
                                                context,
                                                result.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                                is AuthResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        user.message + "MainActivity",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            loadingLayout.visibility = View.GONE
                            homeLayout.visibility = View.VISIBLE
                            if (user.isAnonymous)
                                signInReminder.visibility = View.VISIBLE
                            else
                                signInReminder.visibility = View.GONE
                            launch {
                                viewModel.userProfile.collect { profile ->
                                    Log.d("Home", user.uid  + " "  + profile)
                                    profile?.goalWeight?.let {
                                        if (it > 0) {
                                            val goalText = it.toString() + weightUnits
                                            goalWeightText.text = goalText
                                            setGoalButton.visibility = View.GONE
                                        } else {
                                            goalWeightText.text = "N/A"
                                            setGoalButton.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }

                            launch {
                                viewModel.startingWeight.collect { weight ->
                                    val startText = if (weight != null) weight.weight
                                        .toString() + weightUnits else "N/A"
                                    startingWeightText.text = startText

                                    val startDateString = if (weight != null) DateFormat.getDateInstance(
                                        DateFormat.MEDIUM,
                                        Locale.getDefault()
                                    ).format(weight.recordedDate.toDate()) else "N/A"
                                    startDateText.text = startDateString
                                }
                            }

                            launch {
                                viewModel.currentWeight.collect { weight ->
                                    val currentText = if (weight != null) weight.weight
                                        .toString() + weightUnits else "N/A"
                                    currentWeightText.text = currentText
                                }
                            }

                            launch {
                                viewModel.totalLossPercent.collect { total ->
                                    val percentText = total.toInt().toString() + "%"
                                    percentageText.text = percentText
                                    progressBar.progress = total.toFloat()
                                }
                            }

                            launch {
                                viewModel.totalLossWeight.collect { weight ->
                                    val totalText = weight.toString() + weightUnits
                                    totalLossText.text = totalText
                                }
                            }

                            launch {
                                viewModel.targetLoss.collect { weight ->
                                    val targetLoss = weight.toString() + weightUnits
                                    targetLossText.text = targetLoss
                                }
                            }

                            launch {
                                viewModel.targetLeft.collect { weight ->
                                    val targetLeft = weight.toString() + weightUnits
                                    targetLeftText.text = targetLeft
                                }
                            }
                        }
                    }
                }
            }
        }

        signInButton.setOnClickListener(View.OnClickListener { v: View? ->
            findNavController().navigate(R.id.navigation_sign_in)
        })

        setGoalButton.setOnClickListener(View.OnClickListener { v: View? ->
            val sheet = SetGoalWeightFragment()
            sheet.show(getChildFragmentManager(), "set goal weight")
        })

        addWeightFab.setOnClickListener(View.OnClickListener { v: View? ->
            this.showAddWeightDialog(
                v
            )
        })

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED)
        return rootView
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.top_home_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.add_weight_menu_item) {
            showAddWeightDialog(requireView())
            return true
        }
        return false
    }

    // Function that will send an SMS message to the provided phone number once the goal is reached
    private fun sendSms() {
        // Get the stored phone number from shared preferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val phoneNumber: String = sharedPref.getString("userPhoneNumber", "")!!
        // if the phone number is empty we do nothing and exit
        if (phoneNumber.isBlank()) return
        // Try to send the sms to the provided phone number. If the user denied the permissions or
        // the message cannot be sent we log the error.
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                phoneNumber,
                null,
                "Congratulations on reaching your goal weight!",
                null,
                null
            )
            Log.d("SMS", "SMS sent successfully to " + phoneNumber)
        } catch (e: Exception) {
            Log.d("SMS", "SMS failed: " + e.message)
        }
    }

    // Function to show the add weight sheet
    fun showAddWeightDialog(v: View?) {
        val sheet = AddWeightSheetFragment()
        sheet.show(getParentFragmentManager(), "add weight")
    }
}