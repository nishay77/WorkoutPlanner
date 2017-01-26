package com.nishay.workoutplanner;

import java.util.ArrayList;

/**
 * wrapper --> set with set of exercises, for use in WorkoutSetActivity
 */

public class WorkoutSet {

    private String set = null;
    private ArrayList<String> exercises = new ArrayList<>();
    private boolean checked = false;

    public void setSet(String setName) {
        set = setName;
    }

    public void addExercise(String e) {
        exercises.add(e);
    }

    public String getSetName() {
        return set;
    }

    public ArrayList<String> getExercises() {
        return exercises;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return this.checked;
    }

}
