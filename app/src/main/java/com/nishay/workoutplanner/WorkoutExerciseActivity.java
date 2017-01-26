package com.nishay.workoutplanner;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkoutExerciseActivity extends AppCompatActivity implements WorkoutExerciseDialogFragment.WorkoutExerciseListener, WorkoutExerciseOptionDialogFragment.WorkoutExerciseOptionListener {

    private Bundle bundle;
    private String set;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private ArrayList<WorkoutExercise> exercises;
    private List<Map<String, String>> data;
    private SimpleAdapter adapter;
    private SQL sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_exercise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Exercises");
        setSupportActionBar(toolbar);

        sql = new SQL(this);
        sql.open();

        bundle = getIntent().getExtras();
        set = bundle.getString("SetName");

        //we're doing another listadapter like workoutDayActivity
        setupData();
        ListView listview = (ListView)findViewById(R.id.workout_exercise_list_view);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //create WorkoutExercise object
                WorkoutExercise exercise = new WorkoutExercise();
                exercise.setName(((TextView) view.findViewById(android.R.id.text1)).getText().toString());
                String numbers = ((TextView) view.findViewById(android.R.id.text2)).getText().toString();
                int sets = 0, reps = 0;
                double weight=0.0, increment=0.0;
                Pattern p = Pattern.compile("(\\d*) x (\\d*) - (.*) - (.*) increment");
                Matcher m = p.matcher(numbers);
                if(m.matches()) {
                    sets = Integer.parseInt(m.group(1));
                    reps = Integer.parseInt(m.group(2));

                    //weight is tricky, could be a number or body weight
                    String w = m.group(3);
                    if(w.equals("Body Weight")) {
                        weight = 0.0;
                    }
                    else {
                        weight = Double.parseDouble(w.split(" ")[0]);
                    }

                    increment = Double.parseDouble((m.group(4)));
                }

                exercise.setSets(sets);
                exercise.setReps(reps);
                exercise.setWeight(weight);
                exercise.setIncrement(increment);

                WorkoutExerciseOptionDialogFragment fragment = new WorkoutExerciseOptionDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Edit");
                b.putSerializable("oldExercise", exercise);
                b.putInt("i", i);
                b.putString("setName", set);
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutExerciseOptionDialogFragment");
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.workout_exercise_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkoutExerciseDialogFragment fragment = new WorkoutExerciseDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Add");
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutExerciseDialogFragment");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpTo(this, getIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void setupData() {
        exercises = sql.getExercisesList(set);
        data = new ArrayList<Map<String, String>>();

        if(exercises == null) {
            exercises = new ArrayList<>();
        }

        for(WorkoutExercise exercise: exercises) {
            //for this set, get all exercises

            StringBuilder minorText = new StringBuilder();
            minorText.append(exercise.getSets() + " x ");
            minorText.append(exercise.getReps() + " - ");
            if(exercise.getWeight() == 0) {
                minorText.append("Body Weight");
            }
            else {
                minorText.append(exercise.getWeight() + " lbs");
            }
            minorText.append(" - " + exercise.getIncrement() + " increment");


            Map<String, String> line = new HashMap<String, String>(2);
            line.put("1", exercise.getName());
            line.put("2",minorText.toString());
            data.add(line);
        }


        adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"1", "2"},
                new int[] {android.R.id.text1, android.R.id.text2 });
    }

    @Override
    public void onAddDialogYesClick(WorkoutExerciseDialogFragment fragment, WorkoutExercise exercise) {
        //insert into table, use SQL methods
        sql.addExercise(set, exercise);

        //put Map<String, String> into data and update adapter
        Map<String, String> line = new HashMap<>();
        StringBuilder minorText = new StringBuilder();
        minorText.append(exercise.getSets() + " x ");
        minorText.append(exercise.getReps() + " - ");
        if(exercise.getWeight() == 0) {
            minorText.append("Body Weight");
        }
        else {
            minorText.append(exercise.getWeight() + " lbs");
        }
        minorText.append(" - " + exercise.getIncrement() + " increment");

        line.put("1", exercise.getName());
        line.put("2",minorText.toString());
        data.add(line);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditDialogYesClick(WorkoutExerciseDialogFragment fragment, WorkoutExercise oldExercise, WorkoutExercise newExercise, int index, String setName) {
        //update db
        sql.updateExercise(setName, oldExercise, newExercise);

        HashMap<String, String> entry = (HashMap<String, String>) data.remove(index);
        StringBuilder minorText = new StringBuilder();
        minorText.append(newExercise.getSets() + " x ");
        minorText.append(newExercise.getReps() + " - ");
        if(newExercise.getWeight() == 0) {
            minorText.append("Body Weight");
        }
        else {
            minorText.append(newExercise.getWeight() + " lbs");
        }
        minorText.append(" - " + newExercise.getIncrement() + " increment");

        entry.put("1", newExercise.getName());
        entry.put("2",minorText.toString());

        data.add(index, entry);
        adapter.notifyDataSetChanged();
    }

    /**
     *called when the edit option is chosen
     * purely just passes bundle back to the add dialog fragment
     */
    @Override
    public void onClickWorkoutExerciseEdit(WorkoutExerciseOptionDialogFragment fragment, Bundle bundle) {
        WorkoutExerciseDialogFragment frag = new WorkoutExerciseDialogFragment();
        frag.setArguments(bundle);
        frag.show(getSupportFragmentManager(), "EditWorkoutExerciseDialogFragment");
    }

    @Override
    public void onClickWorkoutExerciseDelete(WorkoutExerciseOptionDialogFragment fragment, Bundle bundle) {
        WorkoutExercise exercise = (WorkoutExercise) bundle.getSerializable("oldExercise");
        int index = bundle.getInt("i");
        String setName = bundle.getString("setName");

        //delete row from SETS_EXERCISES
        sql.deleteExercise(setName, exercise);

        //remove from data
        data.remove(index);
        adapter.notifyDataSetChanged();
    }
}
