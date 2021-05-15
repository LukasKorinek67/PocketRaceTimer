package com.lukaskorinek.pocketracetimer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.dialogs.EndRaceDialog;
import com.lukaskorinek.pocketracetimer.dialogs.NewRaceDialog;

import java.util.Calendar;

abstract public class AppFragment extends Fragment implements EndRaceDialog.EndRaceDialogListener, NewRaceDialog.NewRaceDialogListener {

    private AppFragmentListener listener;

    public interface AppFragmentListener {
        boolean isRaceRunning();
        boolean isRaceEnded();
        void endRace(Calendar endTime);
        void setNewRace();
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
        endRaceDialog.setTargetFragment(AppFragment.this, 1);
        endRaceDialog.show(getActivity().getSupportFragmentManager(), "end race dialog");
    }

    @Override
    public void endRaceYes() {
        listener.endRace(Calendar.getInstance());
        raceEnded();
    }

    private void newRace() {
        NewRaceDialog newRaceDialog = new NewRaceDialog();
        newRaceDialog.setTargetFragment(AppFragment.this, 1);
        newRaceDialog.show(getActivity().getSupportFragmentManager(), "new race dialog");
    }

    @Override
    public void newRaceYes() {
        listener.setNewRace();
        handleRaceStatus();
    }

    abstract void handleRaceStatus();
    abstract void raceEnded();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof AppFragment.AppFragmentListener) {
            listener = (AppFragment.AppFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AppFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
