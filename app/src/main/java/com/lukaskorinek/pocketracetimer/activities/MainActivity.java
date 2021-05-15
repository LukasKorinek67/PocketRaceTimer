package com.lukaskorinek.pocketracetimer.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.database.DAO;
import com.lukaskorinek.pocketracetimer.fragments.AddRacerFragment;
import com.lukaskorinek.pocketracetimer.fragments.AppFragment;
import com.lukaskorinek.pocketracetimer.fragments.DisplayRacerFragment;
import com.lukaskorinek.pocketracetimer.fragments.EditRacerFragment;
import com.lukaskorinek.pocketracetimer.fragments.RaceSettingsFragment;
import com.lukaskorinek.pocketracetimer.fragments.RaceTimeFragment;
import com.lukaskorinek.pocketracetimer.fragments.RacersFragment;
import com.lukaskorinek.pocketracetimer.fragments.ResultsFragment;
import com.lukaskorinek.pocketracetimer.fragments.StartingListFragment;
import com.lukaskorinek.pocketracetimer.models.RaceModel;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements AppFragment.AppFragmentListener, RaceSettingsFragment.RaceSettingsFragmentListener,
        RaceTimeFragment.RaceTimeFragmentListener, RacersFragment.RacersFragmentListener, StartingListFragment.StartingListFragmentListener,
        ResultsFragment.ResultsFragmentListener, EditRacerFragment.EditRacerFragmentListener, AddRacerFragment.AddRacerFragmentListener,
        DisplayRacerFragment.DisplayRacerFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private DAO database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        database = new DAO(MainActivity.this);

        if(isRaceEnded()) {
            bottomNav.getMenu().getItem(1).setTitle(getResources().getString(R.string.nav_results));
            bottomNav.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_baseline_done_outline_24));
        }

        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RaceTimeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_race_time:
                            selectedFragment = new RaceTimeFragment();
                            break;
                        case R.id.nav_racers:
                            if(isRaceEnded()) {
                                selectedFragment = new ResultsFragment();
                            } else {
                                selectedFragment = new RacersFragment();
                            }
                            break;
                        case R.id.nav_starting_list:
                            selectedFragment = new StartingListFragment();
                            break;
                        case R.id.nav_race:
                            selectedFragment = new RaceSettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public ArrayList<RacerModel> getAllFinishedRacers() {
        return database.getAllFinishedRacers();
    }

    @Override
    public ArrayList<RacerModel> getLastFourFinishedRacers() {
        ArrayList<RacerModel> all_racers = database.getAllFinishedRacers();
        Collections.sort(all_racers, RacerModel.RaceTimeComparatorAsc);
        ArrayList<RacerModel> last_four = new ArrayList<>();
        for (int i = all_racers.size(); (i > 0) && (i > all_racers.size() - 4); i--) {
            last_four.add(all_racers.get(i-1));
        }
        return last_four;
    }

    @Override
    public ArrayList<RacerModel> getAllRacers() {
        return database.getAllRacers();
    }

    @Override
    public void addRacerToList(RacerModel racer) {
        database.addOneRacer(racer);
    }

    @Override
    public void deleteFinishedRacer(RacerModel racer) {
        if(database.getRacerByID(racer.getId()).isInStartingList()) {
            racer.setTimeInSeconds(0);
            database.removeRacerFinishTime(racer);
        } else {
            database.deleteOneRacer(racer);
        }
    }

    @Override
    public void deleteRacerFromStartingList(RacerModel racer) {
        if(racer.getTimeInSeconds() == 0) {
            database.deleteOneRacer(racer);
        } else {
            int number = racer.getNumber();
            long time = racer.getTimeInSeconds();
            long endTime = racer.getEndTime();
            database.deleteOneRacer(racer);
            RacerModel newRacer = new RacerModel(number, time, endTime);
            database.addOneRacer(newRacer);
        }
        String category = racer.getCategory();
        deleteCategoryIfEmpty(category);
    }

    @Override
    public void deleteCategoryIfEmpty(String category) {
        if(isCategoryEmpty(category)) {
            database.deleteOneRaceCategory(category);
        }
    }

    private boolean isCategoryEmpty(String category) {
        return database.getRacersFromStartingListByCategory(category).size() == 0;
    }

    @Override
    public void editRacer(RacerModel racer) {
        database.updateOneRacer(racer);
    }

    @Override
    public void editFinishedRacer(RacerModel newRacer) {
        RacerModel oldRacer = database.getRacerByID(newRacer.getId());
        if(oldRacer.isInStartingList()) {
            RacerModel possibleRacer = database.getRacerFromStartingListByNumber(newRacer.getNumber());
            if(possibleRacer == null) {
                if(getRacerByNumber(newRacer.getNumber()) == null) {
                    oldRacer.setTimeInSeconds(0);
                    database.removeRacerFinishTime(oldRacer);
                    newRacer.setId(0);
                    database.addOneRacer(newRacer);
                } else {
                    Toast.makeText(this, this.getResources().getString(R.string.error_racing_number_already_finished), Toast.LENGTH_LONG).show();
                }
            } else {
                if(possibleRacer.getTimeInSeconds() == 0) {
                    oldRacer.setTimeInSeconds(0);
                    database.removeRacerFinishTime(oldRacer);
                    possibleRacer.setTimeInSeconds(newRacer.getTimeInSeconds());
                    database.updateOneRacer(possibleRacer);
                } else {
                    Toast.makeText(this, this.getResources().getString(R.string.error_racer_already_finished), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            RacerModel possibleRacer = database.getRacerFromStartingListByNumber(newRacer.getNumber());
            if(possibleRacer == null) {
                if(getRacerByNumber(newRacer.getNumber()) == null) {
                    database.updateOneRacer(newRacer);
                } else {
                    Toast.makeText(this, this.getResources().getString(R.string.error_racing_number_already_finished), Toast.LENGTH_LONG).show();
                }
            } else {
                if(possibleRacer.getTimeInSeconds() == 0) {
                    database.deleteOneRacer(newRacer);
                    possibleRacer.setTimeInSeconds(newRacer.getTimeInSeconds());
                    database.updateOneRacer(possibleRacer);
                } else {
                    Toast.makeText(this, this.getResources().getString(R.string.error_racer_already_finished), Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override
    public RacerModel getRacerFromStartingListByNumber(int number) {
        return database.getRacerFromStartingListByNumber(number);
    }

    @Override
    public RacerModel getRacerByNumber(int number) {
        return database.getRacerByNumber(number);
    }

    @Override
    public void racerHasFinished(RacerModel racer) {
        database.updateOneRacer(racer);
    }

    @Override
    public Calendar getRaceStartTime() {
        Calendar raceStartTime = Calendar.getInstance();
        RaceModel race = database.getRace();
        raceStartTime.setTimeInMillis(race.getRaceStartTime());
        return raceStartTime;
    }

    @Override
    public Calendar getRaceEndedTime() {
        Calendar raceEndedtime = Calendar.getInstance();
        RaceModel race = database.getRace();
        raceEndedtime.setTimeInMillis(race.getEndTime());
        return raceEndedtime;
    }

    @Override
    public void startRace(Calendar startTime) {
        database.startRace(startTime.getTimeInMillis());
    }

    @Override
    public void endRace(Calendar endTime) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.getMenu().getItem(1).setTitle(getResources().getString(R.string.nav_results));
        bottomNav.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_baseline_done_outline_24));
        database.endRace(endTime.getTimeInMillis());
    }

    @Override
    public boolean isRaceRunning() {
        RaceModel race = database.getRace();
        if(race.isRaceInProgess()) {
            return race.isRaceInProgess();
        } else if (race.getEndTime() != 0){
            return race.isRaceInProgess();
        } else {
            long startTime = race.getRaceStartTime();
            if (startTime == 0) {
                return race.isRaceInProgess();
            }
            Calendar raceStart = Calendar.getInstance();
            raceStart.setTimeInMillis(startTime);
            Calendar now = Calendar.getInstance();
            if (raceStart.compareTo(now) < 0) {
                // nastavit v databázi, že běží a vrátit true
                race.setRaceInProgess(true);
                database.setRaceIsRunning();
                return race.isRaceInProgess();
            } else {
                return race.isRaceInProgess();
            }
        }
    }

    @Override
    public void raceHasStarted(boolean yes) {
        if(yes) {
            database.setRaceIsRunning();
        }
    }

    @Override
    public void setNewRace() {
        database.deleteAllRacers();
        database.newRace();
        database.deleteStartingList();
        database.deleteRaceCategories();
        SharedPreferences sharedPref = getSharedPreferences(this.getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        sharedPref.edit().putString(getResources().getString(R.string.choose_race_option_key), getResources().getString(R.string.choose_race_values_own_race)).apply();
        sharedPref.edit().putString(getResources().getString(R.string.load_race_start_key), "").apply();
        sharedPref.edit().putString(getResources().getString(R.string.shared_pref_race_name), getResources().getString(R.string.not_loaded)).apply();
        sharedPref.edit().putString(getResources().getString(R.string.shared_pref_start_time), getResources().getString(R.string.not_loaded)).apply();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().getItem(1).setTitle(getResources().getString(R.string.nav_racers));
        bottomNav.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_baseline_directions_run_24));
    }

    @Override
    public boolean isRaceEnded() {
        RaceModel race = database.getRace();
        if(race.getEndTime() == 0) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void deleteRaceStartTime(){
        database.deleteRaceStartTime();
    }

    @Override
    public void setRaceIsNotRunning() {
        database.setRaceIsNotRunning();
    }

    @Override
    public void setRaceStartTime(long startTime) {
        database.setRaceStartTime(startTime);
    }

    @Override
    public void setStartingList(ArrayList<RacerModel> startingList) {
        database.deleteStartingList();
        database.setStartingList(startingList);
    }

    @Override
    public ArrayList<RacerModel> getStartingList() {
        return database.getStartingList();
    }

    @Override
    public void deleteStartingList() {
        database.deleteStartingList();
    }

    @Override
    public void addRacerToStartingList(RacerModel racer) {
        RacerModel finishedRacer = database.getRacerByNumber(racer.getNumber());
        if(finishedRacer == null) {
            database.addOneRacer(racer);
        } else {
            if(!(finishedRacer.isInStartingList())) {
                racer.setId(finishedRacer.getId());
                racer.setEndTime(finishedRacer.getEndTime());
                if (isRaceTimeTrial()) {
                    racer.setTimeInSeconds(finishedRacer.getTimeInSeconds() - racer.getStartTime());
                } else {
                    racer.setTimeInSeconds(finishedRacer.getTimeInSeconds());
                }
                database.updateOneRacer(racer);
            }
        }
    }

    @Override
    public void setRaceCategories(ArrayList<String> categories) {
        database.setRaceCategories(categories);
    }

    @Override
    public void addCategory(String category) {
        database.addRaceCategory(category);
    }

    @Override
    public ArrayList<String> getRaceCategories() {
        return database.getRaceCategories();
    }

    @Override
    public ArrayList<RacerModel> getRacersFromStartingListByCategory(String category) {
        return database.getRacersFromStartingListByCategory(category);
    }

    @Override
    public void deleteRaceCategories() {
        database.deleteRaceCategories();
    }

    @Override
    public void changeRaceType(String raceType) {
        if((!isRaceRunning()) && (!isRaceEnded())) {
            database.changeRaceType(raceType);
        }
    }

    @Override
    public String getRaceType() {
        return database.getRace().getRaceType();
    }

    @Override
    public boolean isRaceTimeTrial() {
        return database.getRace().getRaceType().equalsIgnoreCase(getResources().getString(R.string.race_type_values_time_trial));
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerPref();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPref();
    }

    private void registerPref() {
        SharedPreferences pref = getSharedPreferences(this.getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPref() {
        SharedPreferences pref = getSharedPreferences(this.getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        pref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}
