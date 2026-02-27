package com.josephlimbert.weighttracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.repo.FirebaseRepo;

import java.util.List;

public class WeightViewModel extends ViewModel {
    private MutableLiveData<List<Weight>> weightListLiveData = new MutableLiveData<>();
    private MutableLiveData<Weight> weightLiveData = new MutableLiveData<>();
    private MutableLiveData<Float> goalWeightLiveData = new MutableLiveData<>();
    private MutableLiveData<Weight> currentWeightLiveData = new MutableLiveData<>();
    private MutableLiveData<Weight> startingWeightLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> goalReachedLiveData = new MutableLiveData<>();

    public WeightViewModel() {}

    public MutableLiveData<List<Weight>> getWeightList() {
        weightListLiveData = FirebaseRepo.getInstance().getWeightList();
        return weightListLiveData;
    }

    public MutableLiveData<Weight> getWeightById(String id) {
        weightLiveData = FirebaseRepo.getInstance().getWeightById(id);
        return weightLiveData;
    }

    public MutableLiveData<Float> getGoalWeight() {
        goalWeightLiveData = FirebaseRepo.getInstance().getGoalWeight();
        return goalWeightLiveData;
    }

    public MutableLiveData<Weight> getCurrentWeight() {
        currentWeightLiveData = FirebaseRepo.getInstance().getCurrentWeight();
        return currentWeightLiveData;
    }

    public MutableLiveData<Weight> getStartingWeight() {
        startingWeightLiveData = FirebaseRepo.getInstance().getStartingWeight();
        return startingWeightLiveData;
    }

    // Use the starting weight, current weight, and goal weight to get the percentage of weight lost. returns 0 if any variable is not set
    public MutableLiveData<Float> getTotalLossPercent() {
        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value ->
                result.postValue(calculateLossPercent(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        result.addSource(currentWeightLiveData, value ->
                result.postValue(calculateLossPercent(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        result.addSource(goalWeightLiveData, value ->
                result.postValue(calculateLossPercent(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        return result;
    }

    public MutableLiveData<Float> getTotalLossWeight() {
        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value -> {
            result.postValue(calculateTotalLoss(startingWeightLiveData, currentWeightLiveData));
        });
        result.addSource(currentWeightLiveData, value -> {
            result.postValue(calculateTotalLoss(startingWeightLiveData, currentWeightLiveData));
        });

        return result;
    }

    public MutableLiveData<Float> getTargetLoss() {
        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value -> {
            result.postValue(calculateTargetLoss(startingWeightLiveData, goalWeightLiveData));
        });
        result.addSource(goalWeightLiveData, value -> {
            result.postValue(calculateTargetLoss(startingWeightLiveData, goalWeightLiveData));
        });

        return result;
    }

    public MutableLiveData<Float> getTargetLeft() {
        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value -> {
            result.postValue(calculateTargetLeft(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData));
        });
        result.addSource(currentWeightLiveData, value -> {
            result.postValue(calculateTargetLeft(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData));
        });
        result.addSource(goalWeightLiveData, value -> {
            result.postValue(calculateTargetLeft(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData));
        });

        return result;
    }

    private float calculateTotalLoss(LiveData<Weight> startingLive, LiveData<Weight> currentLive) {
        Weight starting = startingLive.getValue();
        Weight current = currentLive.getValue();

        if (starting == null || current == null) {
            return 0;
        }

        return starting.getWeight() - current.getWeight();
    }

    private float calculateTargetLoss(LiveData<Weight> startingLive, LiveData<Float> goalLive) {
        Weight starting = startingLive.getValue();

        if (starting == null || goalLive.getValue() == null) {
            return 0;
        }

        return starting.getWeight() - goalLive.getValue();
    }

    private float calculateTargetLeft(LiveData<Weight> startingLive, LiveData<Weight> currentLive, LiveData<Float> goalLive) {
        Weight starting = startingLive.getValue();
        Weight current = currentLive.getValue();

        if (starting == null || current == null || goalLive.getValue() == null) {
            return 0;
        }

        float currentWeightLoss = starting.getWeight() - current.getWeight();
        float totalLossGoal = starting.getWeight() - goalLive.getValue();

        return totalLossGoal - currentWeightLoss;
    }

    private float calculateLossPercent(LiveData<Weight> startingLive, LiveData<Weight> currentLive, LiveData<Float> goalLive) {
        Weight starting = startingLive.getValue();
        Weight current = currentLive.getValue();

        if (starting == null || current == null || goalLive.getValue() == null) {
            return 0;
        }

        float currentWeightLoss = starting.getWeight() - current.getWeight();
        float totalLossGoal = starting.getWeight() - goalLive.getValue();

        return Math.min(((currentWeightLoss / totalLossGoal) * 100), 100);
    }

    public void addWeight(Weight weight) {
        FirebaseRepo.getInstance().addWeight(weight);
    }

    public void deleteWeight(Weight weight) {
        FirebaseRepo.getInstance().deleteWeight(weight);
    }

    public void addGoalWeight(float goalWeight) { FirebaseRepo.getInstance().addGoalWeight(goalWeight); }

    public void updateGoalWeight(float goalWeight) { FirebaseRepo.getInstance().updateGoalWeight(goalWeight); }

    // Check if the current weight is at or below the goal weight and return true if goal has been reached
    public MutableLiveData<Boolean> checkGoalReached() {
        if (goalWeightLiveData.getValue() == null || currentWeightLiveData.getValue() == null) {
            goalReachedLiveData.postValue(false);
            return goalReachedLiveData;
        }

        goalReachedLiveData.postValue(currentWeightLiveData.getValue().getWeight() <= goalWeightLiveData.getValue());
        return goalReachedLiveData;
    }
}
