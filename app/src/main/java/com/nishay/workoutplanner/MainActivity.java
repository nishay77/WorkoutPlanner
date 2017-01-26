package com.nishay.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nishay.sql.SQL;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DayOptionsDialogFragment.DayOptionsListener {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private SQL sql;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sql = new SQL(this);
        sql.open();

        firstTimeSetup();

        changeDay();

        TextView message = (TextView) findViewById(R.id.main_text_view_message);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        message.setText(preferences.getString("finishedMsg", "Press Setup to customize your workout.\nOr press play to get started."));

        TextView dayView = (TextView) findViewById(R.id.main_text_view_day);
        dayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get list of days
                ArrayList<String> days = new ArrayList<String>();
                ArrayList<WorkoutDay> listDays = sql.getDays();
                for(WorkoutDay day : listDays) {
                    days.add(day.getName());
                }

                DayOptionsDialogFragment fragment = new DayOptionsDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("days", days);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "DayOptionsDialogFragment");
            }
        });

        ImageView go = (ImageView) findViewById(R.id.main_image_view_go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GoActivity.class);
                preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String name = preferences.getString("day", sql.getDays().get(0).getName());
                intent.putExtra("day", name);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode == RESULT_OK) {
            //day finished
            TextView message = (TextView) findViewById(R.id.main_text_view_message);
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            message.setText(preferences.getString("finishedMsg", "Press Setup to customize your workout.\nOr press play to get started."));

            sql = new SQL(this);
            sql.open();
            changeDay();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.graph) {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
        }

        if(id == R.id.add_workout_day) {
            Intent addWorkoutDayIntent = new Intent(this, WorkoutDayActivity.class);
            startActivity(addWorkoutDayIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        sql.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        sql.open();
    }

    private void firstTimeSetup() {
        ArrayList<WorkoutDay> days = sql.getDays();
        ArrayList<String> sets = sql.getSets(null);
        if(days == null && sets == null) {
            Log.v(LOG_TAG, "Doing first time setup");
            //insert dummy data here
            sql.addDay("Day A");
            sql.addDay("Day B");

            sql.addSet("Legs");
            sql.addSet("Back");
            sql.addSet("Arms");
            sql.addSet("Chest");
            sql.addSet("Core");

            ArrayList<String> list = new ArrayList<>();
            list.add("Legs");
            list.add("Back");
            list.add("Core");
            sql.updateSetListForDay("Day A", list);
            list.clear();
            list.add("Arms");
            list.add("Chest");
            list.add("Core");
            sql.updateSetListForDay("Day B", list);

            sql.addSetExercise("Legs", "Leg Press", 3, 10, 45.0, 5.0);
            sql.addSetExercise("Legs", "Standing Calf Raise", 5, 10, 0.0, 2.0);

            sql.addSetExercise("Back", "Deadlift", 1, 3, 100.0, 8.0);
            sql.addSetExercise("Back", "Wide Grip Pull-Up", 3, 15, 0.0, 0.0);

            sql.addSetExercise("Arms", "Dumbbell Curls", 5, 10, 15.0, 2.0);
            sql.addSetExercise("Arms", "Chair Dips", 5, 10, 0.0, 0.0);

            sql.addSetExercise("Chest", "Bench Press", 3, 5, 75.0, 5.0);

            sql.addSetExercise("Core", "Crunches", 5, 20, 0.0, 0.0);


        }
    }

    @Override
    public void onClickDayChange(DayOptionsDialogFragment fragment, String day) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("day", day);
        editor.commit();

        changeDay();
    }

    public void changeDay() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String day = preferences.getString("day", null);
        if(day == null) {
            day = sql.getDays().get(0).getName();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("day", day);
            editor.commit();
        }
        //get all exercises for this day
        String sets;
        ArrayList<String> setsList = sql.getSets(day);
        if(setsList != null) {
            sets = ArrayListToString(setsList);
        }
        else {
            sets = "No workouts on this day.";
        }

        //setup Day indicator
        TextView dayView = (TextView) findViewById(R.id.main_text_view_day);
        dayView.setText(day);
        TextView setsView = (TextView) findViewById(R.id.main_text_view_sets);
        setsView.setText(sets);
    }

    public static String ArrayListToString(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        if(list.size() > 0) {
            sb.append(" | ");
            for (String s : list) {
                sb.append(s);
                sb.append(" | ");
            }
            return sb.toString();
        }
        else {
            return "Click to add exercises.";
        }
    }
}
