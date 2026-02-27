package com.josephlimbert.weighttracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.repo.FirebaseRepo;

public class UserViewModel extends AndroidViewModel {
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<FirebaseUser> authUserLiveData = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public void signInEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        FirebaseRepo.getInstance().signInEmail(email, password, listener);
    }

    public void signUpEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        FirebaseRepo.getInstance().signUpEmail(email, password, listener);
    }

    public void signInAnonymous(OnCompleteListener<AuthResult> listener) {
        FirebaseRepo.getInstance().signInAnonymous(listener);
    }
    public void signOut() {
        FirebaseRepo.getInstance().signOut();
    }
    public MutableLiveData<User> getUserProfile() {
        userLiveData = FirebaseRepo.getInstance().getUserLiveData();
        return userLiveData;
    }

    public void addUserPhone(String phone) { FirebaseRepo.getInstance().addUserPhone(phone); }

    public MutableLiveData<FirebaseUser> getAuthUser() {
        authUserLiveData = FirebaseRepo.getInstance().getAuthUser();
        return authUserLiveData;
    }
}
