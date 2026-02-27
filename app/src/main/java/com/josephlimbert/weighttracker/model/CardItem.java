package com.josephlimbert.weighttracker.model;

public class CardItem {
    public static int ProgressCard = 0;
    public static int StatCard = 1;
    private int viewType;
    private String currentWeight;
    private String goalWeight;
    private String startingWeight;
    private int percentage;
    private String statText;
    private String statLabel;

    public CardItem(int viewType, String currentWeight, String goalWeight, String startingWeight, int percentage) {
        this.viewType = viewType;
        this.currentWeight = currentWeight;
        this.goalWeight = goalWeight;
        this.startingWeight = startingWeight;
        this.percentage = percentage;
    }

    public CardItem(int viewType, String statText, String statLabel) {
        this.viewType = viewType;
        this.statText = statText;
        this.statLabel = statLabel;
    }

    public int getViewType() {
        return viewType;
    }

    public String getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(String currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(String goalWeight) {
        this.goalWeight = goalWeight;
    }

    public String getStartingWeight() {
        return startingWeight;
    }

    public void setStartingWeight(String startingWeight) {
        this.startingWeight = startingWeight;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getStatText() {
        return statText;
    }

    public void setStatText(String statText) {
        this.statText = statText;
    }

    public String getStatLabel() {
        return statLabel;
    }

    public void setStatLabel(String statLabel) {
        this.statLabel = statLabel;
    }
}

