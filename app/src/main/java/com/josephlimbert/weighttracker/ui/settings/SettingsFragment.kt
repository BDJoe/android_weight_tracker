package com.josephlimbert.weighttracker.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.materialswitch.MaterialSwitch
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.ui.UserViewModel
import com.josephlimbert.weighttracker.ui.sheet.SetGoalWeightFragment
import com.josephlimbert.weighttracker.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    var numberInput: ConstraintLayout? = null
    private val userViewModel: UserViewModel by viewModels()
    var userProfile: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        // Initialize variables
        val smsSwitch = rootView.findViewById<MaterialSwitch>(R.id.sms_switch)
        numberInput = rootView.findViewById(R.id.sms_phone_layout)
        val submitPhoneButton = rootView.findViewById<Button>(R.id.phone_submit_button)
        val editPhoneButton = rootView.findViewById<Button>(R.id.phone_edit_button)
        val editGoalWeightButton = rootView.findViewById<Button>(R.id.edit_goal_button)
        val phoneText = rootView.findViewById<TextView>(R.id.phone_input_text)
        val logoutButton = rootView.findViewById<Button>(R.id.logout_button)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.userProfile.collect { user ->
                    if (user != null) {
                        userProfile = user
                        if (user.isAnonymous) {
                            logoutButton.visibility = View.VISIBLE
                        } else {
                            logoutButton.visibility = View.VISIBLE
                        }
                        val phone = user.phone ?: ""
                        if (!phone.isEmpty()) {
                            submitPhoneButton.visibility = View.GONE
                            phoneText.text = phone
                            phoneText.isEnabled = false
                            editPhoneButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        logoutButton.setOnClickListener { _: View? ->
            logoutButton.isEnabled = false
            userViewModel.signOut()
            val intent = Intent(rootView.context, MainActivity::class.java)
            startActivity(intent)
        }

        editGoalWeightButton.setOnClickListener { _: View? ->
            // Create the add goal weight sheet and pass argument for editing
            val sheet = SetGoalWeightFragment()
            val bundle = Bundle()

            bundle.putBoolean("isEditing", true)
            sheet.setArguments(bundle)
            sheet.show(getParentFragmentManager(), "edit goal weight")
        }

        editPhoneButton.setOnClickListener { _: View? ->
            // If the phone number is set, we re-enable the text input and allow for editing it
            submitPhoneButton.visibility = View.VISIBLE
            phoneText.isEnabled = true
            editPhoneButton.visibility = View.GONE
        }

        submitPhoneButton.setOnClickListener { _: View? ->
            //userViewModel.addUserPhone(phoneText.getText().toString())
            submitPhoneButton.visibility = View.GONE
            phoneText.isEnabled = false
            editPhoneButton.visibility = View.VISIBLE
        }

        //This will allow us to request sms permissions and provides a callback after user accepts or denies permission
        val permissionsRegistration = registerForActivityResult<String?, Boolean?>(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean? ->
            if (isGranted == true) {
                // If granted we set the switch to enabled and save the switches state to shared preferences
                Log.d("Permission: ", "GRANTED")
                smsSwitch.setChecked(true)
            } else {
                Log.d("Permission: ", "DENIED")
                // If denied we set the switch to disabled and save the switches state to shared preferences
                smsSwitch.setChecked(false)
            }
        }

        smsSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            // When the switch is enabled or disabled we check if sms permissions have been granted
            // If they haven't then we request permission
            // if the switch is enabled we show the phone input and save the switches state to shared preferences
            if (isChecked) {
                if (!permissionGranted()) permissionsRegistration.launch(SMS_PERMISSION)
                numberInput!!.visibility = View.VISIBLE
            } else {
                // if the switch is disabled we hide the phone input and save the switches state to shared preferences
                numberInput!!.visibility = View.GONE
            }
        }

        // check if the sms permissions have been granted. If so, we set the switch to checked.
        if (permissionGranted()) {
            smsSwitch.setChecked(true)
            numberInput!!.visibility = View.VISIBLE
        } else {
            smsSwitch.setChecked(false)
            numberInput!!.visibility = View.GONE
        }

        return rootView
    }

    // This function will check if the sms permission has been granted
    fun permissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            SMS_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val SMS_PERMISSION = Manifest.permission.SEND_SMS
    }
}