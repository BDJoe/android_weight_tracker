package com.josephlimbert.weighttracker.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.repo.WeightTrackerRepository;

public class UserViewModel extends AndroidViewModel {
    private final WeightTrackerRepository weightRepo;

    public UserViewModel(Application application) {
        super(application);
        weightRepo = WeightTrackerRepository.getInstance(application.getApplicationContext());
    }

    public void setLoggedInUserId(long userId, Context context) {
       weightRepo.setLoggedInUserId(userId, context);
    }

    public MutableLiveData<Long> getLoggedInUserId() { return weightRepo.getLoggedInUserId(); }

    public void logOutUser(Context context) {
        weightRepo.logOutUser(context);
    }

    public User loginUser(String username, String password) {
        return weightRepo.loginUser(username, password);
    }

    public long registerUser(User user) {
        return weightRepo.registerUser(user);
    }
}
