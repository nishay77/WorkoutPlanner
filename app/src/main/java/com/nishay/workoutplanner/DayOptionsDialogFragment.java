package com.nishay.workoutplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by Nishay on 8/15/2016.
 */
public class DayOptionsDialogFragment extends DialogFragment {

    private DayOptionsListener listener;

    public interface DayOptionsListener {
        void onClickDayChange(DayOptionsDialogFragment fragment, String day);
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        ArrayList<String> days = bundle.getStringArrayList("days");
        final String[] options = days.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a day...")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onClickDayChange(DayOptionsDialogFragment.this, options[i]);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DayOptionsListener) {
            listener = (DayOptionsListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement DayOptionsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
