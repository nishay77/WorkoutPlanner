package com.nishay.workoutplanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.nishay.CustomGridAdapter;
import com.nishay.sql.SQL;

import java.util.ArrayList;

public class WorkoutSetActivity extends AppCompatActivity implements WorkoutSetDialogFragment.WorkoutSetListener, WorkoutSetOptionDialogFragment.WorkoutSetOptionListener {

    private SQL sql;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private CustomGridAdapter adapter;
    private ArrayList<WorkoutSet> list;
    private String day;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_set);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.workout_set));
        setSupportActionBar(toolbar);

        sql = new SQL(this);
        sql.open();

        bundle = getIntent().getExtras();
        day = bundle.getString("Day");

        list = sql.getExercises(day);
        GridView gridView = (GridView) findViewById(R.id.workout_set_grid_view);
        adapter = new CustomGridAdapter(this, list);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WorkoutExerciseActivity.class);
                bundle.putString("SetName", ((TextView)view.findViewById(R.id.grid_item_text_1)).getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                WorkoutSetOptionDialogFragment fragment = new WorkoutSetOptionDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Edit");
                b.putString("OldName", ((TextView)(view.findViewById(R.id.grid_item_text_1))).getText().toString());
                b.putInt("i", i);
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutSetOptionDialogFragment");

                return true;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.workout_set_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkoutSetDialogFragment fragment = new WorkoutSetDialogFragment();
                Bundle b = new Bundle();
                b.putString("Type", "Add");
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "WorkoutSetDialogFragment");
            }
        });

        FloatingActionButton fabConfirm = (FloatingActionButton) findViewById(R.id.workout_set_confirm_fab);
        fabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get all checked, update day in days_sets table
                ArrayList<String> checkedList = adapter.getAllChecked();
                sql.updateSetListForDay(day, checkedList);
                //then go up(?), should recreate
                onNavigateUp();
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

    @Override
    public void onAddDialogYesClick(WorkoutSetDialogFragment fragment, String set) {
        if(set.trim().equals("") || sql.setExists(set.trim())) {
            //name already exists or empty
            return;
        }
        sql.addSet(set);
        WorkoutSet exercise = new WorkoutSet();
        exercise.setSet(set);
        exercise.setChecked(true);
        list.add(exercise);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onEditDialogYesClick(WorkoutSetDialogFragment fragment, String newName, String oldName, int index) {
        sql.updateSetName(oldName, newName);
        int i = 0;
        for(; i < list.size(); i++) {
            WorkoutSet e = list.get(i);
            if(e.getSetName().equals(oldName)) {
                break;
            }
        }

        WorkoutSet e = list.remove(i);
        e.setSet(newName);
        list.add(i, e);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickWorkoutSetEdit(WorkoutSetOptionDialogFragment fragment, Bundle bundle) {
        WorkoutSetDialogFragment frag = new WorkoutSetDialogFragment();
        frag.setArguments(bundle);
        frag.show(getSupportFragmentManager(), "EditWorkoutSetDialogFragment");

    }

    @Override
    public void onClickWorkoutSetDelete(WorkoutSetOptionDialogFragment fragment, String name, int index) {
        //delete from adapter
        list.remove(index);
        adapter.notifyDataSetChanged();

        //deleted from sql
        sql.deleteSetName(name);
    }
}
