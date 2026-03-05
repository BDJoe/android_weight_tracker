package com.josephlimbert.weighttracker.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import org.jspecify.annotations.NonNull;

import java.util.Locale;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeFragment extends Fragment implements MenuProvider {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize variables
        FloatingActionButton addWeightFab = rootView.findViewById(R.id.add_weight_fab);
        TextView startingWeightText = rootView.findViewById(R.id.start_weight_text);
        TextView currentWeightText = rootView.findViewById(R.id.current_weight_text);
        TextView goalWeightText = rootView.findViewById(R.id.goal_weight_text);
        TextView percentageText = rootView.findViewById(R.id.progress_percent_text);
        CircularSeekBar progressBar = rootView.findViewById(R.id.progress_bar);
        Button setGoalButton = rootView.findViewById(R.id.set_goal_button);
        TextView targetLossText = rootView.findViewById(R.id.target_loss_text);
        TextView totalLossText = rootView.findViewById(R.id.total_loss_text);
        TextView targetLeftText = rootView.findViewById(R.id.target_left_text);
        TextView startDateText = rootView.findViewById(R.id.start_date_text);

        ConstraintLayout signInReminder = rootView.findViewById(R.id.sign_in_reminder);
        Button signInButton = rootView.findViewById(R.id.sign_in_button);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        WeightViewModel weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        String weightUnits = getString(R.string.unit_pounds);

        userViewModel.getAuthUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser == null) return;
            if (firebaseUser.isAnonymous()){
                signInReminder.setVisibility(View.VISIBLE);
            } else {
                signInReminder.setVisibility(View.GONE);
            }

            weightViewModel.getGoalWeight().observe(getViewLifecycleOwner(), goalWeight -> {
                if (goalWeight > 0) {
                    String weightText = goalWeight + weightUnits;
                    goalWeightText.setText(weightText);
                    setGoalButton.setVisibility(View.GONE);
                } else {
                    goalWeightText.setText("N/A");
                    setGoalButton.setVisibility(View.VISIBLE);
                }
            });

            // Get the current weight and set the text on the view. Set to N/A if no weight data.
            weightViewModel.getCurrentWeight().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight.getWeight() + weightUnits : "N/A";
                currentWeightText.setText(weightText);
            });
            // Get the starting weight and set the text on the view. Set to N/A if no weight data.
            weightViewModel.getStartingWeight().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight.getWeight() + weightUnits : "N/A";
                startingWeightText.setText(weightText);
                String startDateString = weight != null ? DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(weight.getRecordedDate().toDate()) : "N/A";
                startDateText.setText(startDateString);
            });
            // Get the percentage of weight loss and set the text on the view.
            weightViewModel.getTotalLossPercent().observe(getViewLifecycleOwner(), total -> {
                String percentText = total != null ? total.intValue() + "%" : "N/A";
                percentageText.setText(percentText);
                progressBar.setProgress(total != null ? total : 0);
            });
            // get the weight loss in pounds and set the text on the view
            weightViewModel.getTotalLossWeight().observe(getViewLifecycleOwner(), total -> {
                String weightText = total != null ? total + weightUnits : "N/A";
                totalLossText.setText(weightText);
            });
            // get the weight loss in pounds and set the text on the view. Set to N/A if no data returned
            weightViewModel.getTargetLoss().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight + weightUnits : "N/A";
                targetLossText.setText(weightText);
            });
            // get the weight left to lose and set the text on the view
            weightViewModel.getTargetLeft().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight + weightUnits : "N/A";
                targetLeftText.setText(weightText);
            });

            weightViewModel.checkGoalReached().observe(getViewLifecycleOwner(), isReached -> {
                if (isReached)
                    sendSms();
            });
        });

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
        });

        setGoalButton.setOnClickListener(v -> {
            SetGoalWeightFragment sheet = new SetGoalWeightFragment();
            sheet.show(getChildFragmentManager(), "set goal weight");
        });

        addWeightFab.setOnClickListener(this::showAddWeightDialog);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return rootView;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.top_home_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_weight_menu_item) {
            showAddWeightDialog(requireView());
            return true;
        }
        return false;
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