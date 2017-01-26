package com.nishay.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jjoe64.graphview.series.DataPoint;
import com.nishay.workoutplanner.WorkoutDay;
import com.nishay.workoutplanner.WorkoutExercise;
import com.nishay.workoutplanner.WorkoutSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SQL {

    private SQLiteDatabase db;
    private SQLHelper helper;

    public SQL(Context c) {
        helper = new SQLHelper(c);
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    /**
     * Insert into SQLHelper.TABLE_DAYS
     * @param dayName String
     */
    public WorkoutDay addDay(String dayName) {
        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_DAYS_DAYNAME, dayName);
        long id = db.insert(SQLHelper.TABLE_DAYS, null, values);

        return new WorkoutDay(id, dayName);
    }

    public ArrayList<WorkoutDay> getDays() {
        ArrayList<WorkoutDay> days = new ArrayList<>();
        String query = "Select * From " + SQLHelper.TABLE_DAYS + " ORDER BY " + SQLHelper.COLUMN_ID;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                WorkoutDay day = new WorkoutDay(
                        cursor.getLong(cursor.getColumnIndex(SQLHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_DAYS_DAYNAME))
                        );
                days.add(day);
                cursor.moveToNext();
            }
            cursor.close();
            return days;
        }
        else {
            return null;
        }
    }

    public boolean dayExists(String dayName) {
        String query = "Select * from " + SQLHelper.TABLE_DAYS + " where " + SQLHelper.COLUMN_DAYS_DAYNAME + " = \"" + dayName + "\"";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        else {
            c.close();
            return false;
        }
    }

    public void updateDayName(String oldName, String newName) {
        //update daytable, day_set table

        ContentValues dayTableValues = new ContentValues();
        dayTableValues.put(SQLHelper.COLUMN_DAYS_DAYNAME, newName);
        db.update(SQLHelper.TABLE_DAYS, dayTableValues, SQLHelper.COLUMN_DAYS_DAYNAME + "=?", new String[]{oldName});

        ContentValues daySetTableValues = new ContentValues();
        daySetTableValues.put(SQLHelper.COLUMN_DAYS_SETS_DAYNAME, newName);
        db.update(SQLHelper.TABLE_DAYS_SETS, daySetTableValues, SQLHelper.COLUMN_DAYS_SETS_DAYNAME + "=?", new String[]{oldName});
    }

    public void deleteDayName(String name) {
        //delete from daytable, day_set table
        db.delete(SQLHelper.TABLE_DAYS, SQLHelper.COLUMN_DAYS_DAYNAME + "=?", new String[]{name});
        db.delete(SQLHelper.TABLE_DAYS_SETS, SQLHelper.COLUMN_DAYS_SETS_DAYNAME + "=?", new String[]{name});
    }



    public ArrayList<String> getSets(String day) {
        ArrayList<String> sets = new ArrayList<>();
        String query;
        if(day != null) {
            query = "Select * From " + SQLHelper.TABLE_DAYS_SETS + " where " + SQLHelper.COLUMN_DAYS_SETS_DAYNAME + " = \"" + day + "\" order by " + SQLHelper.COLUMN_ID;
        }
        else {
            //grab all days
            query = "Select * from " + SQLHelper.TABLE_SETS + " order by " + SQLHelper.COLUMN_ID;
        }
        Cursor cursor = db.rawQuery(query, null);

        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                sets.add(cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_SETNAME)));
                cursor.moveToNext();
            }
            cursor.close();
            return sets;
        }
        else {
            return null;
        }
    }

    public void addSet(String setName) {
        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_SETS_SETNAME, setName);
        db.insert(SQLHelper.TABLE_SETS, null, values);
    }


    public void addSetExercise(String setName, String exercise, int sets, int reps, double weight, double increment) {
        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_SETNAME, setName);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME, exercise);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_SETS, sets);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_REPS, reps);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT, weight);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT, increment);
        long id = db.insert(SQLHelper.TABLE_SETS_EXERCISES, null, values);
    }

    public ArrayList<WorkoutSet> getExercises(String day) {
        String query = "Select * from " + SQLHelper.TABLE_SETS + " order by " + SQLHelper.COLUMN_ID;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.getCount() != 0) {
            ArrayList<WorkoutSet> set_exercises = new ArrayList<>();
            ArrayList<String> sets = new ArrayList<>();
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                sets.add(cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_SETNAME)));
                cursor.moveToNext();
            }
            cursor.close();

            WorkoutSet exercise = null;
            for(String setName : sets) {
                exercise = new WorkoutSet();

                //query TABLE_SET_EXERCISE for each setname
                query = "Select * from " + SQLHelper.TABLE_SETS_EXERCISES +
                        " where " + SQLHelper.COLUMN_SETS_EXERCISES_SETNAME + "=? ORDER BY " + SQLHelper.COLUMN_ID;
                cursor = db.rawQuery(query, new String[]{setName});
                if(cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    exercise.setSet(setName);
                    while(!cursor.isAfterLast()) {
                        exercise.addExercise(cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME)));
                        cursor.moveToNext();
                    }
                }
                else if (cursor.getCount() == 0) {
                    exercise.setSet(setName);
                }
                cursor.close();

                //now check if this exercise needs to be checked based on day
                //query the days_sets table where this day + this set (setName)
                query = "Select * from " + SQLHelper.TABLE_DAYS_SETS +
                        " where " + SQLHelper.COLUMN_DAYS_SETS_DAYNAME + "=? AND " +
                        SQLHelper.COLUMN_DAYS_SETS_SETNAME + "=?";
                cursor = db.rawQuery(query, new String[]{day, setName});
                if(cursor.getCount() > 0) {
                    exercise.setChecked(true);
                }
                cursor.close();

                set_exercises.add(exercise);
            }

            return set_exercises;
        }
        else {
            return null;
        }
    }

    public boolean setExists(String setName) {
        String query = "Select * from " + SQLHelper.TABLE_SETS + " where " + SQLHelper.COLUMN_SETS_SETNAME + " = \"" + setName + "\"";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        else {
            c.close();
            return false;
        }
    }

    public void updateSetName(String oldName, String newName) {
        //update sets, day_set, set_exercise table

        ContentValues setTableValues = new ContentValues();
        setTableValues.put(SQLHelper.COLUMN_SETS_SETNAME, newName);
        db.update(SQLHelper.TABLE_SETS, setTableValues, SQLHelper.COLUMN_SETS_SETNAME + "=?", new String[]{oldName});

        ContentValues daySetTableValues = new ContentValues();
        daySetTableValues.put(SQLHelper.COLUMN_DAYS_SETS_SETNAME, newName);
        db.update(SQLHelper.TABLE_DAYS_SETS, daySetTableValues, SQLHelper.COLUMN_DAYS_SETS_SETNAME + "=?", new String[]{oldName});

        ContentValues setExerciseTableValues = new ContentValues();
        setExerciseTableValues.put(SQLHelper.COLUMN_SETS_EXERCISES_SETNAME, newName);
        db.update(SQLHelper.TABLE_SETS_EXERCISES, setExerciseTableValues, SQLHelper.COLUMN_SETS_EXERCISES_SETNAME + "=?", new String[]{oldName});
    }

    public void deleteSetName(String name) {
        //delete from daytable, day_set table
        db.delete(SQLHelper.TABLE_SETS, SQLHelper.COLUMN_SETS_SETNAME + "=?", new String[]{name});
        db.delete(SQLHelper.TABLE_DAYS_SETS, SQLHelper.COLUMN_DAYS_SETS_SETNAME + "=?", new String[]{name});
        db.delete(SQLHelper.TABLE_SETS_EXERCISES, SQLHelper.COLUMN_SETS_EXERCISES_SETNAME + "=?", new String[]{name});
    }


    public void updateSetListForDay(String day, ArrayList<String> checkedList) {
        //delete from days_sets for this day
        String query = "delete from " + SQLHelper.TABLE_DAYS_SETS + " where " +
                        SQLHelper.COLUMN_DAYS_SETS_DAYNAME + "=\"" + day + "\";";
        db.execSQL(query);

        //reinsert this new list
        for(String set : checkedList) {
            ContentValues values = new ContentValues();
            values.put(SQLHelper.COLUMN_DAYS_SETS_DAYNAME, day);
            values.put(SQLHelper.COLUMN_DAYS_SETS_SETNAME, set);
            db.insert(SQLHelper.TABLE_DAYS_SETS, null, values);
        }
    }

    /**
     *get all exercises and details for a set, WorkoutExerciseActivity
     */
    public ArrayList<WorkoutExercise> getExercisesList(String set) {
        ArrayList<WorkoutExercise> exercises = new ArrayList<>();
        String query = "Select * from " + SQLHelper.TABLE_SETS_EXERCISES +
                        " where " + SQLHelper.COLUMN_SETS_EXERCISES_SETNAME + "=\"" + set + "\" ORDER BY " + SQLHelper.COLUMN_ID;

        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            WorkoutExercise exercise;
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                exercise = new WorkoutExercise();
                exercise.setName(cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME)));
                exercise.setSets(cursor.getInt(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_SETS)));
                exercise.setReps(cursor.getInt(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_REPS)));
                exercise.setWeight(cursor.getDouble(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT)));
                exercise.setIncrement(cursor.getDouble(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT)));

                exercises.add(exercise);

                cursor.moveToNext();
            }
            return exercises;
        }
        else {
            return null;
        }
    }

    public void addExercise(String set, WorkoutExercise exercise) {
        //for this set, add the exercise into the SET_EXERCISES table
        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_SETNAME, set);
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME, exercise.getName());
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_SETS, exercise.getSets());
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_REPS, exercise.getReps());
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT, exercise.getWeight());
        values.put(SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT, exercise.getIncrement());

        db.insert(SQLHelper.TABLE_SETS_EXERCISES, null, values);
    }

    public void updateExercise(String setName, WorkoutExercise oldExercise, WorkoutExercise newExercise) {
        String table = SQLHelper.TABLE_SETS_EXERCISES;
        String colSetName = SQLHelper.COLUMN_SETS_EXERCISES_SETNAME;
        String colExerciseName = SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME;
        String colSets = SQLHelper.COLUMN_SETS_EXERCISES_SETS;
        String colReps = SQLHelper.COLUMN_SETS_EXERCISES_REPS;
        String colWeight = SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT;
        String colIncrement = SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT;

        ContentValues values = new ContentValues();
        values.put(colSetName, setName);
        values.put(colExerciseName, newExercise.getName());
        values.put(colSets, newExercise.getSets());
        values.put(colReps, newExercise.getReps());
        values.put(colWeight, newExercise.getWeight());
        values.put(colIncrement, newExercise.getIncrement());

        String where =
                colSetName + "=? AND " +
                colExerciseName + "=? AND " +
                colSets + "=? AND " +
                colReps + "=? AND " +
                colWeight + "=? AND " +
                colIncrement + "=?";

        String[] args = new String[]{
                setName,
                oldExercise.getName(),
                oldExercise.getSets() + "",
                oldExercise.getReps() + "",
                oldExercise.getWeight() + "",
                oldExercise.getIncrement() + ""
        };

        db.update(table, values, where, args);
    }

    public void deleteExercise(String setName, WorkoutExercise exercise) {
        String table = SQLHelper.TABLE_SETS_EXERCISES;
        String colSetName = SQLHelper.COLUMN_SETS_EXERCISES_SETNAME;
        String colExerciseName = SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME;
        String colSets = SQLHelper.COLUMN_SETS_EXERCISES_SETS;
        String colReps = SQLHelper.COLUMN_SETS_EXERCISES_REPS;
        String colWeight = SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT;
        String colIncrement = SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT;


        String where =
                colSetName + "=? AND " +
                        colExerciseName + "=? AND " +
                        colSets + "=? AND " +
                        colReps + "=? AND " +
                        colWeight + "=? AND " +
                        colIncrement + "=?";

        String[] args = new String[]{
                setName,
                exercise.getName(),
                exercise.getSets() + "",
                exercise.getReps() + "",
                exercise.getWeight() + "",
                exercise.getIncrement() + ""
        };

        db.delete(table, where, args);
    }

//    public void addDayPercent(String day, double percent) {
//        ContentValues values = new ContentValues();
//        values.put(SQLHelper.COLUMN_DAYS_PERCENTS_DAYNAME, day);
//        values.put(SQLHelper.COLUMN_DAYS_PERCENTS_PERCENT, percent);
//        Calendar c = Calendar.getInstance();
//        Date d = c.getTime();
//        long l = d.getTime();
//        values.put(SQLHelper.COLUMN_DAYS_PERCENTS_DATE, l);
//
//        db.insert(SQLHelper.TABLE_DAYS_PERCENTS, null, values);
//    }
//
//    public ArrayList<DataPoint> getDaysPercents() {
//        ArrayList<DataPoint> data = new ArrayList<>();
//
//        Cursor cursor = db.rawQuery("Select * From " + SQLHelper.TABLE_DAYS_PERCENTS +
//                                    " order by " + SQLHelper.COLUMN_ID, null);
//
//        if(cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            //int x = 0; //x axis
//            while(!cursor.isAfterLast()) {
//                long l = cursor.getLong(cursor.getColumnIndex(SQLHelper.COLUMN_DAYS_PERCENTS_DATE));
//                Date x = new Date(l);
//
//                double y = cursor.getDouble(cursor.getColumnIndex(SQLHelper.COLUMN_DAYS_PERCENTS_PERCENT));
//                DataPoint point = new DataPoint(x, y);
//                data.add(point);
//                cursor.moveToNext();
//            }
//        }
//
//        return data;
//    }

    public void incrementWeight(WorkoutExercise exercise) {
        //update this exercises weight by its increment
        String table = SQLHelper.TABLE_SETS_EXERCISES;
        String colExerciseName = SQLHelper.COLUMN_SETS_EXERCISES_EXERCISENAME;
        String colSets = SQLHelper.COLUMN_SETS_EXERCISES_SETS;
        String colReps = SQLHelper.COLUMN_SETS_EXERCISES_REPS;
        String colWeight = SQLHelper.COLUMN_SETS_EXERCISES_WEIGHT;
        String colIncrement = SQLHelper.COLUMN_SETS_EXERCISES_INCREMENT;

        ContentValues values = new ContentValues();
        values.put(colWeight, exercise.getWeight() + exercise.getIncrement());

        String where =
                        colExerciseName + "=? AND " +
                        colSets + "=? AND " +
                        colReps + "=? AND " +
                        colWeight + "=? AND " +
                        colIncrement + "=?";

        String[] args = new String[]{
                exercise.getName(),
                exercise.getSets() + "",
                exercise.getReps() + "",
                exercise.getWeight() + "",
                exercise.getIncrement() + ""
        };

        db.update(table, values, where, args);

    }

    public void insertWeightDate(WorkoutExercise exercise, String setName) {
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        long l = d.getTime();

        ContentValues values = new ContentValues();
        values.put(SQLHelper.COLUMN_WEIGHT_DATE_SETNAME, setName);
        values.put(SQLHelper.COLUMN_WEIGHT_DATE_EXERCISE, exercise.getName());
        values.put(SQLHelper.COLUMN_WEIGHT_DATE_WEIGHT, exercise.getWeight());
        values.put(SQLHelper.COLUMN_WEIGHT_DATE_DATE, l);

        db.insert(SQLHelper.TABLE_WEIGHT_DATE, null, values);
    }

    public ArrayList<DataPoint> getWeightDates(String set, String exercise) {
        String query = "Select * From " + SQLHelper.TABLE_WEIGHT_DATE +
                        " where " + SQLHelper.COLUMN_WEIGHT_DATE_SETNAME + "=? AND " +
                        SQLHelper.COLUMN_WEIGHT_DATE_EXERCISE + "=? order by " +
                        SQLHelper.COLUMN_ID +";";

        Cursor cursor = db.rawQuery(query, new String[]{set, exercise});
        ArrayList<DataPoint> data = new ArrayList<>();
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                long l = cursor.getLong(cursor.getColumnIndex(SQLHelper.COLUMN_WEIGHT_DATE_DATE));
                Date x = new Date(l);
                double y = cursor.getDouble(cursor.getColumnIndex(SQLHelper.COLUMN_WEIGHT_DATE_WEIGHT));
                DataPoint point = new DataPoint(x, y);
                data.add(point);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return data;
    }

    //more simple get all sets list
    public ArrayList<String> getAllSetsNames() {
        ArrayList<String> list = new ArrayList<>();

        String query = "Select * From " + SQLHelper.TABLE_SETS + " order by " + SQLHelper.COLUMN_ID;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                String s = cursor.getString(cursor.getColumnIndex(SQLHelper.COLUMN_SETS_SETNAME));
                list.add(s);
                cursor.moveToNext();
            }
            cursor.close();
        }


        return list;
    }

    public void deleteHistory() {
        String query = "delete from " + SQLHelper.TABLE_WEIGHT_DATE;
        db.execSQL(query);
    }

}
