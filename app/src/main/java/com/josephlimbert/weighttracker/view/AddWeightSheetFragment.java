package com.josephlimbert.weighttracker.view;

import android.content.DialogInterface;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.Timestamp;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import android.icu.text.DateFormat;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class AddWeightSheetFragment extends BottomSheetDialogFragment {
    EditText datePickerText;
    EditText weightText;
    WeightViewModel weightViewModel;
    Weight weight;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.sheet_add_weight, container, false);
        // Initialize variables
        datePickerText = rootView.findViewById(R.id.date_input_edit_text);
        weightText = rootView.findViewById(R.id.weight_input_edit_text);
        TextView sheetLabel = rootView.findViewById(R.id.weight_sheet_label);
        String todayString = dateToString(new Date());
        Button submitButton = rootView.findViewById(R.id.add_weight_submit_button);
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Set the date on the date picker to today's date
        datePickerText.setText(todayString);

        // If we are editing a weight then a weight ID will be passed as an argument
        // Update views with data from the existing weight
        if (getArguments() != null) {
            String weightId = getArguments().getString("weightId");
            weightViewModel.getWeightById(weightId).observe(getViewLifecycleOwner(), weight -> {
                this.weight = weight;
                if (weight != null) {
                    weightText.setText(String.valueOf(weight.getWeight()));
                    sheetLabel.setText(R.string.edit_weight_label);
                    datePickerText.setText(dateToString(weight.getRecordedDate().toDate()));
                }
            });
        }

        datePickerText.setOnClickListener(this::showDatePicker);
        submitButton.setOnClickListener(this::submitWeight);
        return rootView;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    // Function called to submit the weight entered
    public void submitWeight(View v) {
        if (weightText.getText().toString().isEmpty()){
            weightText.setError(getString(R.string.empty_weight_error));
        } else {
            try {
                Date parsedDate = stringToDate(datePickerText.getText().toString());
                float parsedWeight = Float.parseFloat(weightText.getText().toString());

                // If we are editing a weight then update the current weight
                // Else we add a new weight
                if (weight != null) {
                    weight.setWeight(parsedWeight);
                    weight.setRecordedDate(new Timestamp(parsedDate));
                    weightViewModel.addWeight(weight);
                    dismiss();
                } else {
                    Weight newWeight = new Weight();
                    newWeight.setWeight(parsedWeight);
                    newWeight.setRecordedDate(new Timestamp(parsedDate));
                    weightViewModel.addWeight(newWeight);
                    dismiss();
                }
            } catch (NumberFormatException e) {
                weightText.setError(getString(R.string.weight_number_error));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Function to show the date picker and set the date picker text
    public void showDatePicker(View v) {
        MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
        datePickerBuilder.setTitleText(R.string.select_date);
        if (!datePickerText.getText().toString().isEmpty()) {
            try {
                Date parsedDate = stringToDate(datePickerText.getText().toString());
                datePickerBuilder.setSelection(parsedDate.getTime());

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now());
        datePickerBuilder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Long> datePicker = datePickerBuilder.build();
        MaterialPickerOnPositiveButtonClickListener<Long> clickListener = selection -> {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String selectedDate = dateFormat.format(new Date(selection));
            datePickerText.setText(selectedDate);
        };
        datePicker.addOnPositiveButtonClickListener(clickListener);
        datePicker.show(getParentFragmentManager(), "date picker");
    }

    // Function to convert a date to a string
    private String dateToString(Date date) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    // Function to convert a string to a date
    private Date stringToDate(String dateString) throws ParseException {
        return DateFormat.getDateInstance().parse(dateString);
    }
}
