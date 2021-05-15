package com.lukaskorinek.pocketracetimer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.lukaskorinek.pocketracetimer.R;
import com.lukaskorinek.pocketracetimer.models.RaceModel;
import com.lukaskorinek.pocketracetimer.models.RacerModel;

import java.util.ArrayList;
import java.util.Calendar;

public class DAO extends SQLiteOpenHelper {

    Context context;

    public static final String RACER_TABLE = "Racer";
    public static final String RACER_COLUMN_ID = "id";
    public static final String RACER_COLUMN_NUMBER = "number";
    public static final String RACER_COLUMN_NAME = "name";
    public static final String RACER_COLUMN_SURNAME = "surname";
    public static final String RACER_COLUMN_GENDER = "gender";
    public static final String RACER_COLUMN_DATE_BORN = "date_born";
    public static final String RACER_COLUMN_CATEGORY = "category";
    public static final String RACER_COLUMN_TEAM = "team";
    public static final String RACER_COLUMN_START_TIME = "start_time";
    public static final String RACER_COLUMN_END_TIME = "end_time";
    public static final String RACER_COLUMN_TIME = "time";
    public static final String RACER_COLUMN_IN_STARTING_LIST = "in_starting_list";

    public static final String RACE_TABLE = "Race";
    public static final String RACE_COLUMN_IS_RUNNING = "is_running";
    public static final String RACE_COLUMN_START_TIME = "start_time";
    public static final String RACE_COLUMN_END_TIME = "end_time";
    public static final String RACE_COLUMN_TYPE = "race_type";

    public static final String CATEGORY_TABLE = "Category";
    public static final String CATEGORY_TABLE_NAME = "name";

    public DAO(@Nullable Context context) {
        super(context, "race_time_app_database1", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createRaceTable = "CREATE TABLE " + RACE_TABLE + "(" + RACE_COLUMN_IS_RUNNING + " INT, " + RACE_COLUMN_START_TIME + " LONG, " + RACE_COLUMN_END_TIME + " LONG, " + RACE_COLUMN_TYPE + " TEXT)";
        String createRacersTable = "CREATE TABLE " + RACER_TABLE + " (" + RACER_COLUMN_ID +
                " INTEGER unique, " + RACER_COLUMN_NUMBER + " INT, " + RACER_COLUMN_NAME +
                " TEXT, "+ RACER_COLUMN_SURNAME + " TEXT, " + RACER_COLUMN_GENDER + " TEXT, " +
                RACER_COLUMN_DATE_BORN + " TEXT, " + RACER_COLUMN_CATEGORY + " TEXT, " +
                RACER_COLUMN_TEAM + " TEXT, " + RACER_COLUMN_START_TIME + " LONG, " + RACER_COLUMN_END_TIME + " LONG, " + RACER_COLUMN_TIME + " LONG, " + RACER_COLUMN_IN_STARTING_LIST + " INT)";
        String createCategoryTable = "CREATE TABLE " + CATEGORY_TABLE + "(" + CATEGORY_TABLE_NAME + " TEXT unique)";
        sqLiteDatabase.execSQL(createRaceTable);
        sqLiteDatabase.execSQL(createRacersTable);
        sqLiteDatabase.execSQL(createCategoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addOneRacer(RacerModel racer) {
        int id;
        if(racer.getId() == 0) {
            id = 1;
            while(getRacerByID(id) != null) {
                id++;
            }

        } else {
            id = racer.getId();
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACER_COLUMN_ID, id);
        cv.put(RACER_COLUMN_NUMBER, racer.getNumber());
        cv.put(RACER_COLUMN_NAME, racer.getName());
        cv.put(RACER_COLUMN_SURNAME, racer.getSurname());
        cv.put(RACER_COLUMN_GENDER, racer.getGender());
        cv.put(RACER_COLUMN_DATE_BORN, racer.getDate_born());
        cv.put(RACER_COLUMN_CATEGORY, racer.getCategory());
        cv.put(RACER_COLUMN_TEAM, racer.getTeam());
        cv.put(RACER_COLUMN_START_TIME, racer.getStartTime());
        cv.put(RACER_COLUMN_END_TIME, racer.getEndTime());
        cv.put(RACER_COLUMN_TIME, racer.getTimeInSeconds());
        cv.put(RACER_COLUMN_IN_STARTING_LIST, racer.isInStartingList());

        db.insert(RACER_TABLE, null, cv);
    }

    public ArrayList<RacerModel> getAllRacers() {
        ArrayList<RacerModel> all_racers = new ArrayList<>();

        String queryString = "SELECT * FROM " + RACER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);

                all_racers.add(new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list));

            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();

        return all_racers;
    }

    public ArrayList<RacerModel> getAllFinishedRacers() {
        ArrayList<RacerModel> all_racers = new ArrayList<>();

        String queryString = "SELECT * FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_TIME + " != 0";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);

                all_racers.add(new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list));

            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();

        return all_racers;
    }

    public RacerModel getRacerByNumber(int number) {
        RacerModel racer = null;
        String queryString = "SELECT * FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_NUMBER + " = " + number;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int racer_number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);
                racer = new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list);
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return racer;
    }

    public RacerModel getRacerByID(int id) {
        RacerModel racer = null;
        String queryString = "SELECT * FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            do {
                int racer_id = cursor.getInt(0);
                int number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);
                racer = new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list);
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return racer;
    }

    public void updateOneRacer(RacerModel racer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACER_COLUMN_NUMBER, racer.getNumber());
        cv.put(RACER_COLUMN_NAME, racer.getName());
        cv.put(RACER_COLUMN_SURNAME, racer.getSurname());
        cv.put(RACER_COLUMN_GENDER, racer.getGender());
        cv.put(RACER_COLUMN_DATE_BORN, racer.getDate_born());
        cv.put(RACER_COLUMN_CATEGORY, racer.getCategory());
        cv.put(RACER_COLUMN_TEAM, racer.getTeam());
        cv.put(RACER_COLUMN_START_TIME, racer.getStartTime());
        cv.put(RACER_COLUMN_END_TIME, racer.getEndTime());
        cv.put(RACER_COLUMN_TIME, racer.getTimeInSeconds());
        cv.put(RACER_COLUMN_IN_STARTING_LIST, racer.isInStartingList());

        db.update(RACER_TABLE, cv, "id = " + racer.getId(), null);
    }

    public void removeRacerFinishTime(RacerModel racer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACER_COLUMN_END_TIME, 0);
        cv.put(RACER_COLUMN_TIME, 0);

        db.update(RACER_TABLE, cv, "id = " + racer.getId(), null);
    }

    public boolean deleteOneRacer(RacerModel racer) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_ID + " = " + racer.getId();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteAllRacers() {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + RACER_TABLE;
        db.execSQL(queryString);
        db.close();
    }

    public RaceModel getRace() {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "SELECT * FROM " + RACE_TABLE;
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            boolean raceInProgress = cursor.getInt(0) == 1 ? true : false;
            long raceStartTime = cursor.getLong(1);
            long endTime = cursor.getLong(2);
            String raceType = cursor.getString(3);
            return new RaceModel(raceInProgress, raceStartTime, endTime, raceType);
        } else {
            // ještě tam není žádnej závod - přidám ho tam
            ContentValues cv = new ContentValues();
            cv.put(RACE_COLUMN_IS_RUNNING, 0);
            cv.put(RACE_COLUMN_START_TIME, 0);
            cv.put(RACE_COLUMN_END_TIME, 0);
            String raceType = this.context.getResources().getString(R.string.load_race_type_values_mass_start);
            cv.put(RACE_COLUMN_TYPE, raceType);
            db.insert(RACE_TABLE, null, cv);

            return new RaceModel(false, 0, 0, raceType);
        }
    }

    public void setRaceStartTime(long startTime) {
        // uložit datum začatku (možná i uložit datum konce na 0 nebo null)
        // pokud datum už bylo - race is running změnit na true
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Calendar raceStart = Calendar.getInstance();
        raceStart.setTimeInMillis(startTime);
        Calendar now = Calendar.getInstance();
        if (raceStart.compareTo(now) < 0) {
            cv.put(RACE_COLUMN_IS_RUNNING, 1);
        }
        cv.put(RACE_COLUMN_START_TIME, startTime);
        cv.put(RACE_COLUMN_END_TIME, 0);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void setRaceIsRunning() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACE_COLUMN_IS_RUNNING, 1);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void setRaceIsNotRunning() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACE_COLUMN_IS_RUNNING, 0);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void deleteRaceStartTime() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACE_COLUMN_START_TIME, 0);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void startRace(long startTime) {
        // uložit datum začatku (možná i uložit datum konce na 0 nebo null)
        // race is running změnit na true
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(RACE_COLUMN_IS_RUNNING, 1);
        cv.put(RACE_COLUMN_START_TIME, startTime);
        cv.put(RACE_COLUMN_END_TIME, 0);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void endRace(long endTime) {
        // race is running změnit na false
        // uložit datum konce
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(RACE_COLUMN_IS_RUNNING, 0);
        cv.put(RACE_COLUMN_END_TIME, endTime);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void newRace() {
        // race is running změnit na false
        // datum začátku a konce dát na 0
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(RACE_COLUMN_IS_RUNNING, 0);
        cv.put(RACE_COLUMN_START_TIME, 0);
        cv.put(RACE_COLUMN_END_TIME, 0);
        String raceType = this.context.getResources().getString(R.string.load_race_type_values_mass_start);
        cv.put(RACE_COLUMN_TYPE, raceType);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void changeRaceType(String raceType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RACE_COLUMN_TYPE, raceType);
        // POZOR - updatuje všechny (kdybych měl víc závodů)
        db.update(RACE_TABLE, cv, null, null);
    }

    public void setStartingList(ArrayList<RacerModel> startingList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < startingList.size(); i++) {
            ContentValues cv = new ContentValues();
            RacerModel racer = startingList.get(i);
            cv.put(RACER_COLUMN_ID, racer.getId());
            cv.put(RACER_COLUMN_NUMBER, racer.getNumber());
            cv.put(RACER_COLUMN_NAME, racer.getName());
            cv.put(RACER_COLUMN_SURNAME, racer.getSurname());
            cv.put(RACER_COLUMN_GENDER, racer.getGender());
            cv.put(RACER_COLUMN_DATE_BORN, racer.getDate_born());
            cv.put(RACER_COLUMN_CATEGORY, racer.getCategory());
            cv.put(RACER_COLUMN_TEAM, racer.getTeam());
            cv.put(RACER_COLUMN_START_TIME, racer.getStartTime());
            cv.put(RACER_COLUMN_END_TIME, racer.getEndTime());
            cv.put(RACER_COLUMN_TIME, racer.getTimeInSeconds());
            cv.put(RACER_COLUMN_IN_STARTING_LIST, 1);
            db.insert(RACER_TABLE, null, cv);
        }
    }

    public ArrayList<RacerModel> getStartingList() {
        ArrayList<RacerModel> startingList = new ArrayList<>();
        String queryString = "SELECT * FROM " + RACER_TABLE +  " WHERE " + RACER_COLUMN_IN_STARTING_LIST + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);
                startingList.add(new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list));
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return startingList;
    }

    public void deleteStartingList() {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + RACER_TABLE +  " WHERE " + RACER_COLUMN_IN_STARTING_LIST + " = 1";
        db.execSQL(queryString);
        db.close();
    }

    public RacerModel getRacerFromStartingListByNumber(int number) {
        RacerModel racer = null;
        String queryString = "SELECT * FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_NUMBER + " = " + number + " AND " + RACER_COLUMN_IN_STARTING_LIST + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int racer_number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);
                racer = new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list);
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return racer;
    }

    public void setRaceCategories(ArrayList<String> categories) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < categories.size(); i++) {
            ContentValues cv = new ContentValues();
            String category = categories.get(i);
            cv.put(CATEGORY_TABLE_NAME, category);
            db.insert(CATEGORY_TABLE, null, cv);
        }
    }

    public void deleteRaceCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CATEGORY_TABLE;
        db.execSQL(queryString);
        db.close();
    }

    public void deleteOneRaceCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CATEGORY_TABLE +  " WHERE " + CATEGORY_TABLE_NAME + " = '" + category + "'";
        db.execSQL(queryString);
        db.close();
    }

    public void addRaceCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY_TABLE_NAME, category);
        db.insert(CATEGORY_TABLE, null, cv);
    }

    public ArrayList<String> getRaceCategories() {
        ArrayList<String> categories = new ArrayList<>();
        String queryString = "SELECT * FROM " + CATEGORY_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return categories;
    }

    public ArrayList<RacerModel> getRacersFromStartingListByCategory(String category) {
        ArrayList<RacerModel> racers = new ArrayList<>();
        String queryString = "SELECT * FROM " + RACER_TABLE + " WHERE " + RACER_COLUMN_CATEGORY + " = '" + category + "' AND " + RACER_COLUMN_IN_STARTING_LIST + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int number = cursor.getInt(1);
                String name = cursor.getString(2);
                String surname = cursor.getString(3);
                String gender = cursor.getString(4);
                String date_born = cursor.getString(5);
                String racer_category = cursor.getString(6);
                String team = cursor.getString(7);
                long start_time = cursor.getLong(8);
                long end_time = cursor.getLong(9);
                long time_in_seconds = cursor.getLong(10);
                boolean in_starting_list = (cursor.getInt(11) == 1);
                racers.add(new RacerModel(id, number, name, surname, gender, date_born, racer_category, team, start_time, end_time, time_in_seconds, in_starting_list));
            } while (cursor.moveToNext());
        } else {
            // do nothing
        }
        cursor.close();
        db.close();
        return racers;
    }
}
