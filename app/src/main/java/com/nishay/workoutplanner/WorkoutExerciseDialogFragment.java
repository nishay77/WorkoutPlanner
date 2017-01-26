package com.nishay.workoutplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WorkoutExerciseDialogFragment extends DialogFragment {

    private WorkoutExerciseListener listener;
    private final String LOG_TAG = this.getClass().getSimpleName();

    public interface WorkoutExerciseListener {
        void onAddDialogYesClick(WorkoutExerciseDialogFragment fragment, WorkoutExercise exercise);
        void onEditDialogYesClick(WorkoutExerciseDialogFragment fragment, WorkoutExercise oldExercise, WorkoutExercise exercise, int index, String setName);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_workout_exercise, null);
        String title = "";
        final String type = bundle.getString("Type");
        if(type.equals("Add")) {
            title = "Add Exercise";
        }
        else if(type.equals("Edit")) {
            title = "Edit Exercise";
            WorkoutExercise oldExercise = (WorkoutExercise) getArguments().getSerializable("oldExercise");
            ((EditText) view.findViewById(R.id.workout_exercise_name_edit_text)).setText(oldExercise.getName());
            ((EditText) view.findViewById(R.id.workout_exercise_sets_edit_text)).setText(oldExercise.getSets() + "");
            ((EditText) view.findViewById(R.id.workout_exercise_reps_edit_text)).setText(oldExercise.getReps() + "");
            ((EditText) view.findViewById(R.id.workout_exercise_weight_edit_text)).setText(oldExercise.getWeight() + "");
            ((EditText) view.findViewById(R.id.workout_exercise_increment_edit_text)).setText(oldExercise.getIncrement() + "");

        }

        builder.setView(view)
            .setTitle(title)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    WorkoutExercise exercise = new WorkoutExercise();

                    exercise.setName(((EditText)view.findViewById(R.id.workout_exercise_name_edit_text)).getText().toString());

                    String numSets = ((EditText)view.findViewById(R.id.workout_exercise_sets_edit_text)).getText().toString();
                    numSets = (numSets.equals("")) ? "0" : numSets;
                    exercise.setSets(Integer.parseInt(numSets));

                    String numReps = ((EditText)view.findViewById(R.id.workout_exercise_reps_edit_text)).getText().toString();
                    numReps = (numReps.equals("")) ? "0" : numReps;
                    exercise.setReps(Integer.parseInt(numReps));

                    String numWeight = ((EditText)view.findViewById(R.id.workout_exercise_weight_edit_text)).getText().toString();
                    numWeight = (numWeight.equals("")) ? "0" : numWeight;
                    exercise.setWeight(Double.parseDouble(numWeight));

                    String numIncrement = ((EditText)view.findViewById(R.id.workout_exercise_increment_edit_text)).getText().toString();
                    numIncrement = (numIncrement.equals("")) ? "0" : numIncrement;
                    exercise.setIncrement(Double.parseDouble(numIncrement));


                    //check if each field is blank
                    if(exercise.getName().equals("")) {
                        Toast.makeText(getContext(), "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                    else if(exercise.getSets() == 0) {
                        Toast.makeText(getContext(), "Number of sets cannot be zero.", Toast.LENGTH_SHORT).show();
                    }
                    else if(exercise.getReps() == 0) {
                        Toast.makeText(getContext(), "Number of reps cannot be zero.", Toast.LENGTH_SHORT).show();
                    }
                    //weight 0 is allowed
                    else if(type.equals("Add")){
                        listener.onAddDialogYesClick(WorkoutExerciseDialogFragment.this, exercise);
                    }
                    else if(type.equals("Edit")){
                        listener.onEditDialogYesClick(WorkoutExerciseDialogFragment.this,
                                ((WorkoutExercise)getArguments().getSerializable("oldExercise")),
                                exercise,
                                getArguments().getInt("i"),
                                getArguments().getString("setName"));
                    }
                }
            });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                addButton.setText("");
                //set drawable
                Drawable drawable = getActivity().getResources().getDrawable(android.R.drawable.ic_input_add);
                drawable.setBounds((int) (drawable.getIntrinsicWidth() * 0.5),
                        0, (int) (drawable.getIntrinsicWidth() * 1.5),
                        drawable.getIntrinsicHeight());
                addButton.setCompoundDrawables(drawable, null, null, null);
            }
        });

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WorkoutExerciseListener) {
            listener = (WorkoutExerciseListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement WorkoutExerciseListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
