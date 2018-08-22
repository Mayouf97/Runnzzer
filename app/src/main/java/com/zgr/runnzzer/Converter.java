package com.zgr.runnzzer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import sqlitedataBase.DataBaseFeedEntry;
import sqlitedataBase.DataBaseHelper;
import tracking.Track;

import java.util.Locale;


public class Converter {



    static double convertKiloMeterToMile (double km){
        return km * 1.609344;
    }


    static double convertMileToKiloMeter (double mi){
        return mi * 0.62137119223733;
    }



    //This Method Convert LatLng Object to String
    public static String convertLatLngToString (LatLng latLng){
        Gson gson = new Gson();
        return gson.toJson(latLng);
    }


    //Convert LatLng Object To String
    public static LatLng convertStringToLatLng (String json){
        Gson gson = new Gson();
        return gson.fromJson(json , LatLng.class);
    }


    //This Method Get A Track Object and Convert it to String
    public static String convertSessionPathToString (Track track){
        Gson gson = new Gson();
        return gson.toJson(track);
    }


    //This Method Get A String and Convert it to Track
    public static Track convertStringToSessionPath (String sessionPath){
        Gson gson = new Gson();
        return gson.fromJson(sessionPath , Track.class);
    }


    //Calculate the Distance
    String calculateTotalDistance (Context context , SharedPreferences sharedPreferences){
        //Instance of DataBase
        DataBaseHelper db = new DataBaseHelper(context);
        //Get Cursor of Sessions Table
        Cursor cursor = db.getCursor();
        cursor.moveToFirst();
        // -- get 0 Index May be 0 or 1 and represent MPH or KM --
        int currentUnite = new SettingManager(sharedPreferences).getDistanceUniteSettings();
        double totalDistance = 0;
        String distance = "";
        //That's Mean Current Unite is in Mph and the Out Put of this Method show be the same at the Current Unite !
        if (currentUnite == SettingManager.DEFAULT_UNIT_VALUE){
            //Out Put in KM !

            while (!cursor.isAfterLast()){
                //check if the activity_history Saved with Mph or Km
                if (cursor.getInt(cursor.getColumnIndex(DataBaseFeedEntry._UNITEUSED)) == SettingManager.DEFAULT_UNIT_VALUE){
                    //This activity_history saved with Kilometer unite
                    totalDistance += cursor.getDouble(cursor.getColumnIndex(DataBaseFeedEntry._DISTANCE));
                }else {
                    //This activity_history saved with mile unite
                    totalDistance += cursor.getDouble(cursor.getColumnIndex(DataBaseFeedEntry._DISTANCE)) * 1.609344;
                }
                cursor.moveToNext();
            }

            distance += String.format(Locale.ENGLISH , "%.1f", totalDistance);
        }else {
            //Out Put in MILE !
            while (!cursor.isAfterLast()){
                //check if the activity_history Saved with Mph or Km
                if (cursor.getInt(cursor.getColumnIndex(DataBaseFeedEntry._UNITEUSED)) == 0){
                    //This activity_history saved with Kilometer unite
                    totalDistance += cursor.getDouble(cursor.getColumnIndex(DataBaseFeedEntry._DISTANCE)) * 0.62137119223733 ;
                }else {
                    //This activity_history saved with mile unite
                    totalDistance += cursor.getDouble(cursor.getColumnIndex(DataBaseFeedEntry._DISTANCE));
                }
                cursor.moveToNext();
            }
            distance += String.format(Locale.ENGLISH , "%.1f", totalDistance);
        }
        return distance;
    }


    //Calculate Total Time of All The sessions and return time in text Format
    String calculateTotalTime (Context context){
         //Access Data Base
         DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
         @SuppressLint("Recycle")
         Cursor cursor = dataBaseHelper.getWritableDatabase().rawQuery("select * from " + DataBaseFeedEntry._SESSIONS_TABLE_NAME , null);
         cursor.moveToFirst();
         double TinSec = 0;
         while (!cursor.isAfterLast()){
             TinSec += cursor.getDouble(cursor.getColumnIndex(DataBaseFeedEntry._TIMEINMS));
             cursor.moveToNext();
         }
         //
         return timeFormatter(TinSec);
     }


    //Convert time in Ms to a Format String
    static String timeFormatter (double totalTimeInMs){
        //Convert Milliseconds To seconds
         double timeInSec = totalTimeInMs / 1000;
         int hh = (int)(timeInSec / 3600);
         int mm = (int)((timeInSec % 3600) / 60);
         int ss = (int)(timeInSec % 60);

         return String.format(Locale.ENGLISH ,"%02d:%02d:%02d" , hh , mm , ss);
    }


}
