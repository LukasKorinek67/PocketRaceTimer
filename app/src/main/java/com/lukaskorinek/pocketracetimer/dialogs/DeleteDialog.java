package com.lukaskorinek.pocketracetimer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.lukaskorinek.pocketracetimer.R;

public class DeleteDialog extends AppCompatDialogFragment {

    private DeleteDialogListener listener;
    int racerID;
    int racerNumber;
    long racerTime;

    public interface DeleteDialogListener {
        void delete(boolean areYouSure, int racerID, int racerNumber, long racerTime);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        racerID = bundle.getInt("racerID",-1);
        racerNumber = bundle.getInt("racerNumber",-1);
        racerTime = bundle.getLong("racerTime",-1);

        builder.setTitle(getResources().getString(R.string.dialog_delete_are_you_sure))
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.delete(true, racerID, racerNumber, racerTime);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement DeleteDialogListener");
        }
    }
}
