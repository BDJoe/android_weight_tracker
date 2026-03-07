package com.josephlimbert.weighttracker.view

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.ui.WeightViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {
    private val weightViewModel: WeightViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        // Initialize variables
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
//        val userViewModel =
//            ViewModelProvider(requireActivity()).get<UserViewModel>(UserViewModel::class.java)
//        val weightViewModel = ViewModelProvider(requireActivity()).get<WeightViewModel>(
//            WeightViewModel::class.java
//        )
        val weightUnits = getString(R.string.unit_pounds)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    weightViewModel.startingWeight.collect { weight ->
                        val weightText = if (weight != null) weight.weight
                            .toString() + weightUnits else "N/A"
                        startingWeightText.text = weightText
                        val startDateString = if (weight != null) DateFormat.getDateInstance(
                            DateFormat.MEDIUM,
                            Locale.getDefault()
                        ).format(weight.recordedDate.toDate()) else "N/A"
                        startDateText.text = startDateString
                    }
                }
                launch {
                    weightViewModel.goalWeight.collect { weight ->
                        if (weight != null) {
                            if (weight > 0) {
                                val weightText = weight.toString() + weightUnits
                                goalWeightText.text = weightText
                                setGoalButton.visibility = View.GONE
                            } else {
                                goalWeightText.text = "N/A"
                                setGoalButton.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                launch {
                    weightViewModel.currentWeight.collect { weight ->
                        val weightText = if (weight != null) weight.weight
                            .toString() + weightUnits else "N/A"
                        currentWeightText.text = weightText
                    }
                }
                launch {
                    weightViewModel.totalLossPercent.collect { total ->
                        val percentText = total.toInt().toString() + "%"
                        percentageText.text = percentText
                        progressBar.progress = total.toFloat()
                    }
                }
                launch {
                    weightViewModel.totalLossWeight.collect { weight ->
                        val weightText = weight.toString() + weightUnits
                        totalLossText.text = weightText
                    }
                }
                launch {
                    weightViewModel.targetLoss.collect { weight ->
                        val weightText = weight.toString() + weightUnits
                        targetLossText.text = weightText
                    }
                }
                launch {
                    weightViewModel.targetLeft.collect { weight ->
                        val weightText = weight.toString() + weightUnits
                        targetLeftText.text = weightText
                    }
                }
            }
        }

//        userViewModel.getAuthUser()
//            .observe(getViewLifecycleOwner(), Observer { firebaseUser: FirebaseUser? ->
//                if (firebaseUser == null) return@observe
//                if (firebaseUser.isAnonymous()) {
//                    signInReminder.setVisibility(View.VISIBLE)
//                } else {
//                    signInReminder.setVisibility(View.GONE)
//                }
//
//                weightViewModel.getGoalWeight()
//                    .observe(getViewLifecycleOwner(), Observer { goalWeight: Float? ->
//                        if (goalWeight!! > 0) {
//                            val weightText = goalWeight.toString() + weightUnits
//                            goalWeightText.setText(weightText)
//                            setGoalButton.setVisibility(View.GONE)
//                        } else {
//                            goalWeightText.setText("N/A")
//                            setGoalButton.setVisibility(View.VISIBLE)
//                        }
//                    })
//
//                // Get the current weight and set the text on the view. Set to N/A if no weight data.
//                weightViewModel.getCurrentWeight()
//                    .observe(getViewLifecycleOwner(), Observer { weight: Weight? ->
//                        val weightText = if (weight != null) weight.getWeight()
//                            .toString() + weightUnits else "N/A"
//                        currentWeightText.setText(weightText)
//                    })
//                // Get the starting weight and set the text on the view. Set to N/A if no weight data.
//                weightViewModel.getStartingWeight()
//                    .observe(getViewLifecycleOwner(), Observer { weight: Weight? ->
//                        val weightText = if (weight != null) weight.getWeight()
//                            .toString() + weightUnits else "N/A"
//                        startingWeightText.setText(weightText)
//                        val startDateString = if (weight != null) DateFormat.getDateInstance(
//                            DateFormat.MEDIUM,
//                            Locale.getDefault()
//                        ).format(weight.getRecordedDate().toDate()) else "N/A"
//                        startDateText.setText(startDateString)
//                    })
//                // Get the percentage of weight loss and set the text on the view.
//                weightViewModel.getTotalLossPercent()
//                    .observe(getViewLifecycleOwner(), Observer { total: Float? ->
//                        val percentText = if (total != null) total.toInt() + "%" else "N/A"
//                        percentageText.setText(percentText)
//                        progressBar.progress = if (total != null) total else 0f
//                    })
//                // get the weight loss in pounds and set the text on the view
//                weightViewModel.getTotalLossWeight()
//                    .observe(getViewLifecycleOwner(), Observer { total: Float? ->
//                        val weightText =
//                            if (total != null) total.toString() + weightUnits else "N/A"
//                        totalLossText.setText(weightText)
//                    })
//                // get the weight loss in pounds and set the text on the view. Set to N/A if no data returned
//                weightViewModel.getTargetLoss()
//                    .observe(getViewLifecycleOwner(), Observer { weight: Float? ->
//                        val weightText =
//                            if (weight != null) weight.toString() + weightUnits else "N/A"
//                        targetLossText.setText(weightText)
//                    })
//                // get the weight left to lose and set the text on the view
//                weightViewModel.getTargetLeft()
//                    .observe(getViewLifecycleOwner(), Observer { weight: Float? ->
//                        val weightText =
//                            if (weight != null) weight.toString() + weightUnits else "N/A"
//                        targetLeftText.setText(weightText)
//                    })
//                weightViewModel.checkGoalReached()
//                    .observe(getViewLifecycleOwner(), Observer { isReached: Boolean? ->
//                        if (isReached) sendSms()
//                    })
//            })

        signInButton.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
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
        if (menuItem.getItemId() == R.id.add_weight_menu_item) {
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