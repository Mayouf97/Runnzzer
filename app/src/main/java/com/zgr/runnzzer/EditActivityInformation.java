package com.zgr.runnzzer;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.shawnlin.numberpicker.NumberPicker;
import sqlitedataBase.DataBaseHelper;
import tracking.ActivityInformation;

import java.text.DecimalFormat;
import java.util.Locale;


public class EditActivityInformation extends AppCompatActivity {


    private static final double ONE_HOUR = 3600000;
    private static final double ONE_MIN = 60000;
    private static final double ONE_SEC = 1000;
    private TextView title,duration,distance,elevation;
    private String currentDistanceUnite;
    private Bundle bundle;
    private SettingManager settingManager;
    private ActivityInformation activityInformation;
    private int actualDistanceUniteInSettings;
    private double distanceValue;
    private double parsedDurationValue;
    private double heightsElevation = 0;
    private String selectedTitle;
    private int id;//hold the id of the session


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity_information);
        //Get Bundle From Intent That opens this activity and assert don't equal to null
        bundle = getIntent().getExtras();
        assert bundle != null;
        id = bundle.getInt(Keys.ID);
        //
        activityInformation = new ActivityInformation();
        //
        setUpDistanceUnite();
        //
        setUpTheUi();
    }



    private void setUpDistanceUnite (){
        //
        settingManager = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME , MODE_PRIVATE));
        //
        actualDistanceUniteInSettings = settingManager.getDistanceUniteSettings();
        //
        if (actualDistanceUniteInSettings == SettingManager.DEFAULT_UNIT_VALUE){
            //
            currentDistanceUnite = getResources().getString(R.string.km);
        }else {
            //
            currentDistanceUnite = getResources().getString(R.string.mi);
        }
    }



    private void setUpTheUi (){
        //find text views
        title = findViewById(R.id.edit_activity_date);
        duration = findViewById(R.id.edit_activity_duration);
        distance = findViewById(R.id.edit_activity_distance);
        elevation = findViewById(R.id.edit_activity_max_elevation);
        //
        setTitleText();
        //
        setDurationText();
        //
        setDistanceText();

    }



    private void setTitleText (){
        //
        String titleValue = bundle.getString(Keys.TITLE);
        //
        title.setText(titleValue);
        //
        activityInformation.setTitle(titleValue);
    }



    private void setDurationText (){
        //Get Duration Value for the Intent Bundle as String DataType
        String durationAsText = bundle.getString(Keys.DURATION);
        //Parse String To Double
        parsedDurationValue = Double.parseDouble(durationAsText);
        //Use parsedDurationValue to construct Time format "%02d:%02d:%02d"
        String formattedText = Converter.timeFormatter(parsedDurationValue);
        //formatted duration value
        String FormattedDurationText = String.format(Locale.ENGLISH , "%s" , formattedText);
        //set Text for the Duration text view
        duration.setText(FormattedDurationText);
        //
        activityInformation.setTimeInMs(parsedDurationValue);
    }



    private void setDistanceText (){
        //
        String distanceAsString = bundle.getString(Keys.DISTANCE);
        //
        distanceValue = Double.parseDouble(distanceAsString);
        //
        String distanceUniteAsString = bundle.getString(Keys.UNITE_USED);
        //
        int usedUnite = Integer.parseInt(distanceUniteAsString);
        //get current unite in the activity_settings
        int actualDistanceUnite = settingManager.getDistanceUniteSettings();
        //
        if(usedUnite != actualDistanceUnite){
            if (actualDistanceUnite == SettingManager.DEFAULT_MAP){
                //km so convert to mi
                distanceValue = Converter.convertKiloMeterToMile(distanceValue);
            }else {
                distanceValue = Converter.convertMileToKiloMeter(distanceValue);
            }
        }
        //
        String formattedDecimalDistanceValue = new DecimalFormat("#.##").format(distanceValue);
        //
        String distanceTextFormat = String.format(Locale.ENGLISH , "%s %s" , formattedDecimalDistanceValue , currentDistanceUnite);
        //
        distance.setText(distanceTextFormat);
        //
        activityInformation.setDistance(distanceValue);
    }




    private void editDate (){
        //
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        {
            //
            selectedTitle = mountAsString(month)+" "+dayOfMonth+", "+year;
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

                selectedTitle += String.format(Locale.ENGLISH , " %02d:%02d %s" , hourOfDay , minute , "AM");
            }else {

                selectedTitle += String.format(Locale.ENGLISH , " %02d:%02d %s" , hourOfDay , minute , "PM");
            }
            //
            title.setText(selectedTitle);

        }, 0 , 0 , false);

        //
        timePickerDialog.show();
    }




    private String mountAsString (int mounthIndex){
        String[] months = {
                "Jan","Feb","Mar","Apr","May","Jun"
               ,"Jul","Aug","Sep","Oct","Nov","Dec"
        };
        return months[mounthIndex];
    }




    private void editDuration (){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        //get the layout
        final View v = getLayoutInflater().inflate(R.layout.select_duration, null);
        //find number picker
        final NumberPicker hourNumberPicker = v.findViewById(R.id.numberPicker);
        final NumberPicker minuteNumberPicker = v.findViewById(R.id.numberPicker2);
        final NumberPicker secondNumberPicker = v.findViewById(R.id.numberPicker3);

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
        v.findViewById(R.id.set_time_button).setOnClickListener(view -> {
            //getValues
            int hh = hourNumberPicker.getValue();
            int mm = minuteNumberPicker.getValue();
            int ss = secondNumberPicker.getValue();
            duration.setText(String.format(Locale.ENGLISH , "%01d:%02d:%02d" , hh , mm , ss));

            //calculate the total time regardless the time the user chose ;)
            parsedDurationValue = (hh * ONE_HOUR) + (mm * ONE_MIN) + (ss * ONE_SEC);
            dialog.dismiss();
        });
        //
        dialog.setView(v);
        dialog.show();
    }




    private void editDistanceValue (){
        //Alert dialog
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //Layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        //Inflate the layout resource
        View view  = layoutInflater.inflate(R.layout.select_distance , null);
        //
        TextView dialogTitle = view.findViewById(R.id.select_distance_title_text);
        dialogTitle.setText(currentDistanceUnite);
        //Number picker
        NumberPicker decimalPicker = view.findViewById(R.id.feedback_number_picker_decimal);
        String[] displayValues = {
            "0" , "0.25" , "0.5", "0.75"
        };
        //set display values for the number picker
        decimalPicker.setMaxValue(displayValues.length);
        decimalPicker.setDisplayedValues(displayValues);
        //
        NumberPicker numberPicker = view.findViewById(R.id.feedback_number_picker);
        //Set the content for the alert dialog
        alertDialog.setView(view);
        //Find set button
        Button setButton = view.findViewById(R.id.set_distance_button);
        //Set up click listener for set Button
        setButton.setOnClickListener(v1 ->
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
        //show alert dialog to the user
        alertDialog.show();
    }




    private void editElevation (){
        //AlertDialog
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        //Layout Inflater
        LayoutInflater inflater = getLayoutInflater();
        //Inflate the Resources
        View view = inflater.inflate(R.layout.select_max_elevation , null);
        //find edit Text in Dialog view
        EditText editText = view.findViewById(R.id.select_max_elevation_editText);
        //find set Button in Dialog view
        Button setButton = view.findViewById(R.id.set_calories_button);
        //set up click listener for the set button to handle the click event
        setButton.setOnClickListener(v ->
        {
            //Get The Text From the The EditText
            String tokenText = editText.getText().toString();
            //Parse String to Double Data Type
            heightsElevation = Double.parseDouble(tokenText);
            //construct a text to be displayed to the user
            String elevationText = String.format(Locale.ENGLISH , "%s m" , heightsElevation);
            //close dialog
            dialog.dismiss();
            //set text for the text view
            elevation.setText(elevationText);
        });
        //set Content For the Alert Dialog
        dialog.setView(view);
        //Show the Alert Dialog to the User
        dialog.show();
    }




    public void onClick (View view){
        //
        switch (view.getId()){
            //Back Button
            case R.id.edit_activity_backButton :
                //back to the previous activity
                onBackPressed();
                break;
            //Done
            case R.id.edit_activity_done_text :
                done();
                break;
        }
        onBackPressed();
    }








    //event handling for the Done text in the action bar
    private void done (){
        //title
        if (selectedTitle == null){
            activityInformation.setTitle(bundle.getString(Keys.TITLE));
        }else {
            activityInformation.setTitle(selectedTitle);
        }

        //distance Unite
        activityInformation.setUnit(actualDistanceUniteInSettings);
        //Distance
        activityInformation.setDistance(distanceValue);
        //Best Speed
        activityInformation.setBestSpeed(0);
        //Time
        activityInformation.setTimeInMs(parsedDurationValue);
        //Elevation
        activityInformation.setHighestElevationRecorded(heightsElevation);
        //Path
        activityInformation.setPath(null);
        //
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        //
        dataBaseHelper.updateActivityInformation(activityInformation , id);
        //handle the back button click
        onBackPressed();
    }





    //event Handler for the layouts
    public void onClickEvent (View v) {

        switch (v.getId())
        {

            case R.id.edit_activity_date_layout :
                editDate();
                break;


            case R.id.edit_activity_duration_layout :
                editDuration();
                break;


            case R.id.edit_activity_distance_layout :
                editDistanceValue();
                break;


            case R.id.edit_activity_maxElevation_layout :
                editElevation();
                break;

        }
    }






    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //
        finish();
        //
        Intent intent = new Intent(this , TrackingDetails.class);
        intent.putExtra(TrackingDetails.WHO, TrackingDetails.EDIT_ACTIVITY);
        intent.putExtra(Keys.ID , id);
        startActivity(intent);
        Transitions.closeTransition(this);
    }



}
