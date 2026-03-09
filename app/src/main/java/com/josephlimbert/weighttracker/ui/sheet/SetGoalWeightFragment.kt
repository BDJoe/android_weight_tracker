package com.josephlimbert.weighttracker.ui.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetGoalWeightFragment : BottomSheetDialogFragment() {
    var weightText: EditText? = null
    private val viewModel: SetGoalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.sheet_set_goal_weight, container, false)
        // Initialize variables
        weightText = rootView.findViewById(R.id.goal_weight_input_edit_text)
        val goalWeightLabel = rootView.findViewById<TextView>(R.id.set_goal_label)
        val submitButton = rootView.findViewById<Button>(R.id.set_goal_weight_submit_button)

        var userId: String? = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                if (user != null) {
                    userId = user.id
                    if (arguments != null) {
                        if (requireArguments().getBoolean("isEditing")) {
                                weightText!!.setText(user.goalWeight.toString())
                                goalWeightLabel.text = getString(R.string.change_goal_weight)
                        }
                    }
                }
            }
        }

        // Check for boolean argument indicating whether this view was created for editing
        // an existing goal weight or setting a new one.


        submitButton.setOnClickListener { _: View? -> this.submitWeight(userId) }
        return rootView
    }

    // This function will submit the goal weight to the database
    fun submitWeight(userId: String?) {
        // Check that the goal weight is not empty
        if (weightText!!.text.toString().isEmpty()) {
            weightText!!.error = getString(R.string.empty_weight_error)
        } else {
            try {
                val newWeight = weightText!!.text.toString().toDouble()
                // If we are editing a goal weight then we update the weight and send the update to the database
                // Otherwise, we create a new goal weight and add it to the database
                viewLifecycleOwner.lifecycleScope.launch {
                    if (userId == null) return@launch
                    when (val result = viewModel.setGoalWeight(userId, newWeight)) {
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
            }
        }
    }
}