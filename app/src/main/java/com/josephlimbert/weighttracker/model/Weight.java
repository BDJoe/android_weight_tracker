package com.josephlimbert.weighttracker.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Weight {
    private String id;

    private float weight;

    private Timestamp recordedDate;

    private String userId;

    public Weight () {}

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public Timestamp getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Timestamp recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

