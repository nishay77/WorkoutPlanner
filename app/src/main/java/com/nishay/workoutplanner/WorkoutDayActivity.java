package com.nishay.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.nishay.sql.SQL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutDayActivity extends AppCompatActivity implements WorkoutDayDialogFragment.WorkoutDayListener, WorkoutDayOptionDialogFragment.WorkoutDayOptionListener {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private List<Map<String, String>> data;
    private SimpleAdapter workoutDayAdapter;
    private ArrayList<WorkoutDay> days;
    private SQL sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.workout_day_toolbar);
        toolbar.setTitle(getString(R.string.workout_day));
        setSupportActionBar(toolbar);

        sql = new SQL(this);
        sql.open();

        setupData();

        ListView listview = (ListView)findViewById(R.id.workout_day_list_view);
        listview.setAdapter(workoutDayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WorkoutSetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Day", ((TextView)(view.findViewById(android.R.id.text1))).getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                WorkoutDayOptionDialogFragment fragment = new WorkoutDayOptionDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Edit");
                b.putString("OldName", ((TextView)(view.findViewById(android.R.id.text1))).getText().toString());
                b.putInt("i", i);
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutDayOptionDialogFragment");

                return true;
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.workout_day_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //probably open dialogfragment to add new day
                WorkoutDayDialogFragment fragment = new WorkoutDayDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Add");
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutDayDialogFragment");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStop() {
        sql.close();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        sql.open();
    }

    /**
     * Read from database and get all from table -- Workout.WorkoutDays
     */
    private void setupData() {
        days = sql.getDays();
        data = new ArrayList<Map<String, String>>();

        for(WorkoutDay day: days) {
            //for this day, get all workout sets
            StringBuilder minorText = new StringBuilder();
            ArrayList<String> sets = sql.getSets(day.getName());
            if(sets == null || sets.size() == 0) {
                minorText.append("Click here to add workouts to this day.");
            }
            else {
                for(String set : sets) {
                    minorText.append(set);
                    minorText.append(", ");
                }
                //strip off last comma
                minorText.deleteCharAt(minorText.length()-2);
            }

            Map<String, String> line = new HashMap<String, String>(2);
            line.put("1", day.getName());
            line.put("2",minorText.toString());
            data.add(line);
        }


        workoutDayAdapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"1", "2"},
                new int[] {android.R.id.text1, android.R.id.text2 });
    }

    @Override
    public void onAddDialogYesClick(WorkoutDayDialogFragment fragment, WorkoutDay day) {
        if(day.getName().trim().equals("") || sql.dayExists(day.getName().trim())) {
            //name already exists or empty
            return;
        }
        sql.addDay(day.getName().trim());
        Map<String, String> entry = new HashMap<>();
        entry.put("1", day.getName());
        entry.put("2", getString(R.string.no_workouts));
        data.add(entry);

        workoutDayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditDialogYesClick(WorkoutDayDialogFragment fragment, WorkoutDay day, String oldName, int index) {
        sql.updateDayName(oldName, day.getName());
        HashMap<String, String> entry = (HashMap<String, String>) data.remove(index);
        entry.put("1", day.getName());
        data.add(index, entry);
        workoutDayAdapter.notifyDataSetChanged();

        //also changes sharedpreference is oldname is current day
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentDayPreference = preferences.getString("day", null);
        if(currentDayPreference == null) {
            //first time, not run yet, current day is first day
            if(sql.getDays().get(0).getName().equals(oldName)) {
                //changed name is also first day
                currentDayPreference = day.getName();
            }
        }
        else if(currentDayPreference.equals(oldName)) {
            currentDayPreference = day.getName();
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("day", currentDayPreference);
        editor.commit();
    }

    @Override
    public void onClickWorkoutDayEdit(WorkoutDayOptionDialogFragment fragment, Bundle bundle) {
        WorkoutDayDialogFragment frag = new WorkoutDayDialogFragment();
        frag.setArguments(bundle);
        frag.show(getSupportFragmentManager(), "EditWorkoutDayDialogFragment");
    }

    @Override
    public void onClickWorkoutDayDelete(WorkoutDayOptionDialogFragment fragment, String name, int index) {
        //deleted from sql
        sql.deleteDayName(name);

        //delete from adapter
        data.remove(index);
        workoutDayAdapter.notifyDataSetChanged();
    }
}
