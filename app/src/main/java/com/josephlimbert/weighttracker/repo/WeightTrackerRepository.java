package com.josephlimbert.weighttracker.repo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.josephlimbert.weighttracker.model.GoalWeight;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.model.Weight;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeightTrackerRepository {
    private static WeightTrackerRepository weightTrackerRepo;
    private final WeightDao weightDao;
    private final UserDao userDao;

    // Store the user ID of the logged-in user. Used to get data from the weight table for the user.
    private final MutableLiveData<Long> loggedInUserId = new MutableLiveData<>();

    public static WeightTrackerRepository getInstance(Context context) {
        if (weightTrackerRepo == null) {
            weightTrackerRepo = new WeightTrackerRepository(context);
        }
        return weightTrackerRepo;
    }

    private WeightTrackerRepository(Context context) {
        WeightTrackerDatabase database = Room.databaseBuilder(context, WeightTrackerDatabase.class, "weight.db")
                .allowMainThreadQueries()
                .build();

        weightDao = database.weightDao();
        userDao = database.userDao();
    }

    // Get the latest weight entered into the database. This would be the current weight.
    public LiveData<Weight> getCurrentWeight(long userId) {
        return weightDao.getCurrentWeight(userId);
    }

    // Get the first weight entered into the database. This would be the starting weight.
    public LiveData<Weight> getStartingWeight(long userId) {
        return weightDao.getStartingWeight(userId);
    }

    // Get the list of weights in the database
    public LiveData<List<Weight>> getWeightList(long userId) {
        return weightDao.getWeightList(userId);
    }

    // Get a weight based on its ID
    public Weight getWeight(long id) {
        return weightDao.getWeight(id);
    }

    // Add a new weight to the database.
    public void addWeight(Weight weight) {
        weightDao.addWeight(weight);
    }

    // Update an existing weight
    public void updateWeight(Weight weight) {
        weightDao.updateWeight(weight);
    }

    // Delete a weight from the database
    public void deleteWeight(Weight weight) {
        weightDao.deleteWeight(weight);
    }

    // Get the goal weight from the database
    public LiveData<GoalWeight> getGoalWeight(long userId) {
        return weightDao.getGoalWeight(userId);
    }

    // Add a goal weight to the database
    public void addGoalWeight(GoalWeight goalWeight) {
        weightDao.addGoalWeight(goalWeight);
    }

    // Update the goal weight
    public void updateGoalWeight(GoalWeight goalWeight) {
        weightDao.updateGoalWeight(goalWeight);
    }

    // Store the logged in user ID to a SharedPreferences file then set the value of the
    // MutableLiveData variable so we can start retrieving weight data
    public void setLoggedInUserId(long userId, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("AuthUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("userId", userId);
        editor.apply();
        loggedInUserId.setValue(userId);
    }

    // Get the logged in user ID
    public MutableLiveData<Long> getLoggedInUserId() { return loggedInUserId; }

    // Clear the user ID from shared preference file and the mutable live data variable.
    // This will log us out and cause the log in screen to be shown.
    public void logOutUser(Context context) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("AuthUser", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        loggedInUserId.setValue(null);
    }

    //Get the user from the database when logging in
    public User loginUser(String username, String password) {
        return userDao.loginUser(username, password);
    }

    // add a new user to the database
    public long registerUser(User user) {
        return userDao.registerUser(user);
    }
}
