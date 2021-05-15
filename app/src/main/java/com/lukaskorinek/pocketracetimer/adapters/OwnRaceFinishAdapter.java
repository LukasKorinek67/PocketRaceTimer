package com.lukaskorinek.pocketracetimer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.util.List;

public class OwnRaceFinishAdapter extends ArrayAdapter<RacerModel> {

    private Context context;
    private int resource;

    public OwnRaceFinishAdapter(@NonNull Context context, int resource, @NonNull List<RacerModel> objects) {
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

        TextView number = (TextView) convertView.findViewById(R.id.textViewOwnRaceNumber);
        TextView time = (TextView) convertView.findViewById(R.id.textViewOwnRaceTime);


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