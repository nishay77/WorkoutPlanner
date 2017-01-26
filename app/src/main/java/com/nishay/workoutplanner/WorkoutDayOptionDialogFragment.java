package com.nishay.workoutplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Nishay on 8/2/2016.
 */
public class WorkoutDayOptionDialogFragment extends DialogFragment {

    private WorkoutDayOptionListener listener;

    public interface WorkoutDayOptionListener {
        void onClickWorkoutDayEdit(WorkoutDayOptionDialogFragment fragment, Bundle bundle);  //launches WorkoutDayDialogFragment
        void onClickWorkoutDayDelete(WorkoutDayOptionDialogFragment fragment, String name, int index); //deletes from days and days_sets tables
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] options = new String[]{"Edit Name", "Delete Day"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an option...")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0) {
                            listener.onClickWorkoutDayEdit(WorkoutDayOptionDialogFragment.this, getArguments());
                        }
                        else if (i == 1) {
                            listener.onClickWorkoutDayDelete(WorkoutDayOptionDialogFragment.this, getArguments().getString("OldName"), getArguments().getInt("i"));
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutDayOptionListener) {
            listener = (WorkoutDayOptionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement AddWorkoutSetListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
