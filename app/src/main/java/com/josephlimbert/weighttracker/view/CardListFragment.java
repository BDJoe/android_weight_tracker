package com.josephlimbert.weighttracker.view;

import android.icu.text.DateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.CardItem;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class CardListFragment extends Fragment {
    private RecyclerView recyclerView;
    WeightViewModel weightViewModel;
    UserViewModel userViewModel;
    CardListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_card_list, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Initialize variables
        recyclerView = rootView.findViewById(R.id.card_list_view);
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        adapter = new CardListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        userViewModel.getAuthUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            List<CardItem> cardItemList = new ArrayList<>();
            CardItem progressCard = new CardItem(CardItem.ProgressCard, "", "", "", 0);
            CardItem targetLossCard = new CardItem(CardItem.StatCard, "", getString(R.string.weight_loss_target));
            CardItem totalLossCard = new CardItem(CardItem.StatCard, "", getString(R.string.weight_loss_so_far));
            CardItem targetLeftCard = new CardItem(CardItem.StatCard, "", getString(R.string.weight_left_to_lose));
            CardItem startDateCard = new CardItem(CardItem.StatCard, "", getString(R.string.start_date));
            cardItemList.add(progressCard);
            cardItemList.add(targetLossCard);
            cardItemList.add(totalLossCard);
            cardItemList.add(targetLeftCard);
            cardItemList.add(startDateCard);


            // Get the goal weight and set the text on the view. If no goal weight is set then we display N/A
            // and show a button to add a goal weight
            weightViewModel.getGoalWeight().observe(getViewLifecycleOwner(), goalWeight -> {
                cardItemList.get(0).setGoalWeight(goalWeight.toString());
                adapter.setCardItemList(cardItemList);
            });

            // Get the current weight and set the text on the view. Set to N/A if no weight data.
            weightViewModel.getCurrentWeight().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight.getWeight() + " Lbs" : "N/A";
                cardItemList.get(0).setCurrentWeight(weightText);
                adapter.setCardItemList(cardItemList);
            });
            // Get the starting weight and set the text on the view. Set to N/A if no weight data.
            weightViewModel.getStartingWeight().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight.getWeight() + " Lbs" : "N/A";
                cardItemList.get(0).setStartingWeight(weightText);
                String startDateString = weight != null ? DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(weight.getRecordedDate().toDate()) : "N/A";
                cardItemList.get(4).setStatText(startDateString);
                adapter.setCardItemList(cardItemList);
            });
            // Get the percentage of weight loss and set the text on the view.
            weightViewModel.getTotalLossPercent().observe(getViewLifecycleOwner(), total -> {
                cardItemList.get(0).setPercentage(total.intValue());
                adapter.setCardItemList(cardItemList);
            });
            // get the weight loss in pounds and set the text on the view
            weightViewModel.getTotalLossWeight().observe(getViewLifecycleOwner(), total -> {
                String weightText = total != null ? total + " Lbs" : "N/A";
                cardItemList.get(2).setStatText(weightText);
                adapter.setCardItemList(cardItemList);
            });
            // get the weight loss in pounds and set the text on the view. Set to N/A if no data returned
            weightViewModel.getTargetLoss().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight + " Lbs" : "N/A";
                cardItemList.get(1).setStatText(weightText);
                adapter.setCardItemList(cardItemList);
            });
            // get the weight left to lose and set the text on the view
            weightViewModel.getTargetLeft().observe(getViewLifecycleOwner(), weight -> {
                String weightText = weight != null ? weight + " Lbs" : "N/A";
                cardItemList.get(3).setStatText(weightText);
                adapter.setCardItemList(cardItemList);
            });

            adapter.setCardItemList(cardItemList);
        });

        return rootView;
    }

    private class CardListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<CardItem> cardItemList;
        private final FragmentManager fragmentManager = getParentFragmentManager();

        public CardListAdapter() {
            cardItemList = new ArrayList<>();
        }

        public void setCardItemList(List<CardItem> cardItemList) {

            this.cardItemList = cardItemList;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return cardItemList.get(position).getViewType();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == CardItem.ProgressCard) {
                return new WeightProgressViewHolder(inflater, parent);
            } else {
                return new WeightStatViewHolder(inflater, parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (cardItemList.get(position).getViewType() == CardItem.ProgressCard) {
                String startingWeight = cardItemList.get(position).getStartingWeight();
                String currentWeight = cardItemList.get(position).getCurrentWeight();
                String goalWeight = cardItemList.get(position).getGoalWeight();
                int percentage = cardItemList.get(position).getPercentage();
                ((WeightProgressViewHolder) holder).setViews(startingWeight, currentWeight, goalWeight, percentage, fragmentManager);
                holder.itemView.setTag(CardItem.ProgressCard);
            } else if (cardItemList.get(position).getViewType() == CardItem.StatCard) {
                String statText = cardItemList.get(position).getStatText();
                String statLabel = cardItemList.get(position).getStatLabel();
                ((WeightStatViewHolder) holder).setViews(statText, statLabel);
                holder.itemView.setTag(CardItem.StatCard);
            }
        }

        @Override
        public int getItemCount() {
            return cardItemList.size();
        }
    }

    private static class WeightProgressViewHolder extends RecyclerView.ViewHolder {

        private final TextView startingWeight;
        private final TextView currentWeight;
        private final TextView goalWeight;
        private final TextView percentage;
        private CircularSeekBar progressBar;
        private Button setGoalButton;

        public WeightProgressViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.card_weight_progress, parent, false));
            // Initialize variables
            startingWeight = itemView.findViewById(R.id.start_weight_text);
            currentWeight = itemView.findViewById(R.id.current_weight_text);
            goalWeight = itemView.findViewById(R.id.goal_weight_text);
            percentage = itemView.findViewById(R.id.progress_percent_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
            setGoalButton = itemView.findViewById(R.id.set_goal_button);
        }

        private void setViews(String startingWeight, String currentWeight, String goalWeight, int percentage, FragmentManager fragmentManager) {
            if (!goalWeight.isBlank()) {
                if (Float.parseFloat(goalWeight) > 0) {
                    String goalWeightText = goalWeight + " Lbs";
                    this.goalWeight.setText(goalWeightText);
                    setGoalButton.setVisibility(View.GONE);
                } else {
                    this.goalWeight.setText("N/A");
                    setGoalButton.setVisibility(View.VISIBLE);
                }
            }
            String percentText = percentage + "%";
            progressBar.setProgress(percentage);
            this.percentage.setText(percentText);
            this.startingWeight.setText(startingWeight);
            this.currentWeight.setText(currentWeight);

            setGoalButton.setOnClickListener(v -> {
                SetGoalWeightFragment sheet = new SetGoalWeightFragment();
                sheet.show(fragmentManager, "set goal weight");
            });
        }
    }

    private static class WeightStatViewHolder extends RecyclerView.ViewHolder {

        private final TextView statText;
        private final TextView statLabel;

        public WeightStatViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.card_weight_stat, parent, false));
            // Initialize variables
            statText = itemView.findViewById(R.id.card_stat_text);
            statLabel = itemView.findViewById(R.id.card_stat_label);
        }

        private void setViews(String statText, String statLabel) {
            this.statText.setText(statText);
            this.statLabel.setText(statLabel);
        }
    }
}