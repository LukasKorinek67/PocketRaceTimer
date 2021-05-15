package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.dialogs.AddCategoryDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.DateOfBirthConverter;

import java.util.ArrayList;
import java.util.Collections;

public class AddRacerFragment extends AppFragment implements AdapterView.OnItemSelectedListener, AddCategoryDialog.AddCategoryDialogListener {

    private AddRacerFragment.AddRacerFragmentListener listener;
    EditText name;
    EditText surname;
    Spinner gender;
    EditText date_born_day;
    EditText date_born_month;
    EditText date_born_year;
    Spinner category;
    EditText category_edit_text;
    EditText team;
    EditText number;
    LinearLayout time_trial;
    EditText time_trial_hours;
    EditText time_trial_minutes;
    EditText time_trial_seconds;
    Button button_add;
    Button button_back;

    public interface AddRacerFragmentListener {
        ArrayList<String> getRaceCategories();
        void addCategory(String category);
        RacerModel getRacerByNumber(int number);
        void addRacerToStartingList(RacerModel racer);
        boolean isRaceTimeTrial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_racer, container, false);

        name = root.findViewById(R.id.add_racer_name);
        surname = root.findViewById(R.id.add_racer_surname);
        gender = root.findViewById(R.id.add_racer_gender);
        date_born_day = root.findViewById(R.id.add_racer_date_of_birth_day);
        date_born_month = root.findViewById(R.id.add_racer_date_of_birth_month);
        date_born_year = root.findViewById(R.id.add_racer_date_of_birth_year);
        category = root.findViewById(R.id.add_racer_category);
        category_edit_text = root.findViewById(R.id.add_racer_category_edit_text);
        team = root.findViewById(R.id.add_racer_team);
        number = root.findViewById(R.id.add_racer_number);
        time_trial = root.findViewById(R.id.add_racer_time_trial);
        time_trial_hours = root.findViewById(R.id.add_racer_time_trial_hours);
        time_trial_minutes = root.findViewById(R.id.add_racer_time_trial_minutes);
        time_trial_seconds = root.findViewById(R.id.add_racer_time_trial_seconds);
        button_add = root.findViewById(R.id.saveRacerButton);
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRacer();
            }
        });
        button_back = root.findViewById(R.id.backButton);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        setSpinners();
        checkIfTimeTrial();
        return root;
    }

    private void checkIfTimeTrial(){
        if(listener.isRaceTimeTrial()) {
            time_trial.setVisibility(View.VISIBLE);
        } else {
            time_trial.setVisibility(View.GONE);
        }
    }

    private void setSpinners() {
        setGenderSpinner();
        setCategorySpinner();
    }

    private void setGenderSpinner() {
        ArrayList<String> genders = new ArrayList<>();
        genders.add(getContext().getResources().getString(R.string.man));
        genders.add(getContext().getResources().getString(R.string.woman));
        ArrayAdapter<String> adapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, genders);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        gender.setAdapter(adapter);
    }

    private void setCategorySpinner() {
        ArrayList<String> categories = listener.getRaceCategories();
        if(categories.size() == 0) {
            category.setVisibility(View.GONE);
            category_edit_text.setVisibility(View.VISIBLE);

        } else {
            category.setVisibility(View.VISIBLE);
            category_edit_text.setVisibility(View.GONE);
            Collections.sort(categories);
            categories.add(getContext().getResources().getString(R.string.add_category_with_plus));
            ArrayAdapter<String> categoriesAdapter = new ArrayAdapter(this.getActivity(), R.layout.custom_spinner, categories);
            categoriesAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
            category.setAdapter(categoriesAdapter);
            category.setSelection(0);
            category.setOnItemSelectedListener(this);
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

    private void addRacer() {
        RacerModel racer = null;
        try {
            racer = getRacer();
            listener.addRacerToStartingList(racer);
            goBackToStartingList();
        } catch (Exception e) {
            String message = e.getMessage();
            e.printStackTrace();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    private RacerModel getRacer() throws Exception {
        int id = 0;
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
        ArrayList<String> categories = listener.getRaceCategories();
        String category;
        if(categories.size() != 0) {
            category = this.category.getSelectedItem().toString();
            if(category.equalsIgnoreCase(getContext().getResources().getString(R.string.add_category_with_plus))) {
                category = this.category.getItemAtPosition(0).toString();
            }
        } else {
            category = this.category_edit_text.getText().toString();
            if(!(categories.contains(category))) {
                listener.addCategory(category);
            }
            if(category.equalsIgnoreCase(getContext().getResources().getString(R.string.add_category_with_plus))) {
                throw new Exception(this.getContext().getResources().getString(R.string.racer_error_wrong_category));
            }
        }
        String team = this.team.getText().toString();
        long start_time;
        if(listener.isRaceTimeTrial()) {
            String hours = time_trial_hours.getText().toString();
            String minutes = time_trial_minutes.getText().toString();
            String seconds = time_trial_seconds.getText().toString();
            checkStartTime(hours, minutes, seconds);
            start_time = (Long.parseLong(hours)*3600) + (Long.parseLong(minutes)*60) + Long.parseLong(seconds);
        } else {
            start_time = 0;
        }
        long end_time = 0;
        long time_in_seconds = 0;
        boolean in_starting_list = true;
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
        RacerModel findRacer = listener.getRacerByNumber(Integer.parseInt(number));
        if(findRacer != null && findRacer.isInStartingList()) {
            throw new Exception(this.getContext().getResources().getString(R.string.racer_error_number_already_exists));
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

    private void checkStartTime(String hours, String minutes, String seconds) throws Exception {
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

    private void goBackToStartingList() {
        StartingListFragment fragmentStartingList = new StartingListFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentStartingList, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String category = adapterView.getItemAtPosition(i).toString();
        if (category.equalsIgnoreCase(getResources().getString(R.string.add_category_with_plus))) {
            AddCategoryDialog addCategoryDialog = new AddCategoryDialog();
            addCategoryDialog.setTargetFragment(AddRacerFragment.this, 1);
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
            setCategorySpinner();
            //this.category.setSelection(spinnerPosition);
            allCategories = listener.getRaceCategories();
            Collections.sort(allCategories);
            this.category.setSelection(allCategories.indexOf(category));
        } else {
            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.category_already_exists), Toast.LENGTH_LONG).show();
            this.category.setSelection(0);
        }
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof AddRacerFragment.AddRacerFragmentListener) {
            listener = (AddRacerFragment.AddRacerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddRacerFragmentListener");
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
