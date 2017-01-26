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
public class WorkoutExerciseOptionDialogFragment extends DialogFragment {

    private WorkoutExerciseOptionListener listener;

    public interface WorkoutExerciseOptionListener {
        void onClickWorkoutExerciseEdit(WorkoutExerciseOptionDialogFragment fragment, Bundle bundle);  //launches WorkoutExerciseDialogFragment
        void onClickWorkoutExerciseDelete(WorkoutExerciseOptionDialogFragment fragment, Bundle bundle); //deletes from Exercises and Exercises_sets tables
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] options = new String[]{"Edit Exercise", "Delete Exercise"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an option...")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0) {
                            listener.onClickWorkoutExerciseEdit(WorkoutExerciseOptionDialogFragment.this, getArguments());
                        }
                        else if (i == 1) {
                            listener.onClickWorkoutExerciseDelete(WorkoutExerciseOptionDialogFragment.this, getArguments());
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutExerciseOptionListener) {
            listener = (WorkoutExerciseOptionListener) context;
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
