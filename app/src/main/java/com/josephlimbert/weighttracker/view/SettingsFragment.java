package com.josephlimbert.weighttracker.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;

public class SettingsFragment extends Fragment {
    private static final String smsPermission = Manifest.permission.SEND_SMS;
    ConstraintLayout numberInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        // Initialize variables
        MaterialSwitch smsSwitch = rootView.findViewById(R.id.sms_switch);
        numberInput = rootView.findViewById(R.id.sms_phone_layout);
        Button submitPhoneButton = rootView.findViewById(R.id.phone_submit_button);
        Button editPhoneButton = rootView.findViewById(R.id.phone_edit_button);
        Button editGoalWeightButton = rootView.findViewById(R.id.edit_goal_button);
        TextView phoneText = rootView.findViewById(R.id.phone_input_text);
        Button logoutButton = rootView.findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            logoutButton.setEnabled(false);
            // TODO: call log out function on the view model then load main activity
            UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
            userViewModel.signOut();
            Intent intent = new Intent(rootView.getContext(), MainActivity.class);
            startActivity(intent);
        });

        editGoalWeightButton.setOnClickListener(v -> {
            // Create the add goal weight sheet and pass argument for editing
            SetGoalWeightFragment sheet = new SetGoalWeightFragment();
            Bundle bundle = new Bundle();

            bundle.putBoolean("isEditing", true);
            sheet.setArguments(bundle);
            sheet.show(getParentFragmentManager(), "edit goal weight");
        });

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getAuthUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser == null) return;
            if (firebaseUser.isAnonymous()) {
                logoutButton.setVisibility(View.GONE);
            } else {
                logoutButton.setVisibility(View.VISIBLE);
            }
        });

        userViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String phone = user.getPhone();
                if (phone != null){
                    submitPhoneButton.setVisibility(View.GONE);
                    phoneText.setText(phone);
                    phoneText.setEnabled(false);
                    editPhoneButton.setVisibility(View.VISIBLE);
                }
            }
        });


        editPhoneButton.setOnClickListener(v -> {
            // If the phone number is set, we re-enable the text input and allow for editing it
            submitPhoneButton.setVisibility(View.VISIBLE);
            phoneText.setEnabled(true);
            editPhoneButton.setVisibility(View.GONE);
        });

        submitPhoneButton.setOnClickListener(v -> {
            userViewModel.addUserPhone(phoneText.getText().toString());
            submitPhoneButton.setVisibility(View.GONE);
            phoneText.setEnabled(false);
            editPhoneButton.setVisibility(View.VISIBLE);
        });

        //This will allow us to request sms permissions and provides a callback after user accepts or denies permission
        ActivityResultLauncher<String> permissionsRegistration = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // If granted we set the switch to enabled and save the switches state to shared preferences
                        Log.d("Permission: ", "GRANTED");
                        smsSwitch.setChecked(true);
                    } else {
                        Log.d("Permission: ", "DENIED");
                        // If denied we set the switch to disabled and save the switches state to shared preferences
                        smsSwitch.setChecked(false);
                    }
                }
        );

        smsSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            // When the switch is enabled or disabled we check if sms permissions have been granted
            // If they haven't then we request permission
            // if the switch is enabled we show the phone input and save the switches state to shared preferences
            if (isChecked) {
                if (!permissionGranted())
                    permissionsRegistration.launch(smsPermission);
                numberInput.setVisibility(View.VISIBLE);
            } else {
                // if the switch is disabled we hide the phone input and save the switches state to shared preferences
                numberInput.setVisibility(View.GONE);
            }
        });

        // When creating the settings view we get the state of the sms switch from shared preferences and set it
        boolean smsEnabled = false;
        // check if the sms permissions have been granted. If so, we set the switch to checked.
        if (permissionGranted()) {
            smsSwitch.setChecked(true);
            numberInput.setVisibility(View.VISIBLE);
        } else {
            smsSwitch.setChecked(false);
            numberInput.setVisibility(View.GONE);
        }

        return rootView;
    }

    // This function will check if the sms permission has been granted
    public boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), smsPermission) == PackageManager.PERMISSION_GRANTED;
    }
}