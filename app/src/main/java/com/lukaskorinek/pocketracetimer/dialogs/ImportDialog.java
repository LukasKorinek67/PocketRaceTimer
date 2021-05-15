package com.lukaskorinek.pocketracetimer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.lukaskorinek.pocketracetimer.R;

public class ImportDialog extends AppCompatDialogFragment {

    private ImportDialogListener listener;
    Button buttonCsv;
    Button buttonSportChallenge;
    Button buttonHelpCsv;
    Button buttonHelpSportChallenge;

    public interface ImportDialogListener {
        void importFromCsv();
        void importFromSportChallenge();
        void showCsvHelp();
        void showSportChallengeHelp();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_import, null);
        buttonCsv = view.findViewById(R.id.button_import_csv);
        buttonSportChallenge = view.findViewById(R.id.button_import_sportchallenge);
        buttonHelpCsv = view.findViewById(R.id.button_csv_help);
        buttonHelpSportChallenge = view.findViewById(R.id.button_sportchallenge_help);

        buttonCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.importFromCsv();
            }
        });
        buttonSportChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.importFromSportChallenge();

            }
        });
        buttonHelpCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showCsvHelp();

            }
        });
        buttonHelpSportChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showSportChallengeHelp();

            }
        });

        builder.setView(view).setTitle(getResources().getString(R.string.dialog_import_from_where))
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ImportDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement ImportDialogListener");
        }
    }
}
