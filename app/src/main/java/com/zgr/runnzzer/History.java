package com.zgr.runnzzer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import sqlitedataBase.DataBaseHelper;

import java.util.ArrayList;

public class History extends AppCompatActivity {
    private ListView listView;
    private ArrayList<Integer> list;//list to hold the available id's in database.


    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //find List View
        listView = findViewById(R.id.sessionList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //set up the content
        setListView();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Transitions.closeTransition(this);
    }


    //fill list view with items from the database
    public void setListView (){
        //Access to DataBase
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        //Get the List of the Available ID's in History Table
        list = dataBaseHelper.readAvailableIds();

        //Custom Adapter
        CustomListAdapter customListAdapter = new CustomListAdapter(this , dataBaseHelper.readAllData());
        //Display the List attempt to the The Row's Available in "Session" Table
        listView.setAdapter(customListAdapter);
        //event Handler From the Item List
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //Get the Id from the list View
            //Check for the Available Ids in Session Table this solution was made in case delete One Row And The Id will disappear
            int Id = list.get((int) id);
            //Create intent and send it to the next activity
            Intent i = new Intent( getApplicationContext() , TrackingDetails.class);
            //put the id into the intent cause we using it in the next activity
            i.putExtra("id" , Id);
            //identify the activity to the next activity
            i.putExtra( TrackingDetails.WHO , TrackingDetails.SESSIONS_OPENED_ME);
            startActivity(i);
            Transitions.openTransition(this);
        });
    }



    public void addSession(View view) {
        Intent i = new Intent(this , AddActivityManually.class);
        startActivity(i);
        Transitions.openTransition(this);
    }


    public void backArrow(View view) {
        onBackPressed();
    }
}
