package com.lukaskorinek.pocketracetimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.lukaskorinek.pocketracetimer.models.RacerModel;
import com.lukaskorinek.pocketracetimer.static_classes.TimeToText;

import java.util.ArrayList;
import java.util.Collections;

public class PDFGenerator {
    private Context context;

    private final int TOP_BORDER = 20;
    private final int BOTTOM_BORDER = 15;
    private final int LEFT_BORDER = 20;
    private final int RIGH_BORDER = 20;
    private final int POSITION_LENGHT = 10;
    private final int NUMBER_LENGHT = 7;
    private final int RACER_NUMBER_LENGHT = 25;
    private final int NAME_LENGHT = 22;
    private final int TIME_LENGHT = 15;
    private final int CATEGORY_LENGHT = 20;

    public PDFGenerator(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public PdfDocument getPDF(ArrayList<RacerModel> racers, ArrayList<String> categories, int nOfRacersInStartingList) {
        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create( Typeface.MONOSPACE, Typeface.NORMAL));
        int pageWidth = 250;
        int pageHeight = 400;

        if(nOfRacersInStartingList == 0) {
            pdf = addPageRaceWithoutStartingList(pdf, pageWidth, pageHeight, 1, paint, racers, 0);
        } else {
            ArrayList<RacerModel> racers_done = new ArrayList<>();
            ArrayList<RacerModel> allRacersRemaining = new ArrayList<>();
            for (int i = 0; i < racers.size(); i++) {
                allRacersRemaining.add(racers.get(i));
            }
            ArrayList<String> categories_done = new ArrayList<>();
            pdf = addPage(pdf, pageWidth, pageHeight, 1, paint, racers, racers_done, allRacersRemaining, categories, categories_done, 0, 0);
        }
        return pdf;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private PdfDocument addPage(PdfDocument pdf, int pageWidth, int pageHeight, int pageNumber, Paint paint,
                                ArrayList<RacerModel> racers, ArrayList<RacerModel> racers_done, ArrayList<RacerModel> allRacersRemaining,
                                ArrayList<String> categories, ArrayList<String> categories_done, int lastPosition, int allRacersLastPosition) {

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth,pageHeight,pageNumber).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas = drawHeader(pdf, canvas, paint, pageNumber, pageWidth);

        paint.setTextSize(4);
        int y = TOP_BORDER + 15;
        // Závod se startovní listinou - po kategoriích
        Collections.sort(categories);
        for (int i = 0; i < categories.size(); i++) {
            int position = lastPosition;
            boolean doSpaceAfter = !categories_done.contains(categories.get(i));
            if(!categories_done.contains(categories.get(i))) {
                paint.setTextSize(5);
                canvas.drawText(categories.get(i), LEFT_BORDER, y, paint);
                paint.setTextSize(4);
                categories_done.add(categories.get(i));
                position = 0;
                y = y + 10;
                if (isEndOfPage(y, pageHeight)) {
                    pdf.finishPage(page);
                    pageNumber++;
                    pdf = addPage(pdf, pageWidth, pageHeight, pageNumber, paint, racers, racers_done, allRacersRemaining, categories, categories_done, position, allRacersLastPosition);
                    return pdf;
                }
            }

            for (int j = 0; j < racers.size(); j++) {
                if((racers.get(j).getNumber() != 0) && (!(racers.get(j).getCategory() == null)) && racers.get(j).getCategory().equalsIgnoreCase(categories.get(i)) && (!racers_done.contains(racers.get(j)))) {
                    int x = LEFT_BORDER;
                    int number = racers.get(j).getNumber();
                    String name = racers.get(j).getName() + " " + racers.get(j).getSurname();
                    if(name.length() > NAME_LENGHT) {
                        name = name.substring(0, (NAME_LENGHT-3)) + "..";
                    }
                    String team = racers.get(j).getTeam();
                    if(team.trim().equalsIgnoreCase("")) {
                        team = this.context.getResources().getString(R.string.team_not_filled);
                    }
                    long racerTime = racers.get(j).getTimeInSeconds();
                    if(racerTime != 0) {
                        position++;
                        String time = TimeToText.longTimeToString(racerTime);
                        canvas.drawText((String.valueOf(position) + "."), x, y, paint);
                        x += (POSITION_LENGHT*2);
                        canvas.drawText(String.valueOf(number), x, y, paint);
                        x += (NUMBER_LENGHT*2);
                        canvas.drawText(name, x, y, paint);
                        x += (NAME_LENGHT*2);
                        canvas.drawText(time, x, y, paint);
                        x += (TIME_LENGHT*2);
                        canvas.drawText(team, x, y, paint);
                    } else {
                        canvas.drawText(this.context.getResources().getString(R.string.symbol_for_no_position), x, y, paint);
                        x += (POSITION_LENGHT*2);
                        canvas.drawText(String.valueOf(number), x, y, paint);
                        x += (NUMBER_LENGHT*2);
                        canvas.drawText(name, x, y, paint);
                        x += (NAME_LENGHT*2);
                        canvas.drawText("-", x, y, paint);
                        x += (TIME_LENGHT*2);
                        canvas.drawText(team, x, y, paint);
                    }
                    racers_done.add(racers.get(j));
                    doSpaceAfter = true;
                    y = y + 10;
                    if (isEndOfPage(y, pageHeight)) {
                        pdf.finishPage(page);
                        pageNumber++;
                        pdf = addPage(pdf, pageWidth, pageHeight, pageNumber, paint, racers, racers_done, allRacersRemaining, categories, categories_done, position, allRacersLastPosition);
                        return pdf;
                    }
                }
            }
            if(doSpaceAfter) {
                y = y + 10;
                if (isEndOfPage(y, pageHeight)) {
                    pdf.finishPage(page);
                    pageNumber++;
                    pdf = addPage(pdf, pageWidth, pageHeight, pageNumber, paint, racers, racers_done, allRacersRemaining, categories, categories_done, position, allRacersLastPosition);
                    return pdf;
                }
            }
        }
        // Závod se startovní listinou - všichni závodníci
        int allRacersPosition = allRacersLastPosition;
        if(allRacersPosition == 0) {
            paint.setTextSize(5);
            canvas.drawText(this.context.getResources().getString(R.string.csv_all_racers), LEFT_BORDER, y, paint);
            paint.setTextSize(4);
            y = y + 10;
            if (isEndOfPage(y, pageHeight)) {
                pdf.finishPage(page);
                pageNumber++;
                pdf = addPage(pdf, pageWidth, pageHeight, pageNumber, paint, racers, racers_done, allRacersRemaining, categories, categories_done, lastPosition, allRacersPosition);
                return pdf;
            }
        }

        for (int i = 0; i < allRacersRemaining.size(); i++) {
            if(allRacersPosition > 35) {
            }
            if(allRacersRemaining.get(i).getNumber() != 0) {
                int x = LEFT_BORDER;
                int number = allRacersRemaining.get(i).getNumber();
                String firstName = allRacersRemaining.get(i).getName();
                String secondName = allRacersRemaining.get(i).getSurname();
                String name;
                if(firstName == null || secondName == null) {
                    name = context.getResources().getString(R.string.unknown_racer);
                } else {
                    name = allRacersRemaining.get(i).getName() + " " + allRacersRemaining.get(i).getSurname();
                }
                if (name.length() > NAME_LENGHT) {
                    name = name.substring(0, (NAME_LENGHT - 3)) + "..";
                }
                String category = allRacersRemaining.get(i).getCategory();
                if(category == null) {
                    category = context.getResources().getString(R.string.category_none);
                } else {
                    if (category.length() > CATEGORY_LENGHT) {
                        category = category.substring(0, (CATEGORY_LENGHT - 3)) + "..";
                    }
                }
                String team = allRacersRemaining.get(i).getTeam();
                if (team == null) {
                    team = context.getResources().getString(R.string.team_none);
                }
                if (team.trim().equalsIgnoreCase("")) {
                    team = this.context.getResources().getString(R.string.team_not_filled);
                }
                long racerTime = allRacersRemaining.get(i).getTimeInSeconds();
                if (racerTime != 0) {
                    allRacersPosition++;
                    String time = TimeToText.longTimeToString(racerTime);
                    canvas.drawText((String.valueOf(allRacersPosition) + "."), x, y, paint);
                    x += (POSITION_LENGHT * 2);
                    canvas.drawText(String.valueOf(number), x, y, paint);
                    x += (NUMBER_LENGHT * 2);
                    canvas.drawText(name, x, y, paint);
                    x += (NAME_LENGHT * 2);
                    canvas.drawText(time, x, y, paint);
                    x += (TIME_LENGHT * 2);
                    canvas.drawText(category, x, y, paint);
                    x += (CATEGORY_LENGHT * 2);
                    canvas.drawText(team, x, y, paint);
                } else {
                    canvas.drawText(this.context.getResources().getString(R.string.symbol_for_no_position), x, y, paint);
                    x += (POSITION_LENGHT * 2);
                    canvas.drawText(String.valueOf(number), x, y, paint);
                    x += (NUMBER_LENGHT * 2);
                    canvas.drawText(name, x, y, paint);
                    x += (NAME_LENGHT * 2);
                    canvas.drawText("-", x, y, paint);
                    x += (TIME_LENGHT * 2);
                    canvas.drawText(category, x, y, paint);
                    x += (CATEGORY_LENGHT * 2);
                    canvas.drawText(team, x, y, paint);
                }
                y = y + 10;
                if (isEndOfPage(y, pageHeight)) {
                    pdf.finishPage(page);
                    pageNumber++;
                    ArrayList<RacerModel> remainingRacers = new ArrayList<>();
                    if (i < allRacersRemaining.size() - 1) {
                        for (int j = i + 1; j <= allRacersRemaining.size() - 1; j++) {
                            remainingRacers.add(allRacersRemaining.get(j));
                        }
                    }
                    pdf = addPage(pdf, pageWidth, pageHeight, pageNumber, paint, racers, racers_done, remainingRacers, categories, categories_done, lastPosition, allRacersPosition);
                    return pdf;
                }
            }
        }
        pdf.finishPage(page);
        return pdf;
    }

    private Canvas drawHeader(PdfDocument pdf, Canvas canvas, Paint paint, int pageNumber, int pageWidth) {
        Drawable image = context.getResources().getDrawable(R.drawable.stopwatch_logo);
        Rect imageBounds = new Rect(((pageWidth - RIGH_BORDER) - 15), (TOP_BORDER-10), (pageWidth - RIGH_BORDER), (TOP_BORDER+5));
        image.setBounds(imageBounds);
        image.draw(canvas);
        if(pageNumber == 1) {
            paint.setTextSize(7);
            canvas.drawText(this.context.getResources().getString(R.string.race_results), ((pageWidth / 2) - 35), TOP_BORDER, paint);
        }
        return canvas;
    }

    private boolean isEndOfPage(int y, int pageHeight) {
        return (y > (pageHeight - BOTTOM_BORDER));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private PdfDocument addPageRaceWithoutStartingList(PdfDocument pdf, int pageWidth, int pageHeight, int pageNumber, Paint paint, ArrayList<RacerModel> racers, int lastPosition) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth,pageHeight,pageNumber).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas = drawHeader(pdf, canvas, paint, pageNumber, pageWidth);
        paint.setTextSize(4);
        int position = lastPosition;
        int y = TOP_BORDER + 15;
        for (int i = 0; i < racers.size(); i++) {
            if(racers.get(i).getNumber() != 0) {
                int x = LEFT_BORDER;
                position++;
                int number = racers.get(i).getNumber();
                String time = TimeToText.longTimeToString(racers.get(i).getTimeInSeconds());
                canvas.drawText((String.valueOf(position)+ "."), x, y, paint);
                x += (POSITION_LENGHT * 2);
                canvas.drawText(context.getResources().getString(R.string.race_number_add) + " " + String.valueOf(number), x, y, paint);
                x += (RACER_NUMBER_LENGHT * 2);
                canvas.drawText(time, x, y, paint);
                y = y + 10;
                if (isEndOfPage(y, pageHeight)) {
                    pdf.finishPage(page);
                    pageNumber++;
                    ArrayList<RacerModel> racersRemaining = new ArrayList<>();
                    if (i < racers.size() - 1) {
                        for (int j = i + 1; j <= racers.size() - 1; j++) {
                            racersRemaining.add(racers.get(j));
                        }
                    }
                    pdf = addPageRaceWithoutStartingList(pdf, pageWidth, pageHeight, pageNumber, paint, racersRemaining, position);
                    return pdf;
                }
            }
        }
        pdf.finishPage(page);
        return pdf;
    }
}
