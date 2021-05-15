package com.lukaskorinek.pocketracetimer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.lukaskorinek.pocketracetimer.R;

public class EditDialog extends AppCompatDialogFragment {

    private EditDialogListener listener;
    private EditText newRacerNumber;
    int racerID;
    int racerNumber;
    long racerTime;

    public interface EditDialogListener {
        void edit(int racerID, int racerNumber, long racerTime);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit, null);

        Bundle bundle = getArguments();
        racerID = bundle.getInt("racerID",-1);
        racerNumber = bundle.getInt("racerNumber",-1);
        racerTime = bundle.getLong("racerTime",-1);
        newRacerNumber = view.findViewById(R.id.newRacerNumber);

        builder.setView(view)
                .setTitle(getResources().getString(R.string.dialog_edit_racer_number))
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            int racerNumber = Integer.parseInt(newRacerNumber.getText().toString());
                            listener.edit(racerID, racerNumber, racerTime);
                        } catch (NumberFormatException e) {
                            // do nothing
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EditDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement EditDialogListener");
        }
    }
}
