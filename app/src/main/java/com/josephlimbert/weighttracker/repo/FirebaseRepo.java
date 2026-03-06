package com.josephlimbert.weighttracker.repo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.model.Weight;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseRepo {
    private static FirebaseRepo instance = null;
    private static String TAG = "FirebaseRepository";
    private static String WEIGHT = "weights";
    private static String USER = "users";
    private static MutableLiveData<FirebaseUser> authUser = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private static FirebaseAuth auth;

    private FirebaseRepo() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized  FirebaseRepo getInstance() {
        if (instance == null) {
            instance = new FirebaseRepo();
            auth = FirebaseAuth.getInstance();
            auth.addAuthStateListener(firebaseAuth -> {
                authUser.postValue(firebaseAuth.getCurrentUser());
            });
        }
        return instance;
    }

    // Get the list of weights in the database
    public MutableLiveData<List<Weight>> getWeightList() {
        MutableLiveData<List<Weight>> liveWeightList = new MutableLiveData<>();
        if (authUser.getValue() == null) return liveWeightList;
        db.collection(WEIGHT)
                .whereEqualTo("userId", authUser.getValue().getUid())
                .orderBy("recordedDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed", e);
                        return;
                    }

                    List<Weight> weights = new ArrayList<>();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Weight weight = document.toObject(Weight.class);
                            weights.add(weight);
                        }
                    }
                    liveWeightList.postValue(weights);
                });
        return liveWeightList;
    }

    // Get a weight based on its ID
    public MutableLiveData<Weight> getWeightById(String id) {
        MutableLiveData<Weight> liveWeight = new MutableLiveData<>();
        db.collection(WEIGHT)
                .document(id)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Weight weight = snapshot.toObject(Weight.class);
                        liveWeight.postValue(weight);
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
        return liveWeight;
    }

    public MutableLiveData<Weight> getStartingWeight() {
        MutableLiveData<Weight> liveWeight = new MutableLiveData<>();
        if (authUser.getValue() == null) return liveWeight;
        db.collection(WEIGHT)
                .whereEqualTo("userId", authUser.getValue().getUid())
                .orderBy("recordedDate", Query.Direction.ASCENDING)
                .limit(1)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed", e);
                        return;
                    }

                    List<Weight> weights = new ArrayList<>();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Weight weight = document.toObject(Weight.class);
                            weights.add(weight);
                        }
                        liveWeight.postValue(weights.get(0));
                    }
                });
        return liveWeight;
    }

    public MutableLiveData<Weight> getCurrentWeight() {
        MutableLiveData<Weight> liveWeight = new MutableLiveData<>();
        if (authUser.getValue() == null) return liveWeight;
        db.collection(WEIGHT)
                .whereEqualTo("userId", authUser.getValue().getUid())
                .orderBy("recordedDate", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed", e);
                        return;
                    }

                    List<Weight> weights = new ArrayList<>();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Weight weight = document.toObject(Weight.class);
                            weights.add(weight);
                        }
                        liveWeight.postValue(weights.get(0));
                    }
                });

        return liveWeight;
    }

    // Add a new weight to the database.
    public void addWeight(Weight weight) {
        if (authUser.getValue() == null) return;
        if (weight.getId() == null) {
            weight.setUserId(authUser.getValue().getUid());
        } else {
            deleteWeight(weight);
        }
        String id = hashId(weight.getRecordedDate().toString() + authUser.getValue().getUid());
        weight.setId(id);
        db.collection(WEIGHT)
                .document(weight.getId())
                .set(weight)
                .addOnSuccessListener(unused -> Log.d(TAG, "Weight updated with id: " + weight.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Add weight failed.", e));
    }

    // Delete a weight from the database
    public void deleteWeight(Weight weight) {
        db.collection(WEIGHT)
                .document(weight.getId())
                .delete()
                .addOnSuccessListener(unused -> Log.d("DELETE", "Document deleted with id: " + weight.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Delete document failed.", e));
    }

    public MutableLiveData<User> getUserLiveData() {
        MutableLiveData<User> data = new MutableLiveData<>();
        if (authUser.getValue() == null) return data;
        db.collection(USER)
                .document(authUser.getValue().getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        data.postValue(value.toObject(User.class));

                    } else {
                        Log.w(TAG, "Failed to get user");
                    }
                });
        return data;
    }

    public void addUserPhone(String phone) {
        if (authUser.getValue() != null) {
            db.collection(USER)
                    .document(authUser.getValue().getUid())
                    .update("phone", phone)
                    .addOnSuccessListener(aVoid -> Log.d("ADD", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "failed to update user phone", e));
        }
    }

    // Add a goal weight to the database
    public void addGoalWeight(float goalWeight) {
        if (authUser.getValue() != null) {
            db.collection(USER)
                    .document(authUser.getValue().getUid())
                    .update("goalWeight", goalWeight)
                    .addOnSuccessListener(aVoid -> Log.d("ADD", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "failed to update goal weight", e));
        }
    }

    // Update the goal weight
    public void updateGoalWeight(float goalWeight) {
        if (authUser.getValue() != null) {
            db.collection(USER)
                    .document(authUser.getValue().getUid())
                    .update("goalWeight", goalWeight)
                    .addOnSuccessListener(aVoid -> Log.d("UPDATE", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "failed to update goal weight", e));
        }
    }

    public MutableLiveData<Float> getGoalWeight() {
        MutableLiveData<Float> liveGoalWeight = new MutableLiveData<>();
        if (authUser.getValue() == null) {
            return liveGoalWeight;
        }
        db.collection(USER)
                .document(authUser.getValue().getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            float goal = user.goalWeight;
                            liveGoalWeight.postValue(goal);
                        } else {
                            Log.d(TAG, "Current user: null");
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
        return liveGoalWeight;
    }

    public void signInEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void signUpEmail(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener)
                .addOnSuccessListener(authResult -> {
                    addUser(authResult.getUser());
                });
    }

    public void signInAnonymous(OnCompleteListener<AuthResult> listener) {
        auth.signInAnonymously()
                .addOnCompleteListener(listener)
                .addOnSuccessListener(authResult -> {
                    addUser(authResult.getUser());
                });
    }

    public void signOut() {
        auth.signOut();
    }

    public MutableLiveData<FirebaseUser> getAuthUser() {
        return authUser;
    }

    private void addUser(FirebaseUser user) {
        if (user == null) {
            Log.w(TAG, "No user signed in");
            return;
        }
        User newUser = new User();
        newUser.id = user.getUid();
        newUser.email = user.getEmail();
        db.collection(USER).document(newUser.id).set(newUser)
                .addOnSuccessListener(unused -> Log.d("ADD", "Document added with id: " + user.getUid()))
                .addOnFailureListener(e -> Log.w(TAG, "failed to add user", e));
    }

    private String hashId(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
