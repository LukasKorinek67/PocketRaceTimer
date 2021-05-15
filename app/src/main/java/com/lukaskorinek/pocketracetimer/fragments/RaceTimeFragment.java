package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.adapters.LoadRaceFinishAdapter;
import com.lukaskorinek.pocketracetimer.adapters.OwnRaceFinishAdapter;
import com.lukaskorinek.pocketracetimer.dialogs.DeleteDialog;
import com.lukaskorinek.pocketracetimer.dialogs.EditDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.MillisToStringDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class RaceTimeFragment extends AppFragment implements DeleteDialog.DeleteDialogListener, EditDialog.EditDialogListener {
    private RaceTimeFragmentListener listener;
    Chronometer chronometer;
    EditText editTextRacerNumber;
    Button button_ok;
    Button button_start;
    ListView racers_list;
    ArrayList<RacerModel> last_four_racers;
    ArrayAdapter arrayAdapter;
    TextView info_text;
    TextView text_last_four;
    CountDownTimer countDownToStart;

    public interface RaceTimeFragmentListener {
        boolean isRaceRunning();
        boolean isRaceEnded();
        void startRace(Calendar startTime);
        void raceHasStarted(boolean yes);
        Calendar getRaceStartTime();
        Calendar getRaceEndedTime();
        void addRacerToList(RacerModel racer);
        ArrayList<RacerModel> getLastFourFinishedRacers();
        void deleteFinishedRacer(RacerModel racer);
        void editFinishedRacer(RacerModel racer);
        RacerModel getRacerFromStartingListByNumber(int number);
        RacerModel getRacerByNumber(int number);
        void racerHasFinished(RacerModel racer);
        ArrayList<RacerModel> getStartingList();
        boolean isRaceTimeTrial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_race_time, container, false);

        chronometer = root.findViewById(R.id.chronometer);
        editTextRacerNumber = (EditText)root.findViewById(R.id.racerNumber);
        button_ok = root.findViewById(R.id.numberButton);
        button_start = root.findViewById(R.id.startButton);
        racers_list = root.findViewById(R.id.racers_list);
        last_four_racers = new ArrayList<>();
        info_text = root.findViewById(R.id.info_text);
        text_last_four = root.findViewById(R.id.info_text_last_four);

        chronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnChronometer();
            }
        });
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRacerNumberAndTime();
            }
        });
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRace();
            }
        });

        updateList();
        handleRaceStatus();

        registerForContextMenu(racers_list);
        return root;
    }

    private void setAdapter() {
        if (listener.getStartingList().size() == 0) {
            arrayAdapter = new OwnRaceFinishAdapter(this.getActivity(), R.layout.adapter_own_race_finish, last_four_racers);
        } else {
            arrayAdapter = new LoadRaceFinishAdapter(getActivity(), R.layout.adapter_load_race_finish, last_four_racers);
        }
        racers_list.setAdapter(arrayAdapter);
    }

    @Override
    void raceEnded() {
        chronometer.stop();
        viewsSetRaceEnded();
    }

    private void deleteFinishedRacer(RacerModel racer) {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setTargetFragment(RaceTimeFragment.this, 1);
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
        editDialog.setTargetFragment(RaceTimeFragment.this, 1);
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

    @Override
    public void onResume() {
        super.onResume();
        handleRaceStatus();
    }

    @Override
    void handleRaceStatus() {
        if(listener.isRaceRunning()) {
            setChronometer();
            viewsSetRaceRunning();
        } else {
            if(listener.isRaceEnded()){
                viewsSetRaceEnded();
                Calendar raceStartTime = listener.getRaceStartTime();
                Calendar endTime = listener.getRaceEndedTime();
                chronometer.setBase(SystemClock.elapsedRealtime() - (endTime.getTimeInMillis() - raceStartTime.getTimeInMillis()));
            } else {
                updateList();
                viewSetRaceNotRunning();
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
            }
        }
    }

    private void setChronometer() {
        Calendar raceStartTime = listener.getRaceStartTime();
        Calendar now = Calendar.getInstance();
        chronometer.setBase(SystemClock.elapsedRealtime() - (now.getTimeInMillis() - raceStartTime.getTimeInMillis()));
        chronometer.start();
    }

    private void startRace() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        viewsSetRaceRunning();
        Calendar startTime = Calendar.getInstance();
        listener.startRace(startTime);
    }

    private void viewsSetRaceRunning() {
        button_start.setVisibility(View.GONE);
        button_ok.setVisibility(View.VISIBLE);
        editTextRacerNumber.setVisibility(View.VISIBLE);
        infoTextChangeRaceRunning();
        if(last_four_racers.size() != 0){
            text_last_four.setVisibility(View.VISIBLE);
        }
    }

    private void viewSetRaceNotRunning() {
        editTextRacerNumber.setVisibility(View.GONE);
        text_last_four.setVisibility(View.GONE);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        String choose_race_option = sharedPref.getString(getActivity().getResources().getString(R.string.choose_race_option_key), "Not set");
        String race_start_option = sharedPref.getString(getActivity().getResources().getString(R.string.own_race_start_option_key), "Not set");
        String load_race_start_option = sharedPref.getString(getActivity().getResources().getString(R.string.load_race_start_type_key), "Not set");

        if(choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_own_race))) {
            if (race_start_option.equalsIgnoreCase(getResources().getString(R.string.race_start_values_manual_start_button))) {
                button_ok.setVisibility(View.GONE);
                button_start.setVisibility(View.VISIBLE);

                infoTextChangeManualStart();
            } else if (race_start_option.equalsIgnoreCase(getResources().getString(R.string.race_start_values_manual_start_time))) {
                Log.i("HEREEEEE!", "HERE 2!!");
                button_ok.setVisibility(View.GONE);
                button_start.setVisibility(View.GONE);
                infoTextChangeStartTime();
            }
        } else if(choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_load_race))) {
            String race_name = sharedPref.getString(getActivity().getResources().getString(R.string.shared_pref_race_name), "Not set");
            if(race_name.equalsIgnoreCase("") || race_name.equalsIgnoreCase("Not set") || race_name.equalsIgnoreCase(getActivity().getResources().getString(R.string.not_loaded))) {
                infoTextLoadRaceToStart();
                button_ok.setVisibility(View.GONE);
                button_start.setVisibility(View.GONE);
            } else {
                if (load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_button))) {
                    button_ok.setVisibility(View.GONE);
                    button_start.setVisibility(View.VISIBLE);
                    infoTextChangeManualStart();
                } else if (load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_time))) {
                    button_ok.setVisibility(View.GONE);
                    button_start.setVisibility(View.GONE);
                    long start = listener.getRaceStartTime().getTimeInMillis();
                    if (start != 0) {
                        infoTextCheckStart(start);
                    } else {
                        infoTextChangeStartTime();
                    }
                }
            }
        } else {
            button_ok.setVisibility(View.GONE);
            button_start.setVisibility(View.GONE);
            infoTextSetStartOption();
        }
    }

    private void viewsSetRaceEnded() {
        button_start.setVisibility(View.GONE);
        button_ok.setVisibility(View.INVISIBLE);
        editTextRacerNumber.setVisibility(View.INVISIBLE);
        infoTextChangeRaceEnded();
        if(last_four_racers.size() != 0){
            text_last_four.setVisibility(View.VISIBLE);
        }
    }

    public void clickOnChronometer(){
        if(listener.isRaceRunning()) {
            long racerTime = getRacerTime();
            long endTime = racerTime;
            RacerModel racer = new RacerModel(racerTime, endTime);
            addRacer(racer);
        }
    }


    public void getRacerNumberAndTime() {
        long endTime = getRacerTime();
        try {
            int racerNumber = Integer.parseInt(editTextRacerNumber.getText().toString());
            RacerModel racer = findRacerInStartingList(racerNumber);
            if(racer == null) {
                if(listener.getRacerByNumber(racerNumber) == null) {
                    long racerTime = endTime;
                    racer = new RacerModel(racerNumber, racerTime, endTime);
                    addRacer(racer);
                    clearNumberText();
                } else {
                    Toast.makeText(getActivity(), this.getResources().getString(R.string.error_racing_number_already_finished), Toast.LENGTH_LONG).show();
                    long racerTime = endTime;
                    RacerModel emptyRacer = new RacerModel(racerTime, endTime);
                    addRacer(emptyRacer);
                    clearNumberText();
                }
            } else {
                if(racer.getTimeInSeconds() == 0) {
                    if(listener.isRaceTimeTrial()) {
                        racer.setEndTime(endTime);
                        long racerStartTime = racer.getStartTime();
                        long racerTime = endTime - racerStartTime;
                        racer.setTimeInSeconds(racerTime);
                        racerHasFinished(racer);
                        clearNumberText();

                    } else {
                        long racerTime = endTime;
                        racer.setTimeInSeconds(racerTime);
                        racer.setEndTime(endTime);
                        racerHasFinished(racer);
                        clearNumberText();
                    }
                } else {
                    Toast.makeText(getActivity(), this.getResources().getString(R.string.error_racer_already_finished), Toast.LENGTH_LONG).show();
                    long racerTime = endTime;
                    RacerModel emptyRacer = new RacerModel(racerTime, endTime);
                    addRacer(emptyRacer);
                    clearNumberText();
                }
            }
        } catch (NumberFormatException e) {
            long racerTime = endTime;
            RacerModel racer = new RacerModel(racerTime, endTime);
            addRacer(racer);
            clearNumberText();
        }
    }

    private void clearNumberText() {
        editTextRacerNumber.setText("");
    }

    private RacerModel findRacerInStartingList(int number) {
        return listener.getRacerFromStartingListByNumber(number);
    }

    private void addRacer(RacerModel racer) {
        listener.addRacerToList(racer);
        updateList();
    }

    private void racerHasFinished(RacerModel racer) {

        listener.racerHasFinished(racer);
        updateList();
    }

    private long getRacerTime() {
        long racerTime = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
        return racerTime;
    }

    private void updateList() {
        ArrayList<RacerModel> get_last_four_racers = listener.getLastFourFinishedRacers();
        last_four_racers.clear();
        for (int i = 0; i < get_last_four_racers.size(); i++) {
            last_four_racers.add(get_last_four_racers.get(i));
        }
        if(last_four_racers.size() != 0){
            text_last_four.setVisibility(View.VISIBLE);
        } else {
            text_last_four.setVisibility(View.GONE);
        }
        Collections.sort(last_four_racers, RacerModel.RaceTimeComparatorDesc);
        setAdapter();
    }

    private void infoTextChangeRaceRunning() {
        info_text.setText(getResources().getString(R.string.race_status_in_progress));
    }

    private void infoTextChangeRaceEnded() {
        info_text.setText(getResources().getString(R.string.race_status_ended));
    }

    private void infoTextChangeManualStart() {
        info_text.setText(getResources().getString(R.string.race_status_manual_start));
    }

    private void infoTextChangeStartTime() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        long start = listener.getRaceStartTime().getTimeInMillis();
        if (start != 0) {
            infoTextCheckStart(start);
        } else {
            info_text.setText(getResources().getString(R.string.race_status_starts_in_fill_time));
        }
    }

    private void infoTextCheckStart(long start) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(start);
        Calendar now = Calendar.getInstance();
        if (startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                startTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            // vyřešeno, jen ukázat za jak dlouho
            if (startTime.get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY) > 1) {
                infoTextShowLessThenDay(start, startTime.get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY));
            } else if (startTime.get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY) <= 1) {
                infoTextShowStartLessThenHour(start);
            }
        } else if (startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                (startTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1)) {
            int rozdil = 0;
            if (now.get(Calendar.HOUR_OF_DAY) > startTime.get(Calendar.HOUR_OF_DAY)) {
                rozdil = (24 - now.get(Calendar.HOUR_OF_DAY)) + startTime.get(Calendar.HOUR_OF_DAY);
                if (rozdil > 1) {
                    infoTextShowLessThenDay(start, rozdil);
                } else if (rozdil <= 1) {
                    infoTextShowStartLessThenHour(start);
                }
            } else {
                // víc než 23 hodin
                String stringStart = MillisToStringDate.getStringDateFromMillis(start);
                info_text.setText(getResources().getString(R.string.race_status_starts_in) + " " + stringStart);
            }
        } else if ((startTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) + 1) &&
                ((startTime.get(Calendar.DAY_OF_YEAR) == 0) && ((now.get(Calendar.DAY_OF_MONTH) == 31) && (now.get(Calendar.MONTH) == 11)))) {
            int rozdil = 0;
            if (now.get(Calendar.HOUR_OF_DAY) > startTime.get(Calendar.HOUR_OF_DAY)) {
                rozdil = (24 - now.get(Calendar.HOUR_OF_DAY)) + startTime.get(Calendar.HOUR_OF_DAY);
                if (rozdil > 1) {
                    infoTextShowLessThenDay(start, rozdil);
                } else if (rozdil <= 1) {
                    infoTextShowStartLessThenHour(start);
                }
            } else {
                // víc než 23 hodin
                String stringStart = MillisToStringDate.getStringDateFromMillis(start);
                info_text.setText(getResources().getString(R.string.race_status_starts_in) + " " + stringStart);
            }
        } else {
            String stringStart = MillisToStringDate.getStringDateFromMillis(start);
            info_text.setText(getResources().getString(R.string.race_status_starts_in) + " " + stringStart);
        }
    }

    private void infoTextShowLessThenDay(long start, int hourDifference) {
        String stringStart = MillisToStringDate.getStringDateFromMillis(start);
        String hourText = "hodin";
        if(1 < hourDifference && hourDifference < 5) {
            hourText = "hodiny";
        } else if (hourDifference == 1) {
            hourText = "hodinu";
        }
        info_text.setText(getResources().getString(R.string.race_status_starts_in_count) + " " + hourDifference + " " + hourText + " ("+ stringStart + ")");
    }

    private void infoTextShowStartLessThenHour(long start){
        Calendar now = Calendar.getInstance();
        String infoText = getActivity().getResources().getString(R.string.race_status_starts_in_countdown);
        long countdownTime = start - now.getTimeInMillis();
        setRemainingTimeOnText(countdownTime, infoText, false);
        startCountDownTimer(countdownTime, infoText);
    }

    private void setRemainingTimeOnText(long millisUntilFinished, String infoText, boolean counting) {
        if((millisUntilFinished/1000) < 60) {
            String secondsText = "vteřin";
            if ((1 < (millisUntilFinished/1000)) && ((millisUntilFinished/1000) < 5)) {
                secondsText = "vteřiny";
            } else if ((millisUntilFinished/1000) == 1) {
                secondsText = "vteřinu";
            }
            info_text.setText(infoText + " " + (millisUntilFinished/1000) + " " + secondsText);
        } else {
            if(((millisUntilFinished/1000) % 60) == 0 || !counting) {
                String minutesText = "minut";
                long minutesLeft = ((millisUntilFinished/1000) / 60) + 1;
                if ((1 < minutesLeft) && (minutesLeft < 5)) {
                    minutesText = "minuty";
                } else if (minutesLeft == 1) {
                    minutesText = "minutu";
                }
                info_text.setText(infoText + " " + minutesLeft + " " + minutesText);
            }
        }
    }

    void startCountDownTimer(long countdownTime, final String infoText) {
        countDownToStart = new CountDownTimer(countdownTime, 1000) {
            public void onTick(long millisUntilFinished) {
                setRemainingTimeOnText(millisUntilFinished, infoText,true);
            }

            public void onFinish() {
                // spustit závod
                cancelCountDownTimer();
                if(listener != null) {
                    listener.raceHasStarted(true);
                    handleRaceStatus();
                }
            }
        };
        countDownToStart.start();
    }

    void cancelCountDownTimer() {
        if(countDownToStart!=null) {
            countDownToStart.cancel();
        }
    }

    private void infoTextLoadRaceToStart() {
        info_text.setText(getResources().getString(R.string.race_status_load_race));
    }

    private void infoTextSetStartOption() {
        info_text.setText(getResources().getString(R.string.race_status_not_set));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof RaceTimeFragmentListener) {
            listener = (RaceTimeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RaceTimeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelCountDownTimer();
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
        RacerModel racer = last_four_racers.get(listPosition);
        if(listener.isRaceEnded()) {
            switch (item.getItemId()) {
                case R.id.delete_option:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.race_ended_cant_delete), Toast.LENGTH_LONG).show();
                    return super.onContextItemSelected(item);
                case R.id.edit_option:
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.race_ended_cant_edit), Toast.LENGTH_LONG).show();
                    return super.onContextItemSelected(item);
                default:
                    return super.onContextItemSelected(item);
            }
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
}