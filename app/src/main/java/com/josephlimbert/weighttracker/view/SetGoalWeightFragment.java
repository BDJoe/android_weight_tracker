package com.josephlimbert.weighttracker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

public class SetGoalWeightFragment extends BottomSheetDialogFragment {
    EditText weightText;
    WeightViewModel weightViewModel;
    float goalWeight;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.sheet_set_goal_weight, container, false);
        // Initialize variables
        weightText = rootView.findViewById(R.id.goal_weight_input_edit_text);
        TextView goalWeightLabel = rootView.findViewById(R.id.set_goal_label);
        Button submitButton = rootView.findViewById(R.id.set_goal_weight_submit_button);
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);

        // Check for boolean argument indicating whether this view was created for editing
        // an existing goal weight or setting a new one.
        if (getArguments() != null) {
            // Store existing goal weight if we are editing a goal weight
            if (getArguments().getBoolean("isEditing")) {
                if (weightViewModel.getGoalWeight().getValue() != null) {
                    goalWeight = weightViewModel.getGoalWeight().getValue();
                    weightText.setText(String.valueOf(goalWeight));
                    goalWeightLabel.setText(getString(R.string.change_goal_weight));
                }
            }
        }

        submitButton.setOnClickListener(this::submitWeight);
        return rootView;
    }

    // This function will submit the goal weight to the database
    public void submitWeight(View v) {
        // Check that the goal weight is not empty
        if (weightText.getText().toString().isEmpty()){
            weightText.setError(getString(R.string.empty_weight_error));
        }
        else {
            try {
                float newWeight = Float.parseFloat(weightText.getText().toString());
                // If we are editing a goal weight then we update the weight and send the update to the database
                // Otherwise, we create a new goal weight and add it to the database
                if (weightViewModel.getGoalWeight().getValue() != null) {
                    goalWeight = newWeight;
                    weightViewModel.updateGoalWeight(goalWeight);
                    dismiss();
                } else {
                    weightViewModel.addGoalWeight(newWeight);
                    dismiss();
                }
            } catch (NumberFormatException e) {
                weightText.setError(getString(R.string.weight_number_error));
            }
        }
    }
}
