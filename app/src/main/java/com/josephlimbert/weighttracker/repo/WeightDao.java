package com.josephlimbert.weighttracker.repo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.josephlimbert.weighttracker.model.GoalWeight;
import com.josephlimbert.weighttracker.model.Weight;

import java.util.List;

@Dao
public interface WeightDao {
    @Query("SELECT * FROM Weights WHERE user_id = :userId ORDER BY recorded_date DESC LIMIT 1")
    LiveData<Weight> getCurrentWeight(long userId);

    @Query("SELECT * FROM Weights WHERE user_id = :userId ORDER BY recorded_date ASC LIMIT 1")
    LiveData<Weight> getStartingWeight(long userId);

    @Query("SELECT * FROM Weights WHERE user_id = :userId ORDER BY recorded_date DESC")
    LiveData<List<Weight>> getWeightList(long userId);

    @Query("SELECT * FROM Weights WHERE id = :id")
    Weight getWeight(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addWeight(Weight weight);

    @Update
    void updateWeight(Weight weight);

    @Delete
    void deleteWeight(Weight weight);

    @Query("SELECT * FROM Goal_Weight WHERE user_id = :userId")
    LiveData<GoalWeight> getGoalWeight(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addGoalWeight(GoalWeight goalWeight);

    @Update
    void updateGoalWeight(GoalWeight goalWeight);
}
