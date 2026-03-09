package com.josephlimbert.weighttracker.ui.sheet

import android.content.DialogInterface
import android.icu.text.DateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.firebase.Timestamp
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.ParseException
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddWeightSheetFragment : BottomSheetDialogFragment() {
    var datePickerText: EditText? = null
    var weightText: EditText? = null
    private val viewModel: AddWeightViewModel by viewModels()
    var weight: Weight? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.sheet_add_weight, container, false)
        // Initialize variables
        datePickerText = rootView.findViewById<EditText>(R.id.date_input_edit_text)
        weightText = rootView.findViewById<EditText>(R.id.weight_input_edit_text)
        val sheetLabel = rootView.findViewById<TextView>(R.id.weight_sheet_label)
        val todayString = dateToString(Date())
        val submitButton = rootView.findViewById<Button>(R.id.add_weight_submit_button)

        var userId: String? = null

        // Set the date on the date picker to today's date
        datePickerText!!.setText(todayString)

        // If we are editing a weight then a weight ID will be passed as an argument
        // Update views with data from the existing weight
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user->
                if (user != null) {
                    userId = user.uid
                    if (arguments != null) {
                        val weightId = requireArguments().getString("weightId")
                        when (val result = viewModel.getWeight(weightId!!)) {
                            is FirestoreResult.Success -> {
                                weight = result.data
                                weightText!!.setText(result.data?.weight.toString())
                                sheetLabel.setText(R.string.edit_weight_label)
                                datePickerText!!.setText(dateToString(result.data?.recordedDate?.toDate()))
                            }
                            is FirestoreResult.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        datePickerText!!.setOnClickListener { _: View? -> this.showDatePicker() }
        submitButton.setOnClickListener { _: View? -> this.submitWeight(userId) }
        return rootView
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    // Function called to submit the weight entered
    fun submitWeight(userId: String?) {
        if (weightText!!.text.toString().isEmpty()) {
            weightText!!.error = getString(R.string.empty_weight_error)
        } else {
            try {
                val parsedDate = stringToDate(datePickerText!!.text.toString())
                val parsedWeight = weightText!!.text.toString().toDouble()

                // If we are editing a weight then update the current weight
                // Else we add a new weight
                val newWeight = if (weight != null) {
                    weight!!.copy(weight = parsedWeight, recordedDate = Timestamp(parsedDate))
                } else {
                    Weight(weight = parsedWeight, recordedDate = Timestamp(parsedDate))
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    if (userId == null) return@launch
                    when (val result = viewModel.addWeight(userId, newWeight)) {
                        is FirestoreResult.Success -> {
                            dismiss()
                        }
                        is FirestoreResult.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (_: NumberFormatException) {
                weightText!!.error = getString(R.string.weight_number_error)
            } catch (e: ParseException) {
                throw RuntimeException(e)
            }
        }
    }

    // Function to show the date picker and set the date picker text
    fun showDatePicker() {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
        datePickerBuilder.setTitleText(R.string.select_date)
        if (!datePickerText!!.text.toString().isEmpty()) {
            try {
                val parsedDate = stringToDate(datePickerText!!.text.toString())
                datePickerBuilder.setSelection(parsedDate.time)
            } catch (e: ParseException) {
                throw RuntimeException(e)
            }
        }
        val constraintsBuilder =
            CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())
        datePickerBuilder.setCalendarConstraints(constraintsBuilder.build())
        val datePicker = datePickerBuilder.build()
        val clickListener = MaterialPickerOnPositiveButtonClickListener { selection: Long? ->
            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val selectedDate = dateFormat.format(Date(selection!!))
            datePickerText!!.setText(selectedDate)
        }
        datePicker.addOnPositiveButtonClickListener(clickListener)
        datePicker.show(getParentFragmentManager(), "date picker")
    }

    // Function to convert a date to a string
    private fun dateToString(date: Date?): String? {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
    }

    // Function to convert a string to a date
    @Throws(ParseException::class)
    private fun stringToDate(dateString: String?): Date {
        return DateFormat.getDateInstance().parse(dateString)
    }
}