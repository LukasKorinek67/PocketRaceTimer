package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.CSVReader;
import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.RaceFetcher;
import com.lukaskorinek.pocketracetimer.adapters.StartingListAdapter;
import com.lukaskorinek.pocketracetimer.dialogs.DeleteDialog;
import com.lukaskorinek.pocketracetimer.dialogs.HelpDialog;
import com.lukaskorinek.pocketracetimer.dialogs.ImportDialog;
import com.lukaskorinek.pocketracetimer.dialogs.LoadingDialog;
import com.lukaskorinek.pocketracetimer.dialogs.SportChallengeImportDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static android.app.Activity.RESULT_OK;


public class StartingListFragment extends AppFragment implements AdapterView.OnItemSelectedListener,
        DeleteDialog.DeleteDialogListener, ImportDialog.ImportDialogListener, SportChallengeImportDialog.SportChallengeImportDialogListener {

    private StartingListFragment.StartingListFragmentListener listener;
    Chronometer chronometer;
    CountDownTimer countDownToStart;
    ListView starting_list;
    ArrayList<RacerModel> starting_list_racers;
    TextView infoText;
    Button button_add_racer;
    Button button_import;
    Spinner filter;

    LoadingDialog loadingDialog;
    ImportDialog importDialog;
    SportChallengeImportDialog raceIdDialog;

    public interface StartingListFragmentListener {
        boolean isRaceRunning();
        boolean isRaceEnded();
        void raceHasStarted(boolean yes);
        Calendar getRaceStartTime();
        Calendar getRaceEndedTime();
        ArrayList<RacerModel> getStartingList();
        ArrayList<String> getRaceCategories();
        ArrayList<RacerModel> getRacersFromStartingListByCategory(String category);
        RacerModel getRacerFromStartingListByNumber(int number);
        void deleteRacerFromStartingList(RacerModel racer);
        void addRacerToStartingList(RacerModel racer);
        void addCategory(String category);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_starting_list, container, false);
        chronometer = root.findViewById(R.id.chronometer);
        starting_list = root.findViewById(R.id.all_racers_list);
        infoText = root.findViewById(R.id.info_text);
        button_add_racer = root.findViewById(R.id.button_add_racer);
        button_add_racer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRacerToStartingList();
            }
        });
        button_import = root.findViewById(R.id.button_import_starting_list);
        button_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importStartingList();
            }
        });
        filter = root.findViewById(R.id.filter_starting_list);
        handleRaceStatus();
        setListClick();
        registerForContextMenu(starting_list);

        return root;
    }

    private void importStartingList() {
        importDialog = new ImportDialog();
        importDialog.setTargetFragment(StartingListFragment.this, 1);
        importDialog.show(getActivity().getSupportFragmentManager(), "import dialog");
    }


    @Override
    public void showCsvHelp() {
        HelpDialog helpDialog = new HelpDialog(getContext().getResources().getString(R.string.help_csv_import));
        helpDialog.setTargetFragment(StartingListFragment.this, 1);
        helpDialog.show(getActivity().getSupportFragmentManager(), "help dialog");
    }

    @Override
    public void showSportChallengeHelp() {
        HelpDialog helpDialog = new HelpDialog(getContext().getResources().getString(R.string.help_sportchallenge_import));
        helpDialog.setTargetFragment(StartingListFragment.this, 1);
        helpDialog.show(getActivity().getSupportFragmentManager(), "help dialog");
    }

    @Override
    public void importFromCsv() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        startActivityForResult(intent, 10);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode) {
            case 10:
                if(resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        BufferedReader buff_reader = new BufferedReader(new InputStreamReader(this.getActivity().getContentResolver().openInputStream(uri)));
                        CSVReader reader = new CSVReader(getContext(), buff_reader);
                        ArrayList<RacerModel> startingList = reader.getStartingList();
                        ArrayList<String> categories = reader.getCategories();
                        addStartingListAndCategories(startingList, categories);
                        handleRaceStatus();
                        importDialog.dismiss();
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.starting_list_imported), Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        importDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        importDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void addStartingListAndCategories(ArrayList<RacerModel> startingList, ArrayList<String> categories) {
        for (int i = 0; i < startingList.size(); i++) {
            listener.addRacerToStartingList(startingList.get(i));
        }
        ArrayList<String> race_categories = listener.getRaceCategories();
        for (int j = 0; j < categories.size(); j++) {
            if(!race_categories.contains(categories.get(j))) {
                listener.addCategory(categories.get(j));
            }
        }
    }

    @Override
    public void importFromSportChallenge() {
        raceIdDialog = new SportChallengeImportDialog();
        raceIdDialog.setTargetFragment(StartingListFragment.this, 1);
        raceIdDialog.show(getActivity().getSupportFragmentManager(), "race id dialog");
    }

    @Override
    public void loadStartingList(String raceID) {
        try {
            Integer.parseInt(raceID);
            try {
                startLoadingScreen();
                Boolean validRace = new BackgroundFetch().execute(raceID).get();
                if(!validRace) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
            }
        } catch(NumberFormatException e) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
        }
    }

    private void loadCategories() {
        ArrayList<String> categories = listener.getRaceCategories();
        Collections.sort(categories);
        String all_categories = getResources().getString(R.string.all_categories);
        categories.add(0, all_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, categories);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = adapterView.getItemAtPosition(i).toString();
        if(category.equalsIgnoreCase(getResources().getString(R.string.all_categories))) {
            this.starting_list_racers = listener.getStartingList();
        } else {
            this.starting_list_racers = listener.getRacersFromStartingListByCategory(category);
        }
        setStartingList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void addRacerToStartingList() {
        AddRacerFragment fragmentAdd = new AddRacerFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentAdd, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setListClick() {
        starting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RacerModel clickedRacer = starting_list_racers.get(i);

                Bundle bundle = new Bundle();
                bundle.putInt("id", clickedRacer.getId());
                bundle.putString("name", clickedRacer.getName());
                bundle.putString("surname", clickedRacer.getSurname());
                bundle.putString("gender", clickedRacer.getGender());
                bundle.putString("date_born", clickedRacer.getDate_born());
                bundle.putString("category", clickedRacer.getCategory());
                bundle.putString("team", clickedRacer.getTeam());
                bundle.putLong("start_time", clickedRacer.getStartTime());
                bundle.putLong("end_time", clickedRacer.getEndTime());
                bundle.putLong("time", clickedRacer.getTimeInSeconds());
                bundle.putInt("number", clickedRacer.getNumber());
                bundle.putBoolean("in_starting_list", clickedRacer.isInStartingList());

                DisplayRacerFragment fragmentDisplay = new DisplayRacerFragment();
                fragmentDisplay.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentDisplay, "tag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void setStartingList() {
        if(this.starting_list_racers != null && (this.starting_list_racers.size() != 0)) {
            Collections.sort(this.starting_list_racers);
            StartingListAdapter adapter = new StartingListAdapter(this.getActivity(), R.layout.adapter_starting_list, this.starting_list_racers);
            starting_list.setAdapter(adapter);
            starting_list.setVisibility(View.VISIBLE);
            infoText.setVisibility(View.GONE);
        } else if(this.starting_list_racers == null) {
            infoText.setVisibility(View.VISIBLE);
            infoText.setText(getActivity().getResources().getString(R.string.info_starting_list_here));
            starting_list.setVisibility(View.GONE);
        } else if (this.starting_list_racers.size() == 0) {
            infoText.setVisibility(View.VISIBLE);
            infoText.setText(getActivity().getResources().getString(R.string.info_starting_list_empty));
            starting_list.setVisibility(View.GONE);
        }
    }

    private void loadStartingList() {
        this.starting_list_racers = listener.getStartingList();
        setStartingList();
    }

    @Override
    void handleRaceStatus() {
        if(listener.isRaceRunning()) {
            setChronometer();
        } else {
            if(listener.isRaceEnded()){
                Calendar raceStartTime = listener.getRaceStartTime();
                Calendar endTime = listener.getRaceEndedTime();
                chronometer.setBase(SystemClock.elapsedRealtime() - (endTime.getTimeInMillis() - raceStartTime.getTimeInMillis()));
            } else {
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                checkNearStart();
            }
        }
        loadCategories();
        loadStartingList();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.list_long_click_menu, menu);
    }

    private void deleteRacer(RacerModel racer) {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setTargetFragment(StartingListFragment.this, 1);
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
            RacerModel racer = listener.getRacerFromStartingListByNumber(racerNumber);
            listener.deleteRacerFromStartingList(racer);
            String category = filter.getSelectedItem().toString();
            if(category.equalsIgnoreCase(getResources().getString(R.string.all_categories))) {
                this.starting_list_racers = listener.getStartingList();
            } else {
                this.starting_list_racers = listener.getRacersFromStartingListByCategory(category);
            }
            setStartingList();
            if(!(listener.getRaceCategories().contains(racer.getCategory()))) {
                loadCategories();
            }
        }
    }

    private void editRacer(RacerModel racer) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", racer.getId());
        bundle.putString("name", racer.getName());
        bundle.putString("surname", racer.getSurname());
        bundle.putString("gender", racer.getGender());
        bundle.putString("date_born", racer.getDate_born());
        bundle.putString("category", racer.getCategory());
        bundle.putString("team", racer.getTeam());
        bundle.putLong("start_time", racer.getStartTime());
        bundle.putLong("end_time", racer.getEndTime());
        bundle.putLong("time", racer.getTimeInSeconds());
        bundle.putInt("number", racer.getNumber());
        bundle.putBoolean("in_starting_list", racer.isInStartingList());

        EditRacerFragment fragmentEdit = new EditRacerFragment();
        fragmentEdit.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentEdit, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;
        RacerModel racer = starting_list_racers.get(listPosition);

        switch(item.getItemId()) {
            case R.id.delete_option:
                deleteRacer(racer);
                return true;
            case R.id.edit_option:
                editRacer(racer);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setChronometer() {
        Calendar raceStartTime = listener.getRaceStartTime();
        Calendar now = Calendar.getInstance();
        chronometer.setBase(SystemClock.elapsedRealtime() - (now.getTimeInMillis() - raceStartTime.getTimeInMillis()));
        chronometer.start();
    }

    private void checkNearStart() {
        long start = listener.getRaceStartTime().getTimeInMillis();
        if (start != 0) {
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(start);
            Calendar now = Calendar.getInstance();

            if((startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                    (startTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) &&
                    (startTime.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY)) &&
                    ((startTime.get(Calendar.MINUTE) - now.get(Calendar.MINUTE)) < 3)) {
                startCountDown(start - now.getTimeInMillis());
            } else if((startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                    (startTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) &&
                    (startTime.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) + 1) &&
                    (startTime.get(Calendar.MINUTE) < 2 && now.get(Calendar.MINUTE) > 58)) {
                startCountDown(start - now.getTimeInMillis());
            } else if((startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
                    (startTime.get(Calendar.DAY_OF_YEAR) == (now.get(Calendar.DAY_OF_YEAR) + 1)) &&
                    (startTime.get(Calendar.HOUR_OF_DAY) == 0 && now.get(Calendar.HOUR_OF_DAY) == 11) &&
                    (startTime.get(Calendar.MINUTE) < 2 && now.get(Calendar.MINUTE) > 57)) {
                startCountDown(start - now.getTimeInMillis());
            } else if((startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) + 1) &&
                    (startTime.get(Calendar.DAY_OF_YEAR) == 1 && ((now.get(Calendar.DAY_OF_MONTH) == 31) && (now.get(Calendar.MONTH) == 11))) &&
                    (startTime.get(Calendar.HOUR_OF_DAY) == 0 && now.get(Calendar.HOUR_OF_DAY) == 11) &&
                    (startTime.get(Calendar.MINUTE) < 2 && now.get(Calendar.MINUTE) > 57)) {
                startCountDown(start - now.getTimeInMillis());
            }
        }
    }

    private void startCountDown(long countdownTime) {
        countDownToStart = new CountDownTimer(countdownTime, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // spustit čas závodu
                if(listener != null) {
                    listener.raceHasStarted(true);
                    handleRaceStatus();
                }
            }
        };
        countDownToStart.start();
    }

    @Override
    void raceEnded() {
        chronometer.stop();
    }

    private void startLoadingScreen() {
        this.loadingDialog = new LoadingDialog(this.getActivity());
        this.loadingDialog.startLoadingAnimation();
    }

    private class BackgroundFetch extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            return fetchRace(strings[0]);
        }

        private Boolean fetchRace(String packetId) {
            RaceFetcher fetch = new RaceFetcher(packetId);
            Calendar startTime = fetch.getStartingTime();
            if(startTime != null) {
                setLoadedRace(fetch);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            loadingDialog.dismissLoadingAnimation();
            handleRaceStatus();
            importDialog.dismiss();
        }
    }

    private void setLoadedRace(RaceFetcher fetch) {
        ArrayList<RacerModel> startingList = fetch.getStartingList();
        ArrayList<String> categories = fetch.getCategories();
        addStartingListAndCategories(startingList, categories);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof StartingListFragment.StartingListFragmentListener) {
            listener = (StartingListFragment.StartingListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement StartingListFragmentListener");
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
