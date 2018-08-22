package sqlitedataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import tracking.ActivityInformation;

import java.util.ArrayList;
import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {


    public DataBaseHelper(Context context) {
        super(context, DataBaseFeedEntry._DATABASENAME , null, DataBaseFeedEntry._DATABASEVERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //This Method just get called One time --> when the app installed for the first time !
        //Create Table
        db.execSQL("create table "+ DataBaseFeedEntry._SESSIONS_TABLE_NAME
                +" ("+ DataBaseFeedEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT "
                + ", "+ DataBaseFeedEntry._TITLE + " TEXT "
                + ", "+ DataBaseFeedEntry._UNITEUSED +" INTEGER"
                + ", "+ DataBaseFeedEntry._DISTANCE +" REAL "
                + ", "+ DataBaseFeedEntry._BESTSPEED +" REAL "
                + ", "+ DataBaseFeedEntry._PATH + " TEXT"
                + ", "+ DataBaseFeedEntry._AVERAGESPEED + " REAL "
                + ", "+ DataBaseFeedEntry._TIMEINMS + " REAL "
                + ", "+ DataBaseFeedEntry._HIGHESTELEVATION + " REAL "
                + ")");
        Log.i("Tag", "database created !");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "drop table if exists "+DataBaseFeedEntry._SESSIONS_TABLE_NAME;
        db.execSQL(query);
        onCreate(db);
    }


    //Read All Data in History tables
    public ArrayList<String> readAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> list = new ArrayList<>();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery("select * from " + DataBaseFeedEntry._SESSIONS_TABLE_NAME , null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String info = cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._TITLE));
            list.add(info);
            cursor.moveToNext();
        }
        return list;
    }


    //return the cursor that selects all the History table
    public Cursor getCursor (){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+DataBaseFeedEntry._SESSIONS_TABLE_NAME , null);
    }


    //Save Basic Data of one Activity
    public void saveActivityData(@NonNull ActivityInformation activityInformation){
        //open database in write mood
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //get all data and put them in ContentValues
        contentValues.put(DataBaseFeedEntry._TITLE, activityInformation.getTitle());
        contentValues.put(DataBaseFeedEntry._UNITEUSED , activityInformation.getUnit());
        contentValues.put(DataBaseFeedEntry._DISTANCE , activityInformation.getDistance());
        contentValues.put(DataBaseFeedEntry._BESTSPEED, activityInformation.getBestSpeed());
        contentValues.put(DataBaseFeedEntry._PATH , activityInformation.getPath());
        contentValues.put(DataBaseFeedEntry._AVERAGESPEED, activityInformation.getAverageSpeed());
        contentValues.put(DataBaseFeedEntry._TIMEINMS , activityInformation.getTimeInMs());
        contentValues.put(DataBaseFeedEntry._HIGHESTELEVATION , activityInformation.getHighestElevation());
        //Insert the data using ContentValues
        db.insert(DataBaseFeedEntry._SESSIONS_TABLE_NAME,null,contentValues);
    }


    //Read one row via id in History table and return HashMap holds all the data
    public HashMap<String , String> readSessionByRowId(int id){
        HashMap<String , String> hashMap = new HashMap<>();
        String query = "SELECT * FROM " + DataBaseFeedEntry._SESSIONS_TABLE_NAME+" WHERE "+DataBaseFeedEntry._ID+" = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query , null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            hashMap.put(DataBaseFeedEntry._TITLE ,cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._TITLE)) );
            hashMap.put(DataBaseFeedEntry._UNITEUSED , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._UNITEUSED)));
            hashMap.put(DataBaseFeedEntry._BESTSPEED , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._BESTSPEED)));
            hashMap.put(DataBaseFeedEntry._DISTANCE , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._DISTANCE)));
            hashMap.put(DataBaseFeedEntry._PATH , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._PATH)));
            hashMap.put(DataBaseFeedEntry._AVERAGESPEED , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._AVERAGESPEED)));
            hashMap.put(DataBaseFeedEntry._TIMEINMS , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._TIMEINMS)));
            hashMap.put(DataBaseFeedEntry._HIGHESTELEVATION , cursor.getString(cursor.getColumnIndex(DataBaseFeedEntry._HIGHESTELEVATION)));
            cursor.moveToNext();
        }
        return hashMap;
    }


    //Delete one row from History
    public void deleteActivityById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete Row Where the id equal to the id we giving you !
        db.delete(DataBaseFeedEntry._SESSIONS_TABLE_NAME , DataBaseFeedEntry._ID +" = "+id , null);
        db.close();
    }


    //This Method return to us the Available id's in History table !
    public ArrayList<Integer> readAvailableIds (){
        ArrayList<Integer> list = new ArrayList<>();
        //Open Data Base in Write Mood
        SQLiteDatabase db = this.getWritableDatabase();
        //Get The Cursor
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from "+DataBaseFeedEntry._SESSIONS_TABLE_NAME , null);
        //Set Up the Cursor where to start
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            Integer i = cursor.getInt(cursor.getColumnIndex(DataBaseFeedEntry._ID));
            list.add(i);
            cursor.moveToNext();
        }
        //Close the DataBase
        db.close();
        return list;
    }


    //update one row via id
    public void updateActivityInformation (ActivityInformation activityInformation, int id){
        //DataBase
        SQLiteDatabase db = this.getReadableDatabase();
        //content values --> map from column names to new column values
        ContentValues contentValues = new ContentValues();
        //put all the data to content values
        contentValues.put(DataBaseFeedEntry._TITLE, activityInformation.getTitle());
        contentValues.put(DataBaseFeedEntry._UNITEUSED , activityInformation.getUnit());
        contentValues.put(DataBaseFeedEntry._DISTANCE , activityInformation.getDistance());
        contentValues.put(DataBaseFeedEntry._BESTSPEED, activityInformation.getBestSpeed());
        contentValues.put(DataBaseFeedEntry._PATH , activityInformation.getPath());
        contentValues.put(DataBaseFeedEntry._AVERAGESPEED, activityInformation.getAverageSpeed());
        contentValues.put(DataBaseFeedEntry._TIMEINMS , activityInformation.getTimeInMs());
        contentValues.put(DataBaseFeedEntry._HIGHESTELEVATION , activityInformation.getHighestElevation());
        //Update the Row in DataBase
        db.update(DataBaseFeedEntry._SESSIONS_TABLE_NAME
                , contentValues
                , DataBaseFeedEntry._ID+" = "+id
                , null);
    }



}
