package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lukaskorinek.pocketracetimer.dialogs.InfoDialog;
import com.lukaskorinek.pocketracetimer.dialogs.NotificationDialog;
import com.lukaskorinek.pocketracetimer.static_classes.MillisToStringDate;
import com.lukaskorinek.pocketracetimer.RaceFetcher;
import com.lukaskorinek.pocketracetimer.dialogs.DateTimePreference;
import com.lukaskorinek.pocketracetimer.dialogs.DateTimePreferenceDialog;
import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.dialogs.EndRaceDialog;
import com.lukaskorinek.pocketracetimer.dialogs.LoadingDialog;
import com.lukaskorinek.pocketracetimer.dialogs.NewRaceDialog;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.util.ArrayList;
import java.util.Calendar;

public class RaceSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,
        EndRaceDialog.EndRaceDialogListener, NewRaceDialog.NewRaceDialogListener {

    private RaceSettingsFragmentListener listener;
    SharedPreferences sharedPref;

    ListPreference choose_race_choice;

    ListPreference own_race_start_choice;
    DateTimePreference dateTime;
    ListPreference own_race_type_choice;

    EditTextPreference loadRace;
    ListPreference load_race_start_choice;
    DateTimePreference changeLoadedRaceStart;
    ListPreference load_race_type_choice;

    Preference about_app_info;
    Preference about_app_help;

    LoadingDialog loadingDialog;

    public interface RaceSettingsFragmentListener {
        boolean isRaceRunning();
        boolean isRaceEnded();
        Calendar getRaceStartTime();
        void endRace(Calendar endTime);
        void setNewRace();
        void setRaceIsNotRunning();
        void deleteRaceStartTime();
        void setRaceStartTime(long startTime);
        void setStartingList(ArrayList<RacerModel> startingList);
        void deleteStartingList();
        ArrayList<RacerModel> getStartingList();
        void setRaceCategories(ArrayList<String> categories);
        void deleteRaceCategories();
        void changeRaceType(String raceType);
        String getRaceType();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        choose_race_choice = findPreference(getResources().getString(R.string.choose_race_option_key));
        choose_race_choice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                notificationBeforeChange();
                return false;
            }
        });

        own_race_start_choice = findPreference(getResources().getString(R.string.own_race_start_option_key));
        dateTime = findPreference(getResources().getString(R.string.date_time_preference_key));
        own_race_type_choice = findPreference(getResources().getString(R.string.own_race_type_option_key));

        loadRace = findPreference(getResources().getString(R.string.load_race_start_key));
        loadRace.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                notificationBeforeChange();
                return false;
            }
        });
        load_race_start_choice = findPreference(getResources().getString(R.string.load_race_start_type_key));
        changeLoadedRaceStart = findPreference(getResources().getString(R.string.change_race_start_key));
        load_race_type_choice = findPreference(getResources().getString(R.string.load_race_type_option_key));

        about_app_info = findPreference(getResources().getString(R.string.about_app_info_key));
        about_app_info.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAppInfo();
                return false;
            }
        });

        about_app_help = findPreference(getResources().getString(R.string.about_app_help_and_support_key));
        about_app_help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showHelpAndSupport();
                return false;
            }
        });

        sharedPref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);

        setDateTimePickerPreference();
        setLoadRacePreferences();
        handleRaceStatus();
        setRaceTypePreferences();
    }

    private void setLoadRacePreferences() {
        loadRace.setSummary(this.sharedPref.getString(getActivity().getResources().getString(R.string.shared_pref_race_name), "Not set"));
    }

    private void setRaceTypePreferences() {
        String raceType = listener.getRaceType();
        String load_race_type = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_type_option_key), "Not set");
        String own_race_type = this.sharedPref.getString(getActivity().getResources().getString(R.string.own_race_type_option_key), "Not set");
        if (!(load_race_type.equalsIgnoreCase(raceType))) {
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_type_option_key), raceType).apply();
        }
        if (!(own_race_type.equalsIgnoreCase(raceType))) {
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.own_race_type_option_key), raceType).apply();
        }
    }

    private void showAppInfo() {
        InfoDialog infoDialog = new InfoDialog(getContext().getResources().getString(R.string.about_app_info_title), getContext().getResources().getString(R.string.about_app_info_text));
        infoDialog.setTargetFragment(RaceSettingsFragment.this, 1);
        infoDialog.show(getActivity().getSupportFragmentManager(), "info dialog");
    }

    private void showHelpAndSupport() {
        InfoDialog infoDialog = new InfoDialog(getContext().getResources().getString(R.string.about_app_help_and_support_title), getContext().getResources().getString(R.string.about_app_help_and_support_text));
        infoDialog.setTargetFragment(RaceSettingsFragment.this, 1);
        infoDialog.show(getActivity().getSupportFragmentManager(), "info dialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_right_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(listener.isRaceEnded()) {
            menu.getItem(0).setEnabled(false);
        }
        if(listener.isRaceRunning()) {
            menu.getItem(0).setEnabled(true);
        } else {
            menu.getItem(0).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_race:
                endThisRace();
                return true;

            case R.id.new_race:
                newRace();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void endThisRace() {
        EndRaceDialog endRaceDialog = new EndRaceDialog();
        endRaceDialog.setTargetFragment(RaceSettingsFragment.this, 1);
        endRaceDialog.show(getActivity().getSupportFragmentManager(), "end race dialog");
    }

    @Override
    public void endRaceYes() {
        listener.endRace(Calendar.getInstance());
    }

    private void newRace() {
        NewRaceDialog newRaceDialog = new NewRaceDialog();
        newRaceDialog.setTargetFragment(RaceSettingsFragment.this, 1);
        newRaceDialog.show(getActivity().getSupportFragmentManager(), "new race dialog");
    }

    @Override
    public void newRaceYes() {
        listener.setNewRace();
        handleRaceStatus();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Use instanceof to check if the preference is one of
        // DatePreference, TimePreference or DateTimePreference
        DialogFragment dialogFragment = null;
        if (preference instanceof DateTimePreference) {
            dialogFragment = DateTimePreferenceDialog.newInstance(preference.getKey());
            // You can also specify the minimum and maximum date here
            //dialogFragment = DateTimePreferenceDialog.newInstance(preference.getKey(), minDate, maxDate);
        }

        if (dialogFragment != null) {
            // If it is one of our preferences, show it
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), "YOUR TAG HERE");
        } else {
            // Let super handle it
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void setDateTimePickerPreference() {
        if (dateTime != null) {
            dateTime.setSummaryProvider(new Preference.SummaryProvider<DateTimePreference>() {
                @Override
                public CharSequence provideSummary(DateTimePreference preference) {
                    Calendar startTime = listener.getRaceStartTime();
                    long raceStartTime = startTime.getTimeInMillis();
                    if(raceStartTime == 0) {
                        return getActivity().getResources().getString(R.string.not_set);
                    } else {
                        return MillisToStringDate.getStringDateFromMillis(raceStartTime);
                    }
                }
            });
        }

        if (changeLoadedRaceStart != null) {
            changeLoadedRaceStart.setSummaryProvider(new Preference.SummaryProvider<DateTimePreference>() {
                @Override
                public CharSequence provideSummary(DateTimePreference preference) {
                    Calendar startTime = listener.getRaceStartTime();
                    long raceStartTime = startTime.getTimeInMillis();
                    if(raceStartTime == 0) {
                        return getActivity().getResources().getString(R.string.not_loaded);
                    } else {
                        return MillisToStringDate.getStringDateFromMillis(raceStartTime);
                    }
                }
            });
        }
    }

    private void handleRaceStatus() {
        if (listener.isRaceRunning() || listener.isRaceEnded()) {
            disableAll();
        } else {
            choose_race_choice.setEnabled(true);
            enableAndDisablePreferences();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setDateTimePickerPreference();
        // zobrazit a skrýt preferences
        if((s.equalsIgnoreCase(getActivity().getResources().getString(R.string.choose_race_option_key))) ||
                (s.equalsIgnoreCase(getActivity().getResources().getString(R.string.own_race_start_option_key))) ||
                (s.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_start_key))) ||
                (s.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_start_type_key)))) {
            enableAndDisablePreferences();
        }

        if ((!listener.isRaceRunning()) && (!listener.isRaceEnded())) {
            // fukncionalita jednotlivých preferences
            handlePreferencesFunctionality(s);
        }
    }

    private void handlePreferencesFunctionality(String s) {
        String choose_race_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.choose_race_option_key), "Not set");
        String race_start_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.own_race_start_option_key), "Not set");

        if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.choose_race_option_key))) {
            changedChoosenRace(choose_race_option);

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.own_race_start_option_key))) {
            changedOwnRaceStartType(race_start_option);

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.date_time_preference_key))) {
            changedOwnRaceDateTime(race_start_option);

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.own_race_type_option_key))) {
            changedOwnRaceType();

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_start_key))) {
            changedLoadedRace();

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_start_type_key))) {
            changedLoadedRaceStartType();

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.change_race_start_key))) {
            changedLoadedRaceStartTime(choose_race_option);

        } else if(s.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_type_option_key))) {
            changedLoadedRaceType();
        }
    }

    private void notificationBeforeChange() {
        if(listener.getStartingList().size() > 0) {
            NotificationDialog notificationDialog = new NotificationDialog(getContext().getResources().getString(R.string.notification_text_race_change));
            notificationDialog.setTargetFragment(RaceSettingsFragment.this, 1);
            notificationDialog.show(getActivity().getSupportFragmentManager(), "notification dialog");
        }
    }

    private void changedChoosenRace(String choose_race_option) {
        if (choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_load_race))) {
            listener.setRaceIsNotRunning();
            listener.deleteRaceStartTime();
            listener.deleteStartingList();
            listener.deleteRaceCategories();
        } else if(choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_own_race))) {
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_start_key), "").apply();
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_race_name), getActivity().getResources().getString(R.string.not_loaded)).apply();
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_start_time), getActivity().getResources().getString(R.string.not_loaded)).apply();
            loadRace.setSummary(getActivity().getResources().getString(R.string.not_loaded));
            listener.deleteStartingList();
            listener.deleteRaceCategories();
        }
    }

    private void changedOwnRaceStartType(String race_start_option){
        // pokud se změnil na start zadanej časem tak načíst čas startu z datetimepreference

        if (race_start_option.equalsIgnoreCase(getActivity().getResources().getString(R.string.race_start_values_manual_start_time))) {
            // tady co? Asi změnit čas startu na ten co udává datetimepreference
            // long race_start_time = sharedPref.getLong(getResources().getString(R.string.date_time_preference_key), 0);
            //database.setRaceStartTime(race_start_time);
        } else if (race_start_option.equalsIgnoreCase(getResources().getString(R.string.race_start_values_manual_start_button))) {
            listener.setRaceIsNotRunning();
            listener.deleteRaceStartTime();
        }
    }

    private void changedOwnRaceDateTime(String race_start_option) {
        long race_start_time = this.sharedPref.getLong(getActivity().getResources().getString(R.string.date_time_preference_key), 0);
        if (race_start_option.equalsIgnoreCase(getActivity().getResources().getString(R.string.race_start_values_manual_start_time))) {
            listener.setRaceStartTime(race_start_time);
            Calendar now = Calendar.getInstance();
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(race_start_time);
            if (startTime.compareTo(now) <= 0) {
                disableAll();
            }
        }
    }

    private void changedOwnRaceType() {
        String race_type = this.sharedPref.getString(getActivity().getResources().getString(R.string.own_race_type_option_key), "");
        if (race_type.equalsIgnoreCase(getActivity().getResources().getString(R.string.race_type_values_mass_start))) {
            //hromadný start
            listener.changeRaceType(race_type);
        } else if (race_type.equalsIgnoreCase(getActivity().getResources().getString(R.string.race_type_values_time_trial))) {
            //časovka
            listener.changeRaceType(race_type);
        }
        setRaceTypePreferences();
    }

    private void changedLoadedRace() {
        this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_race_name), getActivity().getResources().getString(R.string.not_loaded)).apply();
        this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_start_time), getActivity().getResources().getString(R.string.not_loaded)).apply();
        String packetId = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_start_key), "Not set");
        try{
            if (!(packetId.equalsIgnoreCase("Not set")) && !(packetId.equalsIgnoreCase(""))) {
                Integer.parseInt(packetId);
                startLoadingScreen();
                try {
                    Boolean validRace = new BackgroundFetch().execute(packetId).get();
                    if(!validRace) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
                }
            }
        } catch(NumberFormatException e) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.invalid_race), Toast.LENGTH_LONG).show();
        }
    }

    private class BackgroundFetch extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            return fetchRace(strings[0]);
        }

        private Boolean fetchRace(String packetId) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
            RaceFetcher fetch = new RaceFetcher(packetId);
            Calendar startTime = fetch.getStartingTime();
            if(startTime != null) {
                setLoadedRace(fetch, startTime);
                return true;
            } else {
                sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_start_key), "").apply();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            loadingDialog.dismissLoadingAnimation();
        }
    }

    private void startLoadingScreen() {
        this.loadingDialog = new LoadingDialog(this.getActivity());
        this.loadingDialog.startLoadingAnimation();
    }

    private void setLoadedRace(RaceFetcher fetch, Calendar startTime) {
        Calendar now = Calendar.getInstance();
        if (startTime.compareTo(now) <= 0) {
            // začal dřív než je teď
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_start_type_key), getActivity().getResources().getString(R.string.load_race_start_values_manual_start_button)).apply();
        } else {
            listener.setRaceStartTime(startTime.getTimeInMillis());
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_start_time), MillisToStringDate.getStringDateFromMillis(fetch.getStartingTime().getTimeInMillis())).apply();
        }
        this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.shared_pref_race_name), fetch.getRaceName()).apply();
        ArrayList<RacerModel> startingList = fetch.getStartingList();
        listener.setStartingList(startingList);
        ArrayList<String> categories = fetch.getCategories();
        listener.setRaceCategories(categories);
        loadRace.setSummary(fetch.getRaceName());
        String race_type = fetch.getRaceType();
        Log.i("Tak co to teda je?", race_type);
        if(race_type.equalsIgnoreCase(getActivity().getResources().getString(R.string.time_trial_web_name))) {
            listener.changeRaceType(getActivity().getResources().getString(R.string.load_race_type_values_time_trial));
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_type_option_key), getActivity().getResources().getString(R.string.load_race_type_values_time_trial)).apply();
        } else {
            listener.changeRaceType(getActivity().getResources().getString(R.string.load_race_type_values_mass_start));
            this.sharedPref.edit().putString(getActivity().getResources().getString(R.string.load_race_type_option_key), getActivity().getResources().getString(R.string.load_race_type_values_mass_start)).apply();
        }
        setRaceTypePreferences();
        enableAndDisablePreferences();
    }

    private void changedLoadedRaceStartType(){
        String load_race_start_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_start_type_key), "Not set");
        if(load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_button))) {
            listener.setRaceIsNotRunning();
            listener.deleteRaceStartTime();
        } else if(load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_time))) {
        }
    }

    private void changedLoadedRaceStartTime(String choose_race_option) {
        long race_start_time = this.sharedPref.getLong(getActivity().getResources().getString(R.string.change_race_start_key), 0);
        if (choose_race_option.equalsIgnoreCase(getActivity().getResources().getString(R.string.choose_race_values_load_race))) {
            listener.setRaceStartTime(race_start_time);
            Calendar now = Calendar.getInstance();
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(race_start_time);
            if (startTime.compareTo(now) <= 0) {
                disableAll();
            }
        }
    }

    private void changedLoadedRaceType() {
        String race_type = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_type_option_key), "");
        if (race_type.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_type_values_mass_start))) {
            //hromadný start
            listener.changeRaceType(race_type);
            Log.i("HLEDANI-2-1", race_type);
        } else if (race_type.equalsIgnoreCase(getActivity().getResources().getString(R.string.load_race_type_values_time_trial))) {
            //časovka
            listener.changeRaceType(race_type);
            Log.i("HLEDANI-2-2", race_type);
        }
        Log.i("ČASOVKA ZMĚNA!", "Jo nebo ne??");
    }

    private void disableAll() {
        choose_race_choice.setEnabled(false);
        own_race_start_choice.setEnabled(false);
        dateTime.setEnabled(false);
        own_race_type_choice.setEnabled(false);
        loadRace.setEnabled(false);
        load_race_start_choice.setEnabled(false);
        changeLoadedRaceStart.setEnabled(false);
        load_race_type_choice.setEnabled(false);
    }

    private void disableOwnRace() {
        own_race_start_choice.setEnabled(false);
        dateTime.setEnabled(false);
        own_race_type_choice.setEnabled(false);
    }

    private void disableLoadRace() {
        loadRace.setEnabled(false);
        load_race_start_choice.setEnabled(false);
        changeLoadedRaceStart.setEnabled(false);
        load_race_type_choice.setEnabled(false);
    }

    private void enableAndDisablePreferences() {
        String choose_race_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.choose_race_option_key), "Not set");
        String own_race_start_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.own_race_start_option_key), "Not set");

        String load_race_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_start_key), "Not set");
        String load_race_start_option = this.sharedPref.getString(getActivity().getResources().getString(R.string.load_race_start_type_key), "Not set");

        if(choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_own_race))) {
            disableLoadRace();
            own_race_start_choice.setEnabled(true);
            own_race_type_choice.setEnabled(true);
            if (own_race_start_option.equalsIgnoreCase(getResources().getString(R.string.race_start_values_manual_start_button))) {
                dateTime.setEnabled(false);
            } else if (own_race_start_option.equalsIgnoreCase(getResources().getString(R.string.race_start_values_manual_start_time))) {
                dateTime.setEnabled(true);
            }
        } else if(choose_race_option.equalsIgnoreCase(getResources().getString(R.string.choose_race_values_load_race))) {
            disableOwnRace();
            loadRace.setEnabled(true);
            if(load_race_option.equalsIgnoreCase("Not set") || load_race_option.equalsIgnoreCase("")) {
                load_race_start_choice.setEnabled(false);
                changeLoadedRaceStart.setEnabled(false);
                load_race_type_choice.setEnabled(false);
            } else {
                load_race_start_choice.setEnabled(true);
                load_race_type_choice.setEnabled(true);
                if(load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_button))) {
                    changeLoadedRaceStart.setEnabled(false);
                } else if(load_race_start_option.equalsIgnoreCase(getResources().getString(R.string.load_race_start_values_manual_start_time))) {
                    changeLoadedRaceStart.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerPref();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPref();
    }

    private void registerPref() {
        SharedPreferences pref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPref() {
        SharedPreferences pref = getActivity().getSharedPreferences(getContext().getResources().getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        pref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof RaceSettingsFragment.RaceSettingsFragmentListener) {
            listener = (RaceSettingsFragment.RaceSettingsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RaceSettingsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}