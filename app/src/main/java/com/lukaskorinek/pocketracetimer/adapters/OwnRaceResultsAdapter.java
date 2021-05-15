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

public class OwnRaceResultsAdapter extends ArrayAdapter<RacerModel> {

    private Context context;
    private int resource;

    public OwnRaceResultsAdapter(@NonNull Context context, int resource, @NonNull List<RacerModel> objects) {
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

        TextView race_position = (TextView) convertView.findViewById(R.id.results_own_race_position);
        TextView number = (TextView) convertView.findViewById(R.id.results_own_race_number);
        TextView time = (TextView) convertView.findViewById(R.id.results_own_race_time);

        race_position.setText("" + (position + 1) + ".");
        if ((position + 1) == 1 || (position + 1) == 2 || (position + 1) == 3) {
            number.setTypeface(null, Typeface.BOLD);
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
        int racerNumber = racer.getNumber();
        if(racerNumber == 0) {
            number.setText(getContext().getResources().getString(R.string.number_not_filled_uppercase));
        } else {
            number.setText(number.getText() + " " + racerNumber);
        }
        time.setText(TimeToText.longTimeToString(racer.getTimeInSeconds()));

        return convertView;
    }
}
