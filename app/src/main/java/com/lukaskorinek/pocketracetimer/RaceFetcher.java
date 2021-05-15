package com.lukaskorinek.pocketracetimer;

import android.util.Log;

import com.lukaskorinek.pocketracetimer.models.RacerModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class RaceFetcher {

    private String raceName;
    private String raceType;
    private String discipline;
    private ArrayList<RacerModel> startingList;
    private ArrayList<String> categories;
    private Calendar startingTime;
    private final String URL = "https://www.sportchallenge.cz/ws/getStartlist?zavod=";

    public RaceFetcher(String packetId) {
        String raceId = getRaceIdFromPacketId(packetId);
        String fullUrl =  URL + raceId;
        Log.i("FULL URL", "" + fullUrl);
        String data = fetchData(fullUrl);
        if(data != null) {
            Log.i("DATA", "" + data);
            setAttributes(data, packetId);
        } else {
            raceName = null;
            raceType = null;
            discipline = null;
            startingList = null;
            categories = null;
            startingTime = null;
        }
    }

    private String getRaceIdFromPacketId(String packetId) {
        return packetId.substring(0,4);
    }

    private void setAttributes(String data, String packetId) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONObject race = new JSONObject(jsonObject.getString("zavod"));

            if(getRaceIdFromPacketId(packetId).equalsIgnoreCase(race.getString("id"))) {
                this.raceName = race.getString("nazev");
                this.raceType = race.getString("typ_zavodu");
                this.discipline = race.getString("disciplina");
                String date = race.getString("datum");
                String time = "";
                JSONObject race_startovne = new JSONObject(race.getString("startovne"));

                JSONObject packet = new JSONObject(race_startovne.getString(packetId));
                time = packet.getString("cas_start");
                String dateAndTime = date + " " + time;
                Calendar start = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                try {
                    start.setTime(sdf.parse(dateAndTime));
                    this.startingTime = start;
                } catch (ParseException e) {
                    Calendar errorStart = Calendar.getInstance();
                    // udělat tady něco když se nepovede načíst?
                    this.startingTime = errorStart;
                }

                ArrayList<RacerModel> racers = new ArrayList<>();
                this.categories = new ArrayList<>();
                if (!(packet.getString("startovka").equalsIgnoreCase("[]"))) {
                    JSONObject race_starting_list = new JSONObject(packet.getString("startovka"));

                    Iterator<String> iter = race_starting_list.keys();
                    while (iter.hasNext()) {
                        String key_startovka = iter.next();
                        JSONObject racer = new JSONObject(race_starting_list.getString(key_startovka));
                        long time_in_seconds = 0;
                        boolean in_starting_list = true;
                        String category = racer.getString("kategorie");

                        long racerStartTime;
                        //String racerStartTime;
                        String time_trial_start = racer.getString("cas_start_casovka");
                        if (time_trial_start.trim().equalsIgnoreCase("")) {
                            racerStartTime = 0;
                            //racerStartTime = "";
                        } else {

                            //
                            String raceStartTimeHours = time.substring(0,2);
                            String raceStartTimeMinutes = time.substring(3,5);
                            String raceStartTimeSeconds = time.substring(6,8);
                            Calendar raceStart = Calendar.getInstance();
                            raceStart.set(raceStart.get(Calendar.YEAR), raceStart.get(Calendar.MONTH),
                                    raceStart.get(Calendar.DAY_OF_MONTH), Integer.parseInt(raceStartTimeHours), Integer.parseInt(raceStartTimeMinutes), Integer.parseInt(raceStartTimeSeconds));

                            String startTimeHours = time_trial_start.substring(0,2);
                            String startTimeMinutes = time_trial_start.substring(3,5);
                            String startTimeSeconds = time_trial_start.substring(6,8);
                            Calendar racerStart = Calendar.getInstance();
                            racerStart.set(raceStart.get(Calendar.YEAR), raceStart.get(Calendar.MONTH),
                                    raceStart.get(Calendar.DAY_OF_MONTH), Integer.parseInt(startTimeHours), Integer.parseInt(startTimeMinutes), Integer.parseInt(startTimeSeconds));

                            if (racerStart.compareTo(raceStart) < 0) {
                                racerStart.add(Calendar.DATE, 1);
                            }

                            long difference = (racerStart.getTimeInMillis()/1000) - (raceStart.getTimeInMillis()/1000);

                            //racerStartTime = (Long.parseLong(startTimeHours)*3600) + (Long.parseLong(startTimeMinutes)*60) + Long.parseLong(startTimeSeconds);
                            racerStartTime = difference;
                            //

                            //racerStartTime = time_trial_start;
                        }
                        long endTime = 0;
                        RacerModel new_racer = new RacerModel(racer.getInt("id"), racer.getInt("cislo"), racer.getString("jmeno"), racer.getString("prijmeni"), racer.getString("pohlavi"), racer.getString("narozen"), category, racer.getString("team_text"), racerStartTime, endTime, time_in_seconds, in_starting_list);
                        racers.add(new_racer);
                        addCategory(category);
                    }
                } else {
                    Log.i("PRÁZDNÁ STARTOVKA", "[]");
                }
                this.startingList = racers;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            //return null;
        }
    }

    private void addCategory(String category) {
        if(this.categories != null) {
            if(!(this.categories.contains(category))) {
                this.categories.add(category);
            }
        } else {
            this.categories = new ArrayList<>();
            this.categories.add(category);
        }
    }

    public String getRaceName() {
        return raceName;
    }

    public ArrayList<RacerModel> getStartingList() {
        return startingList;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public Calendar getStartingTime() {
        return startingTime;
    }

    public String getRaceType() {
        return raceType;
    }

    public String getDiscipline() {
        return discipline;
    }

    private String fetchData(String url_s) {
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(url_s);
            urlConnection = (HttpURLConnection) url.openConnection();
            //
            InputStream in = urlConnection.getInputStream();
            //
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();

            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
