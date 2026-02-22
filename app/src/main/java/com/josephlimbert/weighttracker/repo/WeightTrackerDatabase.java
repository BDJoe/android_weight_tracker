package com.josephlimbert.weighttracker.repo;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.josephlimbert.weighttracker.model.GoalWeight;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.model.Weight;

@Database(entities = {Weight.class, User.class, GoalWeight.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class WeightTrackerDatabase extends RoomDatabase {
    public abstract WeightDao weightDao();
    public abstract UserDao userDao();
}
