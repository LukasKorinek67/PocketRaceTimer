package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.dialogs.DeleteDialog;
import com.lukaskorinek.pocketracetimer.static_classes.DateOfBirthConverter;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

public class DisplayRacerFragment extends AppFragment implements DeleteDialog.DeleteDialogListener {


    private DisplayRacerFragment.DisplayRacerFragmentListener listener;
    RacerModel displayedRacer;
    TextView name;
    TextView gender;
    TextView date_born;
    TextView born_text;
    TextView category;
    TextView team;
    TextView number;
    TextView time;
    LinearLayout time_trial;
    TextView start_time;
    Button editButton;
    Button deleteButton;
    Button backButton;

    public interface DisplayRacerFragmentListener {
        void deleteRacerFromStartingList(RacerModel racer);
        boolean isRaceTimeTrial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_display_racer, container, false);

        Bundle bundle = getArguments();

        displayedRacer = new RacerModel(bundle.getInt("id"), bundle.getInt("number"), bundle.getString("name"),
                bundle.getString("surname"), bundle.getString("gender"), bundle.getString("date_born"),
                bundle.getString("category"), bundle.getString("team"), bundle.getLong("start_time"),
                bundle.getLong("end_time"), bundle.getLong("time"), bundle.getBoolean("in_starting_list"));

        name = root.findViewById(R.id.display_racer_name);
        gender = root.findViewById(R.id.display_racer_gender);
        date_born = root.findViewById(R.id.display_racer_date_of_birth);
        born_text = root.findViewById(R.id.display_racer_born_text);
        category = root.findViewById(R.id.display_racer_category);
        team = root.findViewById(R.id.display_racer_team);
        number = root.findViewById(R.id.display_racer_number);
        time = root.findViewById(R.id.display_racer_time);
        time_trial = root.findViewById(R.id.display_time_trial);
        start_time = root.findViewById(R.id.display_racer_time_trial);
        editButton = root.findViewById(R.id.editRacerButton);
        deleteButton = root.findViewById(R.id.deleteRacerButton);
        backButton = root.findViewById(R.id.backButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editRacer();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRacer();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        setViews();

        return root;
    }

    private void setViews() {
        name.setText(displayedRacer.getName() + " " + displayedRacer.getSurname());
        String racerGender = displayedRacer.getGender();
        if(racerGender.equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
            gender.setText(getContext().getResources().getString(R.string.man));
            born_text.setText(getContext().getResources().getString(R.string.display_born_he));
        } else {
            gender.setText(getContext().getResources().getString(R.string.woman));
            born_text.setText(getContext().getResources().getString(R.string.display_born_she));
        }
        String birthday = displayedRacer.getDate_born();
        if (birthday.equalsIgnoreCase("")) {
            date_born.setText(R.string.detail_not_set);
        } else {
            date_born.setText(DateOfBirthConverter.getCzechDateString(birthday));
        }
        category.setText(displayedRacer.getCategory());

        String racerTeam = displayedRacer.getTeam();
        if (racerTeam.trim().equalsIgnoreCase("")) {
            team.setText(R.string.detail_not_set);
        } else {
            team.setText(racerTeam);
        }
        number.setText("" + displayedRacer.getNumber());
        long racerTime = displayedRacer.getTimeInSeconds();

        if(racerTime == 0) {
            if(racerGender.equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
                time.setText(R.string.not_finished_he);
            } else {
                time.setText(R.string.not_finished_she);
            }

            if(listener.isRaceTimeTrial()) {
                time_trial.setVisibility(View.VISIBLE);
                long racerStartTime = displayedRacer.getStartTime();
                if(racerStartTime == 0) {
                    start_time.setText(R.string.detail_not_set);
                } else {

                    start_time.setText((getContext().getResources().getString(R.string.plus)) + " " + TimeToText.longTimeToString(racerStartTime));
                }
            }
        } else {
            time.setText(TimeToText.longTimeToString(racerTime));
            time_trial.setVisibility(View.GONE);
        }
    }

    private void editRacer() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", this.displayedRacer.getId());
        bundle.putString("name", this.displayedRacer.getName());
        bundle.putString("surname", this.displayedRacer.getSurname());
        bundle.putString("gender", this.displayedRacer.getGender());
        bundle.putString("date_born", this.displayedRacer.getDate_born());
        bundle.putString("category", this.displayedRacer.getCategory());
        bundle.putString("team", this.displayedRacer.getTeam());
        bundle.putLong("start_time", this.displayedRacer.getStartTime());
        bundle.putLong("end_time", this.displayedRacer.getEndTime());
        bundle.putLong("time", this.displayedRacer.getTimeInSeconds());
        bundle.putInt("number", this.displayedRacer.getNumber());
        bundle.putBoolean("in_starting_list", this.displayedRacer.isInStartingList());

        EditRacerFragment fragmentEdit = new EditRacerFragment();
        fragmentEdit.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentEdit, "tag");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void deleteRacer() {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.setTargetFragment(DisplayRacerFragment.this, 1);
        Bundle bundle = new Bundle();
        bundle.putInt("racerID", displayedRacer.getId());
        bundle.putInt("racerNumber", displayedRacer.getNumber());
        bundle.putLong("racerTime", displayedRacer.getTimeInSeconds());
        deleteDialog.setArguments(bundle);
        deleteDialog.show(getActivity().getSupportFragmentManager(), "delete dialog");
    }

    @Override
    public void delete(boolean areYouSure, int racerID, int racerNumber, long racerTime) {
        if(areYouSure) {
            listener.deleteRacerFromStartingList(displayedRacer);
            goBack();

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
        if(context instanceof DisplayRacerFragment.DisplayRacerFragmentListener) {
            listener = (DisplayRacerFragment.DisplayRacerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DisplayRacerFragmentListener");
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
