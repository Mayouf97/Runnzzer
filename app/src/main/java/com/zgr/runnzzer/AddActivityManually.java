package com.zgr.runnzzer;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.shawnlin.numberpicker.NumberPicker;
import sqlitedataBase.DataBaseHelper;
import tracking.ActivityInformation;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;


public class AddActivityManually extends AppCompatActivity {

    private static final double ONE_HOUR = 3600000;
    private static final double ONE_MIN = 60000;
    private static final double ONE_SEC = 1000;
    private TextView dateText,distance , calories ,time;
    private double distanceValue = 0;
    private int caloriesValue = 0;
    private int hh = 0 , mm = 0, ss = 0;
    private double totalTimeInSec = 0;
    private String dateAndTime;
    private String currentDistanceUnite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session_manually);
        //find views and widget
        dateText = findViewById(R.id.add_session_time);
        time = findViewById(R.id.add_session_timeText);
        distance = findViewById(R.id.add_session_distance);
        calories = findViewById(R.id.add_session_calories);

        //set Default texts and values for the views
        dateAndTime = DateFormat.getDateTimeInstance().format(new Date());
        dateText.setText(dateAndTime);

        //
        int distanceUnite = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE)).getDistanceUniteSettings();
        if(distanceUnite == SettingManager.DEFAULT_UNIT_VALUE){
            currentDistanceUnite = getResources().getString(R.string.km);
            distance.setText(String.format(Locale.ENGLISH , "%s %s" , String.valueOf(distanceValue)  , currentDistanceUnite));
        }else {
            currentDistanceUnite = getResources().getString(R.string.mi);
            distance.setText(String.format(Locale.ENGLISH , "%s %s" , String.valueOf(distanceValue)  , currentDistanceUnite));
        }

        //
        calories.setText(String.valueOf(caloriesValue));
    }



    public void onClick(View view) {
        switch (view.getId()) {

            //Date
            case R.id.add_time :
                selectDate();
                break;
            //Time
            case R.id.add_session_timeLayout :
                selectTime();
                break;
            //Distance
            case R.id.distance_add:
                selectDistance();
                break;

            //Calories
            case R.id.add_calories:
                selectCalories();
                break;

            case R.id.save_session :
                saveActivity();
                onBackPressed();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Transitions.closeTransition(this);
    }

    private void selectDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            //
            dateAndTime = mountAsString(month)+" "+dayOfMonth+", "+year;
            //
            editTime();

        }, 2018 , 1, 1);
        //
        datePickerDialog.show();
    }



    private void editTime (){

        //
        TimePickerDialog timePickerDialog = new TimePickerDialog(this , (view, hourOfDay, minute) ->
        {
            //
            if (hourOfDay < 12){

                dateAndTime += String.format(Locale.ENGLISH , " %02d:%02d %s" , hourOfDay , minute , "AM");
            }else {

                dateAndTime += String.format(Locale.ENGLISH , " %02d:%02d %s" , hourOfDay , minute , "PM");
            }
            //
            dateText.setText(dateAndTime);

        }, 0 , 0 , false);

        //
        timePickerDialog.show();
    }



    private void selectDistance (){
        //Alert dialog
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //Layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        //Inflate the layout resource
        @SuppressLint("InflateParams") View view  = layoutInflater.inflate(R.layout.select_distance , null);
        //
        TextView dialogTitle = view.findViewById(R.id.select_distance_title_text);
        dialogTitle.setText(currentDistanceUnite);
        //Number picker
        NumberPicker numberPicker = view.findViewById(R.id.feedback_number_picker);
        //Decimal Number Picker
        NumberPicker decimalPicker = view.findViewById(R.id.feedback_number_picker_decimal);
        String[] displayValues = {
                "0" , "0.25" , "0.5", "0.75"
        };
        //set display values for the number picker
        decimalPicker.setMaxValue(displayValues.length);
        decimalPicker.setDisplayedValues(displayValues);

        //
        Button setButton = view.findViewById(R.id.set_distance_button);
        setButton.setOnClickListener(v ->
        {
            //
            int numberPickerValue = numberPicker.getValue();
            //
            double decimalPickerValue = Double.parseDouble(displayValues[decimalPicker.getValue() - 1]);
            //
            double selectedDistance = numberPickerValue + decimalPickerValue;
            //
            String distanceTextFormat = String.format(Locale.ENGLISH
                    , "%s %s"
                    , new DecimalFormat("#.##").format(selectedDistance)
                    , currentDistanceUnite);
            //
            distanceValue = selectedDistance;
            //
            distance.setText(distanceTextFormat);
            //close dialog
            alertDialog.dismiss();
        });

        alertDialog.setView(view);
        alertDialog.show();
    }



    private void selectCalories (){
        //Alert dialog
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //Layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        //Inflate the layout resource
        @SuppressLint("InflateParams") View view  = layoutInflater.inflate(R.layout.select_calories , null);
        final EditText editText = view.findViewById(R.id.calories_editText);
        view.findViewById(R.id.set_calories_button).setOnClickListener(view1 -> {
            //check if the edit text don't empty to avoid app crash :)
            if (!editText.getText().toString().isEmpty()){
                //get the text
                caloriesValue = Integer.parseInt(editText.getText().toString());
                //set the text
                calories.setText(String.valueOf(caloriesValue));
            }
            //close dialog
            alertDialog.dismiss();
        });
        alertDialog.setView(view);
        alertDialog.show();
    }



    private void selectTime (){

        //Alert dialog
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        //Layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        //Inflate the layout resource
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.select_duration , null);
        //find number picker
        final NumberPicker hourNumberPicker = view.findViewById(R.id.numberPicker);
        final NumberPicker minuteNumberPicker = view.findViewById(R.id.numberPicker2);
        final NumberPicker secondNumberPicker = view.findViewById(R.id.numberPicker3);

        //Set the minimum value of NumberPickers
        hourNumberPicker.setMinValue(0);
        minuteNumberPicker.setMinValue(0);
        secondNumberPicker.setMinValue(0);

        //Specify the maximum value  of NumberPickers
        hourNumberPicker.setMaxValue(10);
        minuteNumberPicker.setMaxValue(59);
        secondNumberPicker.setMaxValue(59);

        //set default Values for the Number Picker
        hourNumberPicker.setValue(0);
        minuteNumberPicker.setValue(0);
        secondNumberPicker.setValue(0);

        //Click Listener for the set Button
        view.findViewById(R.id.set_time_button).setOnClickListener(v -> {
            //getValues
            hh = hourNumberPicker.getValue();
            mm = minuteNumberPicker.getValue();
            ss = secondNumberPicker.getValue();
            //
            time.setText(String.format(Locale.ENGLISH , "%01d:%02d:%02d" , hh , mm , ss));

            //calculate the total time regardless the time the user chose ;)
            totalTimeInSec = (hh * ONE_HOUR) + (mm * ONE_MIN) + (ss * ONE_SEC);
            dialog.dismiss();
        });
        //
        dialog.setView(view);
        dialog.show();
    }



    private String mountAsString (int monthIndex){
        String[] months = {
                "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
        };
        return months[monthIndex];
    }



    private void saveActivity(){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        //
        ActivityInformation activityInformation = new ActivityInformation();
        //title
        activityInformation.setTitle(dateAndTime);
        //unit
        activityInformation.setUnit(new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE)).getDistanceUniteSettings());
        //Set the Best Speed
        activityInformation.setBestSpeed(0);
        //Set Distance
        activityInformation.setDistance(distanceValue);
        //Get the Track --"PolyLine Options Object"-- as String !
        activityInformation.setPath(null);
        //Set Time In Hour to Help Us to get The Average Speed
        activityInformation.setTimeInMs(totalTimeInSec);
        //save data
        dataBaseHelper.saveActivityData(activityInformation);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        //kill activity
        finish();
    }

}
