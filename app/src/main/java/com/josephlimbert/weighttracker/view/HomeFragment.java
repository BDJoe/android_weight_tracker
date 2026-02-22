package com.josephlimbert.weighttracker.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

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
        WeightViewModel weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        addWeightFab.setOnClickListener(this::showAddWeightDialog);

        setGoalButton.setOnClickListener(v -> {
            SetGoalWeightFragment sheet = new SetGoalWeightFragment();
            sheet.show(getParentFragmentManager(), "set goal weight");
        });

        // Observe the user ID live data from the user view model
        // Once set, it will set the user ID live data on the weight view model to trigger
        // the loading of weight data
        userViewModel.getLoggedInUserId().observe(getViewLifecycleOwner(), id -> {
            if (id != null)
                weightViewModel.setLoggedInUserId(id);
        });

        // Get the goal weight and set the text on the view. If no goal weight is set then we display N/A
        // and show a button to add a goal weight
        weightViewModel.getGoalWeight.observe(getViewLifecycleOwner(), goalWeight -> {
            if (goalWeight != null) {
                String weightText = goalWeight.getWeight() + " Lbs";
                goalWeightText.setText(weightText);
                setGoalButton.setVisibility(View.GONE);
            } else {
                goalWeightText.setText("N/A");
                setGoalButton.setVisibility(View.VISIBLE);
            }
        });

        // Get the current weight and set the text on the view. Set to N/A if no weight data.
        weightViewModel.getCurrentWeight.observe(getViewLifecycleOwner(), weight -> {
            String weightText = weight != null ? weight.getWeight() + " Lbs" : "N/A";
            currentWeightText.setText(weightText);
        });
        // Get the starting weight and set the text on the view. Set to N/A if no weight data.
        weightViewModel.getStartingWeight.observe(getViewLifecycleOwner(), weight -> {
            String weightText = weight != null ? weight.getWeight() + " Lbs" : "N/A";
            startingWeightText.setText(weightText);
            String startDateString = weight != null ? DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(weight.getRecordedDate()) : "N/A";
            startDateText.setText(startDateString);
        });
        // Get the percentage of weight loss and set the text on the view.
        weightViewModel.getTotalLossPercentage.observe(getViewLifecycleOwner(), total -> {
            String percentText = total.intValue() + "%";
            progressBar.setProgress(total);
            progressPercentText.setText(percentText);
        });
        // get the weight loss in pounds and set the text on the view
        weightViewModel.getTotalLossWeight.observe(getViewLifecycleOwner(), total -> {
            String weightText = total + " Lbs";
            totalLossText.setText(weightText);
        });
        // get the weight loss in pounds and set the text on the view. Set to N/A if no data returned
        weightViewModel.getTargetLossWeight.observe(getViewLifecycleOwner(), weight -> {
            String weightText = weight != null ? weight + " Lbs" : "N/A";
            targetLossText.setText(weightText);
        });
        // get the weight left to lose and set the text on the view
        weightViewModel.getTargetLeftWeight.observe(getViewLifecycleOwner(), weight -> {
            String weightText = weight + " Lbs";
            targetLeftText.setText(weightText);
        });

        // observe the goal reached live data. if it is true then we send the sms to the user.
        weightViewModel.goalReached.observe(getViewLifecycleOwner(), result -> {
            if (result)
                sendSms();
        });

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