package com.josephlimbert.weighttracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.josephlimbert.weighttracker.model.GoalWeight;
import com.josephlimbert.weighttracker.model.Weight;
import com.josephlimbert.weighttracker.repo.WeightTrackerRepository;

import java.util.List;

public class WeightViewModel extends AndroidViewModel {

    private WeightTrackerRepository weightRepo;

    public MutableLiveData<Boolean> goalReached = new MutableLiveData<>();

    private final MutableLiveData<Long> loggedInUserId = new MutableLiveData<>();

    public WeightViewModel(@NonNull Application application) {
        super(application);
        weightRepo = WeightTrackerRepository.getInstance(application.getApplicationContext());
    }

    public void setLoggedInUserId(long userId) {
        loggedInUserId.setValue(userId);
    }

    public LiveData<List<Weight>> getWeightList = Transformations.switchMap(loggedInUserId, userId ->
            weightRepo.getWeightList(userId));

    public LiveData<Weight> getCurrentWeight = Transformations.switchMap(loggedInUserId, userId -> weightRepo.getCurrentWeight(userId));

    public LiveData<Weight> getStartingWeight = Transformations.switchMap(loggedInUserId, userId-> weightRepo.getStartingWeight(userId));

    public LiveData<GoalWeight> getGoalWeight = Transformations.switchMap(loggedInUserId, userId ->  weightRepo.getGoalWeight(userId));

    // Use the starting weight, current weight, and goal weight to get the percentage of weight lost. returns 0 if any variable is not set
    public LiveData<Float> getTotalLossPercentage = Transformations.switchMap(loggedInUserId, userId -> {
        LiveData<Weight> startingWeightLiveData = getStartingWeight;
        LiveData<Weight> currentWeightLiveData = getCurrentWeight;
        LiveData<GoalWeight> goalWeightLiveData = getGoalWeight;

        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value ->
            result.setValue(calculateResult(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        result.addSource(currentWeightLiveData, value ->
                result.setValue(calculateResult(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        result.addSource(goalWeightLiveData, value ->
                result.setValue(calculateResult(startingWeightLiveData, currentWeightLiveData, goalWeightLiveData)));
        return result;
    });

    // Use the starting weight and current weight to get the total weight lost.
    public LiveData<Float> getTotalLossWeight = Transformations.switchMap(loggedInUserId, userId -> {
        LiveData<Weight> startingWeightLiveData = getStartingWeight;
        LiveData<Weight> currentWeightLiveData = getCurrentWeight;

        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value -> {
                if (currentWeightLiveData.getValue() != null && value != null)
                    result.setValue(value.getWeight() - currentWeightLiveData.getValue().getWeight());
                else
                    result.setValue(0F);
        });
        result.addSource(currentWeightLiveData, value -> {
            if (startingWeightLiveData.getValue() != null && value != null)
                result.setValue(startingWeightLiveData.getValue().getWeight() - value.getWeight());
            else
                result.setValue(0F);
        });

        return result;
    });

    // Use the starting weight and goal weight to get the total weight loss goal.
    public LiveData<Float> getTargetLossWeight = Transformations.switchMap(loggedInUserId, userId -> {
        LiveData<Weight> startingWeightLiveData = getStartingWeight;
        LiveData<GoalWeight> goalWeightLiveData = getGoalWeight;

        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(startingWeightLiveData, value -> {
            if (goalWeightLiveData.getValue() != null && value != null)
                result.setValue(value.getWeight() - goalWeightLiveData.getValue().getWeight());
            else
                result.setValue(0F);
        });
        result.addSource(goalWeightLiveData, value -> {
            if (startingWeightLiveData.getValue() != null && value != null)
                result.setValue(startingWeightLiveData.getValue().getWeight() - value.getWeight());
            else
                result.setValue(0F);
        });

        return result;
    });

    // Use the target weight loss and total weight loss so far to get the amount of weight left to lose
    public LiveData<Float> getTargetLeftWeight = Transformations.switchMap(loggedInUserId, userId -> {
        LiveData<Float> targetLossLiveData = getTargetLossWeight;
        LiveData<Float> totalLossLiveData = getTotalLossWeight;

        MediatorLiveData<Float> result = new MediatorLiveData<>();

        result.addSource(targetLossLiveData, value -> {
            if (totalLossLiveData.getValue() != null && value != null)
                result.setValue(value - totalLossLiveData.getValue());
            else
                result.setValue(0F);
        });
        result.addSource(totalLossLiveData, value -> {
            if (targetLossLiveData.getValue() != null && value != null)
                result.setValue(targetLossLiveData.getValue() - value);
            else
                result.setValue(0F);
        });

        return result;
    });

    public Weight getWeight(long id) { return weightRepo.getWeight(id); }

    public void addWeight(Weight weight) {
        weightRepo.addWeight(weight);
    }

    public void updateWeight(Weight weight) {
        weightRepo.updateWeight(weight);
    }

    public void deleteWeight(Weight weight) {
        weightRepo.deleteWeight(weight);
    }

    public void addGoalWeight(GoalWeight goalWeight) { weightRepo.addGoalWeight(goalWeight); }

    public void updateGoalWeight(GoalWeight goalWeight) { weightRepo.updateGoalWeight(goalWeight); }

    // Check if the current weight is at or below the goal weight and return true if goal has been reached
    public void checkGoalReached(float currentWeight) {
        if (getGoalWeight.getValue() == null) {
            goalReached.setValue(false);
            return;
        }

        float goalWeight = getGoalWeight.getValue().getWeight();
        goalReached.setValue(currentWeight <= goalWeight);
    }

    // Calculate the weight loss percentage
    private float calculateResult(LiveData<Weight> startingLive, LiveData<Weight> currentLive, LiveData<GoalWeight> goalLive) {
        Weight starting = startingLive.getValue();
        Weight current = currentLive.getValue();
        GoalWeight goal = goalLive.getValue();

        if (starting == null || current == null || goal == null) {
            return 0;
        }

        float currentWeightLoss = starting.getWeight() - current.getWeight();
        float totalLossGoal = starting.getWeight() - goal.getWeight();

        return Math.min(((currentWeightLoss / totalLossGoal) * 100), 100);
    }
}
