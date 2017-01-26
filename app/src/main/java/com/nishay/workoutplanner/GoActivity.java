package com.nishay.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nishay.sql.SQL;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GoActivity extends AppCompatActivity {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private LinearLayout masterLinearLayout;
    private SQL sql;
    private ArrayList<WorkoutExercise> fullListExercises;
    private int repsDoneSoFar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sql = new SQL(this);
        sql.open();

        masterLinearLayout = (LinearLayout) findViewById(R.id.go_linear_layout);
        Intent intent = getIntent();
        String day = intent.getStringExtra("day");

        TextView header = (TextView) findViewById(R.id.go_day_text_view);
        header.setText(day);

        fullListExercises = new ArrayList<>();

        //obtain list of all sets
        ArrayList<String> sets = sql.getSets(day);
        if(sets != null && sets.size() > 0) {
            //for each set, get list of exercises
            for(String set: sets) {
                //add setname textview
                TextView view = new TextView(this);
                view.setText(set);
                view.setTextColor(getResources().getColor(android.R.color.white));
                view.setTextSize(24);
                view.setBackground(getResources().getDrawable(R.drawable.box));
                masterLinearLayout.addView(view);


                ArrayList<WorkoutExercise> exercises = sql.getExercisesList(set);
                if(exercises != null  && exercises.size() > 0) {
                    for(WorkoutExercise exercise : exercises) {
                        View listItem = createListItem(exercise, set);
                        fullListExercises.add(exercise);
                        masterLinearLayout.addView(listItem);
                    }
                }
                else {
                    View finishedView = View.inflate(this, R.layout.go_finished_item, null);
                    TextView tv = (TextView) finishedView.findViewById(R.id.go_finished_name);
                    tv.setText("No exercises in this set.");
                    masterLinearLayout.addView(finishedView);
                }
            }
        }
        else {
            View finishedView = View.inflate(this, R.layout.go_finished_item, null);
            TextView tv = (TextView) finishedView.findViewById(R.id.go_finished_name);
            tv.setText("No workouts on this day.");
            masterLinearLayout.addView(finishedView);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_go, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.go_menu_pause) {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }

        if(id == R.id.go_menu_done) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            //set to next day
            String currentDay = preferences.getString("day", null);
            String nextDay = getNextDay(currentDay);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("day", nextDay);

            String msg = calculate(fullListExercises, currentDay);
            editor.putString("finishedMsg", msg);
            editor.commit();
            setResult(RESULT_OK);
            Toast.makeText(this, "Finished " + currentDay + ". Next day is " + nextDay + ".", Toast.LENGTH_LONG).show();
            super.onBackPressed();
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

    public View createListItem(final WorkoutExercise exercise, final String setName) {
        final View item = View.inflate(this, R.layout.go_list_item, null);

        TextView maxSets = (TextView) item.findViewById(R.id.go_max_sets_count_text_view);
        TextView details = (TextView) item.findViewById(R.id.go_exercise_name);

        StringBuilder sb = new StringBuilder();
        sb.append(exercise.getName());
        sb.append("\n");
        sb.append(exercise.getReps());
        sb.append(" Reps");
        sb.append("\n");
        if(exercise.getWeight() == 0) {
            sb.append("Body Weight");
        }
        else {
            sb.append(exercise.getWeight());
            sb.append(" lbs");
        }

        details.setText(sb.toString());


        maxSets.setText("/" + exercise.getSets() + " SETS");

        ImageView button = (ImageView) item.findViewById(R.id.go_plus_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //increment global counter
                repsDoneSoFar += (exercise.getReps());

                TextView count = (TextView) item.findViewById(R.id.go_count_text_view);
                int c = Integer.parseInt(count.getText().toString());
                count.setText((c+1) + "");
                if((c+1) == exercise.getSets()) {
                    ViewGroup parent = (ViewGroup) view.getParent();
                    LinearLayout ll = (LinearLayout) parent.getParent();
                    int index = ll.indexOfChild(item);
                    ll.removeView(item);
                    insertFinishedItem(ll, index, exercise);

                    //add to weight progress table, set/exercise/weight/date
                    sql.insertWeightDate(exercise, setName);
                    //autoincrement weight here
                    sql.incrementWeight(exercise);


                }
            }
        });
        return item;
    }

    private void insertFinishedItem(LinearLayout ll, int index, WorkoutExercise exercise) {
        //add finished item
        View finishedItem = View.inflate(getApplicationContext(), R.layout.go_finished_item, null);
        TextView nameFinished = (TextView) finishedItem.findViewById(R.id.go_finished_name);
        StringBuilder sb = new StringBuilder();
        sb.append(exercise.getName());
        sb.append(" - ");
        sb.append(exercise.getSets());
        sb.append(" x ");
        sb.append(exercise.getReps());
        sb.append(" - ");
        if(exercise.getWeight() == 0.0) {
            sb.append("Body Weight");
        }
        else {
            sb.append(exercise.getWeight());
            sb.append(" lbs");
        }
        sb.append("\n(Incremented by " + exercise.getIncrement() + " lbs)");
        nameFinished.setText(sb.toString());
        nameFinished.setTextColor(Color.GRAY);
        ll.addView(finishedItem, index);
    }

    private String getNextDay(String currentDay) {
        ArrayList<WorkoutDay> workoutDays = sql.getDays();
        ArrayList<String> days = new ArrayList<>();
        for(WorkoutDay w : workoutDays) {
            days.add(w.getName());
        }

        int index = days.indexOf(currentDay);
        if(index == -1) {
            return days.get(0);
        }

        if(index == days.size()-1) {
            //this is the last item, return the first
            return days.get(0);
        }

        return days.get(index+1);
    }

    private String calculate(ArrayList<WorkoutExercise> exercises, String day) {
        int totalReps = 0;

        for(WorkoutExercise exercise : exercises) {
            totalReps += (exercise.getSets() * exercise.getReps());
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double percent = ((double)repsDoneSoFar/(double)totalReps)*100;
        String percentFormatted = df.format(percent) + "%";

        String message;
        if(totalReps == 0) {
            //this would produce NaN
            message = "No exercises to do on " + day + "?  Hit \"Setup\" to add some.";
        }
        else if (percent < 50) {
            message = "Finished " + percentFormatted + " of your workout on " + day + ".  Consider fine tuning that day.";

        }
        else if(percent < 75) {
            message = day + ": " + percentFormatted + "? Step it up, you can do better than that.";
        }
        else if(percent < 100) {
            message = "You finished " + percentFormatted + " on " + day + ". Close enough.";
        }
        else {
            message = "Congratulations, you finished " + percentFormatted + " of your workout on " + day;
        }

        //go ahead and add entry to sql table DAYS_PERCENTS here for this day
        //sql.addDayPercent(day, percent);


        return message;
    }
}
