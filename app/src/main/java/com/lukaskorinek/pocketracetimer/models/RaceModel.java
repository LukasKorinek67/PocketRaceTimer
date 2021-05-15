package com.lukaskorinek.pocketracetimer.models;

public class RaceModel {

    boolean raceInProgess;
    long raceStartTime;
    long endTime;
    String raceType;

    public RaceModel(boolean raceInProgess, long raceStartTime, long endTime, String raceType) {
        this.raceInProgess = raceInProgess;
        this.raceStartTime = raceStartTime;
        this.endTime = endTime;
        this.raceType = raceType;
    }

    public boolean isRaceInProgess() {
        return raceInProgess;
    }

    public long getRaceStartTime() {
        return raceStartTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getRaceType() {
        return raceType;
    }

    public void setRaceInProgess(boolean raceInProgess) {
        this.raceInProgess = raceInProgess;
    }

    public void setRaceStartTime(long raceStartTime) {
        this.raceStartTime = raceStartTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setRaceType(String raceType) {
        this.raceType = raceType;
    }
}
