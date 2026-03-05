package com.josephlimbert.weighttracker.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ListFragment extends Fragment {
    WeightViewModel weightViewModel;
    WeightListAdapter adapter;
    GraphView graph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        graph = (GraphView) rootView.findViewById(R.id.graph);
        // Initialize variables
        RecyclerView recyclerView = rootView.findViewById(R.id.history_list);
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        adapter = new WeightListAdapter(weightViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        weightViewModel.getWeightList().observe(getViewLifecycleOwner(), weightList -> {
            adapter.setWeightList(weightList);
        });
    }

    private class WeightListAdapter extends RecyclerView.Adapter<WeightListHolder> {

        private List<Weight> weightList;
        private final WeightViewModel viewModel;
        private String currentMonth = "";

        public WeightListAdapter(WeightViewModel viewModel) {
            this.weightList = new ArrayList<>();
            this.viewModel = viewModel;
        }

        @NonNull
        @Override
        public WeightListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WeightListHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(WeightListHolder holder, int position) {
            Weight weight = weightList.get(position);

            PopupMenu menu = new PopupMenu(requireContext(), holder.menuButton);

            menu.inflate(R.menu.list_item_menu);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit_weight_button) {
                    editItem(weight);
                    return true;
                } else if (item.getItemId() == R.id.delete_weight_button) {
                    deleteItem(weight);
                    return true;
                }
                return false;
            });
            holder.menuButton.setOnClickListener(btn -> menu.show());

            Float weightDiff = null;
            if (position + 1 < getItemCount()) {
                weightDiff = weight.getWeight() - weightList.get(position + 1).getWeight();
            }

            boolean showDivider = false;
            if (!currentMonth.equals(getMonthYear(weight))) {
                currentMonth = getMonthYear(weight);
                showDivider = true;
            }
            holder.bind(requireContext(), weight, weightDiff, currentMonth, showDivider);
            holder.itemView.setTag(weight.getId());
        }

        @Override
        public int getItemCount() {
            return weightList.size();
        }

        private void editItem(Weight weight) {
            AddWeightSheetFragment sheet = new AddWeightSheetFragment();
            // create a bundle and send the weight ID to the sheet so we can edit the weight
            Bundle bundle = new Bundle();
            bundle.putString("weightId", weight.getId());
            sheet.setArguments(bundle);
            sheet.show(getChildFragmentManager(), "edit weight");
        }

        private void deleteItem(Weight weight) {
            new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialDeleteDialog)
                    .setTitle("Are You Sure?")
                    .setMessage("This action cannot be undone.")
                    .setNeutralButton("Cancel", (dialog, which) -> {

                    })
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteWeight(weight);
                    })
                    .show();
        }

        private String getMonthYear(Weight weight) {
            SimpleDateFormat monthFormat = new SimpleDateFormat(DateFormat.ABBR_MONTH, Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat(DateFormat.YEAR, Locale.getDefault());
            Date recordedDate = weight.getRecordedDate().toDate();
            return monthFormat.format(recordedDate) + " " + yearFormat.format(recordedDate);
        }

        public void setWeightList(List<Weight> weightList) {
            this.weightList = weightList;
            SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat(DateFormat.DAY, Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat(DateFormat.NUM_MONTH, Locale.getDefault());


            if (!weightList.isEmpty()) {
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                for (int i = weightList.size(); i > 0; i--) {
                    DataPoint point = new DataPoint(weightList.size() - i, weightList.get(i - 1).getWeight());
                    series.appendData(point, true, weightList.size());
                }
                graph.addSeries(series);
                weightViewModel.getGoalWeight().observe(getViewLifecycleOwner(), goalWeight -> {
                    LineGraphSeries<DataPoint> goalSeries = new LineGraphSeries<>(new DataPoint[] {
                            new DataPoint(0, goalWeight),
                            new DataPoint(weightList.size(), goalWeight)
                    });
                    goalSeries.setColor(requireContext().getColor(R.color.md_theme_success));
                    goalSeries.setTitle("Goal Weight");
                    goalSeries.setThickness(8);
                    graph.addSeries(goalSeries);
                    graph.getViewport().setMinY(goalWeight - 20);
                });

                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(weightList.size() - 1);
                graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            if (value < weightList.size()) {
                                int index = (int) (weightList.size() - value - 1);
                                return monthFormat.format(weightList.get(index).getRecordedDate().toDate()) + "/" + dayOfMonthFormat.format(weightList.get(index).getRecordedDate().toDate());
                            }
                        }
                            return super.formatLabel(value, isValueX);

                    }
                });
                graph.getGridLabelRenderer().setNumHorizontalLabels(Math.min(weightList.size(), 5));
            }

            notifyDataSetChanged();
        }
    }

    private static class WeightListHolder extends RecyclerView.ViewHolder {
        private final TextView monthText;
        private final ConstraintLayout monthDivider;
        private final TextView recordedWeightView;
        private final TextView dayOfWeekView;
        private final TextView dayOfMonthView;
        private final Button menuButton;
        private final ImageView lossGainIconView;
        private final TextView lossGainTextView;

        public WeightListHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_weight, parent, false));
            // Initialize variables
            monthText = itemView.findViewById(R.id.month_text);
            monthDivider = itemView.findViewById(R.id.month_divider);
            dayOfWeekView = itemView.findViewById(R.id.day_of_week);
            dayOfMonthView = itemView.findViewById(R.id.day_of_month);
            recordedWeightView = itemView.findViewById(R.id.weight);
            lossGainIconView = itemView.findViewById(R.id.loss_gain_icon);
            lossGainTextView = itemView.findViewById(R.id.loss_gain_text);
            menuButton = itemView.findViewById(R.id.menu_button);
        }

        public void bind(Context context, Weight weight, Float weightDiff, String currentMonth, boolean showDivider) {
            String weightUnits = itemView.getContext().getString(R.string.unit_pounds);
            // Initialize variables to format the date into the day of week and day
            SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat(DateFormat.DAY, Locale.getDefault());
            SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat(DateFormat.ABBR_WEEKDAY, Locale.getDefault());

            Date recordedDate = weight.getRecordedDate().toDate();
            String formattedDayOfWeek = dayOfWeekFormat.format(recordedDate);
            String formattedDayOfMonth = dayOfMonthFormat.format(recordedDate);
            String formattedWeight = weight.getWeight() + weightUnits;

            if (showDivider) {
                monthText.setText(currentMonth);
                monthDivider.setVisibility(View.VISIBLE);
            } else {
                monthDivider.setVisibility(View.GONE);
            }

            // Set the view text of each weight view to the correct values
            dayOfWeekView.setText(formattedDayOfWeek);
            dayOfMonthView.setText(formattedDayOfMonth);
            recordedWeightView.setText(formattedWeight);

            if (weightDiff != null) {
                if (weightDiff < 0) {
                    lossGainIconView.setImageResource(R.drawable.down_arrow_icon);
                    lossGainIconView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_success)));
                    String diffText = weightDiff + weightUnits;
                    lossGainTextView.setText(diffText);
                    lossGainTextView.setTextColor(ContextCompat.getColor(context, R.color.md_theme_success));
                } else if (weightDiff > 0) {
                    lossGainIconView.setImageResource(R.drawable.up_arrow_icon);
                    lossGainIconView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_error)));
                    String diffText = "+" + weightDiff + weightUnits;
                    lossGainTextView.setText(diffText);
                    lossGainTextView.setTextColor(ContextCompat.getColor(context, R.color.md_theme_error));
                } else {
                    lossGainIconView.setImageResource(R.drawable.equal_icon);
                }
            }
        }
    }
}