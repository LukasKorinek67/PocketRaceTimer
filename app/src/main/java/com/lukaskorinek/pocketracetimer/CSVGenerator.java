package com.lukaskorinek.pocketracetimer;

import android.content.Context;

import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.ArrayList;
import java.util.Collections;

public class CSVGenerator {

    private Context context;

    public CSVGenerator(Context context) {
        this.context = context;
    }

    public String getCsvData(ArrayList<RacerModel> racers, ArrayList<String> categories, int nOfRacersInStartingList) {
        StringBuilder data = new StringBuilder();
        String header = (this.context.getResources().getString(R.string.csv_export_place) + "," +
                this.context.getResources().getString(R.string.csv_export_number) + "," +
                this.context.getResources().getString(R.string.csv_export_name) + "," +
                this.context.getResources().getString(R.string.csv_export_time) + "," +
                this.context.getResources().getString(R.string.csv_export_team) + "\n");

        String headerWithoutName = (this.context.getResources().getString(R.string.csv_export_place) + "," +
                this.context.getResources().getString(R.string.csv_export_number) + "," +
                this.context.getResources().getString(R.string.csv_export_time) + "\n");

        String headerWithCategory = (this.context.getResources().getString(R.string.csv_export_place) + "," +
                this.context.getResources().getString(R.string.csv_export_number) + "," +
                this.context.getResources().getString(R.string.csv_export_name) + "," +
                this.context.getResources().getString(R.string.csv_export_time) + "," +
                this.context.getResources().getString(R.string.csv_export_category) + "," +
                this.context.getResources().getString(R.string.csv_export_team) + "\n");

        if(nOfRacersInStartingList == 0) {
            data.append(headerWithoutName);
            for (int i = 0; i < racers.size(); i++) {
                if(racers.get(i).getNumber() != 0) {
                    int position = i + 1;
                    int number = racers.get(i).getNumber();
                    String time = TimeToText.longTimeToString(racers.get(i).getTimeInSeconds());
                    data.append(String.valueOf(position) + "," + String.valueOf(number) + "," + time + "\n");
                }
            }
        } else {
            Collections.sort(categories);
            for (int i = 0; i < categories.size(); i++) {
                int position = 0;
                data.append(categories.get(i) + "\n");
                data.append(header);
                for (int j = 0; j < racers.size(); j++) {
                    if((racers.get(j).getNumber() != 0) && (!(racers.get(j).getCategory() == null)) && (racers.get(j).getCategory().equalsIgnoreCase(categories.get(i)))) {
                        int number = racers.get(j).getNumber();
                        String name = racers.get(j).getName() + " " + racers.get(j).getSurname();
                        String team = racers.get(j).getTeam();
                        long racerTime = racers.get(j).getTimeInSeconds();
                        if(racerTime != 0) {
                            position++;
                            String time = TimeToText.longTimeToString(racerTime);
                            data.append(String.valueOf(position) + "," + String.valueOf(number) + "," + name + "," + time + "," + team + "\n");
                        } else {
                            data.append(this.context.getResources().getString(R.string.symbol_for_no_position) + "," + String.valueOf(number) + "," + name + "," + "-" + "," + team + "\n");
                        }
                    }
                }
                data.append("\n\n");
            }
            data.append(this.context.getResources().getString(R.string.csv_all_racers) + "\n");
            data.append(headerWithCategory);
            int allRacersPostion = 0;
            for (int i = 0; i < racers.size(); i++) {
                if(racers.get(i).getNumber() != 0) {
                    int number = racers.get(i).getNumber();
                    String firstName = racers.get(i).getName();
                    String secondName = racers.get(i).getSurname();
                    String name;
                    if (firstName == null || secondName == null) {
                        name = context.getResources().getString(R.string.unknown_racer);
                    } else {
                        name = racers.get(i).getName() + " " + racers.get(i).getSurname();
                    }
                    String category = racers.get(i).getCategory();
                    if (category == null) {
                        category = context.getResources().getString(R.string.category_none);
                    }
                    String team = racers.get(i).getTeam();
                    if (team == null) {
                        team = context.getResources().getString(R.string.team_none);
                    }
                    if (team.trim().equalsIgnoreCase("")) {
                        team = this.context.getResources().getString(R.string.team_not_filled);
                    }
                    long racerTime = racers.get(i).getTimeInSeconds();
                    if (racerTime != 0) {
                        allRacersPostion++;
                        String time = TimeToText.longTimeToString(racerTime);
                        data.append(String.valueOf(allRacersPostion) + "," + String.valueOf(number) + "," + name + "," + time + "," + category + "," + team + "\n");
                    } else {
                        data.append(this.context.getResources().getString(R.string.symbol_for_no_position) + "," + String.valueOf(number) + "," + name + "," + "-" + "," + category + "," + team + "\n");
                    }
                }
            }
        }
        data.append("\n\n");
        return data.toString();
    }
}
