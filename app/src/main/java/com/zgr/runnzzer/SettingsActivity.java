package com.zgr.runnzzer;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.DecimalFormat;
import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {

    private Button feedBackRateButton , mapStyle;
    private RadioButton km , mil , soundOn , soundOff;
    private TextView currentSelected;
    private SettingManager settingsManager;
    private String currentUnite;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //set up the ui
        initializeViews ();
        //Get Setting Manager
        settingsManager = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE));
        //set Up User Interface
        runOnUiThread(this::setUpUi);
    }


    private void initializeViews (){
        //find the Views
        km = findViewById(R.id.km);//Km Radio Button
        mil = findViewById(R.id.mph);//Mi Radio Button
        soundOn = findViewById(R.id.sound_on_radio_button);//On Radio Button
        soundOff = findViewById(R.id.sound_off_radio_button);//Off Radio Button
        feedBackRateButton = findViewById(R.id.select_rate_button);//FeedBack Rate Button
        mapStyle = findViewById(R.id.select_map_style_button);//Map Style Button
    }


    //click event handling for back arrow icon in tool bar
    public void backArrow (View view){
        onBackPressed();
    }


    //Set Up User Interface Depends On The Settings.
    private void setUpUi (){
        //Check for the Current Unite in Settings
        if (settingsManager.getDistanceUniteSettings() == SettingManager.DEFAULT_UNIT_VALUE){
            km.setChecked(true);
            currentUnite = getResources().getString(R.string.km);
        }else {
            currentUnite = getResources().getString(R.string.mi);
            mil.setChecked(true);
        }
        //sound activity_settings
        if (!settingsManager.getSoundSettings()){
            disEnableSoundLayout();
        }else {
            enableSoundLayout();
        }
        //Map Style
        //Get Settings Values
        switch (settingsManager.getMapSettings()){
            case SettingManager.DEFAULT_MAP :
                mapStyle.setText(getResources().getString(R.string.regular));
                break;
            case SettingManager.SATELLITE_MAP :
                mapStyle.setText(getResources().getString(R.string.satellite));
                break;
            case SettingManager.TERRAIN_MAP :
                mapStyle.setText(getResources().getString(R.string.terrain));
                break;
        }
    }


    //event click respond
    public void changeDistanceUnite(View view){
        //When User changeDistanceUnite the distance Unite Settings
        //changeDistanceUnite the unite in Shared Preferences
        settingsManager.changeDistanceUniteSetting();
        //changeDistanceUnite the Text of feedBack Rate Button Depends on the User Settings

            if (settingsManager.getDistanceUniteSettings() == SettingManager.DEFAULT_UNIT_VALUE){
                if (settingsManager.getSoundSettings()){
                    //changeDistanceUnite to KiloMeter
                    feedBackRateButton.setText(String.format(Locale.ENGLISH,"%s %s" ,
                            new DecimalFormat("#.##").format(settingsManager.getAudioFeedbackRateValue() ),
                            getResources().getString(R.string.km)));
                }
                currentUnite = getResources().getString(R.string.km);
            }else {
                if (settingsManager.getSoundSettings()){
                    //changeDistanceUnite to Miles
                    feedBackRateButton.setText(String.format(Locale.ENGLISH,"%s %s" ,
                            new DecimalFormat("#.##").format(settingsManager.getAudioFeedbackRateValue() ),
                            getResources().getString(R.string.mi)));
                }
                currentUnite = getResources().getString(R.string.mi);
            }

    }



    //select FeedBack Rate
    public void changeFeedBacksValue (View view){
        //Get screen displays
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //Get the dimensions of the screen from the metrics
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        //Inflate the layout
        View alertDialogView = getLayoutInflater().inflate( R.layout.select_feedbacks_rate, null);
        //set sizes for the view
        alertDialogView.setMinimumHeight(heightPixels / 2);
        alertDialogView.setMinimumWidth(widthPixels);
        //Alert dialog
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        //Get Setting Manager
        SettingManager settingManager = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE));
        //find the Number Picker in alertDialog view
        final NumberPicker feedNumberPicker = alertDialogView.findViewById(R.id.feedback_number_picker);
        //Display Value for the Number Picker
        String[] displayValues = {
                "0.25","0.5","0.75","1","1.25","1.5","1.75","2"
        };
        feedNumberPicker.setMinValue(0);
        feedNumberPicker.setMaxValue(displayValues.length - 1);
        feedNumberPicker.setDisplayedValues(displayValues);
        feedNumberPicker.setValue((int)(settingManager.getAudioFeedbackRateValue()/0.25)-1);
        //set up the listener for the set feedBackRateButton
        alertDialogView.findViewById(R.id.set_feedback_rate).setOnClickListener(view1 -> {
            float rateValue = (feedNumberPicker.getValue() * 0.25f) + 0.25f;
            settingManager.changeAudioFeedBackRate(rateValue);
            feedBackRateButton.setText(String.format(Locale.ENGLISH , "%s %s" , new DecimalFormat("#.##").format(rateValue) , currentUnite));
            //close the dialog box after the click
            dialog.dismiss();
        });
        //set the view for the Alert Dialog
        dialog.setView(alertDialogView);
        //show the Dialog for the user
        dialog.show();
    }



    //Change Map Style
    public void changeMapStyle (View view){
        //Get screen displays
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //Get the dimensions of the screen from the metrics
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        //Alert dialog
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        //Inflate the layout
        View alertDialogView = getLayoutInflater().inflate( R.layout.select_map_style, null);
        //set sizes for the view
        alertDialogView.setMinimumWidth(widthPixels/2);
        alertDialogView.setMinimumHeight(((heightPixels / 3)));
        //Texts
        TextView regular = alertDialogView.findViewById(R.id.regular_text);
        TextView satellite = alertDialogView.findViewById(R.id.satellite_text);
        TextView terrain = alertDialogView.findViewById(R.id.terrain_text);
        //
        switch (settingsManager.getMapSettings()){

            case SettingManager.DEFAULT_MAP :
                regular.setTextColor(getResources().getColor(R.color.colorAccent));
                currentSelected = regular;
                break;

            case SettingManager.SATELLITE_MAP :
                satellite.setTextColor(getResources().getColor(R.color.colorAccent));
                currentSelected = satellite;
                break;

            case SettingManager.TERRAIN_MAP :
                terrain.setTextColor(getResources().getColor(R.color.colorAccent));
                currentSelected = terrain;
                break;
        }

        //Regular Layout
        alertDialogView.findViewById(R.id.select_regular_layout).setOnClickListener(view1 -> {
            currentSelected.setTextColor(getResources().getColor(R.color.white));
            regular.setTextColor(getResources().getColor(R.color.colorAccent));
            currentSelected = regular;
            //Change
            settingsManager.changeMapSettings(SettingManager.DEFAULT_MAP);
            mapStyle.setText(getResources().getString(R.string.regular));
            dialog.dismiss();
        });

        //Satellite Layout
        alertDialogView.findViewById(R.id.select_satellite_layout).setOnClickListener(view1 -> {
            currentSelected.setTextColor(getResources().getColor(R.color.white));
            satellite.setTextColor(getResources().getColor(R.color.colorAccent));
            currentSelected = satellite;
            settingsManager.changeMapSettings(SettingManager.SATELLITE_MAP);
            mapStyle.setText(getResources().getString(R.string.satellite));
            dialog.dismiss();
        });

        //Terrain Layout
        alertDialogView.findViewById(R.id.select_terrain_layout).setOnClickListener(view1 -> {
            currentSelected.setTextColor(getResources().getColor(R.color.white));
            terrain.setTextColor(getResources().getColor(R.color.colorAccent));
            currentSelected = terrain;
            settingsManager.changeMapSettings(SettingManager.TERRAIN_MAP);
            mapStyle.setText(getResources().getString(R.string.terrain));
            dialog.dismiss();
        });

        //set the view for the Alert Dialog
        dialog.setView(alertDialogView);
        //show the Dialog for the user
        dialog.show();
    }


    //Change Sound Settings Value in SharedPreference
    public void changeSoundSettings (View view){
        //change the setting Using Settings Manager.
        settingsManager.changeSoundSettings();
        //Update the User Interface.
        if (!settingsManager.getSoundSettings()){
            disEnableSoundLayout();
        }else {
            enableSoundLayout();
        }
    }



    private void disEnableSoundLayout(){
        soundOff.setChecked(true);//
        feedBackRateButton.setText("--");
        findViewById(R.id.feedback_rate_layout).setAlpha(0.2f);
        feedBackRateButton.setClickable(false);
        feedBackRateButton.setBackground(getResources().getDrawable(R.drawable.white_stoke_button));
    }



    private void enableSoundLayout (){
        soundOn.setChecked(true);
        feedBackRateButton.setClickable(true);
        findViewById(R.id.feedback_rate_layout).setAlpha(1f);
        feedBackRateButton.setText(String.format(Locale.ENGLISH , "%s %s" ,
                new DecimalFormat("#.##").format(settingsManager.getAudioFeedbackRateValue()) , currentUnite));

        feedBackRateButton.setBackground(getResources().getDrawable(R.drawable.stoke_button));

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Transitions.closeTransition(this);
    }


}
