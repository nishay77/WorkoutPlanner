package com.nishay.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "workout_planner.db";
    public static final String COLUMN_ID = "_id";
    public static final int DBVER = 6;

    //table names
    public static final String TABLE_DAYS = "days";
    public static final String TABLE_DAYS_SETS = "days_sets";
    public static final String TABLE_SETS = "sets";
    public static final String TABLE_SETS_EXERCISES = "sets_exercises";
    public static final String TABLE_DAYS_PERCENTS = "days_percents";
    public static final String TABLE_WEIGHT_DATE = "weight_date";

    //days columns
    public static final String COLUMN_DAYS_DAYNAME = "DayName";

    //sets columns
    public static final String COLUMN_SETS_SETNAME = "WorkoutSetName";

    //days_sets columns
    public static final String COLUMN_DAYS_SETS_DAYNAME = "DayName";
    public static final String COLUMN_DAYS_SETS_SETNAME = "WorkoutSetName";

    //sets_exercises columns
    public static final String COLUMN_SETS_EXERCISES_SETNAME = "WorkoutSetName";
    public static final String COLUMN_SETS_EXERCISES_EXERCISENAME = "ExerciseName";
    public static final String COLUMN_SETS_EXERCISES_SETS = "Sets";
    public static final String COLUMN_SETS_EXERCISES_REPS = "Reps";
    public static final String COLUMN_SETS_EXERCISES_WEIGHT = "Weight";
    public static final String COLUMN_SETS_EXERCISES_INCREMENT = "Increment";

    //days_percents columns
    public static final String COLUMN_DAYS_PERCENTS_DAYNAME = "DayName";
    public static final String COLUMN_DAYS_PERCENTS_PERCENT = "Percent";
    public static final String COLUMN_DAYS_PERCENTS_DATE = "Date";

    //weight_date columns
    public static final String COLUMN_WEIGHT_DATE_SETNAME = "SetName";
    public static final String COLUMN_WEIGHT_DATE_EXERCISE = "ExerciseName";
    public static final String COLUMN_WEIGHT_DATE_WEIGHT = "Weight";
    public static final String COLUMN_WEIGHT_DATE_DATE = "Date";


    //create table strings
    private static final String CREATE_TABLE_DAYS =
            "CREATE TABLE " + TABLE_DAYS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_DAYS_DAYNAME + " text not null unique" +
            ");";

    private static final String CREATE_TABLE_SETS =
            "CREATE TABLE " + TABLE_SETS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_SETS_SETNAME + " text not null unique" +
            ");";

    private static final String CREATE_TABLE_DAYS_SETS =
            "CREATE TABLE " + TABLE_DAYS_SETS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_DAYS_SETS_DAYNAME + " text not null, " +
                    COLUMN_DAYS_SETS_SETNAME + " text, " +
                    "UNIQUE(" + COLUMN_DAYS_SETS_DAYNAME + ", " + COLUMN_DAYS_SETS_SETNAME + ")" +
            ");";

    private static final String CREATE_TABLE_SETS_EXERCISES =
            "CREATE TABLE " + TABLE_SETS_EXERCISES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_SETS_EXERCISES_SETNAME + " text not null, " +
                    COLUMN_SETS_EXERCISES_EXERCISENAME + " text, " +
                    COLUMN_SETS_EXERCISES_SETS + " integer, " +
                    COLUMN_SETS_EXERCISES_REPS + " integer, " +
                    COLUMN_SETS_EXERCISES_WEIGHT + " real, " +
                    COLUMN_SETS_EXERCISES_INCREMENT + " real, " +
                    "UNIQUE(" + COLUMN_SETS_EXERCISES_SETNAME + ", " + COLUMN_SETS_EXERCISES_EXERCISENAME + ")" +
            ");";
    private static final String CREATE_TABLE_WEIGHT_DATE =
            "CREATE TABLE " + TABLE_WEIGHT_DATE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_WEIGHT_DATE_SETNAME + " text not null, " +
                    COLUMN_WEIGHT_DATE_EXERCISE + " text not null, " +
                    COLUMN_WEIGHT_DATE_WEIGHT + " real, " +
                    COLUMN_WEIGHT_DATE_DATE + " real" +
            ");";

    public SQLHelper(Context context) {
        super(context, DBNAME, null, DBVER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DAYS);
        db.execSQL(CREATE_TABLE_SETS);
        db.execSQL(CREATE_TABLE_DAYS_SETS);
        db.execSQL(CREATE_TABLE_SETS_EXERCISES);
        //db.execSQL(CREATE_TABLE_DAYS_PERCENTS);
        db.execSQL(CREATE_TABLE_WEIGHT_DATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL("drop table if exists " + TABLE_DAYS);
        db.execSQL("drop table if exists " + TABLE_SETS);
        db.execSQL("drop table if exists " + TABLE_DAYS_SETS);
        db.execSQL("drop table if exists " + TABLE_SETS_EXERCISES);
        db.execSQL("drop table if exists " + TABLE_DAYS_PERCENTS);
        db.execSQL("drop table if exists " + TABLE_WEIGHT_DATE);
        onCreate(db);

    }
}
