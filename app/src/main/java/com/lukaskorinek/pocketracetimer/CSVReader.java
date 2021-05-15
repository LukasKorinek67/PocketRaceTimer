package com.lukaskorinek.pocketracetimer;

import android.content.Context;
import android.util.Log;

import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.DateOfBirthConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CSVReader {
    Context context;
    private ArrayList<RacerModel> startingList;
    private ArrayList<String> categories = new ArrayList<>();


    public CSVReader(Context context, BufferedReader reader) throws Exception {
        this.context = context;
        ArrayList<String[]> allInfo = read(reader);
        setAttributes(allInfo);
    }

    public CSVReader(Context context, FileReader file) throws Exception {
        this.context = context;
        BufferedReader reader = new BufferedReader(file);
        ArrayList<String[]> allInfo = read(reader);
        setAttributes(allInfo);
    }

    private void setAttributes(ArrayList<String[]> allInfo) throws Exception {
        ArrayList<RacerModel> startingList = new ArrayList<>();

        String[] columns = allInfo.get(0);
        for (int j = 0; j < columns.length; j++) {
            columns[j] = columns[j].trim();
            columns[j] = columns[j].replaceAll("[\uFEFF-\uFFFF]", "");
        }

        for (int i = 0; i < allInfo.size(); i++) {
            if(i != 0) {
                int id = 0;
                int number;
                String name;
                String surname;
                String gender;
                String date_born;
                String category;
                String team;
                long start_time;
                long end_time = 0;
                long time_in_seconds = 0;
                boolean in_starting_list = true;

                int numberIndex = getNumberIndex(columns);
                int nameIndex = getNameIndex(columns);
                int surnameIndex = getSurnameIndex(columns);
                int genderIndex = getGenderIndex(columns);
                int date_bornIndex = getDateBornIndex(columns);
                int categoryIndex = getCategoryIndex(columns);
                int teamIndex = getTeamIndex(columns);
                int start_timeIndex = getStartTimeIndex(columns);

                if(numberIndex == -1) {
                    throw new Exception(this.context.getResources().getString(R.string.csv_import_error_number));
                } else {
                    try{
                        number = Integer.parseInt(allInfo.get(i)[numberIndex]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if(nameIndex == -1) {
                    throw new Exception(this.context.getResources().getString(R.string.csv_import_error_name));
                } else {
                    name = allInfo.get(i)[nameIndex];
                    if (name.equalsIgnoreCase("")) {
                        continue;
                    }
                }

                if(surnameIndex == -1) {
                    throw new Exception(this.context.getResources().getString(R.string.csv_import_error_surname));
                } else {
                    surname = allInfo.get(i)[surnameIndex];
                    if (surname.equalsIgnoreCase("")) {
                        continue;
                    }
                }

                if(genderIndex == -1) {
                    throw new Exception(this.context.getResources().getString(R.string.csv_import_error_gender));
                } else {
                    gender = allInfo.get(i)[genderIndex];
                    if (gender.equalsIgnoreCase("")) {
                        continue;
                    } else if(gender.equalsIgnoreCase("muž") || gender.equalsIgnoreCase("m") ||
                            gender.equalsIgnoreCase("muz") || gender.equalsIgnoreCase("man")) {
                        gender = this.context.getResources().getString(R.string.man_symbol);
                    } else {
                        gender = this.context.getResources().getString(R.string.woman_symbol);
                    }
                }

                if(date_bornIndex == -1) {
                    date_born = "";
                } else {
                    date_born = allInfo.get(i)[date_bornIndex];
                    try {
                        date_born = DateOfBirthConverter.getEnglishDateString(date_born);
                    } catch (Exception e) {
                        date_born = "";
                    }
                }

                if(categoryIndex == -1) {
                    throw new Exception(this.context.getResources().getString(R.string.csv_import_error_category));
                    //continue;
                } else {
                    category = allInfo.get(i)[categoryIndex];
                    if (category.equalsIgnoreCase("")) {
                        continue;
                    }
                }

                if (teamIndex == -1) {
                    team = "";
                } else {
                    team = allInfo.get(i)[teamIndex];
                }

                if (start_timeIndex == -1) {
                    start_time = 0;
                } else {
                    String start_time_text = allInfo.get(i)[start_timeIndex];
                    if (start_time_text.equalsIgnoreCase("")) {
                        start_time = 0;
                    } else {
                        try{
                            start_time = Long.parseLong(start_time_text);
                        } catch (NumberFormatException e) {
                            start_time = 0;
                        }
                    }
                }
                startingList.add(new RacerModel(id, number, name, surname, gender, date_born, category, team, start_time, end_time, time_in_seconds, in_starting_list));
                addCategory(category);
            }
        }
        this.startingList = startingList;
    }

    public ArrayList<RacerModel> getStartingList() {
        return this.startingList;
    }

    public ArrayList<String> getCategories() {
        return this.categories;
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

    public ArrayList<String[]> read(BufferedReader reader) {
        String line;
        ArrayList<String[]> allInfo = new ArrayList<>();
        try {
            while((line = reader.readLine()) != null) {
                Log.i("CSV FILE", line);
                String[] racerInfo = line.split(";", -1);
                allInfo.add(racerInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allInfo;
    }

    private int getIndex(String[] columns, String[] possibleWords) {
        int index = -1;
        for (int i = 0; i < possibleWords.length; i++) {
            index = Arrays.asList(columns).indexOf(possibleWords[i]);
            if (index >= 0) {
                return index;
            }
        }
        return index;
    }

    private int getNumberIndex(String[] columns) {
        String[] possibleWords = new String[]{"Číslo", "číslo", "ČÍSLO", "CISLO", "cislo", "Cislo", "Number", "number", "NUMBER"};
        return getIndex(columns, possibleWords);
    }

    private int getNameIndex(String[] columns) {
        String[] possibleWords = new String[]{"Jméno", "Jmeno", "JMÉNO", "JMENO", "jmeno", "jméno", "Name", "name", "NAME"};
        return getIndex(columns, possibleWords);
    }

    private int getSurnameIndex(String[] columns) {
        String[] possibleWords = new String[]{"Příjmení", "příjmení", "PŘÍJMENÍ", "Prijmeni", "prijmeni", "PRIJMENI", "Surname", "surname", "SURNAME"};
        return getIndex(columns, possibleWords);
    }

    private int getGenderIndex(String[] columns) {
        String[] possibleWords = new String[]{"Pohlaví", "pohlaví", "POHLAVÍ", "POHLAVI", "pohlavi", "Pohlavi", "Gender", "gender", "GENDER"};
        return getIndex(columns, possibleWords);
    }

    private int getDateBornIndex(String[] columns) {
        String[] possibleWords = new String[]{"Datum narození", "Datum Narození", "datum narození", "datum Narození", "DATUM NAROZENÍ",
                "Datum narozeni", "Datum Narozeni", "datum narozeni", "datum Narozeni", "DATUM NAROZENI",
                "Narození", "narození", "NAROZENÍ", "Narozeni", "narozeni", "NAROZENI",
                "Narozen", "narozen", "NAROZEN", "Narozena", "narozena", "NAROZENA",
                "Date of birth", "Date Of Birth", "Date of Birth", "date of birth", "DATE OF BIRTH",
                "Birthday", "birthday", "BIRTHDAY", "Birth day", "birth day", "BIRTH DAY", "Birth Day", "birth Day",
                "Datum_narození", "Datum_Narození", "datum_narození", "datum_Narození", "DATUM_NAROZENÍ",
                "Datum_narozeni", "Datum_Narozeni", "datum_narozeni", "datum_Narozeni", "DATUM_NAROZENI",
                "Date_of_birth", "Date_Of_Birth", "Date_of_Birth", "date_of_birth", "DATE_OF_BIRTH",
                "Birth_day", "birth_day", "BIRTH_DAY", "Birth_Day", "birth_Day"
        };
        return getIndex(columns, possibleWords);
    }

    private int getCategoryIndex(String[] columns) {
        String[] possibleWords = new String[]{"Kategorie", "kategorie", "KATEGORIE", "Category", "category", "CATEGORY", "Kategory", "kategory", "KATEGORY"};
        return getIndex(columns, possibleWords);
    }

    private int getTeamIndex(String[] columns) {
        String[] possibleWords = new String[]{"Tým", "tým", "TÝM", "Tym", "tym", "TYM", "Team", "team", "TEAM"};
        return getIndex(columns, possibleWords);
    }

    private int getStartTimeIndex(String[] columns) {
        String[] possibleWords = new String[]{"Čas startu", "Čas Startu", "čas Startu", "čas startu", "ČAS STARTU",
                "Cas startu", "Cas Startu", "cas Startu", "cas startu", "CAS STARTU",
                "Start time", "Start Time", "start Time", "START TIME",
                "Čas_startu", "Čas_Startu", "čas_Startu", "čas_startu", "ČAS_STARTU",
                "Cas_startu", "Cas_Startu", "cas_Startu", "cas_startu", "CAS_STARTU",
                "Start_time", "Start_Time", "start_Time", "START_TIME"};
        return getIndex(columns, possibleWords);
    }
}
