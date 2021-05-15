package com.lukaskorinek.pocketracetimer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.List;

public class LoadRaceResultsAdapter extends ArrayAdapter<RacerModel> {

    private Context context;
    private int resource;

    public LoadRaceResultsAdapter(@NonNull Context context, int resource, @NonNull List<RacerModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RacerModel racer = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(this.context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView race_position = (TextView) convertView.findViewById(R.id.results_position);
        TextView name = (TextView) convertView.findViewById(R.id.results_name);
        TextView number = (TextView) convertView.findViewById(R.id.results_number);
        TextView time = (TextView) convertView.findViewById(R.id.results_time);
        TextView category = (TextView) convertView.findViewById(R.id.results_category);

        String racerName = racer.getName();
        String racerSurname = racer.getSurname();
        int racerNumber = racer.getNumber();
        long racerTime = racer.getTimeInSeconds();
        String racerCategory = racer.getCategory();

        if(racerTime <= 0) {
            race_position.setText(getContext().getResources().getString(R.string.symbol_for_no_position));
            if(racer.getGender() != null) {
                if (racer.getGender().equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))) {
                    time.setText(getContext().getResources().getString(R.string.not_finished_he));
                } else {
                    time.setText(getContext().getResources().getString(R.string.not_finished_she));
                }
            } else {
                time.setText(getContext().getResources().getString(R.string.not_finished_he));
            }
        } else {
            race_position.setText("" + (position + 1) + ".");
            if ((position + 1) == 1 || (position + 1) == 2 || (position + 1) == 3) {
                name.setTypeface(null, Typeface.BOLD);
                race_position.setTypeface(null, Typeface.BOLD);
                if ((position + 1) == 1) {
                    time.setTypeface(null, Typeface.BOLD);
                }
            }

            if ((position + 1) == 1) {
                race_position.setTextColor(getContext().getResources().getColor(R.color.gold));
            } else if ((position + 1) == 2) {
                race_position.setTextColor(getContext().getResources().getColor(R.color.silver));
            } else if ((position + 1) == 3) {
                race_position.setTextColor(getContext().getResources().getColor(R.color.bronze));
            }
            time.setText(TimeToText.longTimeToString(racerTime));
        }

        if(racerName == null && racerSurname == null) {
            if(racerNumber == 0) {
                number.setText(getContext().getResources().getString(R.string.symbol_for_no_number));
                name.setText(getContext().getResources().getString(R.string.number_not_filled_uppercase));
            } else {
                number.setText("" + racerNumber);
                name.setText(getContext().getResources().getString(R.string.unknown_racer));
            }
        } else {
            number.setText("" + racerNumber);
            name.setText(racerName + " " + racerSurname);
        }

        if(racerCategory != null) {
            if (racerCategory.equalsIgnoreCase("") || racerCategory == null) {
                category.setText(getContext().getResources().getString(R.string.none_she));
            } else {
                category.setText(racerCategory);
            }
        } else {
            category.setText(getContext().getResources().getString(R.string.none_she));
        }
        return convertView;
    }
}
