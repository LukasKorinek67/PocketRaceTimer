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
import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.List;

public class TimeTrialFinishAdapter extends ArrayAdapter<RacerModel> {

    private Context context;
    private int resource;

    public TimeTrialFinishAdapter(@NonNull Context context, int resource, @NonNull List<RacerModel> objects) {
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

        TextView number = (TextView) convertView.findViewById(R.id.timeTrialNumber);
        TextView name = (TextView) convertView.findViewById(R.id.timeTrialName);
        TextView totalTime = (TextView) convertView.findViewById(R.id.timeTrialTotalTime);
        TextView startTime = (TextView) convertView.findViewById(R.id.timeTrialStartTime);
        TextView endTime = (TextView) convertView.findViewById(R.id.timeTrialEndTime);


        int racerNumber = racer.getNumber();
        String racerName = racer.getName();
        String racerSurname = racer.getSurname();

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
        totalTime.setText(TimeToText.longTimeToString(racer.getTimeInSeconds()));
        startTime.setText(TimeToText.longTimeToString(racer.getStartTime()));
        //startTime.setText("+" + TimeToText.longTimeToString(racer.getStartTime()));
        endTime.setText(TimeToText.longTimeToString(racer.getEndTime()));

        return convertView;
    }
}