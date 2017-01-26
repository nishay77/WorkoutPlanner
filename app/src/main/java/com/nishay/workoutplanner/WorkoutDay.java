package com.nishay.workoutplanner;

/**
 * Wrapper class for each workout day
 * Created by Nishay on 8/1/2016.
 */
public class WorkoutDay {
    private long ID;
    private String Name;

    public WorkoutDay(long id, String name) {
        this.ID = id;
        this.Name = name;
    }

    public WorkoutDay(String name){
        this.Name = name;
        this.ID = -1;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
