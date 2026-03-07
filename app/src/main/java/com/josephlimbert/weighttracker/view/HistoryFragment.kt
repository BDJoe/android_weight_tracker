package com.josephlimbert.weighttracker.view

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.ui.WeightViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue
import kotlin.math.min

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private val weightViewModel: WeightViewModel by viewModels()
    private val adapter: WeightListAdapter = WeightListAdapter()
    var graph: GraphView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)
        graph = rootView.findViewById<View?>(R.id.graph) as GraphView
        // Initialize variables
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.history_list)
        recyclerView.setLayoutManager(LinearLayoutManager(activity))
        recyclerView.setAdapter(adapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    weightViewModel.weightList.collect { weights ->
                        adapter.setWeightList(weights)
                        val dayOfMonthFormat = SimpleDateFormat(DateFormat.DAY, Locale.getDefault())
                        val monthFormat = SimpleDateFormat(DateFormat.NUM_MONTH, Locale.getDefault())


                        if (!weights.isEmpty()) {
                            val series = LineGraphSeries<DataPoint?>()
                            for (i in weights.size downTo 1) {
                                val point = DataPoint(
                                    (weights.size - i).toDouble(),
                                    weights[i - 1].weight
                                )
                                series.appendData(point, true, weights.size)
                            }
                            graph!!.addSeries(series)
//                weightViewModel.getGoalWeight()
//                    .observe(getViewLifecycleOwner(), Observer { goalWeight: Float? ->
//                        val goalSeries = LineGraphSeries<DataPoint?>(
//                            arrayOf<DataPoint>(
//                                DataPoint(0.0, goalWeight!!.toDouble()),
//                                DataPoint(weightList.size.toDouble(), goalWeight.toDouble())
//                            )
//                        )
//                        goalSeries.setColor(requireContext().getColor(R.color.md_theme_success))
//                        goalSeries.setTitle("Goal Weight")
//                        goalSeries.setThickness(8)
//                        graph!!.addSeries(goalSeries)
//                        graph!!.getViewport().setMinY((goalWeight - 20).toDouble())
//                    })

                            graph!!.viewport.setXAxisBoundsManual(true)
                            graph!!.viewport.setMinX(0.0)
                            graph!!.viewport.setMaxX((weights.size - 1).toDouble())
                            graph!!.gridLabelRenderer.setLabelFormatter(object : DefaultLabelFormatter() {
                                override fun formatLabel(value: Double, isValueX: Boolean): String? {
                                    if (isValueX) {
                                        if (value < weights.size) {
                                            val index = (weights.size - value - 1).toInt()
                                            return monthFormat.format(
                                                weights[index].recordedDate.toDate()
                                            ) + "/" + dayOfMonthFormat.format(
                                                weights[index].recordedDate.toDate()
                                            )
                                        }
                                    }
                                    return super.formatLabel(value, isValueX)
                                }
                            })
                            graph!!.gridLabelRenderer.numHorizontalLabels = min(weights.size, 5)
                        }
                    }
                }
                launch {
                    weightViewModel.goalWeight.collect { goalWeight ->
                        if (goalWeight == null) return@collect
                        val goalSeries = LineGraphSeries<DataPoint?>(
                            arrayOf<DataPoint>(
                                DataPoint(0.0, goalWeight),
                                DataPoint(graph!!.viewport.getMaxX(true), goalWeight)
                            )
                        )
                        goalSeries.color = requireContext().getColor(R.color.md_theme_success)
                        goalSeries.title = "Goal Weight"
                        goalSeries.thickness = 8
                        graph!!.addSeries(goalSeries)
                        graph!!.viewport.setMinY((goalWeight - 20))
                    }
                }
            }
        }

        return rootView
    }

    private inner class WeightListAdapter() :
        RecyclerView.Adapter<WeightListHolder?>() {
        private var weightList: List<Weight>
        private var currentMonth = ""

        init {
            this.weightList = emptyList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightListHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return WeightListHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: WeightListHolder, position: Int) {
            val weight = weightList[position]

            val menu = PopupMenu(requireContext(), holder.menuButton)

            menu.inflate(R.menu.list_item_menu)
            menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                if (item!!.itemId == R.id.edit_weight_button) {
                    editItem(weight)
                    return@OnMenuItemClickListener true
                } else if (item.itemId == R.id.delete_weight_button) {
                    deleteItem(weight)
                    return@OnMenuItemClickListener true
                }
                false
            })
            holder.menuButton.setOnClickListener { _: View? -> menu.show() }

            var weightDiff: Double? = null
            if (position + 1 < itemCount) {
                weightDiff = weight.weight - weightList[position + 1].weight
            }

            var showDivider = false
            if (currentMonth != getMonthYear(weight)) {
                currentMonth = getMonthYear(weight)
                showDivider = true
            }
            holder.bind(requireContext(), weight, weightDiff, currentMonth, showDivider)
            holder.itemView.tag = weight.id
        }

        override fun getItemCount(): Int {
            return weightList.size
        }

        fun editItem(weight: Weight) {
            val sheet = AddWeightSheetFragment()
            // create a bundle and send the weight ID to the sheet so we can edit the weight
            val bundle = Bundle()
            bundle.putString("weightId", weight.id)
            sheet.setArguments(bundle)
            sheet.show(getChildFragmentManager(), "edit weight")
        }

        fun deleteItem(weight: Weight) {
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_App_MaterialDeleteDialog
            )
                .setTitle("Are You Sure?")
                .setMessage("This action cannot be undone.")
                .setNeutralButton(
                    "Cancel"
                ) { _: DialogInterface?, _: Int -> }
                .setPositiveButton(
                    "Delete"
                ) { _: DialogInterface?, _: Int ->
                    weightViewModel.deleteWeight(weight.id)
                }
                .show()
        }

        fun getMonthYear(weight: Weight): String {
            val monthFormat = SimpleDateFormat(DateFormat.ABBR_MONTH, Locale.getDefault())
            val yearFormat = SimpleDateFormat(DateFormat.YEAR, Locale.getDefault())
            val recordedDate = weight.recordedDate.toDate()
            return monthFormat.format(recordedDate) + " " + yearFormat.format(recordedDate)
        }

        fun setWeightList(weightList: List<Weight>) {
            this.weightList = weightList

            notifyDataSetChanged()
        }
    }

    private class WeightListHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_weight, parent, false)) {
        // Initialize variables
        private val monthText: TextView = itemView.findViewById<TextView>(R.id.month_text)
        private val monthDivider: ConstraintLayout = itemView.findViewById<ConstraintLayout>(R.id.month_divider)
        private val recordedWeightView: TextView = itemView.findViewById<TextView>(R.id.weight)
        private val dayOfWeekView: TextView = itemView.findViewById<TextView>(R.id.day_of_week)
        private val dayOfMonthView: TextView = itemView.findViewById<TextView>(R.id.day_of_month)
        val menuButton: Button = itemView.findViewById<Button>(R.id.menu_button)
        private val lossGainIconView: ImageView = itemView.findViewById<ImageView>(R.id.loss_gain_icon)
        private val lossGainTextView: TextView = itemView.findViewById<TextView>(R.id.loss_gain_text)

        fun bind(
            context: Context,
            weight: Weight,
            weightDiff: Double?,
            currentMonth: String?,
            showDivider: Boolean
        ) {
            val weightUnits = itemView.getContext().getString(R.string.unit_pounds)
            // Initialize variables to format the date into the day of week and day
            val dayOfMonthFormat = SimpleDateFormat(DateFormat.DAY, Locale.getDefault())
            val dayOfWeekFormat = SimpleDateFormat(DateFormat.ABBR_WEEKDAY, Locale.getDefault())

            val recordedDate = weight.recordedDate.toDate()
            val formattedDayOfWeek = dayOfWeekFormat.format(recordedDate)
            val formattedDayOfMonth = dayOfMonthFormat.format(recordedDate)
            val formattedWeight = weight.weight.toString() + weightUnits

            if (showDivider) {
                monthText.text = currentMonth
                monthDivider.visibility = View.VISIBLE
            } else {
                monthDivider.visibility = View.GONE
            }

            // Set the view text of each weight view to the correct values
            dayOfWeekView.text = formattedDayOfWeek
            dayOfMonthView.text = formattedDayOfMonth
            recordedWeightView.text = formattedWeight

            if (weightDiff != null) {
                if (weightDiff < 0) {
                    lossGainIconView.setImageResource(R.drawable.down_arrow_icon)
                    lossGainIconView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.md_theme_success
                        )
                    )
                    val diffText = weightDiff.toString() + weightUnits
                    lossGainTextView.text = diffText
                    lossGainTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.md_theme_success
                        )
                    )
                } else if (weightDiff > 0) {
                    lossGainIconView.setImageResource(R.drawable.up_arrow_icon)
                    lossGainIconView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.md_theme_error
                        )
                    )
                    val diffText = "+$weightDiff$weightUnits"
                    lossGainTextView.text = diffText
                    lossGainTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.md_theme_error
                        )
                    )
                } else {
                    lossGainIconView.setImageResource(R.drawable.equal_icon)
                }
            }
        }
    }
}