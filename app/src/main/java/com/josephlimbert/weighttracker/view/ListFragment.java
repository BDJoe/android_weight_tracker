package com.josephlimbert.weighttracker.view;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {
    private RecyclerView recyclerView;
    WeightViewModel weightViewModel;
    WeightListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        // Initialize variables
        recyclerView = rootView.findViewById(R.id.history_list);
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        adapter = new WeightListAdapter(weightViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        // add a divider between each weight on the recycler view list
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

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
        private final FragmentManager fragmentManager = getParentFragmentManager();

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
            holder.bind(weight, viewModel, fragmentManager);
            holder.itemView.setTag(weight.getId());
        }

        @Override
        public int getItemCount() {
            return weightList.size();
        }

        public void setWeightList(List<Weight> weightList) {
            this.weightList = weightList;
            notifyDataSetChanged();
        }
    }

    private static class WeightListHolder extends RecyclerView.ViewHolder {

        private final TextView recordedWeightView;
        private final TextView dayOfWeekView;
        private final TextView dayOfMonthView;

        public WeightListHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_weight, parent, false));
            // Initialize variables
            dayOfWeekView = itemView.findViewById(R.id.day_of_week);
            dayOfMonthView = itemView.findViewById(R.id.day_of_month);
            recordedWeightView = itemView.findViewById(R.id.weight);
        }

        public void bind(Weight weight, WeightViewModel viewModel, FragmentManager manager) {
            // Initialize variables to format the date into the day of week and day
            SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat(DateFormat.DAY, Locale.getDefault());
            SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat(DateFormat.ABBR_WEEKDAY, Locale.getDefault());
            Date recordedDate = weight.getRecordedDate().toDate();
            String formattedDayOfWeek = dayOfWeekFormat.format(recordedDate);
            String formattedDayOfMonth = dayOfMonthFormat.format(recordedDate);
            String formattedWeight = weight.getWeight() + " Lbs";

            // Set the view text of each weight view to the correct values
            dayOfWeekView.setText(formattedDayOfWeek);
            dayOfMonthView.setText(formattedDayOfMonth);
            recordedWeightView.setText(formattedWeight);


            Button deleteButton = itemView.findViewById(R.id.delete_weight_button);
            Button editButton = itemView.findViewById(R.id.edit_weight_button);

            // delete the weight when the delete button is clicked
            deleteButton.setOnClickListener(v -> viewModel.deleteWeight(weight));

            // show add weight sheet when the edit button is clicked
            editButton.setOnClickListener(v -> {
                AddWeightSheetFragment sheet = new AddWeightSheetFragment();
                // create a bundle and send the weight ID to the sheet so we can edit the weight
                Bundle bundle = new Bundle();

                bundle.putString("weightId", weight.getId());
                sheet.setArguments(bundle);
                sheet.show(manager, "edit weight");
            });
        }
    }
}