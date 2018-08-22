package tracking;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import com.zgr.runnzzer.Keys;
import com.zgr.runnzzer.R;
import com.zgr.runnzzer.SettingManager;

import java.util.ArrayList;
import java.util.Locale;


class DisplayCustomizer {

    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler();
    private Runnable runnable ;
    private ArrayList<TextView> titlesTexts = new ArrayList<>();
    private ArrayList<TextView> valuesTexts = new ArrayList<>();
    private ArrayList<String> displaySettings = new ArrayList<>();
    private ArrayList<TextView> timerTexts = new ArrayList<>();
    private int unite;
    private long baseTime;
    private boolean isTimeInDisplaySettings;
    private SettingManager settingManager;


    DisplayCustomizer(SharedPreferences sharedPreferences , ArrayList<TextView> titles , ArrayList<TextView> values) {
        this.sharedPreferences = sharedPreferences;
        this.titlesTexts = titles;
        this.valuesTexts = values;
        //getDisplays Values
        getDisplaySettings();
        //set Up Display
        setUpdDisplayTitles();
        //setUp Default
        setUpDefaultDisplayValues();
    }


    //Get Stored configuration for each Panel
    private void getDisplaySettings (){
        settingManager = new SettingManager(sharedPreferences);
        displaySettings.add(settingManager.getFirstDisplay());
        displaySettings.add(settingManager.getSecondDisplay());
        displaySettings.add(settingManager.getThirdDisplay());
        unite = settingManager.getDistanceUniteSettings();
    }


    //set up title for each Panel
    private void setUpdDisplayTitles (){
        //Loop throw settings Display array list and set Titles Texts
        for (int i=0 ; i<displaySettings.size() ;i++){
            if (displaySettings.get(i).equals(Keys.DURATION)){
                isTimeInDisplaySettings = true;
                timerTexts.add(valuesTexts.get(i));
            }
            //Get Text View from list
            TextView title = titlesTexts.get(i);
            //set text
            title.setText(displaySettings.get(i));
        }
    }


    //when no data received from the location service
    private void setUpDefaultDisplayValues (){
        for (int i=0;i<valuesTexts.size();i++){
            if (!displaySettings.get(i).equals(Keys.DURATION)){
                valuesTexts.get(i).setText("--");
            }
        }
    }


    //Timer Formatter
    private String formatter (){
        long TinSec = (SystemClock.elapsedRealtime() - baseTime) / 1000;
        // % in java means mod ;)
        int hh = (int)(TinSec / 3600);
        int mm = (int)((TinSec % 3600) / 60);
        int ss = (int)(TinSec % 60);
        if (hh == 0){
            return String.format(Locale.ENGLISH ,"%02d:%02d" , mm , ss);
        }else {
            return String.format(Locale.ENGLISH ,"%02d:%02d:%02d" , hh , mm , ss);
        }
    }


    void startTimer (long baseTime){
        //start timer if the time in settings
        if (isTimeInDisplaySettings){
            this.baseTime = baseTime;
            //Runnable code to update the
            runnable = () -> {
                for (int i=0;i<timerTexts.size();i++){
                    timerTexts.get(i).setText(formatter());
                }
                handler.postDelayed(runnable , 1000);
            };
            //
            handler.postDelayed(runnable , 0);
        }
    }


    //stop updating
    void stopTimer (){
        if (isTimeInDisplaySettings){
            //remove CallBack
            handler.removeCallbacks(runnable);
        }
    }


    //set Duration Text
    void setText (String text){
        for (int i=0;i<timerTexts.size();i++){
            timerTexts.get(i).setText(text);
        }
    }


    //return current text in chronometer
    CharSequence getText (){
        if (isTimeInDisplaySettings){
            if (timerTexts.size() > 0){
                return timerTexts.get(0).getText();
            }
        }
        return null;
    }


    //Update Values Texts
    void UiUpdater (Context context , Intent intent){
       //
       for (int i = 0; i< valuesTexts.size() ; i++){
           //check if the Display not a time
           if (!displaySettings.get(i).equals(Keys.DURATION)){
               //When Unite KiloMeter
               if(unite == SettingManager.DEFAULT_UNIT_VALUE){
                   switch (displaySettings.get(i)){
                       case Keys.SPEED :
                           TextView speedText = valuesTexts.get(i);
                           String speedValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.km_speed);
                           speedText.setText(speedValue);
                           break;
                       case Keys.DISTANCE :
                           TextView distanceText = valuesTexts.get(i);
                           String distanceValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.km);
                           distanceText.setText(distanceValue);
                           break;
                       case Keys.ELEVATION :
                           TextView elevationText = valuesTexts.get(i);
                           String elevationValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.meter);
                           elevationText.setText(elevationValue);
                           break;
                   }
               }else {
               //When Unite Miles
                   switch (displaySettings.get(i)){
                       case Keys.SPEED :
                           TextView speedText = valuesTexts.get(i);
                           String speedValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.mi_speed);
                           speedText.setText(speedValue);
                           break;
                       case Keys.DISTANCE :
                           TextView distanceText = valuesTexts.get(i);
                           String distanceValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.mi);
                           distanceText.setText(distanceValue);
                           break;
                       case Keys.ELEVATION :
                           TextView elevationText = valuesTexts.get(i);
                           String elevationValue = String.format(Locale.ENGLISH , "%.2f", intent.getExtras().getDouble(displaySettings.get(i)))
                                   +" "+context.getResources().getString(R.string.meter);
                           elevationText.setText(elevationValue);
                           break;
                   }
               }
           }
       }
    }


    //ReUpdate ui when User changes Display Settings.
    private void displayRefresher (){
        //Clean Settings Array & Time Texts
        displaySettings.clear();
        timerTexts.clear();
        //Field
        getDisplaySettings();
        //
        setUpdDisplayTitles();
        //setUp Default
        setUpDefaultDisplayValues();
    }


    //Change Displays Settings
    void editFirstDisplayPanel (Context context , View view){
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        //Color the Text depends settings
        switch (settingManager.getFirstDisplay()){
            //
            case Keys.DURATION :
                TextView durationText = view.findViewById(R.id.select_display_duration);
                durationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            case Keys.SPEED :
                TextView speedText = view.findViewById(R.id.select_display_speed);
                speedText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.AVERAGE_SPEED :
                TextView avgTextView = view.findViewById(R.id.select_display_average_speed);
                avgTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.ELEVATION :
                TextView elevationText = view.findViewById(R.id.select_display_elevation);
                elevationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.DISTANCE :
                TextView distanceText = view.findViewById(R.id.select_display_distance);
                distanceText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
        }
        //Event Handlers
        view.findViewById(R.id.duration_layout).setOnClickListener(e->{
            settingManager.changeFirstDisplay(Keys.DURATION);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.speed_layout).setOnClickListener(e->{
            settingManager.changeFirstDisplay(Keys.SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.average_speed_layout).setOnClickListener(e->{
            settingManager.changeFirstDisplay(Keys.AVERAGE_SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.elevation_layout).setOnClickListener(e->{
            settingManager.changeFirstDisplay(Keys.ELEVATION);
            dialog.dismiss();
            displayRefresher();

        });
        view.findViewById(R.id.distance_layout).setOnClickListener(e->{
            settingManager.changeFirstDisplay(Keys.DISTANCE);
            dialog.dismiss();
            displayRefresher();
        });
        //show the dialog
        dialog.setView(view);
        dialog.show();
    }


    //Change Settings Display for Second Display
    void editSecondsDisplayPanel (Context context , View view){
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        //Color the Text depends settings
        switch (settingManager.getSecondDisplay()){
            //
            case Keys.DURATION :
                TextView durationText = view.findViewById(R.id.select_display_duration);
                durationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            case Keys.SPEED :
                TextView speedText = view.findViewById(R.id.select_display_speed);
                speedText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.AVERAGE_SPEED :
                TextView avgTextView = view.findViewById(R.id.select_display_average_speed);
                avgTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.ELEVATION :
                TextView elevationText = view.findViewById(R.id.select_display_elevation);
                elevationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.DISTANCE :
                TextView distanceText = view.findViewById(R.id.select_display_distance);
                distanceText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
        }
        //Event Handlers
        view.findViewById(R.id.duration_layout).setOnClickListener(e->{
            settingManager.changeSecondDisplay(Keys.DURATION);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.speed_layout).setOnClickListener(e->{
            settingManager.changeSecondDisplay(Keys.SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.average_speed_layout).setOnClickListener(e->{
            settingManager.changeSecondDisplay(Keys.AVERAGE_SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.elevation_layout).setOnClickListener(e->{
            settingManager.changeSecondDisplay(Keys.ELEVATION);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.distance_layout).setOnClickListener(e->{
            settingManager.changeSecondDisplay(Keys.DISTANCE);
            dialog.dismiss();
            displayRefresher();
        });
        //show the dialog
        dialog.setView(view);
        dialog.show();
    }


    //Change Settings Display for Third Display
    void editThirdDisplayPanel (Context context , View view){
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        //Color the Text depends settings
        switch (settingManager.getThirdDisplay()){
            //
            case Keys.DURATION :
                TextView durationText = view.findViewById(R.id.select_display_duration);
                durationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            case Keys.SPEED :
                TextView speedText = view.findViewById(R.id.select_display_speed);
                speedText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.AVERAGE_SPEED :
                TextView avgTextView = view.findViewById(R.id.select_display_average_speed);
                avgTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.ELEVATION :
                TextView elevationText = view.findViewById(R.id.select_display_elevation);
                elevationText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;

            case Keys.DISTANCE :
                TextView distanceText = view.findViewById(R.id.select_display_distance);
                distanceText.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
        }
        //Event Handler
        view.findViewById(R.id.duration_layout).setOnClickListener(e->{
            settingManager.changeThirdDisplay(Keys.DURATION);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.speed_layout).setOnClickListener(e->{
            settingManager.changeThirdDisplay(Keys.SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.average_speed_layout).setOnClickListener(e->{
            settingManager.changeThirdDisplay(Keys.AVERAGE_SPEED);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.elevation_layout).setOnClickListener(e->{
            settingManager.changeThirdDisplay(Keys.ELEVATION);
            dialog.dismiss();
            displayRefresher();
        });
        view.findViewById(R.id.distance_layout).setOnClickListener(e->{
            settingManager.changeThirdDisplay(Keys.DISTANCE);
            dialog.dismiss();
            displayRefresher();
        });
        //show the dialog
        dialog.setView(view);
        dialog.show();
    }

}
