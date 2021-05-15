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

public class StartingListAdapter extends ArrayAdapter<RacerModel> {

    private Context context;
    private int resource;

    public StartingListAdapter(@NonNull Context context, int resource, @NonNull List<RacerModel> objects) {
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

        TextView number = (TextView) convertView.findViewById(R.id.textViewNumber);
        TextView name = (TextView) convertView.findViewById(R.id.textViewName);
        TextView category = (TextView) convertView.findViewById(R.id.textViewCategory);
        TextView time = (TextView) convertView.findViewById(R.id.textViewTime);

        number.setText("" + racer.getNumber());
        name.setText(racer.getName() + " " + racer.getSurname());
        category.setText(racer.getCategory());
        long racerTime = racer.getTimeInSeconds();
        if(racerTime > 0) {
            time.setText(TimeToText.longTimeToString(racerTime));
        } else {
            if(racer.getGender().equalsIgnoreCase(getContext().getResources().getString(R.string.man_symbol))){
                time.setText(getContext().getResources().getString(R.string.not_finished_he));
            } else {
                time.setText(getContext().getResources().getString(R.string.not_finished_she));
            }
        }

        return convertView;
    }
}
