package com.nishay.workoutplanner;

import java.io.Serializable;

/**
 * Wrapper for each exercise
 */
public class WorkoutExercise implements Serializable {

    private String name = null;
    private int sets = 0;
    private int reps = 0;
    private double weight = 0.0;
    private double increment = 0.0;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getIncrement() { return increment; }

    public void setIncrement(double increment) { this.increment = increment; }
}
