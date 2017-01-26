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

public class WorkoutDayDialogFragment extends DialogFragment {

    private WorkoutDayListener listener;
    private final String LOG_TAG = this.getClass().getSimpleName();

    public interface WorkoutDayListener {
        void onAddDialogYesClick(WorkoutDayDialogFragment fragment, WorkoutDay day);
        void onEditDialogYesClick(WorkoutDayDialogFragment fragment, WorkoutDay day, String oldName, int index);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        final String type = bundle.getString("Type");


        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_workout_day, null);
        String title = "";
        if(type.equals("Add")) {
            title = "Add Workout Day Name";
        }
        else if (type.equals("Edit")) {
            title = "Edit Workout Day Name";
            ((EditText) view.findViewById(R.id.workout_day_new_edit_text)).setText(bundle.getString("OldName"));
        }


        builder.setView(view)
                .setTitle(title)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        EditText editText = ((EditText)view.findViewById(R.id.workout_day_new_edit_text));
                        WorkoutDay day = new WorkoutDay(editText.getText().toString());

                        if(type.equals("Add")) {
                            //add new Workout Set object
                            listener.onAddDialogYesClick(WorkoutDayDialogFragment.this, day);
                        }
                        else if(type.equals("Edit")) {
                            listener.onEditDialogYesClick(WorkoutDayDialogFragment.this, day, bundle.getString("OldName"), bundle.getInt("i"));
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
        if (context instanceof WorkoutDayListener) {
            listener = (WorkoutDayListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement WorkoutDayListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
