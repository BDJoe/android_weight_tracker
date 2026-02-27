package com.josephlimbert.weighttracker.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.CardItem;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize variables
        FloatingActionButton addWeightFab = rootView.findViewById(R.id.add_weight_fab);
        /*
        TextView currentWeightText = rootView.findViewById(R.id.current_weight_text);
        TextView goalWeightText = rootView.findViewById(R.id.goal_weight_text);
        TextView startingWeightText = rootView.findViewById(R.id.start_weight_text);
        TextView progressPercentText = rootView.findViewById(R.id.progress_percent_text);
        TextView targetLossText = rootView.findViewById(R.id.target_loss_text);
        TextView targetLeftText = rootView.findViewById(R.id.target_left_text);
        TextView totalLossText = rootView.findViewById(R.id.total_loss_text);
        TextView startDateText = rootView.findViewById(R.id.start_date_text);
        CircularSeekBar progressBar = rootView.findViewById(R.id.progress_bar);
        Button setGoalButton = rootView.findViewById(R.id.set_goal_button);

         */
        ConstraintLayout signInReminder = rootView.findViewById(R.id.sign_in_reminder);
        Button signInButton = rootView.findViewById(R.id.sign_in_button);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getAuthUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser == null) return;
            if (firebaseUser.isAnonymous()){
                signInReminder.setVisibility(View.VISIBLE);
            } else {
                signInReminder.setVisibility(View.GONE);
            }
        });

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
        });

        addWeightFab.setOnClickListener(this::showAddWeightDialog);

        return rootView;
    }

    // Function that will send an SMS message to the provided phone number once the goal is reached
    private void sendSms() {
        // Get the stored phone number from shared preferences
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String phoneNumber = sharedPref.getString("userPhoneNumber", "");
        // if the phone number is empty we do nothing and exit
        if (phoneNumber.isBlank()) return;
        // Try to send the sms to the provided phone number. If the user denied the permissions or
        // the message cannot be sent we log the error.
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "Congratulations on reaching your goal weight!", null, null);
            Log.d("SMS", "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            Log.d("SMS", "SMS failed: " + e.getMessage());
        }
    }

    // Function to show the add weight sheet
    public void showAddWeightDialog(View v) {
        AddWeightSheetFragment sheet = new AddWeightSheetFragment();
        sheet.show(getParentFragmentManager(), "add weight");
    }
}