package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.dialogs.AddCategoryDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.DateOfBirthConverter;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class EditRacerFragment extends AppFragment implements AdapterView.OnItemSelectedListener, AddCategoryDialog.AddCategoryDialogListener {

    private EditRacerFragment.EditRacerFragmentListener listener;
    RacerModel editedRacer;
    EditText name;
    EditText surname;
    Spinner gender;
    EditText date_born_day;
    EditText date_born_month;
    EditText date_born_year;
    Spinner category;
    EditText team;
    EditText number;

    LinearLayout time_mass_start;
    EditText time_hours;
    EditText time_minutes;
    EditText time_seconds;

    LinearLayout time_trial_start;
    EditText time_trial_start_hours;
    EditText time_trial_start_minutes;
    EditText time_trial_start_seconds;

    LinearLayout time_trial_end;
    EditText time_trial_end_hours;
    EditText time_trial_end_minutes;
    EditText time_trial_end_seconds;

    LinearLayout time_trial_total_time_layout;
    TextView time_trial_total_time;

    Button button_save;
    Button button_back;


    public interface EditRacerFragmentListener {
        void editRacer(RacerModel racer);
        ArrayList<String> getRaceCategories();
        void addCategory(String category);
        RacerModel getRacerByNumber(int number);
        boolean isRaceTimeTrial();
        boolean isRaceRunning();
        boolean isRaceEnded();
        Calendar getRaceStartTime();
        void deleteCategoryIfEmpty(String category);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_racer, container, false);

        Bundle bundle = getArguments();

        editedRacer = new RacerModel(bundle.getInt("id"), bundle.getInt("number"), bundle.getString("name"),
                bundle.getString("surname"), bundle.getString("gender"), bundle.getString("date_born"),
                bundle.getString("category"), bundle.getString("team"), bundle.getLong("start_time"),
                bundle.getLong("end_time"), bundle.getLong("time"), bundle.getBoolean("in_starting_list"));
        name = root.findViewById(R.id.edit_racer_name);
        surname = root.findViewById(R.id.edit_racer_surname);
        gender = root.findViewById(R.id.edit_racer_gender);
        date_born_day = root.findViewById(R.id.edit_racer_date_of_birth_day);
        date_born_month = root.findViewById(R.id.edit_racer_date_of_birth_month);
        date_born_year = root.findViewById(R.id.edit_racer_date_of_birth_year);
        category = root.findViewById(R.id.edit_racer_category);
        team = root.findViewById(R.id.edit_racer_team);
        number = root.findViewById(R.id.edit_racer_number);

        time_mass_start = root.findViewById(R.id.edit_racer_time_mass_start);
        time_hours = root.findViewById(R.id.edit_racer_time_hours);
        time_minutes = root.findViewById(R.id.edit_racer_time_minutes);
        time_seconds = root.findViewById(R.id.edit_racer_time_seconds);

        time_trial_start = root.findViewById(R.id.edit_racer_time_trial_start);
        time_trial_start_hours = root.findViewById(R.id.edit_racer_time_trial_start_hours);
        time_trial_start_minutes = root.findViewById(R.id.edit_racer_time_trial_start_minutes);
        time_trial_start_seconds = root.findViewById(R.id.edit_racer_time_trial_start_seconds);

        time_trial_end = root.findViewById(R.id.edit_racer_time_trial_end);
        time_trial_end_hours = root.findViewById(R.id.edit_racer_time_trial_end_hours);
        time_trial_end_minutes = root.findViewById(R.id.edit_racer_time_trial_end_minutes);
        time_trial_end_seconds = root.findViewById(R.id.edit_racer_time_trial_end_seconds);

        time_trial_total_time_layout = root.findViewById(R.id.edit_racer_time_trial_total_time_layout);
        time_trial_total_time = root.findViewById(R.id.edit_racer_time_trial_total_time);

        button_save = root.findViewById(R.id.saveRacerButton);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editRacer();
            }
        });
        button_back = root.findViewById(R.id.backButton);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        setTextChangeListeners();
        setViews();

        return root;
    }

    private void setTextChangeListeners() {
        time_trial_start_hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        time_trial_start_minutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        time_trial_start_seconds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        time_trial_end_hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        time_trial_end_minutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        time_trial_end_seconds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTimeTrialTotalTime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void calculateTimeTrialTotalTime() {
        String startTimeHours = time_trial_start_hours.getText().toString();
        String startTimeMinutes = time_trial_start_minutes.getText().toString();
        String startTimeSeconds = time_trial_start_seconds.getText().toString();
        String endTimeHours = time_trial_end_hours.getText().toString();
        String endTimeMinutes = time_trial_end_minutes.getText().toString();
        String endTimeSeconds = time_trial_end_seconds.getText().toString();

        if((!startTimeHours.trim().equalsIgnoreCase("")) && (!startTimeMinutes.trim().equalsIgnoreCase(""))
                && (!startTimeSeconds.trim().equalsIgnoreCase("")) && (!endTimeHours.trim().equalsIgnoreCase(""))
                && (!endTimeMinutes.trim().equalsIgnoreCase("")) && (!endTimeSeconds.trim().equalsIgnoreCase(""))) {
            try {
                checkTotalRaceTime(startTimeHours, startTimeMinutes, startTimeSeconds);
                checkTotalRaceTime(endTimeHours, endTimeMinutes, endTimeSeconds);
                long startTimeInSeconds = (Long.parseLong(startTimeHours)*3600) + (Long.parseLong(startTimeMinutes)*60) + Long.parseLong(startTimeSeconds);
                long finishTimeInSeconds = (Long.parseLong(endTimeHours)*3600) + (Long.parseLong(endTimeMinutes)*60) + Long.parseLong(endTimeSeconds);
                long totalTimeInSecond = finishTimeInSeconds - startTimeInSeconds;
                time_trial_total_time.setText(TimeToText.longTimeToString(totalTimeInSecond));

            } catch (Exception e) {
                if(editedRacer.getGender().equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
                    time_trial_total_time.setText(R.string.not_finished_he);
                } else {
                    time_trial_total_time.setText(R.string.not_finished_she);
                }
            }
        } else {
            if(editedRacer.getGender().equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
                time_trial_total_time.setText(R.string.not_finished_he);
            } else {
                time_trial_total_time.setText(R.string.not_finished_she);
            }
        }

    }

    private void setViews() {
        name.setText(editedRacer.getName());
        surname.setText(editedRacer.getSurname());
        String racerGender = editedRacer.getGender();
        ArrayList<String> genders = new ArrayList<>();
        genders.add(getContext().getResources().getString(R.string.man));
        genders.add(getContext().getResources().getString(R.string.woman));
        ArrayAdapter<String> adapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, genders);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        gender.setAdapter(adapter);


        if(racerGender.equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
            gender.setSelection(genders.indexOf(getContext().getResources().getString(R.string.man)));
        } else {
            gender.setSelection(genders.indexOf(getContext().getResources().getString(R.string.woman)));
        }
        String birthday = editedRacer.getDate_born();
        if (!(birthday.equalsIgnoreCase(""))) {
            String date_born = DateOfBirthConverter.getCzechDateString(birthday);
            date_born_day.setText(date_born.substring(0,2));
            date_born_month.setText(date_born.substring(3,5));
            date_born_year.setText(date_born.substring(6,10));
        }

        setSpinnerCategories();
        team.setText(editedRacer.getTeam());
        number.setText("" + editedRacer.getNumber());

        if(listener.isRaceTimeTrial()) {
            time_mass_start.setVisibility(View.GONE);
            time_trial_start.setVisibility(View.VISIBLE);

            long racerStartTime = editedRacer.getStartTime();
            if(racerStartTime != 0) {
                String racerStartTimeText = TimeToText.longTimeToString(racerStartTime);
                time_trial_start_hours.setText(racerStartTimeText.substring(0,2));
                time_trial_start_minutes.setText(racerStartTimeText.substring(3,5));
                time_trial_start_seconds.setText(racerStartTimeText.substring(6,8));
            }
            if(listener.isRaceRunning() || listener.isRaceEnded()) {
                time_trial_end.setVisibility(View.VISIBLE);
                time_trial_total_time_layout.setVisibility(View.VISIBLE);

                long endTime = editedRacer.getEndTime();
                if(endTime != 0) {
                    String racerEndTime = TimeToText.longTimeToString(endTime);
                    if(!(racerEndTime.trim().equalsIgnoreCase(""))) {
                        time_trial_end_hours.setText(racerEndTime.substring(0,2));
                        time_trial_end_minutes.setText(racerEndTime.substring(3,5));
                        time_trial_end_seconds.setText(racerEndTime.substring(6,8));
                    }
                }
                long racerTime = editedRacer.getTimeInSeconds();
                if(racerTime == 0) {
                    if(racerGender.equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
                        time_trial_total_time.setText(R.string.not_finished_he);
                    } else {
                        time_trial_total_time.setText(R.string.not_finished_she);
                    }
                } else {
                    String stringTime = TimeToText.longTimeToString(racerTime);
                    time_trial_total_time.setText(stringTime);
                }
            } else {
                time_trial_end.setVisibility(View.GONE);
                time_trial_total_time_layout.setVisibility(View.GONE);
            }

        } else {
            time_trial_start.setVisibility(View.GONE);
            time_trial_end.setVisibility(View.GONE);
            time_trial_total_time_layout.setVisibility(View.GONE);

            if(listener.isRaceRunning() || listener.isRaceEnded()) {
                time_mass_start.setVisibility(View.VISIBLE);

                long racerTime = editedRacer.getTimeInSeconds();
                String stringTime = TimeToText.longTimeToString(racerTime);
                time_hours.setText(stringTime.substring(0, 2));
                time_minutes.setText(stringTime.substring(3, 5));
                time_seconds.setText(stringTime.substring(6, 8));
            } else {
                time_mass_start.setVisibility(View.GONE);
            }
        }

    }

    private void setSpinnerCategories() {
        String racerCategory = editedRacer.getCategory();
        ArrayList<String> categories = listener.getRaceCategories();
        Collections.sort(categories);
        categories.add(getContext().getResources().getString(R.string.add_category_with_plus));
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, categories);
        categoriesAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        category.setAdapter(categoriesAdapter);
        category.setSelection(categories.indexOf(racerCategory));
        category.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = adapterView.getItemAtPosition(i).toString();
        if (category.equalsIgnoreCase(getResources().getString(R.string.add_category_with_plus))) {
            AddCategoryDialog addCategoryDialog = new AddCategoryDialog();
            addCategoryDialog.setTargetFragment(EditRacerFragment.this, 1);
            Bundle bundle = new Bundle();
            bundle.putInt("spinner_position", i);
            addCategoryDialog.setArguments(bundle);
            addCategoryDialog.show(getActivity().getSupportFragmentManager(), "add category dialog");
        }
    }

    @Override
    public void addCategory(String category, int spinnerPosition) {
        ArrayList<String> allCategories = listener.getRaceCategories();
        if(!(allCategories.contains(category))) {
            listener.addCategory(category);
            setSpinnerCategories();
            allCategories = listener.getRaceCategories();
            Collections.sort(allCategories);
            this.category.setSelection(allCategories.indexOf(category));
        } else {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.category_already_exists), Toast.LENGTH_LONG).show();
            this.category.setSelection(allCategories.indexOf(editedRacer.getCategory()));
        }
    }

    private void goBack() {
        StartingListFragment fragmentStartingList = new StartingListFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentStartingList, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    void handleRaceStatus() {
        // do nothing
    }

    @Override
    void raceEnded() {
        // do nothing
    }

    private void editRacer() {
        RacerModel racer = null;
        try {
            racer = getRacer();
            listener.editRacer(racer);
            possiblePreviousCategoryDelete();
            goBackToStartingList();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void possiblePreviousCategoryDelete() {
        String previousCategory = editedRacer.getCategory();
        listener.deleteCategoryIfEmpty(previousCategory);
    }

    private void goBackToStartingList() {
        StartingListFragment fragmentStartingList = new StartingListFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentStartingList, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private RacerModel getRacer() throws Exception {
        int id = editedRacer.getId();
        String number = this.number.getText().toString();
        checkNumber(number);
        String name = this.name.getText().toString();
        String surname = this.surname.getText().toString();
        checkIfNameEmpty(name, surname);
        String gender = this.gender.getSelectedItem().toString();
        if(gender.equalsIgnoreCase(getContext().getResources().getString(R.string.man))) {
            gender = getContext().getResources().getString(R.string.man_symbol);
        } else {
            gender = getContext().getResources().getString(R.string.woman_symbol);
        }
        String day = this.date_born_day.getText().toString();
        String month = this.date_born_month.getText().toString();
        String year = this.date_born_year.getText().toString();
        String date_born;
        if(day.trim().equalsIgnoreCase("") || month.trim().equalsIgnoreCase("") || year.trim().equalsIgnoreCase("")) {
            date_born = "";
        } else {
            checkDateOfBirth(day, month, year);
            date_born = DateOfBirthConverter.getEnglishDateString(day, month, year);
        }
        String category = this.category.getSelectedItem().toString();
        if(category.equalsIgnoreCase(getContext().getResources().getString(R.string.add_category_with_plus))) {
            category = editedRacer.getCategory();
        }
        String team = this.team.getText().toString();

        long start_time;
        long end_time;
        long time_in_seconds;
        if(listener.isRaceTimeTrial()) {
            String start_hours = time_trial_start_hours.getText().toString();
            String start_minutes = time_trial_start_minutes.getText().toString();
            String start_seconds = time_trial_start_seconds.getText().toString();
            if(start_hours.trim().equalsIgnoreCase("") && start_minutes.trim().equalsIgnoreCase("") && start_seconds.trim().equalsIgnoreCase("")) {
                start_time = 0;
            } else {
                checkTotalRaceTime(start_hours, start_minutes, start_seconds);
                start_time = (Long.parseLong(start_hours)*3600) + (Long.parseLong(start_minutes)*60) + Long.parseLong(start_seconds);
            }

            if(listener.isRaceRunning() || listener.isRaceEnded()) {
                String end_hours = time_trial_end_hours.getText().toString();
                String end_minutes = time_trial_end_minutes.getText().toString();
                String end_seconds = time_trial_end_seconds.getText().toString();
                if(end_hours.trim().equalsIgnoreCase("") && end_minutes.trim().equalsIgnoreCase("") && end_seconds.trim().equalsIgnoreCase("")) {
                    end_time = 0;
                    time_in_seconds = 0;
                } else {
                    checkTime(end_hours, end_minutes, end_seconds);

                    long finishTimeInSeconds = (Long.parseLong(end_hours)*3600) + (Long.parseLong(end_minutes)*60) + Long.parseLong(end_seconds);
                    long totalTimeInSecond = finishTimeInSeconds - start_time;
                    end_time = finishTimeInSeconds;
                    time_in_seconds = totalTimeInSecond;
                }
            } else {
                end_time = 0;
                time_in_seconds = 0;
            }

        } else {
            start_time = 0;
            if(listener.isRaceRunning() || listener.isRaceEnded()) {
                String hours = time_hours.getText().toString();
                String minutes = time_minutes.getText().toString();
                String seconds = time_seconds.getText().toString();
                checkTotalRaceTime(hours, minutes, seconds);
                end_time = (Long.parseLong(hours)*3600) + (Long.parseLong(minutes)*60) + Long.parseLong(seconds);
                time_in_seconds = (Long.parseLong(hours)*3600) + (Long.parseLong(minutes)*60) + Long.parseLong(seconds);
            } else {
                end_time = 0;
                time_in_seconds = 0;
            }
        }
        boolean in_starting_list = editedRacer.isInStartingList();
        return new RacerModel(id, Integer.parseInt(number), name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list);
    }

    private void checkIfNameEmpty(String name, String surname) throws Exception {
        if((name.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_name));
        }
        if((surname.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_surname));
        }
    }

    private void checkNumber(String number) throws Exception {
        if((number.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_number));
        }
        try {
            Integer.parseInt(number);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_number_not_number));
        }
        if(Integer.parseInt(number) <= 0) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_too_low_number));
        }
        int maxValue = 1000000;
        if(Integer.parseInt(number) > maxValue) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_too_high_number));
        }
        if(Integer.parseInt(number) != editedRacer.getNumber() && listener.getRacerByNumber(Integer.parseInt(number)) != null) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_number_already_exists));
        }
    }

    private void checkTotalRaceTime(String hours, String minutes, String seconds) throws Exception {
        if((hours.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        if((minutes.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        if((seconds.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        try {
            Integer.parseInt(hours);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        try {
            Integer.parseInt(minutes);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        try {
            Integer.parseInt(seconds);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        if(Integer.parseInt(hours) < 0 || Integer.parseInt(minutes) < 0 || Integer.parseInt(seconds) < 0) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_too_low));
        }
        if(Integer.parseInt(minutes) > 59) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_hour_range));
        }
        if(Integer.parseInt(seconds) > 59) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_minute_range));
        }
    }

    private void checkTime(String hours, String minutes, String seconds) throws Exception {
        if((hours.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        if((minutes.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        if((seconds.trim()).equalsIgnoreCase("")) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_empty_time));
        }
        try {
            Integer.parseInt(hours);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        try {
            Integer.parseInt(minutes);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        try {
            Integer.parseInt(seconds);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_not_number));
        }
        if(Integer.parseInt(hours) < 0 || Integer.parseInt(minutes) < 0 || Integer.parseInt(seconds) < 0) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_time_too_low));
        }
        if(Integer.parseInt(minutes) > 59) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_hour_range));
        }
        if(Integer.parseInt(seconds) > 59) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_minute_range));
        }
        if(Integer.parseInt(hours) > 23) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_day_range));
        }
    }

    private void checkDateOfBirth(String day, String month, String year) throws Exception {
        try {
            Integer.parseInt(day);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_day_not_number));
        }
        if(Integer.parseInt(day) <= 0 || Integer.parseInt(day) > 31) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_day));
        }
        try {
            Integer.parseInt(month);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_month_not_number));
        }
        if(Integer.parseInt(month) <= 0 || Integer.parseInt(month) > 12) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_month));
        }
        try {
            Integer.parseInt(year);
        } catch (Exception e) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_year_not_number));
        }
        if(Integer.parseInt(year) <= 1900 || Integer.parseInt(year) > 2021) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_year));
        }
        if(Integer.parseInt(month) == 2 && Integer.parseInt(day) > 29) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_february_too_much_days));
        }
        ArrayList<Integer> month_30_days = new ArrayList<>();
        month_30_days.add(4);
        month_30_days.add(6);
        month_30_days.add(9);
        month_30_days.add(11);
        if(month_30_days.contains(Integer.parseInt(month)) && Integer.parseInt(day) > 30) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_too_much_days));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof EditRacerFragment.EditRacerFragmentListener) {
            listener = (EditRacerFragment.EditRacerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement EditRacerFragmentListener");
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
