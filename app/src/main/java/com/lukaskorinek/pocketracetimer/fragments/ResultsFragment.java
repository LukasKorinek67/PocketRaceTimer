package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.CSVGenerator;
import com.lukaskorinek.pocketracetimer.HTMLGenerator;
import com.lukaskorinek.pocketracetimer.PDFGenerator;
import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.adapters.LoadRaceResultsAdapter;
import com.lukaskorinek.pocketracetimer.adapters.OwnRaceResultsAdapter;
import com.lukaskorinek.pocketracetimer.dialogs.DeleteDialog;
import com.lukaskorinek.pocketracetimer.dialogs.EditDialog;
import com.lukaskorinek.pocketracetimer.dialogs.ExportDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ResultsFragment extends AppFragment implements AdapterView.OnItemSelectedListener,
        DeleteDialog.DeleteDialogListener, EditDialog.EditDialogListener, ExportDialog.ExportDialogListener {

    private ResultsFragment.ResultsFragmentListener listener;
    Chronometer chronometer;
    Spinner filter;
    ListView results_list;
    TextView info_text;
    ArrayList<RacerModel> racers;
    ArrayAdapter arrayAdapter;
    Button buttonExport;

    ExportDialog exportDialog;

    public interface ResultsFragmentListener {
        Calendar getRaceStartTime();
        Calendar getRaceEndedTime();
        ArrayList<String> getRaceCategories();
        ArrayList<RacerModel> getAllRacers();
        ArrayList<RacerModel> getRacersFromStartingListByCategory(String category);
        ArrayList<RacerModel> getStartingList();
        void deleteFinishedRacer(RacerModel racer);
        void editFinishedRacer(RacerModel racer);
        boolean isRaceEnded();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_results, container, false);
        chronometer = root.findViewById(R.id.chronometer);
        filter = root.findViewById(R.id.filter_results);
        results_list = root.findViewById(R.id.results_list);
        info_text = root.findViewById(R.id.info_text_results);
        buttonExport = root.findViewById(R.id.button_export_results);
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportResults();
            }
        });

        setChronometer();
        loadCategories();
        fillList();
        registerForContextMenu(results_list);
        return root;
    }

    private void exportResults() {
        exportDialog = new ExportDialog();
        exportDialog.setTargetFragment(ResultsFragment.this, 1);
        exportDialog.show(getActivity().getSupportFragmentManager(), "export dialog");
    }

    @Override
    public void exportToCsv() {
        CSVGenerator generator = new CSVGenerator(getContext());
        String data = generator.getCsvData(racers, listener.getRaceCategories(), listener.getStartingList().size());
        try{
            //saving the file into device
            FileOutputStream out = getActivity().openFileOutput((getContext().getResources().getString(R.string.export_default_file_name) + ".csv"), Context.MODE_PRIVATE);
            out.write((data).getBytes());
            out.close();

            //exporting
            Context context = getActivity().getApplicationContext();
            File filelocation = new File(getActivity().getFilesDir(), (getContext().getResources().getString(R.string.export_default_file_name) + ".csv"));
            Uri path = FileProvider.getUriForFile(context, getContext().getResources().getString(R.string.app_file_provider), filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.export_default_file_name_extra_subject));
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
            exportDialog.dismiss();
            exportDialog = null;
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            exportDialog.dismiss();
            exportDialog = null;
        }
    }

    @Override
    public void exportToPdf() {
        PDFGenerator generator = new PDFGenerator(getContext());
        PdfDocument pdf = generator.getPDF(racers, listener.getRaceCategories(), listener.getStartingList().size());
        try {
            //saving the file into device
            FileOutputStream out = getActivity().openFileOutput((getContext().getResources().getString(R.string.export_default_file_name) + ".pdf"), Context.MODE_PRIVATE);
            pdf.writeTo(out);
            pdf.close();

            //exporting
            Context context = getActivity().getApplicationContext();
            File filelocation = new File(getActivity().getFilesDir(), (getContext().getResources().getString(R.string.export_default_file_name) + ".pdf"));
            Uri path = FileProvider.getUriForFile(context, getContext().getResources().getString(R.string.app_file_provider), filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("application/pdf");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.export_default_file_name_extra_subject));
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
            exportDialog.dismiss();
            exportDialog = null;
        } catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            exportDialog.dismiss();
            exportDialog = null;
        }
    }

    @Override
    public void exportToHtml() {
        HTMLGenerator generator = new HTMLGenerator(getContext());
        String data = generator.getHtmlData(racers, listener.getRaceCategories(), listener.getStartingList().size());
        try{
            //saving the file into device
            FileOutputStream out = getActivity().openFileOutput((getContext().getResources().getString(R.string.export_default_file_name) + ".html"), Context.MODE_PRIVATE);
            out.write((data).getBytes());
            out.close();

            //exporting
            Context context = getActivity().getApplicationContext();
            File filelocation = new File(getActivity().getFilesDir(), (getContext().getResources().getString(R.string.export_default_file_name) + ".html"));
            Uri path = FileProvider.getUriForFile(context, getContext().getResources().getString(R.string.app_file_provider), filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/html");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.export_default_file_name_extra_subject));
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
            exportDialog.dismiss();
            exportDialog = null;
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            exportDialog.dismiss();
            exportDialog = null;
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.list_long_click_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;
        RacerModel racer = racers.get(listPosition);
        if(racer.getTimeInSeconds() == 0) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.racer_not_finished_cant_edit_or_delete), Toast.LENGTH_LONG).show();
            return super.onContextItemSelected(item);
        } else {
            switch (item.getItemId()) {
                case R.id.delete_option:
                    deleteFinishedRacer(racer);
                    return true;
                case R.id.edit_option:
                    editFinishedRacer(racer);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
    }

    private void deleteFinishedRacer(RacerModel racer) {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setTargetFragment(ResultsFragment.this, 1);
        Bundle bundle = new Bundle();
        bundle.putInt("racerID", racer.getId());
        bundle.putInt("racerNumber", racer.getNumber());
        bundle.putLong("racerTime", racer.getTimeInSeconds());
        deleteDialog.setArguments(bundle);
        deleteDialog.show(getActivity().getSupportFragmentManager(), "delete dialog");
    }

    @Override
    public void delete(boolean areYouSure, int racerID, int racerNumber, long racerTime) {
        if(areYouSure) {
            listener.deleteFinishedRacer(new RacerModel(racerID, racerNumber, racerTime));
            updateArrayList();
            updateList();
        }
    }

    private void editFinishedRacer(RacerModel racer) {
        EditDialog editDialog = new EditDialog();
        editDialog.setTargetFragment(ResultsFragment.this, 1);
        Bundle bundle = new Bundle();
        bundle.putInt("racerID", racer.getId());
        bundle.putInt("racerNumber", racer.getNumber());
        bundle.putLong("racerTime", racer.getTimeInSeconds());
        editDialog.setArguments(bundle);
        editDialog.show(getActivity().getSupportFragmentManager(), "edit dialog");
    }

    @Override
    public void edit(int racerID, int racerNumber, long racerTime) {
        listener.editFinishedRacer(new RacerModel(racerID, racerNumber, racerTime));
        updateArrayList();
        updateList();
    }

    private void updateArrayList() {
        Object filterCategory = filter.getSelectedItem();
        String category;
        if(filterCategory == null) {
            category = getContext().getResources().getString(R.string.all_categories);
        } else {
            category = filterCategory.toString();
        }
        if(category.equalsIgnoreCase(getResources().getString(R.string.all_categories))) {
            this.racers = listener.getAllRacers();
        } else {
            this.racers = listener.getRacersFromStartingListByCategory(category);
        }
    }


    private void setChronometer() {
        Calendar raceStartTime = listener.getRaceStartTime();
        Calendar endTime = listener.getRaceEndedTime();
        chronometer.setBase(SystemClock.elapsedRealtime() - (endTime.getTimeInMillis() - raceStartTime.getTimeInMillis()));
    }

    private void setAdapter() {
        if (listener.getStartingList().size() == 0) {
            arrayAdapter = new OwnRaceResultsAdapter(this.getActivity(), R.layout.adapter_own_race_results, racers);
        } else {
            arrayAdapter = new LoadRaceResultsAdapter(getActivity(), R.layout.adapter_load_race_results, racers);
        }
        results_list.setAdapter(arrayAdapter);
    }

    private void loadCategories() {
        ArrayList<String> categories = listener.getRaceCategories();
        if(categories.size() != 0) {
            Collections.sort(categories);
            String all_categories = getResources().getString(R.string.all_categories);
            categories.add(0, all_categories);
            ArrayAdapter<String> adapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, categories);
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
            filter.setAdapter(adapter);
            filter.setOnItemSelectedListener(this);
            filter.setVisibility(View.VISIBLE);
            //buttonExport.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            filter.setVisibility(View.GONE);
            //buttonExport.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void fillList() {
        this.racers = listener.getAllRacers();
        updateList();
    }

    private void updateList() {
        if(this.racers != null && (this.racers.size() != 0)) {
            Collections.sort(this.racers, RacerModel.RaceTimeComparatorAscUnfinishedLast);
            setAdapter();
            info_text.setVisibility(View.GONE);
            results_list.setVisibility(View.VISIBLE);
        } else {
            info_text.setVisibility(View.VISIBLE);
            results_list.setVisibility(View.GONE);
            filter.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = adapterView.getItemAtPosition(i).toString();
        if(category.equalsIgnoreCase(getResources().getString(R.string.all_categories))) {
            this.racers = listener.getAllRacers();
        } else {
            this.racers = listener.getRacersFromStartingListByCategory(category);
        }
        updateList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    void handleRaceStatus() {
        if(!listener.isRaceEnded()) {
            RacersFragment fragmentRacers = new RacersFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragmentRacers, "tag");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            setChronometer();
            loadCategories();
            fillList();
        }
    }

    @Override
    void raceEnded() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof ResultsFragment.ResultsFragmentListener) {
            listener = (ResultsFragment.ResultsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ResultsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        handleRaceStatus();
    }
}
