package com.lukaskorinek.pocketracetimer.models;

import java.util.Comparator;

public class RacerModel implements Comparable<RacerModel>{
    private int id;
    private int number;
    private String name;
    private String surname;
    private String gender;
    private String date_born;
    private String category;
    private String team;
    private long start_time;
    private long end_time;
    private long time_in_seconds;
    private boolean in_starting_list;

    public RacerModel(int id, int number, String name, String surname, String gender, String date_born, String category, String team, long start_time, long end_time, long time_in_seconds, boolean in_starting_list) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.date_born = date_born;
        this.category = category;
        this.team = team;
        this.start_time = start_time;
        this.end_time = end_time;
        this.time_in_seconds = time_in_seconds;
        this.in_starting_list = in_starting_list;
    }

    public RacerModel(int id, int number, long time_in_seconds) {
        this.id = id;
        this.number = number;
        this.time_in_seconds = time_in_seconds;
    }

    public RacerModel(int number, long time_in_seconds, long end_time) {
        this.number = number;
        this.time_in_seconds = time_in_seconds;
        this.end_time = end_time;
    }

    public RacerModel(long time_in_seconds, long end_time) {
        this.time_in_seconds = time_in_seconds;
        this.end_time = end_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate_born() {
        return date_born;
    }

    public void setDate_born(String date_born) {
        this.date_born = date_born;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public long getStartTime() {
        return start_time;
    }

    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }

    public long getEndTime() {
        return end_time;
    }

    public void setEndTime(long end_time) {
        this.end_time = end_time;
    }

    public void setTimeInSeconds(long time_in_seconds) {
        this.time_in_seconds = time_in_seconds;
    }

    public long getTimeInSeconds() {
        return time_in_seconds;
    }



    public boolean isInStartingList() {
        return in_starting_list;
    }

    public void setInStartingList(boolean in_starting_list) {
        this.in_starting_list = in_starting_list;
    }

    @Override
    public int compareTo(RacerModel racer) {
        return this.number - racer.getNumber();
    }

    public static Comparator<RacerModel> RaceNumberComparator = new Comparator<RacerModel>() {

        public int compare(RacerModel racer1, RacerModel racer2) {

            int racer1Number = racer1.getNumber();
            int racer2Number = racer2.getNumber();

            return racer1Number-racer2Number;
        }

    };

    public static Comparator<RacerModel> RaceTimeComparatorDesc = new Comparator<RacerModel>() {

        public int compare(RacerModel racer1, RacerModel racer2) {

            long racer1Time = racer1.getTimeInSeconds();
            long racer2Time = racer2.getTimeInSeconds();

            if(racer1Time < racer2Time){
                return 1;
            } else if(racer1Time == racer2Time) {
                return 0;
            } else {
                return -1;
            }
        }
    };

    public static Comparator<RacerModel> RaceTimeComparatorAsc = new Comparator<RacerModel>() {

        public int compare(RacerModel racer1, RacerModel racer2) {

            long racer1Time = racer1.getTimeInSeconds();
            long racer2Time = racer2.getTimeInSeconds();

            if(racer1Time > racer2Time){
                return 1;
            } else if(racer1Time == racer2Time) {
                return 0;
            } else {
                return -1;
            }
        }
    };

    public static Comparator<RacerModel> RaceTimeComparatorAscUnfinishedLast = new Comparator<RacerModel>() {

        public int compare(RacerModel racer1, RacerModel racer2) {

            long racer1Time = racer1.getTimeInSeconds();
            long racer2Time = racer2.getTimeInSeconds();

            if(racer1Time == 0 && racer2Time == 0) {
                return racer1.compareTo(racer2);
            } else if(racer1Time == 0 && racer2Time != 0) {
                return 1;
            } else if(racer1Time != 0 && racer2Time == 0) {
                return -1;
            } else {
                if (racer1Time > racer2Time) {
                    return 1;
                } else if (racer1Time == racer2Time) {
                    return racer1.compareTo(racer2);
                } else {
                    return -1;
                }
            }
        }
    };
}
