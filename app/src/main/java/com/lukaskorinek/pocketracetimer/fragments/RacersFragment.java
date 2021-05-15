package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
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
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.adapters.LoadRaceFinishAdapter;
import com.lukaskorinek.pocketracetimer.adapters.OwnRaceFinishAdapter;
import com.lukaskorinek.pocketracetimer.dialogs.DeleteDialog;
import com.lukaskorinek.pocketracetimer.dialogs.EditDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class RacersFragment extends AppFragment implements DeleteDialog.DeleteDialogListener, EditDialog.EditDialogListener {

    private RacersFragment.RacersFragmentListener listener;
    Chronometer chronometer;
    ListView all_finished_racers_list;
    ArrayAdapter arrayAdapter;
    TextView info_text;
    TextView text_number_of_racers;
    ArrayList<RacerModel> all_finished_racers;
    CountDownTimer countDownToStart;

    public interface RacersFragmentListener {
        boolean isRaceRunning();
        boolean isRaceEnded();
        void raceHasStarted(boolean yes);
        Calendar getRaceStartTime();
        Calendar getRaceEndedTime();
        ArrayList<RacerModel> getAllFinishedRacers();
        void deleteFinishedRacer(RacerModel racer);
        void editFinishedRacer(RacerModel racer);
        ArrayList<RacerModel> getStartingList();
        boolean isRaceTimeTrial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_racers, container, false);
        chronometer = root.findViewById(R.id.chronometer);
        all_finished_racers_list = root.findViewById(R.id.all_racers_list);
        info_text = root.findViewById(R.id.info_text);
        text_number_of_racers = root.findViewById(R.id.info_text_number_of_racers);
        all_finished_racers = new ArrayList<>();
        setAdapter();
        updateList();
        handleRaceStatus();
        registerForContextMenu(all_finished_racers_list);

        return root;
    }

    private void setAdapter() {
        if (listener.getStartingList().size() == 0) {
            arrayAdapter = new OwnRaceFinishAdapter(this.getActivity(), R.layout.adapter_own_race_finish, all_finished_racers);
        } else {
            arrayAdapter = new LoadRaceFinishAdapter(getActivity(), R.layout.adapter_load_race_finish, all_finished_racers);
        }
        all_finished_racers_list.setAdapter(arrayAdapter);
    }


    @Override
    void raceEnded() {
        chronometer.stop();
        ResultsFragment fragmentresults = new ResultsFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentresults, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
        RacerModel racer = all_finished_racers.get(listPosition);

        switch(item.getItemId()) {
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

    private void deleteFinishedRacer(RacerModel racer) {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setTargetFragment(RacersFragment.this, 1);
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
            updateList();
        }
    }

    private void editFinishedRacer(RacerModel racer) {
        EditDialog editDialog = new EditDialog();
        editDialog.setTargetFragment(RacersFragment.this, 1);
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
        updateList();
    }

    private void updateList() {
        fillList();
        text_number_of_racers.setText(getContext().getResources().getString(R.string.number_of_finished_racers) + " " + Integer.toString(all_finished_racers.size()));
        if(all_finished_racers.size() != 0){
            info_text.setVisibility(View.GONE);
            text_number_of_racers.setVisibility(View.VISIBLE);
            all_finished_racers_list.setVisibility(View.VISIBLE);
        } else {
            info_text.setVisibility(View.VISIBLE);
            text_number_of_racers.setVisibility(View.GONE);
            all_finished_racers_list.setVisibility(View.GONE);
        }
        arrayAdapter.notifyDataSetChanged();
        all_finished_racers_list.setAdapter(arrayAdapter);
    }

    private void fillList() {
        ArrayList<RacerModel> get_all_finished_racers = listener.getAllFinishedRacers();
        all_finished_racers.clear();

        for (int i = 0; i < get_all_finished_racers.size(); i++) {
            all_finished_racers.add(get_all_finished_racers.get(get_all_finished_racers.size() - 1 - i));
        }
        Collections.sort(all_finished_racers, RacerModel.RaceTimeComparatorDesc);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof RacersFragment.RacersFragmentListener) {
            listener = (RacersFragment.RacersFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RacersFragmentListener");
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

    @Override
    void handleRaceStatus() {
        if(listener.isRaceRunning()) {
            setChronometer();
            if(all_finished_racers.size() == 0) {
                info_text.setVisibility(View.VISIBLE);
                text_number_of_racers.setVisibility(View.GONE);
                all_finished_racers_list.setVisibility(View.GONE);
            } else {
                info_text.setVisibility(View.GONE);
                text_number_of_racers.setVisibility(View.VISIBLE);
                all_finished_racers_list.setVisibility(View.VISIBLE);
            }
        } else {
            if(listener.isRaceEnded()){
                if(all_finished_racers.size() == 0) {
                    info_text.setVisibility(View.VISIBLE);
                    text_number_of_racers.setVisibility(View.GONE);
                    all_finished_racers_list.setVisibility(View.GONE);
                } else {
                    info_text.setVisibility(View.GONE);
                    text_number_of_racers.setVisibility(View.VISIBLE);
                    all_finished_racers_list.setVisibility(View.VISIBLE);
                }
                Calendar raceStartTime = listener.getRaceStartTime();
                Calendar endTime = listener.getRaceEndedTime();
                chronometer.setBase(SystemClock.elapsedRealtime() - (endTime.getTimeInMillis() - raceStartTime.getTimeInMillis()));
            } else {
                updateList();
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                checkNearStart();
            }
        }
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

    private void setChronometer() {
        Calendar raceStartTime = listener.getRaceStartTime();
        Calendar now = Calendar.getInstance();
        chronometer.setBase(SystemClock.elapsedRealtime() - (now.getTimeInMillis() - raceStartTime.getTimeInMillis()));
        chronometer.start();
    }
}

