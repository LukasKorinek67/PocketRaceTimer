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

public class ExportDialog extends AppCompatDialogFragment {

    private ExportDialogListener listener;
    Button buttonCsv;
    Button buttonPdf;
    Button buttonHtml;

    public interface ExportDialogListener {
        void exportToCsv();
        void exportToPdf();
        void exportToHtml();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_export, null);
        buttonCsv = view.findViewById(R.id.button_export_csv);
        buttonPdf = view.findViewById(R.id.button_export_pdf);
        buttonHtml = view.findViewById(R.id.button_export_html);
        buttonCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.exportToCsv();
            }
        });
        buttonPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.exportToPdf();
            }
        });
        buttonHtml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.exportToHtml();
            }
        });

        builder.setView(view).setTitle(getResources().getString(R.string.export_into))
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
            listener = (ExportDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement ExportDialogListener");
        }
    }
}
