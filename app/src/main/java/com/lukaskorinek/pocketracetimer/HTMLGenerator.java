package com.lukaskorinek.pocketracetimer;

import android.content.Context;

import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.ArrayList;
import java.util.Collections;

public class HTMLGenerator {


    private Context context;

    public HTMLGenerator(Context context) {
        this.context = context;
    }

    private StringBuilder appendHeader(StringBuilder data) {
        data.append("<!DOCTYPE html>\n");
        data.append("<html lang=\"cs-cz\">\n\n");

        data.append("<head>\n");
        data.append("<meta charset=\"utf-8\" />\n");
        data.append("<meta name=\"description\" content=\"Výsledky závodu\" />\n");
        data.append("<meta name=\"keywords\" content=\"results, vysledky, zavod, race\" />\n");
        data.append("<title>");
        data.append(this.context.getResources().getString(R.string.race_results));
        data.append("</title>\n");
        data.append("</head>\n\n");

        data.append("<body>\n\n");

        return data;
    }

    private StringBuilder appendEnd(StringBuilder data) {
        data.append("</body>\n");
        data.append("</html>\n");
        return data;
    }

    private StringBuilder addStyle(StringBuilder data) {
        data.append("<style>\n");
        data.append("body {padding-top: 20px;\npadding-right: 60px;\npadding-bottom: 20px;\npadding-left: 60px;\nfont-family: Arial, Helvetica, sans-serif;}");
        data.append("h1 {color: black;\ntext-align: center;\nfont-size:250%;}\n");
        data.append("thead {background-color: black;\ncolor: white;\nfont-weight: bold;}\n");
        data.append("th, td {padding-top: 5px;\npadding-right: 15px;\npadding-bottom: 5px;\npadding-left: 15px;\n text-align: center;\nborder-bottom: 1px solid #ddd;}\n");
        data.append("</style>\n");
        return data;
    }

    public String getHtmlData(ArrayList<RacerModel> racers, ArrayList<String> categories, int nOfRacersInStartingList) {
        StringBuilder data = new StringBuilder();
        data = appendHeader(data);
        data = addStyle(data);
        data.append("<h1>");
        data.append(this.context.getResources().getString(R.string.race_results));
        data.append("</h1>\n");
        data.append("<br>\n");


        if(nOfRacersInStartingList == 0) {
            data.append("<table>\n");

            data.append("<thead>\n");
            data.append("<tr>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_place)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_number)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_time)).append("</td>\n");
            data.append("</tr>\n");
            data.append("</thead>\n");

            for (int i = 0; i < racers.size(); i++) {
                if(racers.get(i).getNumber() != 0) {
                    int position = i + 1;
                    int number = racers.get(i).getNumber();
                    String time = TimeToText.longTimeToString(racers.get(i).getTimeInSeconds());
                    data.append("<tr>");
                    data.append("<td>").append(String.valueOf(position)+ ".").append("</td>\n");
                    data.append("<td>").append(context.getResources().getString(R.string.race_number_add) + " " + String.valueOf(number)).append("</td>\n");
                    data.append("<td>").append(time).append("</td>\n");
                    data.append("</tr>\n");
                }
            }
            data.append("</table>\n");
        } else {
            Collections.sort(categories);
            for (int i = 0; i < categories.size(); i++) {
                int position = 0;
                data.append("<h2>");
                data.append(categories.get(i));
                data.append("</h2>\n");


                data.append("<table>\n");

                data.append("<thead>\n");
                data.append("<tr>\n");
                data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_place)).append("</td>\n");
                data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_number)).append("</td>\n");
                data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_name)).append("</td>\n");
                data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_time)).append("</td>\n");
                data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_team)).append("</td>\n");
                data.append("</tr>\n");
                data.append("</thead>\n");

                for (int j = 0; j < racers.size(); j++) {
                    if((racers.get(j).getNumber() != 0) && (!(racers.get(j).getCategory() == null)) && (racers.get(j).getCategory().equalsIgnoreCase(categories.get(i)))) {
                        int number = racers.get(j).getNumber();
                        String name = racers.get(j).getName() + " " + racers.get(j).getSurname();
                        String team = racers.get(j).getTeam();
                        if (team.trim().equalsIgnoreCase("")) {
                            team = this.context.getResources().getString(R.string.team_not_filled);
                        }
                        long racerTime = racers.get(j).getTimeInSeconds();
                        data.append("<tr>");
                        if(racerTime != 0) {
                            position++;
                            String time = TimeToText.longTimeToString(racerTime);
                            data.append("<td>").append(String.valueOf(position)+ ".").append("</td>\n");
                            data.append("<td>").append(String.valueOf(number)).append("</td>\n");
                            data.append("<td>").append(name).append("</td>\n");
                            data.append("<td>").append(time).append("</td>\n");
                            data.append("<td>").append(team).append("</td>\n");
                        } else {
                            data.append("<td>").append(this.context.getResources().getString(R.string.symbol_for_no_position)).append("</td>\n");
                            data.append("<td>").append(String.valueOf(number)).append("</td>\n");
                            data.append("<td>").append(name).append("</td>\n");
                            data.append("<td>").append("-").append("</td>\n");
                            data.append("<td>").append(team).append("</td>\n");
                        }
                        data.append("</tr>\n");
                    }
                }
                data.append("</table>\n");
                data.append("<br>\n");
                data.append("\n\n");
            }
            data.append("<h2>");
            data.append(this.context.getResources().getString(R.string.csv_all_racers) + "\n");
            data.append("</h2>\n");

            data.append("<table>\n");

            data.append("<thead>\n");
            data.append("<tr>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_place)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_number)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_name)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_time)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_category)).append("</td>\n");
            data.append("<td>").append(this.context.getResources().getString(R.string.csv_export_team)).append("</td>\n");
            data.append("</tr>\n");
            data.append("</thead>\n");

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
                    data.append("<tr>\n");
                    if (racerTime != 0) {
                        allRacersPostion++;
                        String time = TimeToText.longTimeToString(racerTime);
                        data.append("<td>").append(String.valueOf(allRacersPostion)+ ".").append("</td>\n");
                        data.append("<td>").append(String.valueOf(number)).append("</td>\n");
                        data.append("<td>").append(name).append("</td>\n");
                        data.append("<td>").append(time).append("</td>\n");
                        data.append("<td>").append(category).append("</td>\n");
                        data.append("<td>").append(team).append("</td>\n");
                    } else {
                        data.append("<td>").append(this.context.getResources().getString(R.string.symbol_for_no_position)).append("</td>\n");
                        data.append("<td>").append(String.valueOf(number)).append("</td>\n");
                        data.append("<td>").append(name).append("</td>\n");
                        data.append("<td>").append("-").append("</td>\n");
                        data.append("<td>").append(category).append("</td>\n");
                        data.append("<td>").append(team).append("</td>\n");
                    }
                    data.append("</tr>\n");
                }
            }
            data.append("</table>\n");
            data.append("<br>\n");
        }
        data.append("\n\n");

        data = appendEnd(data);
        return data.toString();

    }
}
