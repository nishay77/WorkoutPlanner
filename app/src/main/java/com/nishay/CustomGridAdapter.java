package com.nishay;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nishay.workoutplanner.MainActivity;
import com.nishay.workoutplanner.R;
import com.nishay.workoutplanner.WorkoutSet;

import java.util.ArrayList;

/**
 * GridAdapter for the WorkoutSetActivity
 * grid_item.xml
 */
public class CustomGridAdapter extends BaseAdapter {

    //arraylist of Map<String, ArrayList<String>>
    //                  ^setname,  ^list of exercises

    private final String LOG_TAG = this.getClass().getSimpleName();
    private ArrayList<WorkoutSet> list;
    private Context context;
    private static LayoutInflater inflater = null;
    private ArrayList<String> checkedList;

    public CustomGridAdapter(Activity activity, ArrayList<WorkoutSet> list) {
        this.context = activity;
        this.list = list;
        inflater = (LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        checkedList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public class ViewHolder {
        private TextView major;
        private TextView minor;
        private CheckBox checkbox;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;

        if(view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.grid_item, viewGroup, false);
            holder.major = (TextView) view.findViewById(R.id.grid_item_text_1);
            holder.minor = (TextView) view.findViewById(R.id.grid_item_text_2);
            holder.checkbox = (CheckBox) view.findViewById(R.id.grid_item_checkbox);

            view.setTag(holder);

        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        //view properly inflated, set text now
        final WorkoutSet exercise = list.get(i);
        holder.major.setText(exercise.getSetName());
        holder.minor.setText(MainActivity.ArrayListToString(exercise.getExercises()));
        if(exercise.isChecked()) {
            holder.checkbox.setChecked(true);
            if(!checkedList.contains(exercise.getSetName())) {
                checkedList.add(exercise.getSetName());
            }
        }


        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    checkedList.add(exercise.getSetName());
                }
                else {
                    checkedList.remove(exercise.getSetName());
                }
            }
        });

        return view;
    }

    public ArrayList<String> getAllChecked() {
        return checkedList;
    }
}
