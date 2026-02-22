package com.josephlimbert.weighttracker.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.josephlimbert.weighttracker.model.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM Users WHERE username = :username AND password = :password")
    User loginUser(String username, String password);

    @Insert
    long registerUser(User user);
}
